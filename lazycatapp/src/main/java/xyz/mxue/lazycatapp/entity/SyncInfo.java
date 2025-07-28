package xyz.mxue.lazycatapp.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_sync_info")
public class SyncInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sync_type", unique = true, nullable = false)
    private String syncType;  // APP, CATEGORY, GITHUB, SCORE, COMMENT

    /**
     * 同步最近时间
     */
    @Column(name = "last_sync_time")
    private LocalDateTime lastSyncTime;


    @Column(name = "initial_sync_completed")
    private boolean initialSyncCompleted;

    @Column(name = "sync_status")
    private Integer syncStatus;

    @Column(name = "sync_strategy")
    private String syncStrategy;  // FULL, INCREMENTAL

    /**
     * 全量同步时间间隔 （毫秒）
     */
    @Column(name = "full_sync_interval")
    private Long fullSyncInterval;

    /**
     * 增量同步时间间隔 （毫秒）
     */
    @Column(name = "incremental_sync_interval")
    private Long incrementalSyncInterval;

    @Column(name = "last_error")
    private String lastError;

    /**
     * 全量同步下一次时间
     */
    @Column(name = "full_next_sync_time")
    private LocalDateTime fullNextSyncTime;

    /**
     * 增量同步下一次时间
     */
    @Column(name = "incremental_next_sync_time")
    private LocalDateTime incrementalNextSyncTime;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "total_count")
    private Long totalCount;

    @Column(name = "description")
    private String description;
} 