package xyz.mxue.lazycatapp.model.vo;

import lombok.Data;

@Data
public class SyncDataSourceVO {

    /**
     * 数据名称
     */
    private String dataName;

    /**
     * 数据条数
     */
    private Long dataCount;

    /**
     * 数据目标条数
     */
    private Long targetCount;

}
