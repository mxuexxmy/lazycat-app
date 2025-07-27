package xyz.mxue.lazycatapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.mxue.lazycatapp.entity.App;
import xyz.mxue.lazycatapp.enums.SyncTypeEnum;
import xyz.mxue.lazycatapp.service.AppService;
import xyz.mxue.lazycatapp.service.CategoryService;
import xyz.mxue.lazycatapp.service.UserService;
import xyz.mxue.lazycatapp.entity.AppComment;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

import xyz.mxue.lazycatapp.sync.AppSyncService;
import xyz.mxue.lazycatapp.sync.CategorySyncService;
import xyz.mxue.lazycatapp.sync.SyncService;
import xyz.mxue.lazycatapp.entity.SyncInfo;

@Tag(name = "应用管理", description = "应用管理")
@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
public class AppController {

    private final AppService appService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final SyncService syncService;
    private final AppSyncService appSyncService;
    private final CategorySyncService categorySyncService;

    @Operation(summary = "获取应用", description = "获取应用")
    @GetMapping
    public ResponseEntity<Page<App>> getApps(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastUpdated") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        return ResponseEntity.ok(appService.findAll(pageRequest));
    }

    @Operation(summary = "获取所有应用", description = "获取所有应用")
    @GetMapping("/all")
    public ResponseEntity<List<App>> getAllApps() {
        return ResponseEntity.ok(appService.findAll());
    }

    @Operation(summary = "根据包ID获取应用", description = "根据包ID获取应用")
    @GetMapping("/{pkgId}")
    public ResponseEntity<App> getApp(@PathVariable String pkgId) {
        Optional<App> appOpt = appService.findByPkgId(pkgId);

        if (appOpt.isPresent()) {
            App app = appOpt.get();
            //app.setCreator(appService.getCreatorNickname(app.getCreatorId()));
            return ResponseEntity.ok(app);
        }

        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "搜索应用", description = "搜索应用")
    @GetMapping("/search")
    public ResponseEntity<Page<App>> searchApps(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return ResponseEntity.ok(appService.search(keyword, pageRequest));
    }

    @Operation(summary = "全站应用搜索", description = "全站应用搜索")
    @GetMapping("/search/all")
    public ResponseEntity<List<App>> searchAllApps(@RequestParam String keyword) {
        return ResponseEntity.ok(appService.searchAll(keyword));
    }

    @Operation(summary = "根据分类ID获取应用", description = "根据分类ID获取应用")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<App>> getAppsByCategory(@PathVariable Integer categoryId) {
        // 如果 categoryId 为 0，返回所有应用
        if (categoryId == 0) {
            return ResponseEntity.ok(appService.findAll());
        }

        // 先查询分类信息
        try {
            List<App> apps = appService.findByCategory(categoryId.toString());
            return ResponseEntity.ok(apps);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "获取最新应用", description = "获取最新应用")
    @GetMapping("/latest")
    public ResponseEntity<List<App>> getLatestApps(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(appService.findLatestApps(limit));
    }

    @Operation(summary = "获取最热门应用", description = "获取最热门应用")
    @GetMapping("/popular")
    public ResponseEntity<List<App>> getPopularApps(
            @RequestParam(defaultValue = "100") int limit) {
        List<App> apps = appService.findPopularApps(limit);
        apps.sort((a, b) -> {
            int aCount = a.getDownloadCount() != null ? a.getDownloadCount() : 0;
            int bCount = b.getDownloadCount() != null ? b.getDownloadCount() : 0;
            return Integer.compare(bCount, aCount);
        });
        return ResponseEntity.ok(apps);
    }

    @Operation(summary = "获取最常用应用", description = "获取最常用应用")
    @GetMapping("/used")
    public ResponseEntity<List<App>> getUsedApps(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(appService.findUsedApps(limit));
    }

    @Operation(summary = "获取开发者排名", description = "获取开发者排名")
    @GetMapping("/developers/ranking")
    public ResponseEntity<Map<String, Object>> getDeveloperRanking() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", appService.getDeveloperRanking()));
    }

    @Operation(summary = "根据开发者ID获取应用", description = "根据开发者ID获取应用")
    @GetMapping("/developers/{creatorId}/apps")
    public ResponseEntity<List<App>> getAppsByCreatorId(@PathVariable Long creatorId) {
        List<App> apps = appService.findByCreatorId(creatorId);
        return ResponseEntity.ok(apps);
    }

    @Operation(summary = "获取应用统计信息", description = "获取应用统计信息")
    @GetMapping("/statistics/monthly-new")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyNewApps() {
        return ResponseEntity.ok(appService.getMonthlyNewApps());
    }

    @Operation(summary = "获取应用分类分布", description = "获取应用分类分布")
    @GetMapping("/statistics/category-distribution")
    public ResponseEntity<List<Map<String, Object>>> getCategoryDistribution() {
        return ResponseEntity.ok(appService.getCategoryDistribution());
    }

    @Operation(summary = "获取开发者活跃度", description = "获取开发者活跃度")
    @GetMapping("/statistics/developer-activity")
    public ResponseEntity<List<Map<String, Object>>> getDeveloperActivity() {
        return ResponseEntity.ok(appService.getDeveloperActivity());
    }

    @Operation(summary = "获取应用更新频率", description = "获取应用更新频率")
    @GetMapping("/statistics/update-frequency")
    public ResponseEntity<List<Map<String, Object>>> getUpdateFrequency() {
        return ResponseEntity.ok(appService.getUpdateFrequency());
    }

    @Operation(summary = "获取应用仓库", description = "获取应用仓库")
    @GetMapping("/repositories")
    public ResponseEntity<List<App>> getAppRepositories() {
        List<App> apps = appService.findAll().stream()
                .filter(app -> app.getSource() != null && !app.getSource().isEmpty())
                .sorted((a, b) -> {
                    // 按更新时间倒序排序
                    if (a.getUpdateTime() == null)
                        return 1;
                    if (b.getUpdateTime() == null)
                        return -1;
                    return b.getUpdateTime().compareTo(a.getUpdateTime());
                })
                .toList();
        return ResponseEntity.ok(apps);
    }

    @Operation(summary = "获取同步状态", description = "获取同步状态")
    @GetMapping("/status")
    public Map<String, Object> getSyncStatus() {
        Map<String, Object> status = new HashMap<>();

        // 获取应用和分类的同步信息
        SyncInfo appSyncInfo = syncService.getSyncInfo(SyncTypeEnum.APP);
        SyncInfo categorySyncInfo = syncService.getSyncInfo(SyncTypeEnum.CATEGORY);

        // 计算是否初始同步已完成
        boolean isInitialSyncComplete = (appSyncInfo != null && appSyncInfo.isInitialSyncCompleted()) &&
                (categorySyncInfo != null && categorySyncInfo.isInitialSyncCompleted());

        status.put("isInitialSyncComplete", isInitialSyncComplete);

        // 获取当前完成的数量
        long appCount = appService.count();
        long categoryCount = categoryService.count();

        // 安全地获取 totalCount，如果为 null 则使用 0
        Long totalApps = appSyncService.getTotalAppsCount();
        Long totalCategories = categorySyncService.getTotalCategoryCount();

        status.put("appCount", appCount);
        status.put("categoryCount", categoryCount);
        status.put("totalApps", totalApps);
        status.put("totalCategories", totalCategories);

        return status;
    }

    @Operation(summary = "获取统计信息", description = "获取统计信息")
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 获取应用总数
        long totalApps = appService.count();

        // 获取开发者总数
        long totalDevelopers = userService.count();

        statistics.put("totalApps", totalApps);
        statistics.put("totalDevelopers", totalDevelopers);

        return statistics;
    }

    @Operation(summary = "获取统计概览", description = "获取统计概览")
    @GetMapping("/statistics/overview")
    public Map<String, Object> getStatisticsOverview() {
        return appService.getStatisticsOverview();
    }

    @Operation(summary = "获取热门应用", description = "获取热门应用")
    @GetMapping("/statistics/apps/popular")
    public List<Map<String, Object>> getPopularApps() {
        return appService.getPopularApps(10);
    }

    @Operation(summary = "获取应用分类统计", description = "获取应用分类统计")
    @GetMapping("/statistics/apps/categories")
    public List<Map<String, Object>> getCategoryStats() {
        return appService.getCategoryDistribution();
    }

    @Operation(summary = "获取活跃用户", description = "获取活跃用户")
    @GetMapping("/statistics/users/active")
    public List<Map<String, Object>> getActiveUsers() {
        return userService.getActiveUsers(10);
    }

    @Operation(summary = "获取用户增长统计", description = "获取用户增长统计")
    @GetMapping("/statistics/users/growth")
    public Map<String, Object> getUserGrowth() {
        return userService.getUserGrowthStats();
    }

    @Operation(summary = "获取应用评论", description = "获取应用评论")
    @GetMapping("/{pkgId}/comments")
    public List<AppComment> getAppComments(@PathVariable String pkgId) {
        return appService.getAppComments(pkgId);
    }

    @Operation(summary = "点赞评论", description = "点赞评论")
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<?> likeComment(@PathVariable Long commentId) {
        appService.likeComment(commentId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "获取五星应用排名", description = "获取五星应用排名")
    @GetMapping("/statistics/app/five-star")
    public ResponseEntity<List<Map<String, Object>>> getFiveStarAppsRanking(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(appService.getFiveStarAppsRanking(limit));
    }
}