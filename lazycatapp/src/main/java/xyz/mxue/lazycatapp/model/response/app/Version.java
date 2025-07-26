package xyz.mxue.lazycatapp.model.response.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 应用版本信息
 */
@Data
public class Version {

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
     * 版本名称
     */
    private String name;

    /**
     * 包名
     */
    @JsonProperty("package")
    private String packageName;
    /**
     * 包hash
     */
    @JsonProperty("pkg_hash")
    private String pkgHash;

    /**
     * 包路径
     */
    @JsonProperty("pkg_path")
    private String pkgPath;

    /**
     * 图标路径
     */
    @JsonProperty("icon_path")
    private String iconPath;

    /**
     * 不支持的平台
     */
    @JsonProperty("unsupported_platforms")
    private List<String> unsupportedPlatforms;

    /**
     * 最小系统版本
     */
    @JsonProperty("min_os_version")
    private String minOsVersion;

    @JsonProperty("changelog_list")
    private List<String> changelogList;

    /**
     * 更新日志语言
     */
    @JsonProperty("changelog_language")
    private String changelogLanguage;
}