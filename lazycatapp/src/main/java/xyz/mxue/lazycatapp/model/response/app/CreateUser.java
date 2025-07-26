package xyz.mxue.lazycatapp.model.response.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 创建用户信息
 */
@Data
public class CreateUser {

    /**
     * 开发者ID
     */
    @JsonProperty("developer_id")
    private Integer developerId;

    /**
     * 用户ID
     */
    private Integer id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 描述
     */
    private String description;

    /**
     * 头像
     */
    private String avatar;

    /**
     * GitHub用户名
     */
    @JsonProperty("github_username")
    private String githubUsername;

    @JsonProperty("continuous_submission_day_count")
    private Integer continuousSubmissionDayCount;

    @JsonProperty("is_official")
    private Boolean isOfficial;

    @JsonProperty("app_total_count")
    private int appTotalCount;

}
