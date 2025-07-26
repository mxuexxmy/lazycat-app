package xyz.mxue.lazycatapp.sync;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.entity.GitHubInfo;
import xyz.mxue.lazycatapp.model.response.github.GitHubRank;
import xyz.mxue.lazycatapp.model.response.github.GitHubSummary;
import xyz.mxue.lazycatapp.model.response.github.GitHubUser;
import xyz.mxue.lazycatapp.model.response.github.GitHubUserResponse;
import xyz.mxue.lazycatapp.repository.GitHubInfoRepository;
import xyz.mxue.lazycatapp.sync.api.LazyCatInterfaceInfo;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubSyncService {

    private final GitHubInfoRepository githubInfoRepository;
    private final ObjectMapper objectMapper;

    public void syncGitHubInfoForUser(Long userId) {
        String url = LazyCatInterfaceInfo.GITHUB_INFO_URL + userId;

        try {
            HttpResponse accept = HttpRequest.get(url)
                    .header("Accept", "application/json")
                    .execute();

            if (accept.getStatus() != 200) {
                log.error("获取用户 {} 的 GitHub 信息失败: {}", userId, accept.body());
                return;
            }

            String responseBody = accept.body();
            if ("record not found" .equals(responseBody)) {
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
        } catch (IOException e) {
            log.error("更新用户 {} 的 GitHub 信息时发生错误", userId, e);
        }
    }

}
