package xyz.mxue.lazycatapp.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppRunInitialService {

    private final AppSyncService appSyncService;

    private final CategorySyncService categorySyncService;

    private final UserSyncService userSyncService;

    /**
     * 初始化
     */
    @Async("taskExecutor")
    public void sync() {
        log.info("项目启动时初始化");
        userSyncService.syncDevelopers(false);
        // 睡眠 5 秒
        try {
            Thread.sleep(5000);
            appSyncService.syncApps(false);
        } catch (Exception e) {
            log.error("项目启动时初始化失败-同步应用失败-", e);
        }
        // 睡眠 5 秒
        try {
            Thread.sleep(5000);
            categorySyncService.syncCategories(false);
        } catch (Exception e) {
            log.error("项目启动时初始化失败-同步分类失败-", e);
        }

        log.info("项目启动时初始化完成");
    }


}
