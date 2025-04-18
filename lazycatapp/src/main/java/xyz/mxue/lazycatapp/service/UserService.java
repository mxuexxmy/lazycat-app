package xyz.mxue.lazycatapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.entity.CommunityUser;
import xyz.mxue.lazycatapp.entity.UserInfo;
import xyz.mxue.lazycatapp.repository.CommunityUserRepository;
import xyz.mxue.lazycatapp.repository.UserInfoRepository;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserInfoRepository userInfoRepository;
    private final CommunityUserRepository communityUserRepository;
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
                    CommunityUser communityUser = new CommunityUser();
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
                    UserInfo userInfo = new UserInfo();
                    userInfo.setId(userResponse.user.id);
                    userInfo.setUsername(userResponse.user.username);
                    userInfo.setIsUsernameSet(userResponse.user.isUsernameSet);
                    userInfo.setNickname(userResponse.user.nickname);
                    userInfo.setAvatar(userResponse.user.avatar);
                    userInfo.setDescription(userResponse.user.description);
                    userInfo.setStatus(userResponse.user.status);
                    userInfo.setGithubUsername(userResponse.user.githubUsername);
                    userInfo.setIsCurrentLoginUser(userResponse.user.isCurrentLoginUser);
                    userInfoRepository.save(userInfo);
                    
                    log.info("成功更新用户信息: {}", userId);
                }
            }
        } catch (IOException e) {
            log.error("更新用户信息时发生错误", e);
        }
    }
    
    public boolean existsUserInfo(Long userId) {
        return userInfoRepository.existsById(userId);
    }
    
    public boolean existsCommunityUser(Long userId) {
        return communityUserRepository.existsById(userId);
    }
    
    public UserInfo getUserInfo(Long userId) {
        return userInfoRepository.findById(userId).orElse(null);
    }
    
    public CommunityUser getCommunityUser(Long userId) {
        return communityUserRepository.findById(userId).orElse(null);
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