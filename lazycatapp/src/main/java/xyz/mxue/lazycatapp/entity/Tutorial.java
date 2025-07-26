package xyz.mxue.lazycatapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 攻略图文视频教程
 */
@Data
@Entity
@Table(name = "t_tutorial")
public class Tutorial {

    /**
     * ID
     */
    @Id
    private Long id;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 标题
     */
    private String title;

    /**
     * 类型
     */
    private String type;

    /**
     * 封面
     */
    private String cover;

    /**
     * 应用ID集合
     */
    private String products;

    /**
     * 图片集合
     */
    private String images;

    /**
     * 视频集合
     */
    private String videos;

    /**
     * 阅读次数
     */
    private Long views;

    /**
     * 点赞次数
     */
    private Long thumbCount;

    /**
     * 评论次数
     */
    private Long commentCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 内容
     */
    @Column(columnDefinition = "TEXT")
    private String content;

}
