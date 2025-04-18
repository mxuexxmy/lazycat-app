package xyz.mxue.lazycatapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.mxue.lazycatapp.entity.CommunityUser;
import xyz.mxue.lazycatapp.entity.UserInfo;
import xyz.mxue.lazycatapp.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取用户社区信息
     * @param userId 用户ID
     * @return 用户社区信息
     */
    @GetMapping("/{userId}/community")
    public ResponseEntity<CommunityUser> getCommunityUserInfo(@PathVariable Long userId) {
        userService.updateUserInfo(userId);
        return ResponseEntity.ok(userService.getCommunityUser(userId));
    }

    /**
     * 获取用户个人信息
     * @param userId 用户ID
     * @return 用户个人信息
     */
    @GetMapping("/{userId}/info")
    public ResponseEntity<UserInfo> getUserInfo(@PathVariable Long userId) {
        userService.updateUserInfo(userId);
        return ResponseEntity.ok(userService.getUserInfo(userId));
    }

    /**
     * 获取完整的用户信息（包含社区信息和个人信息）
     * @param userId 用户ID
     * @return 完整的用户信息
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        userService.updateUserInfo(userId);
        UserInfo userInfo = userService.getUserInfo(userId);
        CommunityUser communityUser = userService.getCommunityUser(userId);
        
        UserResponse response = new UserResponse();
        response.setUserInfo(userInfo);
        response.setCommunityUser(communityUser);
        
        return ResponseEntity.ok(response);
    }

    @lombok.Data
    private static class UserResponse {
        private UserInfo userInfo;
        private CommunityUser communityUser;
    }
} 