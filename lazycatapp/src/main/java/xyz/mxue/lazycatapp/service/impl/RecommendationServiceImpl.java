package xyz.mxue.lazycatapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.entity.App;
import xyz.mxue.lazycatapp.repository.AppRepository;
import xyz.mxue.lazycatapp.service.RecommendationService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final AppRepository appRepository;

    /**
     * 获取相似应用推荐
     */
    @Override
    public List<App> getSimilarApps(String pkgId, int limit) {
        Optional<App> targetApp = appRepository.findById(pkgId);
        if (targetApp.isEmpty()) {
            return Collections.emptyList();
        }

        App app = targetApp.get();
        List<App> allApps = appRepository.findAll();

        return allApps.stream()
                .filter(a -> !a.getPackageName().equals(pkgId))
                .map(a -> {
                    int score = calculateSimilarityScore(app, a);
                    return new AbstractMap.SimpleEntry<>(a, score);
                })
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(limit)
                .map(AbstractMap.SimpleEntry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 获取分类下的热门应用
     */
    @Override
    public List<App> getPopularAppsByCategory(String category, int limit) {
        return appRepository.findByCategoryContaining(category).stream()
                .sorted((a, b) -> {
                    int aCount = a.getDownloadCount() != null ? a.getDownloadCount() : 0;
                    int bCount = b.getDownloadCount() != null ? b.getDownloadCount() : 0;
                    return Integer.compare(bCount, aCount);
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 获取新兴分类趋势
     */
    @Override
    public List<Map<String, Object>> getEmergingCategories() {
        List<App> recentApps = appRepository.findAll().stream()
                .filter(app -> app.getCreateTime() != null &&
                        app.getCreateTime().isAfter(java.time.LocalDateTime.now().minusMonths(3)))
                .toList();

        Map<String, Long> categoryGrowth = new HashMap<>();

        return categoryGrowth.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .map(entry -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("category", entry.getKey());
                    result.put("growth", entry.getValue());
                    return result;
                })
                .collect(Collectors.toList());
    }

    /**
     * 计算两个应用之间的相似度分数
     */
    private int calculateSimilarityScore(App app1, App app2) {
        int score = 0;

        // 分类相似度
//        if (app1.getCategory() != null && app2.getCategory() != null) {
//            Set<String> categories1 = new HashSet<>(app1.getCategory());
//            Set<String> categories2 = new HashSet<>(app2.getCategory());
//            score += (int) (3 * categories1.stream().filter(categories2::contains).count());
//        }

        // 关键词相似度
        if (app1.getKeywords() != null && app2.getKeywords() != null) {
            Set<String> keywords1 = new HashSet<>(Arrays.asList(app1.getKeywords().split(",")));
            Set<String> keywords2 = new HashSet<>(Arrays.asList(app2.getKeywords().split(",")));
            score += (int) (2 * keywords1.stream().filter(keywords2::contains).count());
        }

        // 平台支持相似度
        if (Objects.equals(app1.getSupportPC(), app2.getSupportPC())) score++;
        if (Objects.equals(app1.getSupportMobile(), app2.getSupportMobile())) score++;

        return score;
    }

}
