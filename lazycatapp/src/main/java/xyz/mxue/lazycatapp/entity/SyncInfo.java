package xyz.mxue.lazycatapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sync_info")
public class SyncInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sync_type", unique = true, nullable = false)
    private String syncType;  // APP, CATEGORY, GITHUB, SCORE, COMMENT

    @Column(name = "last_sync_time")
    private LocalDateTime lastSyncTime;

    @Column(name = "initial_sync_completed")
    private boolean initialSyncCompleted;

    @Column(name = "sync_strategy")
    private String syncStrategy;  // FULL, INCREMENTAL

    @Column(name = "sync_interval")
    private Long syncInterval;  // 同步间隔（毫秒）

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "next_sync_time")
    private LocalDateTime nextSyncTime;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "enabled")
    private boolean enabled;

    @PrePersist
    protected void onCreate() {
        if (lastSyncTime == null) {
            lastSyncTime = LocalDateTime.now();
        }
        if (retryCount == null) {
            retryCount = 0;
        }
        enabled = true;
    }
} 