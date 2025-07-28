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
import java.time.format.DateTimeFormatter;

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
    @Scheduled(cron = "0 0/5 * * * ?")
    public void syncApps() {
        log.info("jobEnable: {}", jobEnable);
        log.info("定时任务执行...");
        if (jobEnable) {
            // 睡眠 2 秒
            try {
                Thread.sleep(2000);
                log.info("执行全量同步分类-{}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                categorySyncService.syncCategories(false);
            } catch (InterruptedException e) {
                log.error("执行全量同步分类定时任务执行异常", e);
            }

            // 睡眠 2 秒
            try {
                Thread.sleep(2000);
                log.info("执行增量同步分类-{}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                categorySyncService.syncCategoriesIncremental(false);
            } catch (InterruptedException e) {
                log.error("执行增量同步分类定时任务执行异常", e);
            }

            // 睡眠 2 秒
            try {
                Thread.sleep(2000);
                log.info("执行全量同步应用-{}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                appSyncService.syncApps(false);
            } catch (InterruptedException e) {
                log.error("执行全量同步应用定时任务执行异常", e);
            }

            // 睡眠 2 秒
            try {
                Thread.sleep(2000);
                log.info("执行增量同步应用-{}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                appSyncService.syncAppsIncremental(false);
            } catch (InterruptedException e) {
                log.error("执行增量同步应用定时任务执行异常", e);
            }

            // 睡眠 2 秒
            try {
                Thread.sleep(2000);
                log.info("执行全量同步用户-{}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                userSyncService.syncDevelopers(false);
            } catch (InterruptedException e) {
                log.error("执行全量同步用户定时任务执行异常", e);
            }

            try {
                Thread.sleep(2000);
                log.info("执行增量同步用户-{}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                userSyncService.syncUsersIncremental(false);
            } catch (InterruptedException e) {
                log.error("执行增量同步用户定时任务执行异常", e);
            }
        }
    }

}
