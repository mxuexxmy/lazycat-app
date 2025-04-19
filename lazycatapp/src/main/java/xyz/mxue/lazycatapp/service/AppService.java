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
import xyz.mxue.lazycatapp.entity.UserInfo;
import xyz.mxue.lazycatapp.repository.AppRepository;
import xyz.mxue.lazycatapp.repository.UserInfoRepository;

import java.io.IOException;
import java.time.Instant;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
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
    private final UserInfoRepository userInfoRepository;
    private final UserService userService;
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
    
    public List<App> findByCategory(String category) {
        return appRepository.findByCategoryContaining(category);
    }
    
    public Page<App> findByCategory(String category, Pageable pageable) {
        return appRepository.findByCategoryContaining(category, pageable);
    }
    
    public List<App> findLatestApps(int limit) {
        return appRepository.findTopNByOrderByLastUpdatedDesc(limit);
    }
    
    public List<App> findPopularApps(int limit) {
        List<App> allApps = appRepository.findAll();
        return allApps.stream()
            .sorted((a, b) -> {
                int aCount = a.getDownloadCount() != null ? a.getDownloadCount() : 0;
                int bCount = b.getDownloadCount() != null ? b.getDownloadCount() : 0;
                return Integer.compare(bCount, aCount);
            })
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    public List<App> searchAll(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return appRepository.findAll().stream()
                .filter(app -> 
                    containsIgnoreCase(app.getName(), keyword) ||
                    containsIgnoreCase(app.getPackageName(), keyword) ||
                    containsIgnoreCase(app.getKeywords(), keyword) ||
                    containsIgnoreCase(app.getSource(), keyword) ||
                    containsIgnoreCase(app.getDescription(), keyword) ||
                    containsIgnoreCase(app.getBrief(), keyword)
                )
                .collect(Collectors.toList());
    }
    
    private boolean containsIgnoreCase(String text, String searchTerm) {
        return text != null && text.toLowerCase().contains(searchTerm.toLowerCase());
    }
    
    public List<App> findUsedApps(int limit) {
        return appRepository.findTopNByOrderByDownloadCountDesc(limit);
    }
    
    public List<App> findByCreatorId(Long creatorId) {
        return appRepository.findByCreatorId(creatorId);
    }
    
    public List<Map<String, Object>> getDeveloperRanking() {
        List<Long> creatorIds = appRepository.findDistinctCreatorIds();
        
        return creatorIds.stream()
            .map(creatorId -> {
                UserInfo userInfo = userInfoRepository.findById(creatorId)
                    .orElse(new UserInfo());
                
                List<App> apps = appRepository.findByCreatorId(creatorId);
                
                // 按下载量排序并取前三
                List<App> topApps = apps.stream()
                    .sorted((a, b) -> {
                        int aCount = a.getDownloadCount() != null ? a.getDownloadCount() : 0;
                        int bCount = b.getDownloadCount() != null ? b.getDownloadCount() : 0;
                        return Integer.compare(bCount, aCount);
                    })
                    .limit(3)
                    .collect(Collectors.toList());
                
                int totalDownloads = apps.stream()
                    .mapToInt(app -> app.getDownloadCount() != null ? app.getDownloadCount() : 0)
                    .sum();
                
                String lastUpdateDate = apps.stream()
                    .map(App::getUpdateDate)
                    .max(String::compareTo)
                    .orElse("");

                return Map.of(
                    "id", creatorId,
                    "nickName", userInfo.getNickname(),
                    "avatar", userInfo.getAvatar(),
                    "appCount", apps.size(),
                    "totalDownloads", totalDownloads,
                    "topApps", topApps,
                    "lastUpdateDate", lastUpdateDate
                );
            })
            .sorted((a, b) -> {
                Integer bDownloads = (Integer) b.get("totalDownloads");
                Integer aDownloads = (Integer) a.get("totalDownloads");
                if (!bDownloads.equals(aDownloads)) {
                    return bDownloads.compareTo(aDownloads);
                }
                Integer bCount = (Integer) b.get("appCount");
                Integer aCount = (Integer) a.get("appCount");
                return bCount.compareTo(aCount);
            })
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
                            userService.updateUserInfo(app.getCreatorId());
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
            try {
                updateDownloadCount(app);
                appRepository.save(app); // 立即保存每个更新后的应用
                log.info("应用 {} 下载量更新完成", app.getPkgId());
                Thread.sleep(5000); // 每次更新后睡眠5秒
            } catch (InterruptedException e) {
                log.error("更新下载量时被中断", e);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("更新应用 {} 下载量时发生错误", app.getPkgId(), e);
                // 继续处理下一个应用
            }
        }
        log.info("应用下载量更新完成");
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

    /**
     * 获取所有不重复的开发者ID
     * @return 开发者ID列表
     */
    public List<Long> getDistinctCreatorIds() {
        return appRepository.findDistinctCreatorIds();
    }

    // 获取月度新增应用数据
    public List<Map<String, Object>> getMonthlyNewApps() {
        List<App> apps = appRepository.findAll();
        Map<YearMonth, Long> monthlyCount = apps.stream()
            .collect(Collectors.groupingBy(
                app -> YearMonth.from(app.getCreateTime()),
                Collectors.counting()
            ));

        return monthlyCount.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                Map<String, Object> result = new HashMap<>();
                result.put("month", entry.getKey().toString());
                result.put("count", entry.getValue());
                return result;
            })
            .collect(Collectors.toList());
    }

    // 获取应用类别分布数据
    public List<Map<String, Object>> getCategoryDistribution() {
        List<App> apps = appRepository.findAll();
        Map<String, Long> categoryCount = new HashMap<>();
        
        // 遍历所有应用，统计每个类别的数量
        for (App app : apps) {
            if (app.getCategory() != null) {
                for (String category : app.getCategory()) {
                    categoryCount.merge(category, 1L, Long::sum);
                }
            }
        }

        return categoryCount.entrySet().stream()
            .map(entry -> {
                Map<String, Object> result = new HashMap<>();
                result.put("category", entry.getKey());
                result.put("count", entry.getValue());
                return result;
            })
            .collect(Collectors.toList());
    }

    // 获取开发者活跃度数据
    public List<Map<String, Object>> getDeveloperActivity() {
        List<App> apps = appRepository.findAll();
        Map<String, Long> developerAppCounts = new HashMap<>();
        
        for (App app : apps) {
            String creator = app.getCreator();
            developerAppCounts.merge(creator, 1L, Long::sum);
        }

        return developerAppCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("developer", entry.getKey());
                    result.put("appCount", entry.getValue());
                    return result;
                })
                .collect(Collectors.toList());
    }

    // 获取应用更新频率数据
    public List<Map<String, Object>> getUpdateFrequency() {
        List<App> apps = appRepository.findAll();
        return apps.stream()
            .filter(app -> app.getUpdateTime() != null && app.getCreateTime() != null)
            .map(app -> {
                Map<String, Object> result = new HashMap<>();
                result.put("pkgId", app.getPkgId());
                result.put("name", app.getName());
                long daysBetween = ChronoUnit.DAYS.between(
                    app.getCreateTime(),
                    app.getUpdateTime()
                );
                result.put("updateFrequency", daysBetween);
                return result;
            })
            .sorted(Comparator.comparingLong(m -> (Long) m.get("updateFrequency")))
            .collect(Collectors.toList());
    }
} 