package xyz.mxue.lazycatapp.sync;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.converter.AppConvert;
import xyz.mxue.lazycatapp.entity.App;
import xyz.mxue.lazycatapp.model.response.app.AppInfoApiResponse;
import xyz.mxue.lazycatapp.model.response.app.AppItemInfo;
import xyz.mxue.lazycatapp.repository.AppRepository;
import xyz.mxue.lazycatapp.sync.api.LazyCatInterfaceInfo;

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

    /**
     * 同步 APP 列表
     */
    public void syncApps() {
        if (syncService.shouldSync(SyncService.SYNC_TYPE_APP)) {
            return;
        }
        log.info("开始更新应用信息...");
        try {
            // 使用新的v3接口，分页获取所有应用
            int page = 0;
            int size = 100;
            boolean hasMore = true;
            int totalProcessed = 0;

            while (hasMore) {

                Map<String, Object> queryParams = new HashMap<>();
                queryParams.put("category_ids", "0");
                queryParams.put("sort", "version_updated_at.desc");
                queryParams.put("page", page);
                queryParams.put("size", size);

                HttpResponse execute = HttpRequest.get(LazyCatInterfaceInfo.APP_LIST_URL)
                        .header("Accept-language", "zh-CN,zh")//头信息，多个头信息多次调用此方法即可
                        .form(queryParams)//表单内容
                        .timeout(20000)//超时，毫秒
                        .execute();

                if (execute.getStatus() != 200) {
                    return;
                }

                log.info("----execute-----");
                log.info(JSONUtil.toJsonPrettyStr(execute.body()));
                AppInfoApiResponse appInfoApiResponse = objectMapper.readValue(execute.body(), AppInfoApiResponse.class);
                log.info("----appInfoApiResponse-----");
                log.info(JSONUtil.toJsonPrettyStr(appInfoApiResponse));
                List<AppItemInfo> items = appInfoApiResponse.getItems();
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
            log.info("分类信息同步完成");
            syncService.updateSyncInfo(SyncService.SYNC_TYPE_APP, true, null);
            log.info("应用信息更新完成，共处理 {} 个应用", totalProcessed);
            syncService.updateTotalCount(SyncService.SYNC_TYPE_APP, totalProcessed);
        } catch (Exception e) {
            String error = "更新应用信息时发生错误: " + e.getMessage();
            log.error(error, e);
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
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("category_ids", "0");
        queryParams.put("sort", "version_updated_at.desc");
        queryParams.put("page", page);
        queryParams.put("size", size);
        HttpResponse execute = HttpRequest.get(LazyCatInterfaceInfo.APP_LIST_URL)
                .header("Accept-language", "zh-CN,zh")//头信息，多个头信息多次调用此方法即可
                .form(queryParams)//表单内容
                .timeout(20000)//超时，毫秒
                .execute();

        if (execute.getStatus() != 200) {
            return 0;
        }
        long total = 0;
        try {
            AppInfoApiResponse appInfoApiResponse = objectMapper.readValue(execute.body(), AppInfoApiResponse.class);
            total = appInfoApiResponse.getTotal();
        } catch (Exception e) {
            log.info("获取分类时发生错误: {}", e.getMessage());
        }


        return total;
    }

}
