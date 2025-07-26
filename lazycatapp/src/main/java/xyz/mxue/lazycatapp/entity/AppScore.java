package xyz.mxue.lazycatapp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_app_score")
public class AppScore {
    @Id
    private String pkgId;

    private Double score;
    private Integer totalReviews;

    // 各分数段的评论数
    private Integer oneStarCount;
    private Integer twoStarCount;
    private Integer threeStarCount;
    private Integer fourStarCount;
    private Integer fiveStarCount;

    private LocalDateTime lastSyncTime;
}