package xyz.mxue.lazycatapp.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "t_user")
public class User {

    /**
     * 用户ID
     */
    @Id
    private Long id;

    /**
     * 开发者ID
     */
    private Long developerId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 用户名
     */
    private String username;

    /**
     * 描述
     */
    private String description;

    /**
     * 头像
     */
    private String avatar;

    private String githubUsername;

    /**
     * 连续提交天数
     */
    private Long continuousSubmissionDayCount;

    /**
     * 是否官方
     */
    private Boolean isOfficial;

    /**
     * 应用总数
     */
    private Long appTotalCount;
} 