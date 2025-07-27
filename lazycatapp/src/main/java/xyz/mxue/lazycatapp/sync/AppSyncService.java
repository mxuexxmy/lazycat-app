package xyz.mxue.lazycatapp.sync;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.converter.AppConvert;
import xyz.mxue.lazycatapp.entity.App;
import xyz.mxue.lazycatapp.entity.SyncInfo;
import xyz.mxue.lazycatapp.enums.SyncStatusEnum;
import xyz.mxue.lazycatapp.model.response.app.AppInfoApiResponse;
import xyz.mxue.lazycatapp.model.response.app.AppItemInfo;
import xyz.mxue.lazycatapp.repository.AppRepository;
import xyz.mxue.lazycatapp.sync.api.LazyCatInterfaceInfo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppSyncService {

    private final ObjectMapper objectMapper;

    private final SyncService syncService;

    private final AppRepository appRepository;

    private final AppScoreSyncService appScoreSyncService;

    private final AppCommentSyncService appCommentSyncService;

    @PostConstruct
    public void init() {
        // 检查是否需要首次同步
        if (appRepository.count() == 0) {
            log.info("No apps found, performing initial sync");
            syncApps(false);
        }
    }

    /**
     * 同步 APP 列表
     */
    @Async("taskExecutor")
    public void syncApps(boolean forceSync) {
        log.info("开始同步 APP 列表-{}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        if (syncService.isSync(SyncService.SYNC_TYPE_APP, forceSync)) {
            log.info("进行同步 APP 列表-{}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            try {
                // 更改同步状态- 同步中
                syncService.updateSyncStatus(SyncService.SYNC_TYPE_APP, SyncStatusEnum.SYNCING);
                int page = 0;
                int size = 100;
                boolean hasMore = true;
                int totalProcessed = 0;

                while (hasMore) {
                    HttpResponse execute = queryAppInfo(page, size);

                    if (execute.getStatus() != 200) {
                        return;
                    }

                    AppInfoApiResponse appInfoApiResponse = objectMapper.readValue(execute.body(), AppInfoApiResponse.class);
                    List<AppItemInfo> items = appInfoApiResponse.getItems();
                    // 更新总数量到 SyncInfo
                    SyncInfo syncInfo = syncService.getSyncInfo(SyncService.SYNC_TYPE_APP);
                    if (syncInfo != null) {
                        syncInfo.setTotalCount((long) appInfoApiResponse.getTotal());
                        syncService.saveSyncInfo(syncInfo);
                    }
                    for (AppItemInfo item : items) {
                        App app = AppConvert.convert(item);
                        appRepository.save(app);
                        // 同步得分
                        appScoreSyncService.syncAppScore(item);
                        // 同步评论
                        appCommentSyncService.syncAppComments(item.getPackageName());
                        // 更新同步数量

                    }
                    totalProcessed += appInfoApiResponse.getItems().size();
                    page++;

                    // 检查是否还有更多数据
                    hasMore = totalProcessed < appInfoApiResponse.getTotal();

                    // 添加延迟，避免请求过于频繁
                    Thread.sleep(2000);

                }
                // 更改同步状态- 完成
                log.info("分类信息同步完成");
                syncService.updateSyncInfo(SyncService.SYNC_TYPE_APP, true, null);
            } catch (Exception e) {
                String error = "更新应用信息时发生错误: " + e.getMessage();
                log.error(error, e);
                // 更改同步状态- 失败
                syncService.updateSyncInfo(SyncService.SYNC_TYPE_APP, false, e.getMessage());
            }
        }
    }

    /**
     * 查询懒猫应用商店中应用数量
     *
     * @return 应用数量
     */
    public long getTotalAppsCount() {
        // 使用新的v3接口，分页获取所有应用
        int page = 0;
        int size = 1;
        HttpResponse execute = queryAppInfo(page, size);

        if (execute.getStatus() != 200) {
            return 0;
        }
        long total = 0;
        try {
            AppInfoApiResponse appInfoApiResponse = objectMapper.readValue(execute.body(), AppInfoApiResponse.class);
            total = appInfoApiResponse.getTotal();
        } catch (Exception e) {
            log.error("获取分类时发生错误: {}", e.getMessage());
        }

        return total;
    }

    /**
     * 查询应用信息
     *
     * @param page 第几页
     * @param size 每页多少个
     * @return 查询结果
     */
    private HttpResponse queryAppInfo(int page, int size) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("category_ids", "0");
        queryParams.put("sort", "version_updated_at.desc");
        queryParams.put("page", page);
        queryParams.put("size", size);
        return HttpRequest.get(LazyCatInterfaceInfo.APP_LIST_URL)
                .header("Accept-language", "zh-CN,zh")//头信息，多个头信息多次调用此方法即可
                .form(queryParams)//表单内容
                .timeout(20000)//超时，毫秒
                .execute();
    }

}
