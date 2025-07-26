package xyz.mxue.lazycatapp.model.response.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 应用信息
 */
@Data
public class AppItemInfo {

    /**
     * ID
     */
    private Long id;

    /**
     * 包名
     */
    @JsonProperty("package")
    private String packageName;

    /**
     * 分类
     */
    @JsonProperty("kind_ids")
    private String kindIds;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonProperty("created_at")
    private String createdAt;

    /**
     * 更新时间
     */
    @JsonProperty("updated_at")
    private String updatedAt;

    /**
     * 版本更新时间
     */
    @JsonProperty("version_updated_at")
    private String versionUpdatedAt;
    /**
     * 创建用户
     */
    @JsonProperty("create_user")
    private CreateUser createUser;
    /**
     * 应用信息
     */
    private Information information;
    /**
     * 应用版本信息
     */
    private Version version;
    /**
     * 评分信息
     */
    private Rating rating;
    /**
     * 统计信息
     */
    private Count count;

}
