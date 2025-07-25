package xyz.mxue.lazycatapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.entity.CommunityUser;
import xyz.mxue.lazycatapp.entity.UserInfo;
import xyz.mxue.lazycatapp.repository.CommunityUserRepository;
import xyz.mxue.lazycatapp.repository.UserInfoRepository;
import xyz.mxue.lazycatapp.repository.AppRepository;
import xyz.mxue.lazycatapp.entity.App;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserInfoRepository userInfoRepository;
    private final CommunityUserRepository communityUserRepository;
    private final AppRepository appRepository;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    private static final String USER_INFO_URL = "https://playground.api.lazycat.cloud/api/user/info/";

    public void updateUserInfo(Long userId) {
        log.info("开始更新用户信息: {}", userId);
        try {
            Request request = new Request.Builder()
                    .url(USER_INFO_URL + userId)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("获取用户信息失败: {}", response);
                    return;
                }

                String responseBody = response.body().string();
                UserResponse userResponse = objectMapper.readValue(responseBody, UserResponse.class);

                if (userResponse != null) {
                    // 更新社区用户信息
                    CommunityUser communityUser = communityUserRepository.findById(userResponse.uid)
                            .orElse(new CommunityUser());
                    communityUser.setUid(userResponse.uid);
                    communityUser.setReceiveThumbs(userResponse.receiveThumbs);
                    communityUser.setFollows(userResponse.follows);
                    communityUser.setFans(userResponse.fans);
                    communityUser.setFollowed(userResponse.followed);
                    communityUser.setGuidelineCounts(userResponse.guidelineCounts);
                    communityUser.setHideFollows(userResponse.hideFollows);
                    communityUser.setHideFans(userResponse.hideFans);
                    communityUser.setHideThumbs(userResponse.hideThumbs);
                    communityUser.setOnlyFollowedAtMe(userResponse.onlyFollowedAtMe);
                    communityUser.setOnlyFollowedCommentMe(userResponse.onlyFollowedCommentMe);
                    communityUserRepository.save(communityUser);

                    // 更新用户个人信息
                    UserInfo userInfo = userInfoRepository.findById(userResponse.user.id)
                            .orElse(new UserInfo());
                    userInfo.setId(userResponse.user.id);
                    userInfo.setUsername(userResponse.user.username);
                    userInfo.setIsUsernameSet(userResponse.user.isUsernameSet);
                    userInfo.setNickname(userResponse.user.nickname);
                    userInfo.setAvatar(userResponse.user.avatar);
                    userInfo.setDescription(userResponse.user.description);
                    userInfo.setStatus(userResponse.user.status);
                    userInfo.setGithubUsername(userResponse.user.githubUsername);
                    userInfo.setIsCurrentLoginUser(userResponse.user.isCurrentLoginUser);

                    // 设置时间戳
                    String now = Instant.now().toString();
                    if (userInfo.getCreatedAt() == null) {
                        userInfo.setCreatedAt(now);
                    }
                    userInfo.setUpdatedAt(now);

                    userInfoRepository.save(userInfo);
                    log.info("成功更新用户信息: {}", userId);
                }
            }
        } catch (IOException e) {
            log.error("更新用户信息时发生错误", e);
        }
    }

    public UserInfo getUserInfo(Long userId) {
        return userInfoRepository.findById(userId).orElse(null);
    }

    public CommunityUser getCommunityUser(Long userId) {
        return communityUserRepository.findById(userId).orElse(null);
    }

    /**
     * 获取所有用户个人信息
     *
     * @return 用户个人信息列表
     */
    public List<UserInfo> getAllUserInfos() {
        return userInfoRepository.findAll();
    }

    /**
     * 获取所有用户社区信息
     *
     * @return 用户社区信息列表
     */
    public List<CommunityUser> getAllCommunityUsers() {
        return communityUserRepository.findAll();
    }

    public long count() {
        return userInfoRepository.count();
    }

    public List<Map<String, Object>> getActiveUsers(int limit) {
        List<UserInfo> users = userInfoRepository.findActiveUsers(PageRequest.of(0, limit));
        return users.stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("nickname", user.getNickname());
            userMap.put("avatar", user.getAvatar());
            userMap.put("lastActive", user.getUpdatedAt());

            // 获取该用户的所有应用并计算总下载量
            List<App> userApps = appRepository.findByCreatorId(user.getId());
            int totalDownloads = userApps.stream()
                    .mapToInt(app -> app.getDownloadCount() != null ? app.getDownloadCount() : 0)
                    .sum();

            userMap.put("downloads", totalDownloads);
            return userMap;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getUserGrowthStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("daily", userInfoRepository.countNewUsersToday());
        stats.put("weekly", userInfoRepository.countNewUsersThisWeek());
        stats.put("monthly", userInfoRepository.countNewUsersThisMonth());
        return stats;
    }

    /**
     * 同步所有开发者信息（人员信息）
     * 每天凌晨2点自动同步
     */
   // @Scheduled(cron = "0 2 * * * *")
    public void syncAllDevelopers() {
        log.info("开始同步所有开发者信息...");
        int page = 0;
        int size = 100;
        boolean hasMore = true;
        String apiUrl = "https://appstore.api.lazycat.cloud/api/v3/user/developer/list";
        try {
            while (hasMore) {
                String url = apiUrl + "?page=" + page + "&size=" + size;
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        log.error("获取开发者列表失败: {}", response);
                        break;
                    }
                    String responseBody = response.body().string();
                    DeveloperListResponse devList = objectMapper.readValue(responseBody, DeveloperListResponse.class);
                    if (devList != null && devList.items != null) {
                        for (DeveloperItem dev : devList.items) {
                            if (dev.developer_id != null) {
                                updateUserInfo(dev.developer_id);
                            }
                        }
                        hasMore = (devList.page + 1) * devList.size < devList.total;
                        page++;
                    } else {
                        hasMore = false;
                    }
                }
            }
            log.info("同步所有开发者信息完成");
        } catch (Exception e) {
            log.error("同步所有开发者信息时发生错误", e);
        }
    }

    // 用于解析开发者列表API响应
    @lombok.Data
    private static class DeveloperListResponse {
        private List<DeveloperItem> items;
        private int page;
        private int size;
        private int total;
    }
    @lombok.Data
    private static class DeveloperItem {
        private Long developer_id;
        private Long id;
        private String username;
        private String nickname;
        private String description;
        private String avatar;
        private String github_username;
        private int continuous_submission_day_count;
        private int app_total_count;
    }

    @lombok.Data
    private static class UserResponse {
        private Long uid;
        private UserData user;
        private Integer receiveThumbs;
        private Integer follows;
        private Integer fans;
        private Boolean followed;
        private Integer guidelineCounts;
        private Boolean hideFollows;
        private Boolean hideFans;
        private Boolean hideThumbs;
        private Boolean onlyFollowedAtMe;
        private Boolean onlyFollowedCommentMe;
    }

    @lombok.Data
    private static class UserData {
        private Long id;
        private String username;
        private Boolean isUsernameSet;
        private String nickname;
        private String avatar;
        private String description;
        private Integer status;
        private String githubUsername;
        private Boolean isCurrentLoginUser;
    }
} 