package xyz.mxue.lazycatapp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.mxue.lazycatapp.entity.GitHubInfo;
import xyz.mxue.lazycatapp.service.GitHubInfoService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GitHubInfoController {

    private final GitHubInfoService githubInfoService;

    /**
     * 获取单个用户的 GitHub 信息
     * @param userId 用户ID
     * @return GitHub 信息
     */
    @GetMapping("/info/{userId}")
    public ResponseEntity<?> getGitHubInfo(@PathVariable Long userId) {
        try {
            GitHubInfo info = githubInfoService.getGitHubInfo(userId);
            if (info == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            log.error("获取用户 {} 的 GitHub 信息失败: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取 GitHub 信息失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 批量获取用户的 GitHub 信息
     * @param userIds 用户ID列表
     * @return 用户ID到GitHub信息的映射
     */
    @PostMapping("/info/batch")
    public ResponseEntity<?> getGitHubInfos(@RequestBody List<Long> userIds) {
        try {
            Map<Long, GitHubInfo> infos = githubInfoService.getGitHubInfos(userIds);
            return ResponseEntity.ok(infos);
        } catch (Exception e) {
            log.error("批量获取 GitHub 信息失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "批量获取 GitHub 信息失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 手动触发 GitHub 信息同步
     * @param userId 用户ID
     * @return 同步结果
     */
    @PostMapping("/sync/{userId}")
    public ResponseEntity<?> syncGitHubInfo(@PathVariable Long userId) {
        try {
            githubInfoService.syncGitHubInfoForUser(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "GitHub 信息同步成功"
            ));
        } catch (Exception e) {
            log.error("同步用户 {} 的 GitHub 信息失败: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "同步 GitHub 信息失败: " + e.getMessage()
            ));
        }
    }
} 