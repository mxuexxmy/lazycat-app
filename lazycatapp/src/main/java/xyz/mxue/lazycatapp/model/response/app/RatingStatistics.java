package xyz.mxue.lazycatapp.model.response.app;

import lombok.Data;

/**
 * 评分
 */
@Data
public class RatingStatistics {
    /**
     * 总数
     */
    private Integer total;

    /**
     * 一星评分
     */
    private Integer one;

    /**
     * 二星评分
     */
    private Integer two;

    /**
     * 三星评分
     */
    private Integer three;

    /**
     * 四星评分
     */
    private Integer four;

    /**
     * 五星评分
     */
    private Integer five;
}