package xyz.mxue.lazycatapp.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "github_info")
public class GitHubInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true)
    private Long userId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "uid")
    private Long uid;

    @Column(name = "username")
    private String username;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "github_username")
    private String githubUsername;

    @Column(name = "total_prs")
    private Integer totalPRs;

    @Column(name = "total_commits")
    private Integer totalCommits;

    @Column(name = "total_issues")
    private Integer totalIssues;

    @Column(name = "contributed_to")
    private Integer contributedTo;

    @Column(name = "rank_level")
    private String rankLevel;

    @Column(name = "rank_score")
    private Double rankScore;

    @Column(name = "top_langs", columnDefinition = "TEXT")
    private String topLangs;

    @Column(name = "last_sync_time")
    private LocalDateTime lastSyncTime;
} 