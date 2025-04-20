package xyz.mxue.lazycatapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.entity.App;
import xyz.mxue.lazycatapp.entity.GitHubInfo;
import xyz.mxue.lazycatapp.entity.UserInfo;
import xyz.mxue.lazycatapp.repository.AppRepository;
import xyz.mxue.lazycatapp.repository.GitHubInfoRepository;
import xyz.mxue.lazycatapp.repository.UserInfoRepository;
import xyz.mxue.lazycatapp.repository.AppScoreRepository;
import xyz.mxue.lazycatapp.repository.AppCommentRepository;
import org.springframework.web.client.RestTemplate;
import xyz.mxue.lazycatapp.entity.Category;
import xyz.mxue.lazycatapp.entity.AppScore;
import xyz.mxue.lazycatapp.entity.AppComment;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
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
import java.util.Objects;
import com.fasterxml.jackson.databind.JsonNode;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppService {

    private final AppRepository appRepository;
    private final UserInfoRepository userInfoRepository;
    private final UserService userService;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final GitHubInfoRepository githubInfoRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String APP_LIST_URL = "https://appstore.api.lazycat.cloud/api/app/list";
    private static final String DOWNLOAD_COUNT_URL = "https://appstore.api.lazycat.cloud/api/counting";

    private final AppScoreRepository appScoreRepository;
    private static final String SCORE_API_URL = "https://appstore.api.lazycat.cloud/api/comment/score/";

    private final AppCommentRepository appCommentRepository;
    private static final String COMMENT_LIST_URL = "https://appstore.api.lazycat.cloud/api/comment/list/";

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
                    Map<String, App> existingAppMap = existingApps.stream()
                            .collect(Collectors.toMap(App::getPkgId, app -> app));

                    for (App app : apps) {
                        app.setLastUpdated(Instant.now().toString());
                        app.setPackageName(app.getPkgId());

                        // 检查是否为已存在的应用
                        App existingApp = existingAppMap.get(app.getPkgId());
                        if (existingApp != null) {
                            // 如果是已存在的应用，保留原有的下载量
                            app.setDownloadCount(existingApp.getDownloadCount());
                        } else {
                            // 如果是新应用，获取下载量
                            updateDownloadCount(app);
                            userService.updateUserInfo(app.getCreatorId());
                        }

                        // 逐个保存应用
                        appRepository.save(app);
                        log.info("成功保存应用: {}", app.getPkgId());
                    }
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
                DownloadCountResponse downloadCountResponse = objectMapper.readValue(responseBody,
                        DownloadCountResponse.class);

                if (downloadCountResponse.errorCode == 0 && downloadCountResponse.data != null) {
                    app.setDownloadCount(downloadCountResponse.data);
                    log.info("成功更新应用 {} 的下载量: {}", app.getPkgId(), downloadCountResponse.data);
                } else {
                    log.warn("应用 {} 的下载量为空，保留原有值", app.getPkgId());
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
            Request request = new Request.Builder()
                    .url(APP_LIST_URL)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("获取应用总数失败: {}", response);
                    return 0;
                }

                String responseBody = response.body().string();
                AppListResponse appListResponse = objectMapper.readValue(responseBody, AppListResponse.class);

                if (appListResponse.errorCode == 0 && appListResponse.data != null) {
                    return appListResponse.data.length;
                } else {
                    log.error("获取应用总数失败: {}", appListResponse.message);
                    return 0;
                }
            }
        } catch (IOException e) {
            log.error("获取应用总数时发生错误", e);
            return 0;
        }
    }

    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // 每6小时执行一次
    public void syncGitHubInfo() {
        // 获取所有需要同步 GitHub 信息的用户
        List<App> apps = appRepository.findAll();
        Set<Long> userIds = apps.stream()
                .map(App::getCreatorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 为每个用户同步 GitHub 信息
        for (Long userId : userIds) {
            try {
                String url = "https://playground.api.lazycat.cloud/api/github/info/" + userId;
                String response = restTemplate.getForObject(url, String.class);

                // 检查是否返回 "record not found"
                if ("record not found".equals(response)) {
                    log.warn("用户 {} 的 GitHub 信息不存在", userId);
                    continue;
                }

                // 解析响应
                JsonNode root = objectMapper.readTree(response);

                // 更新 GitHub 信息
                GitHubInfo githubInfo = githubInfoRepository.findByUserId(userId)
                        .orElse(new GitHubInfo());

                // 设置基本信息
                githubInfo.setUserId(userId);
                githubInfo.setUid(root.get("uid").asLong());
                githubInfo.setCreatedAt(LocalDateTime.parse(root.get("createdAt").asText().replace("Z", "")));
                githubInfo.setUpdatedAt(LocalDateTime.parse(root.get("updatedAt").asText().replace("Z", "")));

                // 设置用户信息
                JsonNode userNode = root.get("user");
                githubInfo.setUsername(userNode.get("username").asText());
                githubInfo.setNickname(userNode.get("nickname").asText());
                githubInfo.setAvatar(userNode.get("avatar").asText());
                githubInfo.setDescription(userNode.get("description").asText());
                githubInfo.setGithubUsername(userNode.get("githubUsername").asText());

                // 设置统计信息
                JsonNode summaryNode = root.get("summary");
                githubInfo.setTotalPRs(summaryNode.get("totalPRs").asInt());
                githubInfo.setTotalCommits(summaryNode.get("totalCommits").asInt());
                githubInfo.setTotalIssues(summaryNode.get("totalIssues").asInt());
                githubInfo.setContributedTo(summaryNode.get("contributedTo").asInt());

                // 设置排名信息
                JsonNode rankNode = summaryNode.get("rank");
                githubInfo.setRankLevel(rankNode.get("level").asText());
                githubInfo.setRankScore(rankNode.get("score").asDouble());

                // 设置编程语言信息
                githubInfo.setTopLangs(root.get("topLangs").toString());

                // 设置最后同步时间
                githubInfo.setLastSyncTime(LocalDateTime.now());

                // 保存信息
                githubInfoRepository.save(githubInfo);
                log.info("成功同步用户 {} 的 GitHub 信息", userId);
            } catch (Exception e) {
                log.error("同步用户 {} 的 GitHub 信息失败: {}", userId, e.getMessage());
            }
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

    @Scheduled(cron = "0 0 */2 * * *") // 每2小时执行一次，从0点开始
    public void syncAllAppScores() {
        log.info("开始同步所有应用评分...");
        List<App> apps = appRepository.findAll();
        for (App app : apps) {
            try {
                syncAppScore(app.getPkgId());
                Thread.sleep(1000); // 添加1秒延迟，避免请求过于频繁
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        log.info("应用评分同步完成");
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
                        // 删除旧的评论
                        if (page == 1) {
                            appCommentRepository.deleteByPkgId(pkgId);
                        }

                        // 保存新的评论
                        for (Comment comment : commentResponse.data.list) {
                            AppComment appComment = new AppComment();
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

    @Scheduled(cron = "0 0 */2 * * *") // 每2小时执行一次，从0点开始
    public void syncAllAppComments() {
        log.info("开始同步所有应用评论...");
        List<AppScore> scores = appScoreRepository.findAll();
        for (AppScore score : scores) {
            try {
                syncAppComments(score.getPkgId());
                Thread.sleep(5000); // 添加10秒延迟，避免请求过于频繁
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        log.info("应用评论同步完成");
    }

    public List<AppComment> getAppComments(String pkgId) {
        return appCommentRepository.findByPkgId(pkgId);
    }

}