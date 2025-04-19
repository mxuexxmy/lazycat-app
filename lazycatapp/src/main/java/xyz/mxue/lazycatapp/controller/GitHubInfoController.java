package xyz.mxue.lazycatapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.mxue.lazycatapp.entity.GitHubInfo;
import xyz.mxue.lazycatapp.repository.GitHubInfoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GitHubInfoController {

    private final GitHubInfoRepository githubInfoRepository;
    private final ObjectMapper objectMapper;

    @GetMapping("/info/{userId}")
    public ResponseEntity<Map<String, Object>> getGitHubInfo(@PathVariable Long userId) {
        Optional<GitHubInfo> githubInfoOpt = githubInfoRepository.findByUserId(userId);
        
        if (githubInfoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        GitHubInfo githubInfo = githubInfoOpt.get();
        Map<String, Object> response = new HashMap<>();
        
        // 基本信息
        response.put("id", githubInfo.getId());
        response.put("uid", githubInfo.getUid());
        response.put("createdAt", githubInfo.getCreatedAt());
        response.put("updatedAt", githubInfo.getUpdatedAt());
        
        // 用户信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", githubInfo.getUid());
        userInfo.put("username", githubInfo.getUsername());
        userInfo.put("nickname", githubInfo.getNickname());
        userInfo.put("avatar", githubInfo.getAvatar());
        userInfo.put("description", githubInfo.getDescription());
        userInfo.put("githubUsername", githubInfo.getGithubUsername());
        response.put("user", userInfo);
        
        // 编程语言信息
        try {
            JsonNode topLangs = objectMapper.readTree(githubInfo.getTopLangs());
            response.put("topLangs", topLangs);
        } catch (Exception e) {
            response.put("topLangs", new HashMap<>());
        }
        
        // 统计信息
        Map<String, Object> summary = new HashMap<>();
        summary.put("name", githubInfo.getNickname());
        summary.put("totalPRs", githubInfo.getTotalPRs());
        summary.put("totalCommits", githubInfo.getTotalCommits());
        summary.put("totalIssues", githubInfo.getTotalIssues());
        summary.put("contributedTo", githubInfo.getContributedTo());
        
        // 排名信息
        Map<String, Object> rank = new HashMap<>();
        rank.put("level", githubInfo.getRankLevel());
        rank.put("score", githubInfo.getRankScore());
        summary.put("rank", rank);
        
        response.put("summary", summary);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/achievements/{userId}")
    public ResponseEntity<Map<String, Object>> getGitHubAchievements(@PathVariable Long userId) {
        Optional<GitHubInfo> githubInfoOpt = githubInfoRepository.findByUserId(userId);
        
        if (githubInfoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        GitHubInfo githubInfo = githubInfoOpt.get();
        Map<String, Object> achievements = new HashMap<>();
        
        // 贡献成就
        Map<String, Object> contributions = new HashMap<>();
        contributions.put("totalCommits", githubInfo.getTotalCommits());
        contributions.put("totalPRs", githubInfo.getTotalPRs());
        contributions.put("totalIssues", githubInfo.getTotalIssues());
        contributions.put("contributedTo", githubInfo.getContributedTo());
        achievements.put("contributions", contributions);
        
        // 排名成就
        Map<String, Object> rank = new HashMap<>();
        rank.put("level", githubInfo.getRankLevel());
        rank.put("score", githubInfo.getRankScore());
        achievements.put("rank", rank);
        
        // 编程语言成就
        try {
            JsonNode topLangs = objectMapper.readTree(githubInfo.getTopLangs());
            Map<String, Object> languages = new HashMap<>();
            topLangs.fields().forEachRemaining(entry -> {
                JsonNode langInfo = entry.getValue();
                Map<String, Object> langData = new HashMap<>();
                langData.put("name", langInfo.get("name").asText());
                langData.put("color", langInfo.get("color").asText());
                langData.put("size", langInfo.get("size").asLong());
                languages.put(entry.getKey(), langData);
            });
            achievements.put("languages", languages);
        } catch (Exception e) {
            achievements.put("languages", new HashMap<>());
        }
        
        return ResponseEntity.ok(achievements);
    }

    @GetMapping("/achievements")
    public ResponseEntity<List<Map<String, Object>>> getAllGitHubAchievements() {
        List<GitHubInfo> allGithubInfo = githubInfoRepository.findAll();
        
        List<Map<String, Object>> achievementsList = allGithubInfo.stream()
            .map(githubInfo -> {
                Map<String, Object> achievements = new HashMap<>();
                
                // 用户信息
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", githubInfo.getUid());
                userInfo.put("username", githubInfo.getUsername());
                userInfo.put("nickname", githubInfo.getNickname());
                userInfo.put("avatar", githubInfo.getAvatar());
                userInfo.put("description", githubInfo.getDescription());
                userInfo.put("githubUsername", githubInfo.getGithubUsername());
                achievements.put("user", userInfo);
                
                // 贡献成就
                Map<String, Object> contributions = new HashMap<>();
                contributions.put("totalCommits", githubInfo.getTotalCommits());
                contributions.put("totalPRs", githubInfo.getTotalPRs());
                contributions.put("totalIssues", githubInfo.getTotalIssues());
                contributions.put("contributedTo", githubInfo.getContributedTo());
                achievements.put("contributions", contributions);
                
                // 排名成就
                Map<String, Object> rank = new HashMap<>();
                rank.put("level", githubInfo.getRankLevel());
                rank.put("score", githubInfo.getRankScore());
                achievements.put("rank", rank);
                
                // 编程语言成就
                try {
                    JsonNode topLangs = objectMapper.readTree(githubInfo.getTopLangs());
                    Map<String, Object> languages = new HashMap<>();
                    topLangs.fields().forEachRemaining(entry -> {
                        JsonNode langInfo = entry.getValue();
                        Map<String, Object> langData = new HashMap<>();
                        langData.put("name", langInfo.get("name").asText());
                        langData.put("color", langInfo.get("color").asText());
                        langData.put("size", langInfo.get("size").asLong());
                        languages.put(entry.getKey(), langData);
                    });
                    achievements.put("languages", languages);
                } catch (Exception e) {
                    achievements.put("languages", new HashMap<>());
                }
                
                return achievements;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(achievementsList);
    }
} 