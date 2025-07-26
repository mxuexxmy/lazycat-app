package xyz.mxue.lazycatapp.model.response.app;

import lombok.Data;

import java.util.List;

@Data
public class AppInfoApiResponse {

    /**
     * 应用消息集合
     */
    private List<AppItemInfo> items;

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