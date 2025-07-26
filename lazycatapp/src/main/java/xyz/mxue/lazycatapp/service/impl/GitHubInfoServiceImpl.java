package xyz.mxue.lazycatapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.entity.GitHubInfo;
import xyz.mxue.lazycatapp.repository.GitHubInfoRepository;
import xyz.mxue.lazycatapp.service.GitHubInfoService;
import xyz.mxue.lazycatapp.sync.GitHubSyncService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubInfoServiceImpl implements GitHubInfoService {

    private final GitHubInfoRepository githubInfoRepository;
    private final GitHubSyncService gitHubSyncService;

    /**
     * 获取指定用户的 GitHub 信息
     *
     * @param userId 用户ID
     * @return GitHub 信息，如果不存在则返回 null
     */
    @Override
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
            gitHubSyncService.syncGitHubInfoForUser(userId);
            return githubInfoRepository.findByUserId(userId).orElse(null);
        } catch (Exception e) {
            log.error("获取用户 {} 的 GitHub 信息时发生错误: {}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * 获取多个用户的 GitHub 信息
     *
     * @return 用户ID到GitHub信息的映射
     */
    @Override
    public Map<Long, GitHubInfo> getGitHubInfos() {
        Map<Long, GitHubInfo> result = new HashMap<>();
        List<GitHubInfo> gitHubInfoList = githubInfoRepository.findAll();
        for (GitHubInfo gitHubInfo : gitHubInfoList) {
            result.put(gitHubInfo.getUid(), gitHubInfo);
        }
        return result;
    }

}

