package xyz.mxue.lazycatapp.entity;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "t_community_user")
public class CommunityUser {

    @Id
    @Column(name = "uid")
    private Long uid;

    @Column(name = "receive_thumbs")
    private Integer receiveThumbs;

    private Integer follows;
    private Integer fans;
    private Boolean followed;

    @Column(name = "guideline_counts")
    private Integer guidelineCounts;

    @Column(name = "hide_follows")
    private Boolean hideFollows;

    @Column(name = "hide_fans")
    private Boolean hideFans;

    @Column(name = "hide_thumbs")
    private Boolean hideThumbs;

    @Column(name = "only_followed_at_me")
    private Boolean onlyFollowedAtMe;

    @Column(name = "only_followed_comment_me")
    private Boolean onlyFollowedCommentMe;

    @Column(name = "created_at")
    private String createdAt;

    @Column(name = "updated_at")
    private String updatedAt;
} 