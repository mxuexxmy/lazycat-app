package xyz.mxue.lazycatapp.model.response.app;

import lombok.Data;

/**
 * 统计信息
 */
@Data
public class Count {

    /**
     * 下载次数
     */
    private int downloads;
    /**
     * 点赞次数
     */
    private int likes;
    /**
     * 评论次数
     */
    private int comments;
}
