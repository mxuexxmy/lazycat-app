package xyz.mxue.lazycatapp.model.vo;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class SyncInfoVO {

    private Long id;

    private String syncType;

    private String syncTypeName;

    private LocalDateTime lastSyncTime;

    private boolean initialSyncCompleted;

    private String initialSyncCompletedName;

    private Integer syncStatus;

    private String syncStatusName;


    private String syncStrategy;  // FULL, INCREMENTAL

    private Long syncInterval;  // 同步间隔（毫秒）

    private String lastError;

    private LocalDateTime nextSyncTime;

    private Integer retryCount;

    private boolean enabled;

    private Long totalCount;

}
