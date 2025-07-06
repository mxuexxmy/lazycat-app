package xyz.mxue.lazycatapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import xyz.mxue.lazycatapp.converter.StringListConverter;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Data
@Entity
@Table(name = "apps")
public class App {
    @Id
    private String pkgId;

    @Column(name = "package_name", nullable = false)
    private String packageName;

    private String name;
    private String pkgPath;
    private String pkgHash;

    @Column(length = 4000)
    private String description;
    private String brief;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> category;

    private String iconPath;
    private String keywords;
    @Column(nullable = false)
    private String version;
    private String creator;
    private Long creatorId;
    private String author;
    private Integer updateId;
    private Double price;
    private String updateContent;

    @Column(name = "update_date")
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

    @ManyToMany
    @JoinTable(
            name = "app_category",
            joinColumns = @JoinColumn(name = "app_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

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

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }
} 