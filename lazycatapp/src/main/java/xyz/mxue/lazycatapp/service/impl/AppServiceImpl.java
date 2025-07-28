package xyz.mxue.lazycatapp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.entity.*;
import xyz.mxue.lazycatapp.repository.*;
import xyz.mxue.lazycatapp.service.AppService;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppServiceImpl implements AppService {

    private final AppRepository appRepository;
    private final UserRepository userRepository;

    private final AppScoreRepository appScoreRepository;

    private final AppCommentRepository appCommentRepository;

    private final CommunityUserRepository communityUserRepository;

    @Override
    public Page<App> findAll(Pageable pageable) {
        return appRepository.findAll(pageable);
    }

    @Override
    public List<App> findAll() {
        return appRepository.findAll();
    }

    @Override
    public Optional<App> findByPkgId(String pkgId) {
        App queryApp = new App();
        queryApp.setPackageName(pkgId);
        Example<App> example = Example.of(queryApp);
        return appRepository.findOne(example);
    }

    @Override
    public Page<App> search(String keyword, Pageable pageable) {
        return appRepository.findByNameContainingOrDescriptionContaining(keyword, keyword, pageable);
    }

    @Override
    public List<App> findByCategory(String category) {
        return appRepository.findByCategoryContaining(category);
    }

    @Override
    public List<App> findLatestApps(int limit) {
        return appRepository.findTopNByOrderByLastUpdatedDesc(limit);
    }

    @Override
    public List<App> findPopularApps(int limit) {
        List<App> allApps = appRepository.findAll();
        return allApps.stream().sorted((a, b) -> {
            int aCount = a.getDownloadCount() != null ? a.getDownloadCount() : 0;
            int bCount = b.getDownloadCount() != null ? b.getDownloadCount() : 0;
            return Integer.compare(bCount, aCount);
        }).limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<App> searchAll(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return appRepository.findAll().stream().filter(app -> containsIgnoreCase(app.getName(), keyword) || containsIgnoreCase(app.getPackageName(), keyword) || containsIgnoreCase(app.getKeywords(), keyword) || containsIgnoreCase(app.getSource(), keyword) || containsIgnoreCase(app.getDescription(), keyword) || containsIgnoreCase(app.getBrief(), keyword)).collect(Collectors.toList());
    }

    private boolean containsIgnoreCase(String text, String searchTerm) {
        return text != null && text.toLowerCase().contains(searchTerm.toLowerCase());
    }

    @Override
    public List<App> findUsedApps(int limit) {
        return appRepository.findTopNByOrderByDownloadCountDesc(limit);
    }

    @Override
    public List<App> findByCreatorId(Long creatorId) {
        App app = new App();
        app.setCreatorId(creatorId);
        Example<App> queryParam = Example.of(app);
        return appRepository.findAll(queryParam);
    }

    @Override
    public List<Map<String, Object>> getDeveloperRanking() {
        List<User> userList = userRepository.findAll();
        List<CommunityUser> communityUserList = communityUserRepository.findAll();
        Map<Long, Integer> userGuidelineCountsNap = new HashMap<>();
        for (CommunityUser communityUser : communityUserList) {
            userGuidelineCountsNap.put(communityUser.getUid(), communityUser.getGuidelineCounts());
        }

        return userList.stream().map(user -> {
            List<App> apps = findByCreatorId(user.getId());
            log.error(user.getId() + "-的-"  + apps);
            Map<String, Object> result = new HashMap<>();
            int totalDownloads = 0;
            String lastUpdateDate = "";
            List<Map<String, Object>> topApps = new ArrayList<>();
            if (CollectionUtil.isNotEmpty(apps)) {
                // 按下载量排序并取前三
                topApps = apps.stream().sorted((a, b) -> {
                    int aCount = a.getDownloadCount() != null ? a.getDownloadCount() : 0;
                    int bCount = b.getDownloadCount() != null ? b.getDownloadCount() : 0;
                    return Integer.compare(bCount, aCount);
                }).limit(3).map(app -> {
                    Map<String, Object> appMap = new HashMap<>();
                    appMap.put("pkgId", app.getPackageName());
                    appMap.put("name", app.getName());
                    appMap.put("updateDate", app.getAppUpdateTime() != null ?
                            app.getAppUpdateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
                    return appMap;
                }).collect(Collectors.toList());

                totalDownloads = apps.stream().mapToInt(app -> app.getDownloadCount() != null ? app.getDownloadCount() : 0).sum();

                lastUpdateDate = apps.stream().map(App::getAppUpdateTime).filter(Objects::nonNull)
                        .map(getAppUpdateTime -> getAppUpdateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).max(String::compareTo).orElse("");
            }

            result.put("id", user.getId());
            result.put("nickName", user.getNickname() != null ? user.getNickname() : "未知开发者");
            result.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
            result.put("appCount", apps.size());
            result.put("totalDownloads", totalDownloads);
            result.put("apps", topApps);
            result.put("lastUpdateDate", lastUpdateDate);
            result.put("guidelineCounts", 0);
            if (userGuidelineCountsNap.containsKey(user.getId())) {
                result.put("guidelineCounts", userGuidelineCountsNap.get(user.getId()));
            }
            return result;
        }).collect(Collectors.toList());
    }

    /**
     * 获取所有不重复的开发者ID
     *
     * @return 开发者ID列表
     */
    @Override
    public List<Long> getDistinctCreatorIds() {
        return appRepository.findDistinctCreatorIds();
    }

    @Override
    // 获取月度新增应用数据
    public List<Map<String, Object>> getMonthlyNewApps() {
        List<App> apps = appRepository.findAll();
        Map<YearMonth, Long> monthlyCount = apps.stream().collect(Collectors.groupingBy(app -> YearMonth.from(app.getCreateTime()), Collectors.counting()));

        return monthlyCount.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(entry -> {
            Map<String, Object> result = new HashMap<>();
            result.put("month", entry.getKey().toString());
            result.put("count", entry.getValue());
            return result;
        }).collect(Collectors.toList());
    }

    @Override
    // 获取应用类别分布数据
    public List<Map<String, Object>> getCategoryDistribution() {
        List<App> apps = appRepository.findAll();
        Map<String, Long> categoryCount = new HashMap<>();
        categoryCount.put("原创应用", 0L);
        categoryCount.put("移植应用", 0L);
        categoryCount.put("官方应用", 0L);
        categoryCount.put("其他", 0L);
        // 原创应用、移植应用、官方应用
        for (App app : apps) {
            if (StrUtil.isBlank(app.getKindIds())) {
                categoryCount.put("移植应用", categoryCount.get("移植应用") + 1);
            } else if ("2" .equals(app.getKindIds())) {
                categoryCount.put("官方应用", categoryCount.get("官方应用") + 1);
            } else if ("1" .equals(app.getKindIds())) {
                categoryCount.put("原创应用", categoryCount.get("原创应用") + 1);
            } else {
                categoryCount.put("其他", categoryCount.get("其他") + 1);
            }
        }

        return categoryCount.entrySet().stream().map(entry -> {
            Map<String, Object> result = new HashMap<>();
            result.put("category", entry.getKey());
            result.put("count", entry.getValue());
            return result;
        }).collect(Collectors.toList());
    }

    @Override
    // 获取开发者活跃度数据
    public List<Map<String, Object>> getDeveloperActivity() {
        List<App> apps = appRepository.findAll();
        Map<String, Long> developerAppCounts = new HashMap<>();

        return developerAppCounts.entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed()).map(entry -> {
            Map<String, Object> result = new HashMap<>();
            result.put("developer", entry.getKey());
            result.put("appCount", entry.getValue());
            return result;
        }).collect(Collectors.toList());
    }

    @Override
    // 获取应用更新频率数据
    public List<Map<String, Object>> getUpdateFrequency() {
        List<App> apps = appRepository.findAll();
        return apps.stream().filter(app -> app.getUpdateTime() != null && app.getCreateTime() != null).map(app -> {
            Map<String, Object> result = new HashMap<>();
            result.put("pkgId", app.getPackageName());
            result.put("name", app.getName());
            long daysBetween = ChronoUnit.DAYS.between(app.getCreateTime(), app.getUpdateTime());
            result.put("updateFrequency", daysBetween);
            return result;
        }).sorted(Comparator.comparingLong(m -> (Long) m.get("updateFrequency"))).collect(Collectors.toList());
    }

    @Override
    public long count() {
        return appRepository.count();
    }

    @Override
    public long getTotalDownloads() {
        return appRepository.getTotalDownloads();
    }

    @Override
    public List<Map<String, Object>> getPopularApps(int limit) {
        List<App> apps = appRepository.findAll().stream().sorted((a, b) -> {
            int aCount = a.getDownloadCount() != null ? a.getDownloadCount() : 0;
            int bCount = b.getDownloadCount() != null ? b.getDownloadCount() : 0;
            return Integer.compare(bCount, aCount);
        }).limit(limit).toList();

        return apps.stream().map(app -> {
            Map<String, Object> appMap = new HashMap<>();
            appMap.put("name", app.getName());
            appMap.put("pkgId", app.getPackageName());
            appMap.put("downloads", app.getDownloadCount() != null ? app.getDownloadCount() : 0);
            if (StrUtil.isBlank(app.getKindIds())) {
                appMap.put("category", "移植应用");
            } else if ("1" .equals(app.getKindIds())) {
                appMap.put("category", "原创应用");
            } else if ("2" .equals(app.getKindIds())) {
                appMap.put("category", "官方应用");
            } else {
                appMap.put("category", "其他");
            }

            return appMap;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getStatisticsOverview() {
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalApps", appRepository.count());
        overview.put("totalDownloads", appRepository.findAll().stream().mapToInt(app -> app.getDownloadCount() != null ? app.getDownloadCount() : 0).sum());
        overview.put("developerCount", userRepository.count());
        return overview;
    }

    @Override
    public List<Map<String, Object>> getFiveStarApps() {
        return appScoreRepository.findFiveStarApps().stream().map(score -> {
            Map<String, Object> result = new HashMap<>();
            App app = appRepository.findById(score.getPkgId()).orElse(null);
            if (app != null) {
                result.put("pkgId", score.getPkgId());
                result.put("name", app.getName());
                result.put("score", score.getScore());
                result.put("totalReviews", score.getTotalReviews());
            }
            return result;
        }).filter(map -> !map.isEmpty()).collect(Collectors.toList());
    }

    @Override
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

    @Override
    public List<Map<String, Object>> getMostReviewedApps() {
        return appScoreRepository.findTop10ByOrderByTotalReviewsDesc().stream().map(score -> {
            Map<String, Object> result = new HashMap<>();
            App app = appRepository.findById(score.getPkgId()).orElse(null);
            if (app != null) {
                result.put("pkgId", score.getPkgId());
                result.put("name", app.getName());
                result.put("totalReviews", score.getTotalReviews());
                result.put("score", score.getScore());
            }
            return result;
        }).filter(map -> !map.isEmpty()).collect(Collectors.toList());
    }

    @Override
    public List<AppComment> getAppComments(String pkgId) {
        return appCommentRepository.findByPkgId(pkgId);
    }

    @Override
    public List<Map<String, Object>> getFiveStarAppsRanking(int limit) {
        return appScoreRepository.findAll().stream().filter(score -> score.getScore() != null && score.getScore() >= 4.5) // 筛选评分大于等于4.5的应用
                .sorted((a, b) -> {
                    // 首先按评分排序
                    int scoreCompare = Double.compare(b.getScore(), a.getScore());
                    if (scoreCompare != 0) {
                        return scoreCompare;
                    }
                    // 如果评分相同，则按评论数排序
                    return Integer.compare(b.getTotalReviews(), a.getTotalReviews());
                }).limit(limit).map(score -> {
                    Map<String, Object> appMap = new HashMap<>();
                    // 获取应用信息
                    App queryApp = new App();
                    queryApp.setPackageName(score.getPkgId());
                    Example<App> example = Example.of(queryApp);
                    App app = appRepository.findOne(example).orElse(null);
                    if (app != null) {
                        appMap.put("pkgId", app.getPackageName());
                        appMap.put("name", app.getName());
                        appMap.put("iconPath", app.getIconPath());
                        appMap.put("description", app.getDescription());
                        appMap.put("downloadCount", app.getDownloadCount());
                        appMap.put("score", score.getScore());
                        appMap.put("totalReviews", score.getTotalReviews());
                        appMap.put("creator", app.getCreator());
                    }
                    return appMap;
                }).collect(Collectors.toList());
    }

    @Override
    public void likeComment(Long commentId) {
        AppComment comment = appCommentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));
        comment.setLikeCounts(comment.getLikeCounts() + 1);
        appCommentRepository.save(comment);
    }

    /**
     * 根据 creatorId 查询用户昵称
     *
     * @param creatorId 创建者ID
     * @return 用户昵称，如果未找到则返回 null
     */
    @Override
    public String getCreatorNickname(Long creatorId) {
        if (creatorId == null) {
            return null;
        }
        User userInfo = userRepository.findById(creatorId).orElse(null);
        return userInfo != null ? userInfo.getNickname() : null;
    }

}
