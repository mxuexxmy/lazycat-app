package xyz.mxue.lazycatapp.service;

import xyz.mxue.lazycatapp.entity.App;
import xyz.mxue.lazycatapp.repository.AppRepository;

import java.util.*;
import java.util.stream.Collectors;

public interface RecommendationService {

    /**
     * 获取相似应用推荐
     */
    List<App> getSimilarApps(String pkgId, int limit);

    /**
     * 获取分类下的热门应用
     */
    List<App> getPopularAppsByCategory(String category, int limit);

    /**
     * 获取新兴分类趋势
     */
    List<Map<String, Object>> getEmergingCategories();
} 