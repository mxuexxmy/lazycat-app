package xyz.mxue.lazycatapp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "user_infos")
public class UserInfo {
    @Id
    private Long id;
    private String username;
    private Boolean isUsernameSet;
    private String nickname;
    private String avatar;
    private String description;
    private Integer status;
    private String githubUsername;
    private Boolean isCurrentLoginUser;
} 