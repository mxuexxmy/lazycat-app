package xyz.mxue.lazycatapp.sync;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.converter.UserConvert;
import xyz.mxue.lazycatapp.entity.SyncInfo;
import xyz.mxue.lazycatapp.entity.User;
import xyz.mxue.lazycatapp.enums.SyncStatusEnum;
import xyz.mxue.lazycatapp.enums.SyncStrategyEnum;
import xyz.mxue.lazycatapp.enums.SyncTypeEnum;
import xyz.mxue.lazycatapp.model.response.user.UserInfoApiResponse;
import xyz.mxue.lazycatapp.repository.UserRepository;
import xyz.mxue.lazycatapp.sync.api.LazyCatInterfaceInfo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private final TutorialSyncService tutorialSyncService;

    private final SyncService syncService;

    @Async("taskExecutor")
    public void syncDevelopers(boolean forceSync) {
        log.info("开始同步用户信息-{}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        if (syncService.isSync(SyncTypeEnum.USER, SyncStrategyEnum.FULL, forceSync)) {
            log.info("进行同步用户信息-{}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            try {
                // 更改同步状态- 同步中
                syncService.updateSyncStatus(SyncTypeEnum.USER, SyncStatusEnum.SYNCING);
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

                        // 更新总数量到 SyncInfo
                        SyncInfo syncInfo = syncService.getSyncInfo(SyncTypeEnum.USER);
                        if (syncInfo != null) {
                            syncInfo.setTotalCount((long) userInfoApiResponse.getTotal());
                            syncService.saveSyncInfo(syncInfo);
                        }

                        userInfoApiResponse.getItems().forEach(entity -> {
                            User user = UserConvert.convert(entity);
                            userRepository.save(user);
                            // 更新 GitHub 用户信息
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
                            // 更新用户的教程
                            try {
                                tutorialSyncService.syncTutorials(entity.getId());
                            } catch (Exception e) {
                                log.error("同步教程失败: {}", e.getMessage());
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
                // 更改同步状态为完成
                syncService.updateSyncInfo(SyncTypeEnum.USER, SyncStrategyEnum.FULL, true, null);
            } catch (Exception e) {
                log.error("获取开发者列表时发生错误: {}", e.getMessage());
                // 更改同步状态为失败
                syncService.updateSyncInfo(SyncTypeEnum.USER, SyncStrategyEnum.FULL, false, e.getMessage());
            }
        }

    }

    /**
     * 增量同步
     */
    public void syncUsersIncremental(boolean forceSync) {
        if (syncService.isSync(SyncTypeEnum.USER, SyncStrategyEnum.INCREMENTAL, forceSync)) {
            long localCount = userRepository.count();
            long remoteCount = getTotalUserCount();
            if (localCount < remoteCount) {
                log.info("开始同步用户增量数据");
                try {
                    // 更改同步状态- 同步中
                    syncService.updateSyncStatus(SyncTypeEnum.USER, SyncStatusEnum.SYNCING);
                    int page = 0;
                    int size = Math.toIntExact(remoteCount - localCount);
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

                        // 更新总数量到 SyncInfo
                        SyncInfo syncInfo = syncService.getSyncInfo(SyncTypeEnum.USER);
                        if (syncInfo != null) {
                            syncInfo.setTotalCount((long) userInfoApiResponse.getTotal());
                            syncService.saveSyncInfo(syncInfo);
                        }

                        userInfoApiResponse.getItems().forEach(entity -> {
                            User user = UserConvert.convert(entity);
                            userRepository.save(user);
                            // 更新 GitHub 用户信息
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
                            // 更新用户的教程
                            try {
                                tutorialSyncService.syncTutorials(entity.getId());
                            } catch (Exception e) {
                                log.error("同步教程失败: {}", e.getMessage());
                            }

                        });
                    }

                    // 更改同步状态为完成
                    syncService.updateSyncInfo(SyncTypeEnum.USER, SyncStrategyEnum.INCREMENTAL, true, null);
                } catch (Exception e) {
                    log.error("获取开发者列表时发生错误: {}", e.getMessage());
                    // 更改同步状态为失败
                    syncService.updateSyncInfo(SyncTypeEnum.USER, SyncStrategyEnum.INCREMENTAL, false, e.getMessage());
                }
            }
        }

    }

    public long getTotalUserCount() {
        long total = 0;
        int page = 0;
        int size = 100;

        try {
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
                total = userInfoApiResponse.getTotal();
            }
        } catch (Exception e) {
            log.error("同步用户信息失败: {}", e.getMessage());
        }

        return total;
    }
}
