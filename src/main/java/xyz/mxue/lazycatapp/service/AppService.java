package xyz.mxue.lazycatapp.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class AppService {

    private static final Logger log = LoggerFactory.getLogger(AppService.class);
    private final AppRepository appRepository;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AppService(AppRepository appRepository, OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.appRepository = appRepository;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Scheduled(cron = "0 0 * * * ?") // 每小时执行一次
    public void updateDownloadCounts() {
        log.info("开始更新应用下载量");
        List<App> apps = appRepository.findAll();
        int totalApps = apps.size();
        int successCount = 0;
        int failureCount = 0;
        
        for (int i = 0; i < apps.size(); i += 10) {
            List<App> batch = apps.subList(i, Math.min(i + 10, apps.size()));
            for (App app : batch) {
                try {
                    updateDownloadCount(app);
                    successCount++;
                } catch (Exception e) {
                    log.error("更新应用 {} 的下载量失败: {}", app.getId(), e.getMessage());
                    failureCount++;
                }
            }
            
            // 每批处理完后等待5秒，避免请求过于频繁
            if (i + 10 < apps.size()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("更新下载量任务被中断");
                    return;
                }
            }
        }
        
        log.info("应用下载量更新完成: 总数={}, 成功={}, 失败={}", 
            totalApps, successCount, failureCount);
    }

    private void updateDownloadCount(App app) throws IOException {
        if (app == null || app.getPkgId() == null) {
            log.warn("应用或包名为空，跳过下载量更新");
            return;
        }

        String url = DOWNLOAD_COUNT_URL + app.getPkgId();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("获取应用 {} 的下载量失败: HTTP {} - {}", 
                    app.getPkgId(), response.code(), response.message());
                return;
            }

            String responseBody = response.body().string();
            if (responseBody == null || responseBody.isEmpty()) {
                log.warn("应用 {} 的下载量响应为空", app.getPkgId());
                return;
            }

            try {
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                if (jsonNode.has("downloadCount")) {
                    int downloadCount = jsonNode.get("downloadCount").asInt();
                    app.setDownloadCount(downloadCount);
                    appRepository.save(app);
                    log.debug("应用 {} 的下载量已更新为 {}", app.getPkgId(), downloadCount);
                } else {
                    log.warn("应用 {} 的下载量数据格式不正确", app.getPkgId());
                }
            } catch (JsonProcessingException e) {
                log.error("解析应用 {} 的下载量数据失败: {}", app.getPkgId(), e.getMessage());
            }
        } catch (IOException e) {
            log.error("获取应用 {} 的下载量时发生IO异常: {}", app.getPkgId(), e.getMessage());
            throw e;
        }
    }
} 