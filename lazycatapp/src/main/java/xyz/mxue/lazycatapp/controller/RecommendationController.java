package xyz.mxue.lazycatapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.mxue.lazycatapp.entity.App;
import xyz.mxue.lazycatapp.service.RecommendationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {
    private final RecommendationService recommendationService;

    @GetMapping("/similar/{pkgId}")
    public ResponseEntity<List<App>> getSimilarApps(
            @PathVariable String pkgId,
            @RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok(recommendationService.getSimilarApps(pkgId, limit));
    }

    @GetMapping("/category/{category}/popular")
    public ResponseEntity<List<App>> getPopularAppsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(recommendationService.getPopularAppsByCategory(category, limit));
    }

    @GetMapping("/categories/emerging")
    public ResponseEntity<List<Map<String, Object>>> getEmergingCategories() {
        return ResponseEntity.ok(recommendationService.getEmergingCategories());
    }
} 