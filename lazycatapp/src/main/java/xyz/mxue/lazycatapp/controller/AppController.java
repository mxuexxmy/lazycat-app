package xyz.mxue.lazycatapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.mxue.lazycatapp.entity.App;
import xyz.mxue.lazycatapp.service.AppService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/apps")
@RequiredArgsConstructor
public class AppController {

    private final AppService appService;

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

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<App>> getAppsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return ResponseEntity.ok(appService.findByCategory(category, pageRequest));
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
    public ResponseEntity<Map<String, Object>> getAppsByCreatorId(@PathVariable Long creatorId) {
        List<App> apps = appService.findByCreatorId(creatorId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", apps.stream()
                .map(app -> Map.of(
                    "pkgId", app.getPkgId(),
                    "name", app.getName(),
                    "updateDate", app.getUpdateDate(),
                    "downloadCount", app.getDownloadCount()
                ))
                .collect(Collectors.toList())
        ));
    }
} 