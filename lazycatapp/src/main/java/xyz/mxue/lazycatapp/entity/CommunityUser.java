package xyz.mxue.lazycatapp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "community_users")
public class CommunityUser {
    @Id
    private Long uid;
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