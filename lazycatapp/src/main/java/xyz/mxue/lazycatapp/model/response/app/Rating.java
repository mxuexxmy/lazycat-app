package xyz.mxue.lazycatapp.model.response.app;

import lombok.Data;

/**
 * 评分
 */
@Data
public class Rating {

    /**
     * 分数
     */
    private int score;

    /**
     * 评分统计
     */
    private RatingStatistics statistics;
}