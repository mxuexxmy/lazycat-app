package xyz.mxue.lazycatapp.entity;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "user_infos")
public class UserInfo {
    @Id
    private Long id;

    private String username;

    @Column(name = "is_username_set")
    private Boolean isUsernameSet;

    private String nickname;
    private String avatar;
    private String description;
    private Integer status;

    @Column(name = "github_username")
    private String githubUsername;

    @Column(name = "is_current_login_user")
    private Boolean isCurrentLoginUser;

    @Column(name = "created_at")
    private String createdAt;

    @Column(name = "updated_at")
    private String updatedAt;
} 