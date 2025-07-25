package xyz.mxue.lazycatapp.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.service.SyncService;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppSyncService {

    private static final String APP_LIST_URL = "https://appstore.api.lazycat.cloud/api/v3/user/app/list";

    private final OkHttpClient httpClient;

    private final SyncService syncService;

    /**
     * 同步 APP 列表
     */
    public void syncApps() {
        if (!syncService.shouldSync(SyncService.SYNC_TYPE_APP)) {
            return;
        }
        log.info("开始更新应用信息...");
        try {
            // 使用新的v3接口，分页获取所有应用
            int page = 0;
            int size = 1;
            boolean hasMore = true;
            int totalProcessed = 0;

            while (hasMore) {
                String url = APP_LIST_URL + "?category_ids=0&sort=version_updated_at.desc&page=" + page + "&size=" + size;
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                Response response = httpClient.newCall(request).execute();

                if (!response.isSuccessful()) {
                    String error = "获取应用列表失败: " + response;
                    log.error(error);
                    return;
                }

                if (response.body() == null) {
                    log.error("获取应用列表失败: 响应体为空");
                    return;
                }
                String responseBody = response.body().toString();

                hasMore = false;
                log.info("---返回应用信息-----");
                log.info("{}", responseBody);
            }
            log.info("应用信息更新完成，共处理 {} 个应用", totalProcessed);
        } catch (Exception e) {
            String error = "更新应用信息时发生错误: " + e.getMessage();
            log.error(error, e);
        }

    }

}
