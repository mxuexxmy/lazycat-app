package xyz.mxue.lazycatapp.model.response.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserInfo {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 开发者ID
     */
    @JsonProperty("developer_id")
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

    @JsonProperty("github_username")
    private String githubUsername;

    /**
     * 连续提交天数
     */
    @JsonProperty("continuous_submission_day_count")
    private Long continuousSubmissionDayCount;

    /**
     * 是否官方
     */
    @JsonProperty("is_official")
    private Boolean isOfficial;

    /**
     * 应用总数
     */
    @JsonProperty("app_total_count")
    private Long appTotalCount;
}
