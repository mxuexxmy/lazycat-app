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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    
    public List<App> findAll() {
        return appRepository.findAll();
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
    
    public List<App> searchAll(String keyword) {
        return appRepository.findByNameContainingOrDescriptionContaining(keyword, keyword);
    }
    
    public List<App> findUsedApps(int limit) {
        return appRepository.findTopNByOrderByDownloadCountDesc(limit);
    }
    
    public List<Map<String, Object>> getDeveloperRanking() {
        List<App> allApps = appRepository.findAll();
        Map<Long, Map<String, Object>> developerStats = new HashMap<>();
        
        // 统计每个开发者的应用数量和总下载量
        for (App app : allApps) {
            Long creatorId = app.getCreatorId();
            if (creatorId != null) {
                developerStats.computeIfAbsent(creatorId, k -> {
                    Map<String, Object> stats = new HashMap<>();
                    stats.put("creatorId", creatorId);
                    stats.put("creator", app.getCreator());
                    stats.put("appCount", 0);
                    stats.put("totalDownloads", 0);
                    return stats;
                });
                
                Map<String, Object> stats = developerStats.get(creatorId);
                stats.put("appCount", (Integer) stats.get("appCount") + 1);
                stats.put("totalDownloads", (Integer) stats.get("totalDownloads") + (app.getDownloadCount() != null ? app.getDownloadCount() : 0));
            }
        }
        
        // 转换为列表并按总下载量排序
        return developerStats.values().stream()
                .sorted((a, b) -> ((Integer) b.get("totalDownloads")).compareTo((Integer) a.get("totalDownloads")))
                .collect(Collectors.toList());
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
                    List<App> existingApps = appRepository.findAll();
                    Set<String> existingPkgIds = existingApps.stream()
                            .map(App::getPkgId)
                            .collect(Collectors.toSet());
                    
                    for (App app : apps) {
                        app.setLastUpdated(Instant.now().toString());
                        app.setPackageName(app.getPkgId());
                        
                        // 只在新增应用时获取下载量
                        if (!existingPkgIds.contains(app.getPkgId())) {
                            updateDownloadCount(app);
                        }
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