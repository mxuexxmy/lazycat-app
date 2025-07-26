package xyz.mxue.lazycatapp.sync;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.converter.UserConvert;
import xyz.mxue.lazycatapp.entity.User;
import xyz.mxue.lazycatapp.model.response.user.UserInfoApiResponse;
import xyz.mxue.lazycatapp.repository.UserRepository;
import xyz.mxue.lazycatapp.sync.api.LazyCatInterfaceInfo;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSyncService {

    private final ObjectMapper objectMapper;

    private final UserRepository userRepository;

    private final GitHubSyncService gitHubSyncService;

    private final CommunitySyncUserService communitySyncUserService;

    public void syncDevelopers() {
        try {
            int page = 0;
            int size = 100;
            boolean hasMore = true;
            int totalProcessed = 0;
            while (hasMore) {
                Map<String, Object> params = new HashMap<>();
                params.put("page", page);
                params.put("size", size);
                HttpResponse execute = HttpRequest
                        .get(LazyCatInterfaceInfo.DEVELOPER_LIST)
                        .form(params)
                        .header("Accept-language", "zh-CN,zh")//头信息，多个头信息多次调用此方法即可
                        .execute();

                if (execute.getStatus() == 200) {
                    String body = execute.body();
                    UserInfoApiResponse userInfoApiResponse = objectMapper.readValue(body, UserInfoApiResponse.class);
                    userInfoApiResponse.getItems().forEach(entity -> {
                        log.info("----用户信息------");
                        log.info(JSONUtil.toJsonPrettyStr(entity));
                        User user = UserConvert.convert(entity);
                        userRepository.save(user);
                        if (StrUtil.isNotBlank(entity.getGithubUsername())) {
                            try {
                                gitHubSyncService.syncGitHubInfoForUser(entity.getId());
                            } catch (Exception e) {
                                log.error("同步GitHub信息失败: {}", e.getMessage());
                            }
                        }
                        // 更新社区信息
                        try {
                            communitySyncUserService.syncAllCommunityUsers(entity.getId(), false);
                        } catch (Exception e) {
                            log.error("同步社区信息失败: {}", e.getMessage());
                        }

                    });
                    totalProcessed += userInfoApiResponse.getSize();
                    page++;

                    // 检查是否还有更多数据
                    hasMore = totalProcessed < userInfoApiResponse.getTotal();

                    // 添加延迟，避免请求过于频繁
                    Thread.sleep(2000);
                } else {
                    hasMore = false;
                }
            }

        } catch (Exception e) {
            log.info("获取开发者列表时发生错误: {}", e.getMessage());
        }

    }

}
