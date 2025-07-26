package xyz.mxue.lazycatapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.entity.App;
import xyz.mxue.lazycatapp.entity.User;
import xyz.mxue.lazycatapp.repository.AppRepository;
import xyz.mxue.lazycatapp.repository.UserRepository;
import xyz.mxue.lazycatapp.repository.AppScoreRepository;
import xyz.mxue.lazycatapp.repository.AppCommentRepository;
import xyz.mxue.lazycatapp.entity.Category;
import xyz.mxue.lazycatapp.entity.AppScore;
import xyz.mxue.lazycatapp.entity.AppComment;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import xyz.mxue.lazycatapp.entity.SyncInfo;
import xyz.mxue.lazycatapp.repository.CategoryRepository;
import xyz.mxue.lazycatapp.sync.SyncService;

import java.util.ArrayList;
import java.time.format.DateTimeFormatter;

public interface AppService {

    Page<App> findAll(Pageable pageable);

    List<App> findAll();

    Optional<App> findByPkgId(String pkgId);

    Page<App> search(String keyword, Pageable pageable);

    List<App> findByCategory(String category);

    Page<App> findByCategory(String category, Pageable pageable);

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

    void syncAppComments(String pkgId);

    void syncAllAppComments();

    List<AppComment> getAppComments(String pkgId);

    List<Map<String, Object>> getFiveStarAppsRanking(int limit);

    void likeComment(Long commentId);

    List<Map<String, Object>> getAllComments();

    /**
     * 根据 creatorId 查询用户昵称
     *
     * @param creatorId 创建者ID
     * @return 用户昵称，如果未找到则返回 null
     */
    String getCreatorNickname(Long creatorId);

}