package xyz.mxue.lazycatapp.sync;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.converter.CommunityUserConvert;
import xyz.mxue.lazycatapp.entity.CommunityUser;
import xyz.mxue.lazycatapp.entity.User;
import xyz.mxue.lazycatapp.model.response.community.UserResponse;
import xyz.mxue.lazycatapp.repository.CommunityUserRepository;
import xyz.mxue.lazycatapp.repository.UserRepository;
import xyz.mxue.lazycatapp.sync.api.LazyCatInterfaceInfo;


@Slf4j
@Service
@RequiredArgsConstructor
public class CommunitySyncUserService {

    private final CommunityUserRepository communityUserRepository;

    private final ObjectMapper objectMapper;

    private final UserRepository userRepository;

    private final GitHubSyncService gitHubSyncService;

    public void syncAllCommunityUsers(Long userId, boolean syncUserInfo) {
        log.info("开始更新用户信息: {}", userId);
        try {

            HttpResponse execute = HttpRequest.get(LazyCatInterfaceInfo.USER_INFO_URL + userId)
                    .execute();

            if (execute.getStatus() != 200) {
                log.error("获取用户信息失败: {}", execute.body());
                return;
            }

            String responseBody = execute.body();
            UserResponse userResponse = objectMapper.readValue(responseBody, UserResponse.class);

            if (userResponse != null) {
                // 更新社区用户信息
                CommunityUser communityUser = CommunityUserConvert.convert(userResponse);
                communityUserRepository.save(communityUser);

                if (syncUserInfo) {
                    User userInfo = userRepository.findById(userResponse.getUser().getId())
                            .orElse(null);
                    if (userInfo != null) {
                        CommunityUserConvert.convert(userResponse, userInfo);
                        userRepository.save(userInfo);

                        if (StrUtil.isNotBlank(userResponse.getUser().getGithubUsername())) {
                            gitHubSyncService.syncGitHubInfoForUser(userInfo.getId());
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("更新用户信息时发生错误-{}", e.getMessage());
        }
    }

}
