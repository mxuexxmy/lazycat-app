package xyz.mxue.lazycatapp.model.response.tutorial;

import lombok.Data;

import java.util.List;

@Data
public class TutorialResponse {

    /**
     * 攻略集合
     */

    private List<TutorialInfo> items;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页数量
     */
    private Integer size;

    /**
     * 总数
     */
    private Integer total;

}
