package xyz.mxue.lazycatapp.model.response.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 应用信息
 */
@Data
public class Information {

    /**
     * id
     */
    private Long id;
    /**
     * 创建用户id
     */
    @JsonProperty("create_user_id")
    private Long createUserId;

    /**
     * 应用id
     */
    @JsonProperty("app_id")
    private Long appId;

    /**
     * 语言
     */
    private String language;


    /**
     * 名称
     */
    private String name;
    /**
     * 简要描述
     */

    private String brief;
    /**
     * 描述
     */
    private String description;

    /**
     * 关键词
     */
    private String keywords;

    /**
     * 来源
     */
    private String source;

    /**
     * 源作者
     */
    @JsonProperty("source_author")
    private String sourceAuthor;

    /**
     * 支持PC
     */
    @JsonProperty("support_pc")
    private Boolean supportPc;

    /**
     * 支持移动端
     */
    @JsonProperty("support_mobile")
    private Boolean supportMobile;

    /**
     * PC截图路径
     */
    @JsonProperty("pc_screenshot_paths")
    private List<String> screenshotPcPaths;

    /**
     * 移动端截图路径
     */
    @JsonProperty("mobile_screenshot_paths")
    private List<String> screenshotMobilePaths;
}