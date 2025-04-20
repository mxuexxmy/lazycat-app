package xyz.mxue.lazycatapp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.mxue.lazycatapp.entity.GitHubInfo;
import xyz.mxue.lazycatapp.service.GitHubInfoService;
import xyz.mxue.lazycatapp.entity.App;
import xyz.mxue.lazycatapp.repository.AppRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Objects;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GitHubInfoController {

    private final GitHubInfoService githubInfoService;
    private final AppRepository appRepository;

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

    /**
     * 获取用户的 GitHub 成就信息
     * @param userId 用户ID
     * @return GitHub 成就信息
     */
    @GetMapping("/achievements/{userId}")
    public ResponseEntity<?> getGitHubAchievements(@PathVariable Long userId) {
        try {
            GitHubInfo info = githubInfoService.getGitHubInfo(userId);
            if (info == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(Map.of(
                "totalPRs", info.getTotalPRs(),
                "totalCommits", info.getTotalCommits(),
                "totalIssues", info.getTotalIssues(),
                "contributedTo", info.getContributedTo(),
                "rankLevel", info.getRankLevel(),
                "rankScore", info.getRankScore()
            ));
        } catch (Exception e) {
            log.error("获取用户 {} 的 GitHub 成就信息失败: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取 GitHub 成就信息失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取所有用户的 GitHub 成就信息
     * @return 所有用户的 GitHub 成就信息列表
     */
    @GetMapping("/achievements")
    public ResponseEntity<?> getAllGitHubAchievements() {
        try {
            List<App> apps = appRepository.findAll();
            Set<Long> userIds = apps.stream()
                    .map(App::getCreatorId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Map<Long, GitHubInfo> infos = githubInfoService.getGitHubInfos(new ArrayList<>(userIds));
            
            List<Map<String, Object>> achievements = infos.entrySet().stream()
                    .map(entry -> {
                        GitHubInfo info = entry.getValue();
                        Map<String, Object> achievement = new HashMap<>();
                        
                        // 用户信息
                        Map<String, Object> user = new HashMap<>();
                        user.put("id", entry.getKey());
                        user.put("username", info.getUsername());
                        user.put("nickname", info.getNickname());
                        user.put("avatar", info.getAvatar());
                        user.put("description", info.getDescription());
                        user.put("githubUsername", info.getGithubUsername());
                        achievement.put("user", user);
                        
                        // 贡献统计
                        Map<String, Object> contributions = new HashMap<>();
                        contributions.put("totalCommits", info.getTotalCommits());
                        contributions.put("totalPRs", info.getTotalPRs());
                        contributions.put("totalIssues", info.getTotalIssues());
                        contributions.put("contributedTo", info.getContributedTo());
                        achievement.put("contributions", contributions);
                        
                        // 排名信息
                        Map<String, Object> rank = new HashMap<>();
                        rank.put("level", info.getRankLevel());
                        rank.put("score", info.getRankScore());
                        achievement.put("rank", rank);
                        
                        // 编程语言
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            Map<String, Object> languages = mapper.readValue(info.getTopLangs(), Map.class);
                            achievement.put("languages", languages);
                        } catch (Exception e) {
                            log.warn("解析编程语言信息失败: {}", e.getMessage());
                            achievement.put("languages", new HashMap<>());
                        }
                        
                        return achievement;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(achievements);
        } catch (Exception e) {
            log.error("获取所有用户的 GitHub 成就信息失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取 GitHub 成就信息失败: " + e.getMessage()
            ));
        }
    }
} 