package xyz.mxue.lazycatapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.entity.App;
import xyz.mxue.lazycatapp.entity.GitHubInfo;
import xyz.mxue.lazycatapp.repository.AppRepository;
import xyz.mxue.lazycatapp.repository.GitHubInfoRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubInfoService {
    private final AppRepository appRepository;
    private final GitHubInfoRepository githubInfoRepository;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final SyncService syncService;

    private static final String GITHUB_INFO_URL = "https://playground.api.lazycat.cloud/api/github/info/";

    @lombok.Data
    private static class GitHubUserResponse {
        private Long id;
        private String createdAt;
        private String updatedAt;
        private Long uid;
        private GitHubUser user;
        private Map<String, GitHubLanguage> topLangs;
        private GitHubSummary summary;
    }

    @lombok.Data
    private static class GitHubUser {
        private Long id;
        private String username;
        @JsonProperty("isUsernameSet")
        private boolean usernameSet;
        private String nickname;
        private String avatar;
        private String description;
        private int status;
        private String githubUsername;
        @JsonProperty("isCurrentLoginUser")
        private boolean currentLoginUser;
    }

    @lombok.Data
    private static class GitHubLanguage {
        private String name;
        private String color;
        private int size;
    }

    @lombok.Data
    private static class GitHubSummary {
        private String name;
        private int totalPRs;
        private int totalCommits;
        private int totalIssues;
        private int contributedTo;
        private GitHubRank rank;
    }

    @lombok.Data
    private static class GitHubRank {
        private String level;
        private double score;
    }

    //@Scheduled(fixedRate = 6 * 60 * 60 * 1000) // 每6小时执行一次
    public void syncGitHubInfo() {
        if (!syncService.shouldSync(SyncService.SYNC_TYPE_GITHUB)) {
            return;
        }

        log.info("开始同步 GitHub 信息...");
        try {
            // 获取所有需要同步 GitHub 信息的用户
            List<App> apps = appRepository.findAll();
            Set<Long> userIds = apps.stream()
                    .map(App::getCreatorId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            // 获取现有 GitHub 信息
            List<GitHubInfo> existingInfos = githubInfoRepository.findAll();
            Map<Long, GitHubInfo> existingInfoMap = existingInfos.stream()
                    .collect(Collectors.toMap(GitHubInfo::getUserId, info -> info));

            // 获取需要更新的用户列表
            List<Long> usersToUpdate = userIds.stream()
                    .filter(userId -> {
                        GitHubInfo existingInfo = existingInfoMap.get(userId);
                        return existingInfo == null ||
                                existingInfo.getLastSyncTime() == null ||
                                existingInfo.getLastSyncTime().isBefore(LocalDateTime.now().minusHours(24));
                    })
                    .toList();

            if (usersToUpdate.isEmpty()) {
                log.info("没有需要更新的 GitHub 信息");
                return;
            }

            log.info("需要更新 {} 个用户的 GitHub 信息", usersToUpdate.size());

            // 为每个需要更新的用户同步 GitHub 信息
            for (Long userId : usersToUpdate) {
                try {
                    syncGitHubInfoForUser(userId);
                } catch (Exception e) {
                    log.error("处理用户 {} 的 GitHub 信息时发生错误: {}", userId, e.getMessage());
                }
            }

            log.info("GitHub 信息同步完成");
            syncService.updateSyncInfo(SyncService.SYNC_TYPE_GITHUB, true, null);
        } catch (Exception e) {
            log.error("同步 GitHub 信息时发生错误", e);
            syncService.updateSyncInfo(SyncService.SYNC_TYPE_GITHUB, false, e.getMessage());
        }
    }


    public void syncGitHubInfoForUser(Long userId) throws IOException {
        String url = GITHUB_INFO_URL + userId;
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("获取用户 {} 的 GitHub 信息失败: {}", userId, response);
                return;
            }

            String responseBody = response.body().string();
            if ("record not found".equals(responseBody)) {
                log.warn("用户 {} 的 GitHub 信息不存在", userId);
                return;
            }

            // 解析响应
            GitHubUserResponse githubResponse = objectMapper.readValue(responseBody, GitHubUserResponse.class);

            // 获取或创建 GitHub 信息
            GitHubInfo githubInfo = githubInfoRepository.findByUserId(userId)
                    .orElse(new GitHubInfo());

            // 设置基本信息
            githubInfo.setUserId(userId);
            githubInfo.setUid(githubResponse.getUid());
            githubInfo.setCreatedAt(LocalDateTime.parse(githubResponse.getCreatedAt().replace("Z", "")));
            githubInfo.setUpdatedAt(LocalDateTime.parse(githubResponse.getUpdatedAt().replace("Z", "")));

            // 设置用户信息
            GitHubUser user = githubResponse.getUser();
            githubInfo.setUsername(user.getUsername());
            githubInfo.setNickname(user.getNickname());
            githubInfo.setAvatar(user.getAvatar());
            githubInfo.setDescription(user.getDescription());
            githubInfo.setGithubUsername(user.getGithubUsername());

            // 设置统计信息
            GitHubSummary summary = githubResponse.getSummary();
            githubInfo.setTotalPRs(summary.getTotalPRs());
            githubInfo.setTotalCommits(summary.getTotalCommits());
            githubInfo.setTotalIssues(summary.getTotalIssues());
            githubInfo.setContributedTo(summary.getContributedTo());

            // 设置排名信息
            GitHubRank rank = summary.getRank();
            githubInfo.setRankLevel(rank.getLevel());
            githubInfo.setRankScore(rank.getScore());

            // 设置编程语言信息
            githubInfo.setTopLangs(objectMapper.writeValueAsString(githubResponse.getTopLangs()));

            // 设置最后同步时间
            githubInfo.setLastSyncTime(LocalDateTime.now());

            // 保存更新
            githubInfoRepository.save(githubInfo);
            log.info("成功更新用户 {} 的 GitHub 信息", userId);
        }
    }

    /**
     * 获取指定用户的 GitHub 信息
     *
     * @param userId 用户ID
     * @return GitHub 信息，如果不存在则返回 null
     */
    public GitHubInfo getGitHubInfo(Long userId) {
        try {
            // 首先检查本地数据库
            Optional<GitHubInfo> existingInfo = githubInfoRepository.findByUserId(userId);
            if (existingInfo.isPresent()) {
                GitHubInfo info = existingInfo.get();
                // 如果信息在24小时内更新过，直接返回
                if (info.getLastSyncTime() != null &&
                        info.getLastSyncTime().isAfter(LocalDateTime.now().minusHours(24))) {
                    return info;
                }
            }

            // 如果本地没有信息或信息过期，从API获取
            syncGitHubInfoForUser(userId);
            return githubInfoRepository.findByUserId(userId).orElse(null);
        } catch (Exception e) {
            log.error("获取用户 {} 的 GitHub 信息时发生错误: {}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * 获取多个用户的 GitHub 信息
     *
     * @param userIds 用户ID列表
     * @return 用户ID到GitHub信息的映射
     */
    public Map<Long, GitHubInfo> getGitHubInfos(List<Long> userIds) {
        Map<Long, GitHubInfo> result = new HashMap<>();
        for (Long userId : userIds) {
            GitHubInfo info = getGitHubInfo(userId);
            if (info != null) {
                result.put(userId, info);
            }
        }
        return result;
    }

    /**
     * 同步用户的 GitHub 信息
     *
     * @param userId 用户ID
     */
    public void publicSyncGitHubInfoForUser(Long userId) {
        try {
            syncGitHubInfoForUser(userId);
        } catch (Exception e) {
            log.error("获取用户 {} 的 GitHub 信息时发生错误: {}", userId, e.getMessage());

        }
    }
}