package xyz.mxue.lazycatapp.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class SyncInfoVO {

    private Long id;

    private String syncType;

    private String syncTypeName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastSyncTime;

    private boolean initialSyncCompleted;

    private String initialSyncCompletedName;

    private Integer syncStatus;

    private String syncStatusName;


    private String syncStrategy;  // FULL, INCREMENTAL

    private String syncStrategyName;

    private Long fullSyncInterval;  // 同步间隔（毫秒）

    private Long incrementalSyncInterval;

    private String lastError;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fullNextSyncTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime incrementalNextSyncTime;

    private Integer retryCount;

    private boolean enabled;

    private Long totalCount;

    private String description;

}
