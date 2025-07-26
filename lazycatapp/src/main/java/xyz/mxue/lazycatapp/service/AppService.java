package xyz.mxue.lazycatapp.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import xyz.mxue.lazycatapp.entity.App;
import xyz.mxue.lazycatapp.entity.AppComment;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AppService {

    Page<App> findAll(Pageable pageable);

    List<App> findAll();

    Optional<App> findByPkgId(String pkgId);

    Page<App> search(String keyword, Pageable pageable);

    List<App> findByCategory(String category);

    List<App> findLatestApps(int limit);

    List<App> findPopularApps(int limit);

    List<App> searchAll(String keyword);


    List<App> findUsedApps(int limit);

    List<App> findByCreatorId(Long creatorId);

    List<Map<String, Object>> getDeveloperRanking();


    /**
     * 获取所有不重复的开发者ID
     *
     * @return 开发者ID列表
     */
    List<Long> getDistinctCreatorIds();

    // 获取月度新增应用数据
    List<Map<String, Object>> getMonthlyNewApps();

    // 获取应用类别分布数据
    List<Map<String, Object>> getCategoryDistribution();

    // 获取开发者活跃度数据
    List<Map<String, Object>> getDeveloperActivity();

    // 获取应用更新频率数据
    List<Map<String, Object>> getUpdateFrequency();

    long count();

    long getTotalDownloads();

    List<Map<String, Object>> getPopularApps(int limit);

    Map<String, Object> getStatisticsOverview();


    List<Map<String, Object>> getFiveStarApps();

    Map<String, Integer> getScoreDistribution();

    List<Map<String, Object>> getMostReviewedApps();



    List<AppComment> getAppComments(String pkgId);

    List<Map<String, Object>> getFiveStarAppsRanking(int limit);

    void likeComment(Long commentId);

    /**
     * 根据 creatorId 查询用户昵称
     *
     * @param creatorId 创建者ID
     * @return 用户昵称，如果未找到则返回 null
     */
    String getCreatorNickname(Long creatorId);

}