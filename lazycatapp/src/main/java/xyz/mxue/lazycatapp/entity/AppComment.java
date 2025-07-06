package xyz.mxue.lazycatapp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "app_comments")
public class AppComment {
    @Id
    private Long commentId;
    private String pkgId;
    private Long userId;
    private String nickname;
    private String avatar;
    private Integer score;
    private String content;
    private Boolean liked;
    private Integer likeCounts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}