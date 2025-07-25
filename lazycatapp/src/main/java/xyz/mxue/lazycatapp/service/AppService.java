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
import xyz.mxue.lazycatapp.repository.AppScoreRepository;
import xyz.mxue.lazycatapp.repository.AppCommentRepository;
import xyz.mxue.lazycatapp.entity.Category;
import xyz.mxue.lazycatapp.entity.AppScore;
import xyz.mxue.lazycatapp.entity.AppComment;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import xyz.mxue.lazycatapp.entity.SyncInfo;
import xyz.mxue.lazycatapp.repository.CategoryRepository;

import java.util.ArrayList;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppService {

    private final AppRepository appRepository;
    private final UserInfoRepository userInfoRepository;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final SyncService syncService;
    private final CategoryRepository categoryRepository;

    private static final String APP_LIST_URL = "https://appstore.api.lazycat.cloud/api/v3/user/app/list";
    private static final String DOWNLOAD_COUNT_URL = "https://appstore.api.lazycat.cloud/api/counting";

    private final AppScoreRepository appScoreRepository;
    private static final String SCORE_API_URL = "https://appstore.api.lazycat.cloud/api/comment/score/";

    private final AppCommentRepository appCommentRepository;
    private static final String COMMENT_LIST_URL = "https://appstore.api.lazycat.cloud/api/comment/list/";

    // 新的v3接口响应类
    @lombok.Data
    private static class AppListV3Response {
        private List<AppV3Item> items;
        private int page;
        private int size;
        private int total;
    }

    @lombok.Data
    private static class AppV3Item {
        private Long id;
        @JsonProperty("package")
        private String packageName;
        private int status;
        private String created_at;
        private String updated_at;
        private String version_updated_at;
        private CreateUser create_user;
        private Information information;
        private Version version;
        private Rating rating;
        private Count count;
    }

    @lombok.Data
    private static class CreateUser {
        private Long developer_id;
        private Long id;
        private String username;
        private String nickname;
        private String description;
        private String avatar;
        private String github_username;
        private int continuous_submission_day_count;
        private int app_total_count;
    }

    @lombok.Data
    private static class Information {
        private Long id;
        private Long create_user_id;
        private Long app_id;
        private String language;
        private String name;
        private String brief;
        private String description;
        private String keywords;
        private String source;
        private String source_author;
        private boolean support_pc;
        private boolean support_mobile;
        private List<String> screenshot_pc_paths;
        private List<String> screenshot_mobile_paths;
    }

    @lombok.Data
    private static class Version {
        private Long id;
        private Long create_user_id;
        private Long app_id;
        private String name;
        @JsonProperty("package")
        private String packageName;
        private String pkg_hash;
        private String pkg_path;
        private String icon_path;
        private List<String> unsupported_platforms;
        private String min_os_version;
        private List<String> changelog_list;
        private String changelog_language;
    }

    @lombok.Data
    private static class Rating {
        private double score;
        private Statistics statistics;
    }

    @lombok.Data
    private static class Statistics {
        private int total;
        private int one;
        private int two;
        private int three;
        private int four;
        private int five;
    }

    @lombok.Data
    private static class Count {
        private int downloads;
        private int likes;
        private int comments;
    }

    @lombok.Data
    private static class ScoreResponse {
        private int errorCode;
        private String message;
        private ScoreData data;
        private boolean success;
    }

    @lombok.Data
    private static class ScoreData {
        private double score;
        private int[] score_table;
        private int total;
    }

    @lombok.Data
    private static class CommentListResponse {
        private int errorCode;
        private String message;
        private CommentListData data;
        private boolean success;
    }

    @lombok.Data
    private static class CommentListData {
        private List<Comment> list;
        @JsonProperty("isNext")
        private boolean isNext;
    }

    @lombok.Data
    private static class Comment {
        private Long commentId;
        private String appId;
        private Long userid;
        private String nickname;
        private String avatar;
        private int score;
        private String content;
        private boolean liked;
        private int likeCounts;
        private String createdAt;
        private String updatedAt;
    }

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
                .filter(app -> containsIgnoreCase(app.getName(), keyword) ||
                        containsIgnoreCase(app.getPackageName(), keyword) ||
                        containsIgnoreCase(app.getKeywords(), keyword) ||
                        containsIgnoreCase(app.getSource(), keyword) ||
                        containsIgnoreCase(app.getDescription(), keyword) ||
                        containsIgnoreCase(app.getBrief(), keyword) ||
                        (app.getCategory() != null && app.getCategory().stream()
                                .anyMatch(cat -> containsIgnoreCase(cat, keyword))))
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
                    List<Map<String, Object>> topApps = apps.stream()
                            .sorted((a, b) -> {
                                int aCount = a.getDownloadCount() != null ? a.getDownloadCount() : 0;
                                int bCount = b.getDownloadCount() != null ? b.getDownloadCount() : 0;
                                return Integer.compare(bCount, aCount);
                            })
                            .limit(3)
                            .<Map<String, Object>>map(app -> {
                                Map<String, Object> appMap = new HashMap<>();
                                appMap.put("pkgId", app.getPkgId());
                                appMap.put("name", app.getName());
                                appMap.put("updateDate", app.getUpdateDate() != null ? app.getUpdateDate() : "");
                                return appMap;
                            })
                            .collect(Collectors.toList());

                    int totalDownloads = apps.stream()
                            .mapToInt(app -> app.getDownloadCount() != null ? app.getDownloadCount() : 0)
                            .sum();

                    String lastUpdateDate = apps.stream()
                            .map(App::getUpdateDate)
                            .max(String::compareTo)
                            .orElse("");

                    Map<String, Object> result = new HashMap<>();
                    result.put("id", creatorId);
                    result.put("nickName", userInfo.getNickname() != null ? userInfo.getNickname() : "未知开发者");
                    result.put("avatar", userInfo.getAvatar() != null ? userInfo.getAvatar() : "");
                    result.put("appCount", apps.size());
                    result.put("totalDownloads", totalDownloads);
                    result.put("apps", topApps);
                    result.put("lastUpdateDate", lastUpdateDate);
                    return result;
                })
                .collect(Collectors.toList());
    }

    //@Scheduled(fixedRate = 1800000) // 每30分钟执行一次
    public void updateApps() {
        if (!syncService.shouldSync(SyncService.SYNC_TYPE_APP)) {
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
                String url = APP_LIST_URL + "?category_ids=0&sort=version_updated_at.desc&page=" + page + "&size=" + size;
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        String error = "获取应用列表失败: " + response;
                        log.error(error);
                        syncService.updateSyncInfo(SyncService.SYNC_TYPE_APP, false, error);
                        return;
                    }

                    String responseBody = response.body().string();
                    AppListV3Response appListResponse = objectMapper.readValue(responseBody, AppListV3Response.class);

                    if (appListResponse.items != null && !appListResponse.items.isEmpty()) {
                        // 更新总数量到 SyncInfo
                        SyncInfo appSyncInfo = syncService.getSyncInfo(SyncService.SYNC_TYPE_APP);
                        if (appSyncInfo != null) {
                            appSyncInfo.setTotalCount((long) appListResponse.total);
                            syncService.saveSyncInfo(appSyncInfo);
                        }

                        // 获取现有应用
                        List<App> existingApps = appRepository.findAll();
                        Map<String, App> existingAppMap = existingApps.stream()
                                .collect(Collectors.toMap(App::getPkgId, app -> app));

                        // 获取所有分类
                        List<Category> allCategories = categoryRepository.findAll();
                        Map<String, Category> categoryMap = allCategories.stream()
                                .collect(Collectors.toMap(Category::getName, category -> category));

                        // 获取同步信息
                        boolean isInitialSync = !appSyncInfo.isInitialSyncCompleted();

                        for (AppV3Item appV3Item : appListResponse.items) {
                            try {
                                // 转换AppV3Item为App实体
                                App app = convertAppV3ToApp(appV3Item);
                                
                                App existingApp = existingAppMap.get(app.getPkgId());
                                LocalDateTime lastUpdateTime = existingApp != null ? existingApp.getUpdateTime() : null;

                                if (existingApp != null) {
                                    // 如果是已存在的应用，检查是否需要更新
                                    LocalDateTime appUpdateTime = LocalDateTime.parse(appV3Item.version_updated_at.replace("Z", ""));
                                    if (appUpdateTime.isAfter(lastUpdateTime)) {
                                        // 保留原有的下载量
                                        app.setDownloadCount(existingApp.getDownloadCount());
                                        
                                        // 设置分类关联
                                        Set<Category> categories = new HashSet<>();
                                        if (app.getCategory() != null) {
                                            for (String categoryName : app.getCategory()) {
                                                Category category = categoryMap.get(categoryName);
                                                if (category != null) {
                                                    categories.add(category);
                                                }
                                            }
                                        }
                                        app.setCategories(categories);

                                        appRepository.save(app);
                                        log.info("更新应用: {}", app.getPkgId());
                                    }
                                } else {
                                    // 如果是新应用，设置下载量（从新接口的count中获取）
                                    if (appV3Item.count != null) {
                                        app.setDownloadCount(appV3Item.count.downloads);
                                    }
                                    
                                    // 设置分类关联
                                    Set<Category> categories = new HashSet<>();
                                    if (app.getCategory() != null) {
                                        for (String categoryName : app.getCategory()) {
                                            Category category = categoryMap.get(categoryName);
                                            if (category != null) {
                                                categories.add(category);
                                            }
                                        }
                                    }
                                    app.setCategories(categories);

                                    appRepository.save(app);
                                    log.info("新增应用: {}", app.getPkgId());
                                }

                                // 同步评分信息（从新接口的rating中获取）
                                if (appV3Item.rating != null) {
                                    syncAppScoreFromV3(app.getPkgId(), appV3Item.rating);
                                }

                                // 如果是首次同步，同步相关数据
                                if (isInitialSync) {
                                    // 不再同步用户信息和GitHub信息
                                    // 同步评论信息
                                    syncAppComments(app.getPkgId());

                                    // 添加延迟，避免频繁调用
                                    Thread.sleep(1000);
                                }
                            } catch (Exception e) {
                                log.error("处理应用 {} 时发生错误: {}", appV3Item.packageName, e.getMessage());
                            }
                        }

                        totalProcessed += appListResponse.items.size();
                        page++;
                        
                        // 检查是否还有更多数据
                        hasMore = totalProcessed < appListResponse.total;
                        
                        // 添加延迟，避免请求过于频繁
                        Thread.sleep(2000);
                    } else {
                        hasMore = false;
                    }
                }
            }

            log.info("应用信息更新完成，共处理 {} 个应用", totalProcessed);
            syncService.updateSyncInfo(SyncService.SYNC_TYPE_APP, true, null);
        } catch (Exception e) {
            String error = "更新应用信息时发生错误: " + e.getMessage();
            log.error(error, e);
            syncService.updateSyncInfo(SyncService.SYNC_TYPE_APP, false, error);
        }
    }

    // @Scheduled(cron = "0 0 */1 * * *") // 每1小时执行一次，从0点开始
    public void updateDownloadCounts() {
        log.info("开始更新应用下载量...");
        List<App> apps = appRepository.findAll();
        int batchSize = 10; // 每批处理的应用数量
        int totalApps = apps.size();
        int processedApps = 0;
        int successCount = 0;
        int failureCount = 0;

        for (int i = 0; i < totalApps; i += batchSize) {
            List<App> batch = apps.subList(i, Math.min(i + batchSize, totalApps));
            List<App> updatedApps = new ArrayList<>();

            for (App app : batch) {
                try {
                    updateDownloadCount(app);
                    updatedApps.add(app);
                    successCount++;
                    log.info("应用 {} 下载量更新完成", app.getPkgId());
                } catch (IOException e) {
                    log.error("网络错误: 更新应用 {} 下载量失败", app.getPkgId(), e);
                    failureCount++;
                } catch (Exception e) {
                    log.error("更新应用 {} 下载量时发生错误", app.getPkgId(), e);
                    failureCount++;
                }
                processedApps++;
            }

            // 批量保存更新后的应用
            if (!updatedApps.isEmpty()) {
                try {
                    appRepository.saveAll(updatedApps);
                    log.info("批量保存 {} 个应用的更新", updatedApps.size());
                } catch (Exception e) {
                    log.error("批量保存应用更新失败", e);
                }
            }

            // 每批处理完后等待5秒
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.error("更新下载量时被中断", e);
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.info("应用下载量更新完成。总计: {} 个应用, 成功: {} 个, 失败: {} 个",
                totalApps, successCount, failureCount);
    }

    private void updateDownloadCount(App app) throws IOException {
        try {
            Request request = new Request.Builder()
                    .url(DOWNLOAD_COUNT_URL + "?pkgId=" + app.getPkgId())
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("HTTP请求失败: " + response.code() + " " + response.message());
                }

                String responseBody = response.body().string();
                DownloadCountResponse downloadCountResponse = objectMapper.readValue(responseBody,
                        DownloadCountResponse.class);

                if (downloadCountResponse.errorCode == 0 && downloadCountResponse.data != null) {
                    app.setDownloadCount(downloadCountResponse.data);
                    log.info("成功更新应用 {} 的下载量: {}", app.getPkgId(), downloadCountResponse.data);
                } else {
                    throw new IOException("API返回错误: " + downloadCountResponse.message);
                }
            }
        } catch (IOException e) {
            log.error("网络错误: 更新应用 {} 下载量失败", app.getPkgId(), e);
            throw e;
        } catch (Exception e) {
            log.error("更新应用 {} 下载量时发生未知错误", app.getPkgId(), e);
            throw new IOException("更新下载量失败: " + e.getMessage(), e);
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
     *
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
                        Collectors.counting()));

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
                            app.getUpdateTime());
                    result.put("updateFrequency", daysBetween);
                    return result;
                })
                .sorted(Comparator.comparingLong(m -> (Long) m.get("updateFrequency")))
                .collect(Collectors.toList());
    }

    public long count() {
        return appRepository.count();
    }

    public long getTotalAppsCount() {
        try {
            String url = APP_LIST_URL + "?category_ids=0&sort=version_updated_at.desc&page=0&size=1";
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("获取应用总数失败: {}", response);
                    return 0;
                }

                assert response.body() != null;
                String responseBody = response.body().string();
                AppListV3Response appListResponse = objectMapper.readValue(responseBody, AppListV3Response.class);

                if (appListResponse.items != null) {
                    return appListResponse.total;
                } else {
                    log.error("获取应用总数失败: 响应数据为空");
                    return 0;
                }
            }
        } catch (IOException e) {
            log.error("获取应用总数时发生错误", e);
            return 0;
        }
    }

    public long getTotalDownloads() {
        return appRepository.getTotalDownloads();
    }

    public List<Map<String, Object>> getPopularApps(int limit) {
        List<App> apps = appRepository.findAll().stream()
                .sorted((a, b) -> {
                    int aCount = a.getDownloadCount() != null ? a.getDownloadCount() : 0;
                    int bCount = b.getDownloadCount() != null ? b.getDownloadCount() : 0;
                    return Integer.compare(bCount, aCount);
                })
                .limit(limit)
                .collect(Collectors.toList());

        return apps.stream().map(app -> {
            Map<String, Object> appMap = new HashMap<>();
            appMap.put("name", app.getName());
            appMap.put("pkgId", app.getPkgId());
            appMap.put("downloads", app.getDownloadCount() != null ? app.getDownloadCount() : 0);
            appMap.put("category", app.getCategory());
            return appMap;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getCategoryStats() {
        return appRepository.getCategoryStats();
    }

    public Map<String, Object> getStatisticsOverview() {
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalApps", appRepository.count());
        overview.put("totalDownloads", appRepository.findAll().stream()
                .mapToInt(app -> app.getDownloadCount() != null ? app.getDownloadCount() : 0)
                .sum());
        overview.put("developerCount", userInfoRepository.count());
        return overview;
    }

    /**
     * 修复现有应用的 packageName 字段
     */
    public void fixPackageNames() {
        log.info("开始修复应用包名...");
        List<App> apps = appRepository.findAll();
        int fixed = 0;
        for (App app : apps) {
            if (app.getPackageName() == null) {
                app.setPackageName(app.getPkgId());
                appRepository.save(app);
                fixed++;
                log.info("修复应用包名: {}", app.getPkgId());
            }
        }
        log.info("应用包名修复完成，共修复 {} 个应用", fixed);
    }

    public void syncAppScore(String pkgId) {
        try {
            Request request = new Request.Builder()
                    .url(SCORE_API_URL + pkgId)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("获取应用评分失败: {}", response);
                    return;
                }

                String responseBody = response.body().string();
                ScoreResponse scoreResponse = objectMapper.readValue(responseBody, ScoreResponse.class);

                if (scoreResponse.errorCode == 0 && scoreResponse.data != null) {
                    AppScore appScore = appScoreRepository.findById(pkgId)
                            .orElse(new AppScore());

                    appScore.setPkgId(pkgId);
                    appScore.setScore(scoreResponse.data.score);
                    appScore.setTotalReviews(scoreResponse.data.total);

                    // 设置各分数段的评论数
                    int[] scoreTable = scoreResponse.data.score_table;
                    appScore.setOneStarCount(scoreTable[0]);
                    appScore.setTwoStarCount(scoreTable[1]);
                    appScore.setThreeStarCount(scoreTable[2]);
                    appScore.setFourStarCount(scoreTable[3]);
                    appScore.setFiveStarCount(scoreTable[4]);

                    appScore.setLastSyncTime(LocalDateTime.now());

                    appScoreRepository.save(appScore);
                    log.info("成功同步应用 {} 的评分信息", pkgId);
                }
            }
        } catch (Exception e) {
            log.error("同步应用 {} 评分时发生错误", pkgId, e);
        }
    }

    //@Scheduled(cron = "0 0 */2 * * *") // 每2小时执行一次，从0点开始
    public void syncAllAppScores() {
        if (!syncService.shouldSync(SyncService.SYNC_TYPE_SCORE)) {
            return;
        }

        log.info("开始同步应用评分...");
        try {
            // 获取所有应用
            List<App> apps = appRepository.findAll();

            // 更新总数量到 SyncInfo
            SyncInfo syncInfo = syncService.getSyncInfo(SyncService.SYNC_TYPE_SCORE);
            if (syncInfo != null) {
                syncInfo.setTotalCount((long) apps.size());
                syncService.saveSyncInfo(syncInfo);
            }

            // 获取现有评分信息
            List<AppScore> existingScores = appScoreRepository.findAll();
            Map<String, AppScore> existingScoreMap = existingScores.stream()
                    .collect(Collectors.toMap(AppScore::getPkgId, score -> score));

            // 获取需要更新的应用列表
            List<String> appsToUpdate = apps.stream()
                    .map(App::getPkgId)
                    .filter(pkgId -> {
                        AppScore existingScore = existingScoreMap.get(pkgId);
                        return existingScore == null ||
                                existingScore.getLastSyncTime() == null ||
                                existingScore.getLastSyncTime().isBefore(LocalDateTime.now().minusHours(24));
                    })
                    .collect(Collectors.toList());

            if (appsToUpdate.isEmpty()) {
                log.info("没有需要更新的应用评分");
                return;
            }

            log.info("需要更新 {} 个应用的评分", appsToUpdate.size());

            for (String pkgId : appsToUpdate) {
                try {
                    syncAppScore(pkgId);
                    Thread.sleep(1000); // 添加1秒延迟，避免请求过于频繁
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            log.info("应用评分同步完成");
            syncService.updateSyncInfo(SyncService.SYNC_TYPE_SCORE, true, null);
        } catch (Exception e) {
            String error = "同步应用评分时发生错误: " + e.getMessage();
            log.error(error, e);
            syncService.updateSyncInfo(SyncService.SYNC_TYPE_SCORE, false, error);
        }
    }

    public List<Map<String, Object>> getFiveStarApps() {
        return appScoreRepository.findFiveStarApps().stream()
                .map(score -> {
                    Map<String, Object> result = new HashMap<>();
                    App app = appRepository.findById(score.getPkgId()).orElse(null);
                    if (app != null) {
                        result.put("pkgId", score.getPkgId());
                        result.put("name", app.getName());
                        result.put("score", score.getScore());
                        result.put("totalReviews", score.getTotalReviews());
                    }
                    return result;
                })
                .filter(map -> !map.isEmpty())
                .collect(Collectors.toList());
    }

    public Map<String, Integer> getScoreDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        List<AppScore> scores = appScoreRepository.findAll();

        distribution.put("oneStarCount", scores.stream().mapToInt(AppScore::getOneStarCount).sum());
        distribution.put("twoStarCount", scores.stream().mapToInt(AppScore::getTwoStarCount).sum());
        distribution.put("threeStarCount", scores.stream().mapToInt(AppScore::getThreeStarCount).sum());
        distribution.put("fourStarCount", scores.stream().mapToInt(AppScore::getFourStarCount).sum());
        distribution.put("fiveStarCount", scores.stream().mapToInt(AppScore::getFiveStarCount).sum());

        return distribution;
    }

    public List<Map<String, Object>> getMostReviewedApps() {
        return appScoreRepository.findTop10ByOrderByTotalReviewsDesc().stream()
                .map(score -> {
                    Map<String, Object> result = new HashMap<>();
                    App app = appRepository.findById(score.getPkgId()).orElse(null);
                    if (app != null) {
                        result.put("pkgId", score.getPkgId());
                        result.put("name", app.getName());
                        result.put("totalReviews", score.getTotalReviews());
                        result.put("score", score.getScore());
                    }
                    return result;
                })
                .filter(map -> !map.isEmpty())
                .collect(Collectors.toList());
    }

    public void syncAppComments(String pkgId) {
        AppScore appScore = appScoreRepository.findById(pkgId).orElse(null);
        if (appScore == null || appScore.getTotalReviews() == 0) {
            return;
        }

        int page = 1;
        int limit = 500;
        boolean hasNext = true;

        while (hasNext) {
            try {
                Request request = new Request.Builder()
                        .url(COMMENT_LIST_URL + pkgId + "?page=" + page + "&limit=" + limit)
                        .get()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        log.error("获取应用评论失败: {}", response);
                        return;
                    }

                    String responseBody = response.body().string();
                    CommentListResponse commentResponse = objectMapper.readValue(responseBody,
                            CommentListResponse.class);

                    if (commentResponse.errorCode == 0 && commentResponse.data != null) {
                        // 保存或更新评论
                        for (Comment comment : commentResponse.data.list) {
                            AppComment appComment = appCommentRepository.findById(comment.commentId)
                                    .orElse(new AppComment());
                            appComment.setCommentId(comment.commentId);
                            appComment.setPkgId(comment.appId);
                            appComment.setUserId(comment.userid);
                            appComment.setNickname(comment.nickname);
                            appComment.setAvatar(comment.avatar);
                            appComment.setScore(comment.score);
                            appComment.setContent(comment.content);
                            appComment.setLiked(comment.liked);
                            appComment.setLikeCounts(comment.likeCounts);
                            appComment.setCreatedAt(LocalDateTime.parse(comment.createdAt.replace("Z", "")));
                            appComment.setUpdatedAt(LocalDateTime.parse(comment.updatedAt.replace("Z", "")));

                            appCommentRepository.save(appComment);
                        }

                        hasNext = commentResponse.data.isNext;
                        page++;

                        if (!hasNext) {
                            log.info("成功同步应用 {} 的所有评论信息", pkgId);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("同步应用 {} 评论时发生错误", pkgId, e);
                break;
            }
        }
    }

    //@Scheduled(cron = "0 0 */2 * * *") // 每2小时执行一次，从0点开始
    public void syncAllAppComments() {
        if (!syncService.shouldSync(SyncService.SYNC_TYPE_COMMENT)) {
            return;
        }

        log.info("开始同步应用评论...");
        try {
            // 获取所有有评论的应用
            List<AppScore> scores = appScoreRepository.findAll().stream()
                    .filter(score -> score.getTotalReviews() != null && score.getTotalReviews() > 0)
                    .collect(Collectors.toList());

            // 更新总数量到 SyncInfo
            SyncInfo syncInfo = syncService.getSyncInfo(SyncService.SYNC_TYPE_COMMENT);
            if (syncInfo != null) {
                syncInfo.setTotalCount((long) scores.size());
                syncService.saveSyncInfo(syncInfo);
            }

            // 获取现有评论信息
            List<AppComment> existingComments = appCommentRepository.findAll();
            Map<String, LocalDateTime> lastCommentTimeMap = existingComments.stream()
                    .collect(Collectors.groupingBy(
                            AppComment::getPkgId,
                            Collectors.mapping(
                                    AppComment::getCreatedAt,
                                    Collectors.maxBy(LocalDateTime::compareTo)
                            )
                    ))
                    .entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().orElse(LocalDateTime.MIN)
                    ));

            // 获取需要更新的应用列表
            List<String> appsToUpdate = scores.stream()
                    .map(AppScore::getPkgId)
                    .filter(pkgId -> {
                        LocalDateTime lastCommentTime = lastCommentTimeMap.getOrDefault(pkgId, LocalDateTime.MIN);
                        return lastCommentTime.isBefore(LocalDateTime.now().minusHours(24));
                    })
                    .collect(Collectors.toList());

            if (appsToUpdate.isEmpty()) {
                log.info("没有需要更新的应用评论");
                return;
            }

            log.info("需要更新 {} 个应用的评论", appsToUpdate.size());

            for (String pkgId : appsToUpdate) {
                try {
                    syncAppComments(pkgId);
                    Thread.sleep(5000); // 添加5秒延迟，避免请求过于频繁
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            log.info("应用评论同步完成");
            syncService.updateSyncInfo(SyncService.SYNC_TYPE_COMMENT, true, null);
        } catch (Exception e) {
            String error = "同步应用评论时发生错误: " + e.getMessage();
            log.error(error, e);
            syncService.updateSyncInfo(SyncService.SYNC_TYPE_COMMENT, false, error);
        }
    }

    public List<AppComment> getAppComments(String pkgId) {
        return appCommentRepository.findByPkgId(pkgId);
    }

    public List<Map<String, Object>> getFiveStarAppsRanking(int limit) {
        return appScoreRepository.findAll().stream()
                .filter(score -> score.getScore() != null && score.getScore() >= 4.5) // 筛选评分大于等于4.5的应用
                .sorted((a, b) -> {
                    // 首先按评分排序
                    int scoreCompare = Double.compare(b.getScore(), a.getScore());
                    if (scoreCompare != 0) {
                        return scoreCompare;
                    }
                    // 如果评分相同，则按评论数排序
                    return Integer.compare(b.getTotalReviews(), a.getTotalReviews());
                })
                .limit(limit)
                .map(score -> {
                    Map<String, Object> appMap = new HashMap<>();
                    // 获取应用信息
                    App app = appRepository.findById(score.getPkgId()).orElse(null);
                    if (app != null) {
                        appMap.put("pkgId", app.getPkgId());
                        appMap.put("name", app.getName());
                        appMap.put("iconPath", app.getIconPath());
                        appMap.put("description", app.getDescription());
                        appMap.put("downloadCount", app.getDownloadCount());
                        appMap.put("score", score.getScore());
                        appMap.put("totalReviews", score.getTotalReviews());
                        appMap.put("category", app.getCategory());
                        appMap.put("creator", app.getCreator());
                    }
                    return appMap;
                })
                .collect(Collectors.toList());
    }

    public void likeComment(Long commentId) {
        AppComment comment = appCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        comment.setLikeCounts(comment.getLikeCounts() + 1);
        appCommentRepository.save(comment);
    }

    public List<Map<String, Object>> getAllComments() {
        List<AppComment> comments = appCommentRepository.findAll();
        return comments.stream().map(comment -> {
            Map<String, Object> commentMap = new HashMap<>();
            commentMap.put("commentId", comment.getCommentId());
            commentMap.put("pkgId", comment.getPkgId());
            commentMap.put("userId", comment.getUserId());
            commentMap.put("nickname", comment.getNickname());
            commentMap.put("avatar", comment.getAvatar());
            commentMap.put("score", comment.getScore());
            commentMap.put("content", comment.getContent());
            commentMap.put("liked", comment.getLiked());
            commentMap.put("likeCounts", comment.getLikeCounts());
            commentMap.put("createdAt", comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            commentMap.put("updatedAt", comment.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // 获取应用信息
            App app = appRepository.findById(comment.getPkgId()).orElse(null);
            if (app != null) {
                commentMap.put("appName", app.getName());
                commentMap.put("appIcon", app.getIconPath());
                commentMap.put("brief", app.getBrief());
            }

            return commentMap;
        }).collect(Collectors.toList());
    }

    /**
     * 根据 creatorId 查询用户昵称
     *
     * @param creatorId 创建者ID
     * @return 用户昵称，如果未找到则返回 null
     */
    public String getCreatorNickname(Long creatorId) {
        if (creatorId == null) {
            return null;
        }
        UserInfo userInfo = userInfoRepository.findById(creatorId).orElse(null);
        return userInfo != null ? userInfo.getNickname() : null;
    }

    /**
     * 将AppV3Item转换为App实体
     */
    private App convertAppV3ToApp(AppV3Item appV3Item) {
        App app = new App();
        
        // 基本信息
        app.setPkgId(appV3Item.packageName);
        app.setPackageName(appV3Item.packageName);
        
        if (appV3Item.information != null) {
            app.setName(appV3Item.information.name);
            app.setBrief(appV3Item.information.brief);
            app.setDescription(appV3Item.information.description);
            app.setKeywords(appV3Item.information.keywords);
            app.setSource(appV3Item.information.source);
            app.setSupportPC(appV3Item.information.support_pc);
            app.setSupportMobile(appV3Item.information.support_mobile);
            app.setPcScreenshotPaths(appV3Item.information.screenshot_pc_paths);
            app.setMobileScreenshotPaths(appV3Item.information.screenshot_mobile_paths);
        }
        
        if (appV3Item.version != null) {
            app.setVersion(appV3Item.version.name);
            app.setPkgPath(appV3Item.version.pkg_path);
            app.setPkgHash(appV3Item.version.pkg_hash);
            app.setIconPath(appV3Item.version.icon_path);
            app.setUnsupportedPlatforms(appV3Item.version.unsupported_platforms);
            app.setOsDependence(appV3Item.version.min_os_version);
            if (appV3Item.version.changelog_list != null && !appV3Item.version.changelog_list.isEmpty()) {
                app.setChangelog(String.join("\n", appV3Item.version.changelog_list));
            }
        }
        
        if (appV3Item.create_user != null) {
            app.setCreator(appV3Item.create_user.nickname);
            app.setCreatorId(appV3Item.create_user.developer_id);
        }
        
        // 时间信息
        if (appV3Item.created_at != null) {
            app.setCreateTime(LocalDateTime.parse(appV3Item.created_at.replace("Z", "")));
        }
        if (appV3Item.updated_at != null) {
            app.setUpdateTime(LocalDateTime.parse(appV3Item.updated_at.replace("Z", "")));
        }
        if (appV3Item.version_updated_at != null) {
            app.setLastUpdated(appV3Item.version_updated_at);
        }
        
        // 统计信息
        if (appV3Item.count != null) {
            app.setDownloadCount(appV3Item.count.downloads);
        }
        
        return app;
    }

    /**
     * 从v3接口的rating信息同步评分数据
     */
    private void syncAppScoreFromV3(String pkgId, Rating rating) {
        try {
            AppScore appScore = appScoreRepository.findById(pkgId)
                    .orElse(new AppScore());

            appScore.setPkgId(pkgId);
            appScore.setScore(rating.score);
            appScore.setTotalReviews(rating.statistics.total);

            // 设置各分数段的评论数
            appScore.setOneStarCount(rating.statistics.one);
            appScore.setTwoStarCount(rating.statistics.two);
            appScore.setThreeStarCount(rating.statistics.three);
            appScore.setFourStarCount(rating.statistics.four);
            appScore.setFiveStarCount(rating.statistics.five);

            appScore.setLastSyncTime(LocalDateTime.now());

            appScoreRepository.save(appScore);
            log.info("成功同步应用 {} 的评分信息", pkgId);
        } catch (Exception e) {
            log.error("同步应用 {} 评分时发生错误", pkgId, e);
        }
    }

}