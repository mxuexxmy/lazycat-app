package xyz.mxue.lazycatapp.sync;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.converter.GitHubConvert;
import xyz.mxue.lazycatapp.entity.GitHubInfo;
import xyz.mxue.lazycatapp.model.response.github.GitHubUserResponse;
import xyz.mxue.lazycatapp.repository.GitHubInfoRepository;
import xyz.mxue.lazycatapp.sync.api.LazyCatInterfaceInfo;

import java.io.IOException;

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

            GitHubInfo githubInfo = GitHubConvert.convert(userId, githubResponse);
            // 保存更新
            githubInfoRepository.save(githubInfo);
            log.info("成功更新用户 {} 的 GitHub 信息", userId);
        } catch (IOException e) {
            log.error("更新用户 {} 的 GitHub 信息时发生错误", userId, e);
        }
    }

}
