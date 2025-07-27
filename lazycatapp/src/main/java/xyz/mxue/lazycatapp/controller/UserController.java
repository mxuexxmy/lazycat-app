package xyz.mxue.lazycatapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.mxue.lazycatapp.entity.CommunityUser;
import xyz.mxue.lazycatapp.entity.User;
import xyz.mxue.lazycatapp.model.vo.UserVO;
import xyz.mxue.lazycatapp.service.UserService;
import xyz.mxue.lazycatapp.service.AppService;
import xyz.mxue.lazycatapp.model.R;
import xyz.mxue.lazycatapp.sync.CommunitySyncUserService;
import xyz.mxue.lazycatapp.sync.UserSyncService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

@Tag(name = "用户管理", description = "用户管理")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CommunitySyncUserService communitySyncUserService;
    private final AppService appService;

    /**
     * 获取用户社区信息
     *
     * @param userId 用户ID
     * @return 用户社区信息
     */
    @Operation(summary = "获取用户社区信息", description = "获取用户社区信息")
    @GetMapping("/{userId}/community")
    public ResponseEntity<CommunityUser> getCommunityUserInfo(@PathVariable Long userId) {
        communitySyncUserService.syncAllCommunityUsers(userId, true);
        return ResponseEntity.ok(userService.getCommunityUser(userId));
    }

    /**
     * 获取用户个人信息
     *
     * @param userId 用户ID
     * @return 用户个人信息
     */
    @Operation(summary = "获取用户个人信息", description = "获取用户个人信息")
    @GetMapping("/{userId}/info")
    public ResponseEntity<User> getUserInfo(@PathVariable Long userId) {
        communitySyncUserService.syncAllCommunityUsers(userId, true);
        return ResponseEntity.ok(userService.getUserInfo(userId));
    }

    /**
     * 获取完整的用户信息（包含社区信息和个人信息）
     *
     * @param userId 用户ID
     * @return 完整的用户信息
     */
    @Operation(summary = "获取完整的用户信息", description = "获取完整的用户信息")
    @GetMapping("/{userId}")
    public ResponseEntity<UserVO> getUser(@PathVariable Long userId) {
        communitySyncUserService.syncAllCommunityUsers(userId, true);
        User userInfo = userService.getUserInfo(userId);
        CommunityUser communityUser = userService.getCommunityUser(userId);

        UserVO response = new UserVO();
        response.setUserInfo(userInfo);
        response.setCommunityUser(communityUser);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有用户信息
     *
     * @return 所有用户信息列表
     */
    @Operation(summary = "获取所有用户信息", description = "获取所有用户信息")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        List<User> userInfos = userService.getAllUserInfos();
        List<CommunityUser> communityUsers = userService.getAllCommunityUsers();

        // 将用户信息和社区信息合并
        List<Map<String, Object>> users = userInfos.stream()
                .map(userInfo -> {
                    CommunityUser communityUser = communityUsers.stream()
                            .filter(cu -> cu.getUid().equals(userInfo.getId()))
                            .findFirst()
                            .orElse(new CommunityUser());

                    return Map.of(
                            "userInfo", userInfo,
                            "communityUser", communityUser
                    );
                })
                .toList();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", users
        ));
    }

    /**
     * 根据应用更新用户信息
     *
     * @return 更新结果
     */
    @Operation(summary = "根据应用更新用户信息", description = "根据应用更新用户信息")
    @PostMapping("/update-from-apps")
    public ResponseEntity<Map<String, Object>> updateUsersFromApps() {
        // 获取所有不重复的开发者ID
        List<Long> creatorIds = appService.getDistinctCreatorIds();

        // 更新每个开发者的信息
        creatorIds.forEach(creatorId -> {
            communitySyncUserService.syncAllCommunityUsers(creatorId, true);
        });

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "已更新 " + creatorIds.size() + " 个用户的信息"
        ));

    }

    @Operation(summary = "获取开发者社区信息", description = "获取开发者社区信息")
    @GetMapping("/developers")
    public R<List<Map<String, Object>>> getDeveloperCommunityInfo() {
        try {
            List<User> userInfos = userService.getAllUserInfos();
            List<CommunityUser> communityUsers = userService.getAllCommunityUsers();

            // 将用户信息和社区信息合并成开发者列表
            List<Map<String, Object>> developers = userInfos.stream()
                    .map(userInfo -> {
                        CommunityUser communityUser = communityUsers.stream()
                                .filter(cu -> cu.getUid().equals(userInfo.getId()))
                                .findFirst()
                                .orElse(new CommunityUser());

                        Map<String, Object> developer = new HashMap<>();

                        // 添加 user_infos 表的所有字段
                        developer.put("id", userInfo.getId());
                        developer.put("username", userInfo.getUsername());
                        //developer.put("isUsernameSet", userInfo.getIsUsernameSet());
                        developer.put("nickname", userInfo.getNickname());
                        developer.put("avatar", userInfo.getAvatar());
                        developer.put("description", userInfo.getDescription());
                        //developer.put("status", userInfo.getStatus());
                        developer.put("githubUsername", userInfo.getGithubUsername());
                        // developer.put("isCurrentLoginUser", userInfo.getIsCurrentLoginUser());
                        // developer.put("createdAt", userInfo.getCreatedAt());
                        // developer.put("updatedAt", userInfo.getUpdatedAt());

                        // 添加 community_users 表的所有字段
                        developer.put("uid", communityUser.getUid());
                        developer.put("receiveThumbs", communityUser.getReceiveThumbs());
                        developer.put("follows", communityUser.getFollows());
                        developer.put("fans", communityUser.getFans());
                        developer.put("followed", communityUser.getFollowed());
                        developer.put("guidelineCounts", communityUser.getGuidelineCounts());
                        developer.put("hideFollows", communityUser.getHideFollows());
                        developer.put("hideFans", communityUser.getHideFans());
                        developer.put("hideThumbs", communityUser.getHideThumbs());
                        developer.put("onlyFollowedAtMe", communityUser.getOnlyFollowedAtMe());
                        developer.put("onlyFollowedCommentMe", communityUser.getOnlyFollowedCommentMe());
                        developer.put("communityCreatedAt", communityUser.getCreatedAt());
                        developer.put("communityUpdatedAt", communityUser.getUpdatedAt());

                        return developer;
                    })
                    .collect(Collectors.toList());

            return R.success(developers);
        } catch (Exception e) {
            return R.error("获取开发者社区信息失败: " + e.getMessage());
        }
    }

} 