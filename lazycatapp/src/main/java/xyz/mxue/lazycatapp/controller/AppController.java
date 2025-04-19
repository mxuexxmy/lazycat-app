package xyz.mxue.lazycatapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.mxue.lazycatapp.entity.App;
import xyz.mxue.lazycatapp.entity.Category;
import xyz.mxue.lazycatapp.service.AppService;
import xyz.mxue.lazycatapp.service.CategoryService;
import xyz.mxue.lazycatapp.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/apps")
@RequiredArgsConstructor
public class AppController {

    private final AppService appService;
    private final CategoryService categoryService;
    private final UserService userService;
    private boolean isInitialSyncComplete = false;

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

    @GetMapping("/all")
    public ResponseEntity<List<App>> getAllApps() {
        return ResponseEntity.ok(appService.findAll());
    }

    @GetMapping("/{pkgId}")
    public ResponseEntity<App> getApp(@PathVariable String pkgId) {
        return appService.findByPkgId(pkgId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<App>> searchApps(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return ResponseEntity.ok(appService.search(keyword, pageRequest));
    }

    @GetMapping("/search/all")
    public ResponseEntity<List<App>> searchAllApps(@RequestParam String keyword) {
        return ResponseEntity.ok(appService.searchAll(keyword));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<App>> getAppsByCategory(@PathVariable Integer categoryId) {
        // 如果 categoryId 为 0，返回所有应用
        if (categoryId == 0) {
            return ResponseEntity.ok(appService.findAll());
        }
        
        // 先查询分类信息
        Category category = categoryService.findById(categoryId);
        if (category == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 先尝试用中文名称查询
        List<App> apps = appService.findByCategory(category.getName());
        if (apps.isEmpty() && category.getEnglishName() != null) {
            // 如果中文名称查询不到结果，且存在英文名称，则尝试用英文名称查询
            apps = appService.findByCategory(category.getEnglishName());
        }
        
        return ResponseEntity.ok(apps);
    }

    @GetMapping("/latest")
    public ResponseEntity<List<App>> getLatestApps(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(appService.findLatestApps(limit));
    }

    @GetMapping("/popular")
    public ResponseEntity<List<App>> getPopularApps(
            @RequestParam(defaultValue = "10") int limit) {
        List<App> apps = appService.findPopularApps(limit);
        apps.sort((a, b) -> {
            int aCount = a.getDownloadCount() != null ? a.getDownloadCount() : 0;
            int bCount = b.getDownloadCount() != null ? b.getDownloadCount() : 0;
            return Integer.compare(bCount, aCount);
        });
        return ResponseEntity.ok(apps);
    }

    @GetMapping("/used")
    public ResponseEntity<List<App>> getUsedApps(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(appService.findUsedApps(limit));
    }

    @GetMapping("/developers/ranking")
    public ResponseEntity<Map<String, Object>> getDeveloperRanking() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", appService.getDeveloperRanking()
        ));
    }

    @GetMapping("/developers/{creatorId}/apps")
    public ResponseEntity<List<App>> getAppsByCreatorId(@PathVariable Long creatorId) {
        List<App> apps = appService.findByCreatorId(creatorId);
        return ResponseEntity.ok(apps);
    }

    @GetMapping("/statistics/monthly-new")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyNewApps() {
        return ResponseEntity.ok(appService.getMonthlyNewApps());
    }

    @GetMapping("/statistics/category-distribution")
    public ResponseEntity<List<Map<String, Object>>> getCategoryDistribution() {
        return ResponseEntity.ok(appService.getCategoryDistribution());
    }

    @GetMapping("/statistics/developer-activity")
    public ResponseEntity<List<Map<String, Object>>> getDeveloperActivity() {
        return ResponseEntity.ok(appService.getDeveloperActivity());
    }

    @GetMapping("/statistics/update-frequency")
    public ResponseEntity<List<Map<String, Object>>> getUpdateFrequency() {
        return ResponseEntity.ok(appService.getUpdateFrequency());
    }

    @GetMapping("/repositories")
    public ResponseEntity<List<App>> getAppRepositories() {
        List<App> apps = appService.findAll().stream()
                .filter(app -> app.getSource() != null && !app.getSource().isEmpty())
                .sorted((a, b) -> {
                    // 按更新时间倒序排序
                    if (a.getUpdateTime() == null) return 1;
                    if (b.getUpdateTime() == null) return -1;
                    return b.getUpdateTime().compareTo(a.getUpdateTime());
                })
                .toList();
        return ResponseEntity.ok(apps);
    }

    @GetMapping("/status")
    public Map<String, Object> getSyncStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("isInitialSyncComplete", isInitialSyncComplete);
        
        // 获取当前完成的数量
        long appCount = appService.count();
        long categoryCount = categoryService.count();
        
        // 从同步接口获取总数量
        long totalApps = appService.getTotalAppsCount();
        long totalCategories = categoryService.getTotalCategoriesCount();
        
        status.put("appCount", appCount);
        status.put("categoryCount", categoryCount);
        status.put("totalApps", totalApps);
        status.put("totalCategories", totalCategories);
        
        return status;
    }

    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取应用总数
        long totalApps = appService.count();
        
        // 获取开发者总数
        long totalDevelopers = appService.getDistinctCreatorIds().size();
        
        statistics.put("totalApps", totalApps);
        statistics.put("totalDevelopers", totalDevelopers);
        
        return statistics;
    }

    // 在数据同步完成后调用此方法
    public void setInitialSyncComplete() {
        this.isInitialSyncComplete = true;
    }
} 