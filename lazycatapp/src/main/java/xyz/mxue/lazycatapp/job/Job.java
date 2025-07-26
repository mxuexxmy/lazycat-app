package xyz.mxue.lazycatapp.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.sync.AppSyncService;
import xyz.mxue.lazycatapp.sync.CategorySyncService;
import xyz.mxue.lazycatapp.sync.UserSyncService;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class Job {

    @Value("${custom.job}")
    private Boolean jobEnable;

    private final AppSyncService appSyncService;

    private final UserSyncService userSyncService;

    private final CategorySyncService categorySyncService;

    /**
     * 每 5 分钟执行一次
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void syncApps() {
        log.info("jobEnable: {}", jobEnable);
        log.info("定时任务执行...");
        if (jobEnable) {
            System.out.println("执行同步分类-" + LocalDateTime.now());
            categorySyncService.syncCategories();
            System.out.println("执行同步应用-" + LocalDateTime.now());
            appSyncService.syncApps();
            System.out.println("执行同步用户-" + LocalDateTime.now());
            userSyncService.syncDevelopers();
        }
    }

}
