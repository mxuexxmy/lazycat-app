package xyz.mxue.lazycatapp.model.response.tutorial;

import jakarta.persistence.Id;
import lombok.Data;


@Data
public class TutorialInfo {

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

//    /**
//     * 应用ID集合
//     */
//    private String products;

//    /**
//     * 图片集合
//     */
//    private String images;

//    /**
//     * 视频集合
//     */
//    private String videos;

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
    private String createdAt;

    /**
     * 更新时间
     */
    private String updatedAt;

    /**
     * 内容
     */
    private String content;

}
