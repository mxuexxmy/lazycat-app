package xyz.mxue.lazycatapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.mxue.lazycatapp.entity.App;
import xyz.mxue.lazycatapp.service.RecommendationService;

import java.util.List;
import java.util.Map;

@Tag(name = "推荐管理", description = "推荐管理")
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {
    private final RecommendationService recommendationService;

    @Operation(summary = "获取相似应用", description = "获取相似应用")
    @GetMapping("/similar/{pkgId}")
    public ResponseEntity<List<App>> getSimilarApps(
            @PathVariable String pkgId,
            @RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok(recommendationService.getSimilarApps(pkgId, limit));
    }

    @Operation(summary = "获取分类应用", description = "获取分类应用")
    @GetMapping("/category/{category}/popular")
    public ResponseEntity<List<App>> getPopularAppsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(recommendationService.getPopularAppsByCategory(category, limit));
    }

    @Operation(summary = "获取 emerging 分类", description = "获取 emerging 分类")
    @GetMapping("/categories/emerging")
    public ResponseEntity<List<Map<String, Object>>> getEmergingCategories() {
        return ResponseEntity.ok(recommendationService.getEmergingCategories());
    }
} 