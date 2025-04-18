package xyz.mxue.lazycatapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.entity.App;
import xyz.mxue.lazycatapp.repository.AppRepository;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppService {
    
    private final AppRepository appRepository;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    private static final String APP_LIST_URL = "https://appstore.api.lazycat.cloud/api/app/list";
    private static final String DOWNLOAD_COUNT_URL = "https://appstore.api.lazycat.cloud/api/counting";
    
    public Page<App> findAll(Pageable pageable) {
        return appRepository.findAll(pageable);
    }
    
    public Optional<App> findByPkgId(String pkgId) {
        return appRepository.findById(pkgId);
    }
    
    public Page<App> search(String keyword, Pageable pageable) {
        return appRepository.findByNameContainingOrDescriptionContaining(keyword, keyword, pageable);
    }
    
    public Page<App> findByCategory(String category, Pageable pageable) {
        return appRepository.findByCategoryContaining(category, pageable);
    }
    
    public List<App> findLatestApps(int limit) {
        return appRepository.findTopNByOrderByLastUpdatedDesc(limit);
    }
    
    public List<App> findPopularApps(int limit) {
        return appRepository.findTopNByOrderByUpdateIdDesc(limit);
    }
    
    @Scheduled(fixedRate = 600000) // 每10分钟执行一次
    public void updateApps() {
        log.info("开始更新应用信息...");
        try {
            Request request = new Request.Builder()
                    .url(APP_LIST_URL)
                    .get()
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("获取应用列表失败: {}", response);
                    return;
                }
                
                String responseBody = response.body().string();
                AppListResponse appListResponse = objectMapper.readValue(responseBody, AppListResponse.class);
                
                if (appListResponse.errorCode == 0 && appListResponse.data != null) {
                    List<App> apps = Arrays.asList(appListResponse.data);
                    for (App app : apps) {
                        app.setLastUpdated(Instant.now().toString());
                        app.setPackageName(app.getPkgId());
                        // 获取下载量
                        updateDownloadCount(app);
                    }
                    appRepository.saveAll(apps);
                    log.info("成功更新 {} 个应用信息", apps.size());
                } else {
                    log.error("获取应用列表失败: {}", appListResponse.message);
                }
            }
        } catch (IOException e) {
            log.error("更新应用信息时发生错误", e);
        }
    }
    
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void updateDownloadCounts() {
        log.info("开始更新应用下载量...");
        List<App> apps = appRepository.findAll();
        for (App app : apps) {
            updateDownloadCount(app);
        }
        appRepository.saveAll(apps);
        log.info("成功更新应用下载量");
    }
    
    private void updateDownloadCount(App app) {
        try {
            Request request = new Request.Builder()
                    .url(DOWNLOAD_COUNT_URL + "?pkgId=" + app.getPkgId())
                    .get()
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("获取应用下载量失败: {}", response);
                    return;
                }
                
                String responseBody = response.body().string();
                DownloadCountResponse downloadCountResponse = objectMapper.readValue(responseBody, DownloadCountResponse.class);
                
                if (downloadCountResponse.errorCode == 0) {
                    app.setDownloadCount(downloadCountResponse.data);
                    log.info("成功更新应用 {} 的下载量: {}", app.getPkgId(), downloadCountResponse.data);
                } else {
                    log.error("获取应用下载量失败: {}", downloadCountResponse.message);
                }
            }
        } catch (IOException e) {
            log.error("更新应用下载量时发生错误", e);
        }
    }
    
    @lombok.Data
    private static class AppListResponse {
        private int errorCode;
        private String message;
        private App[] data;
        private boolean success;
    }
    
    @lombok.Data
    private static class DownloadCountResponse {
        private int errorCode;
        private String message;
        private Integer data;
        private boolean success;
    }
} 