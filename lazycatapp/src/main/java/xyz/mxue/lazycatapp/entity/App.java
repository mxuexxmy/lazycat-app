package xyz.mxue.lazycatapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import xyz.mxue.lazycatapp.converter.StringListConverter;

import java.util.List;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_app")
public class App {

    /**
     * 应用ID
     */
    @Id
    private Long id;

    /**
     * 应用包名
     */
    private String packageName;

    /**
     * 应用名称
     */
    private String name;

    /**
     * 应用路径
     */
    private String pkgPath;

    /**
     * 应用 hash
     */
    private String pkgHash;

    /**
     * 应用描述
     */
    @Column(length = 4000)
    private String description;

    /**
     * 应用简称
     */
    private String brief;

    /**
     * 应用分类，空的指的是移植应用，1指的是原创应用，2指的是官方应用
     */
    private String kindIds;

    /**
     * 图标路径
     */
    private String iconPath;

    /**
     * 关键字
     */
    private String keywords;

    @Column(nullable = false)
    private String version;

    private Long creator;

    private Long creatorId;

    private String author;

    private Long updateId;

    private String price;

    private String updateContent;

    private String updateDate;

    private Boolean supportPC;
    private Boolean supportMobile;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> pcScreenshotPaths;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> mobileScreenshotPaths;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> unsupportedPlatforms;

    private String changelog;
    private String source;
    private String osDependence;

    /**
     * 应用创建时间
     */
    private LocalDateTime appCreateTime;

    /**
     * 应用更新时间
     */
    private LocalDateTime appUpdateTime;

    @Column(name = "last_updated")
    private String lastUpdated;

    @Column(name = "download_count")
    private Integer downloadCount;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "tags")
    private String tags;


    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public long getDownloads() {
        return downloadCount != null ? downloadCount : 0;
    }

    public void setDownloads(long downloads) {
        this.downloadCount = (int) downloads;
    }

} 