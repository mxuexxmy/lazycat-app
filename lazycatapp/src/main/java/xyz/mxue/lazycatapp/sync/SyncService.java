package xyz.mxue.lazycatapp.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.entity.SyncInfo;
import xyz.mxue.lazycatapp.repository.SyncInfoRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {
    private final SyncInfoRepository syncInfoRepository;

    // 同步类型
    public static final String SYNC_TYPE_APP = "APP";
    public static final String SYNC_TYPE_CATEGORY = "CATEGORY";
    public static final String SYNC_TYPE_GITHUB = "GITHUB";
    public static final String SYNC_TYPE_SCORE = "SCORE";
    public static final String SYNC_TYPE_COMMENT = "COMMENT";
    public static final String SYNC_TYPE_USER = "USER";

    // 执行策略
    public static final String SYNC_STRATEGY_FULL = "FULL";
    public static final String SYNC_STRATEGY_INCREMENTAL = "INCREMENTAL";

    public SyncInfo getSyncInfo(String syncType) {
        return syncInfoRepository.findBySyncType(syncType)
                .orElseGet(() -> createDefaultSyncInfo(syncType));
    }

    private SyncInfo createDefaultSyncInfo(String syncType) {
        SyncInfo syncInfo = new SyncInfo();
        syncInfo.setSyncType(syncType);
        syncInfo.setInitialSyncCompleted(false);
        syncInfo.setSyncStrategy(SYNC_STRATEGY_FULL);

        // 设置默认同步间隔
        switch (syncType) {
            case SYNC_TYPE_APP:
                syncInfo.setSyncInterval(600000L); // 10分钟
                break;
            case SYNC_TYPE_CATEGORY:
                syncInfo.setSyncInterval(3600000L); // 1小时
                break;
            case SYNC_TYPE_GITHUB:
                syncInfo.setSyncInterval(21600000L); // 6小时
                break;
            case SYNC_TYPE_SCORE:
            case SYNC_TYPE_COMMENT:
                syncInfo.setSyncInterval(7200000L); // 2小时
                break;
            case SYNC_TYPE_USER:
                syncInfo.setSyncInterval(21600000L); // 默认 30 分钟
                break;
            default:
                syncInfo.setSyncInterval(3600000L); // 默认1小时
                break;
        }

        syncInfo.setEnabled(true);
        syncInfo.setRetryCount(0);
        return syncInfoRepository.save(syncInfo);
    }

    public void updateTotalCount(String syncType, long totalCount) {
        SyncInfo syncInfo = getSyncInfo(syncType);
        syncInfo.setTotalCount(totalCount);
        syncInfoRepository.save(syncInfo);
    }

    public void updateSyncInfo(String syncType, boolean success, String error) {
        SyncInfo syncInfo = getSyncInfo(syncType);

        if (success) {
            syncInfo.setLastSyncTime(LocalDateTime.now());
            syncInfo.setNextSyncTime(LocalDateTime.now().plusNanos(syncInfo.getSyncInterval() * 1000000)); // 转换为纳秒
            syncInfo.setRetryCount(0);
            syncInfo.setLastError(null);

            // 如果是全量同步且成功，则标记为已完成初始同步
            if (SYNC_STRATEGY_FULL.equals(syncInfo.getSyncStrategy())) {
                syncInfo.setInitialSyncCompleted(true);
                syncInfo.setSyncStrategy(SYNC_STRATEGY_INCREMENTAL);
            }
        } else {
            syncInfo.setLastError(error);
            syncInfo.setRetryCount(syncInfo.getRetryCount() + 1);

            // 如果重试次数过多，可以选择禁用同步或增加同步间隔
            if (syncInfo.getRetryCount() >= 3) {
                syncInfo.setSyncInterval(syncInfo.getSyncInterval() * 2); // 加倍同步间隔
                log.warn("同步 {} 失败次数过多，增加同步间隔至 {} 毫秒", syncType, syncInfo.getSyncInterval());
            }
        }

        syncInfoRepository.save(syncInfo);
    }

    /**
     * 检查是否需要同步
     *
     * @param syncType 同步类型
     * @return 是否需要同步
     */
    public boolean shouldSync(String syncType) {
        SyncInfo syncInfo = getSyncInfo(syncType);

        if (!syncInfo.isEnabled()) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();

        // 如果从未同步过，或者未完成初始同步，应该进行同步
        if (syncInfo.getLastSyncTime() == null || !syncInfo.isInitialSyncCompleted()) {
            return false;
        }

        // 如果设置了下次同步时间，检查是否到达
        if (syncInfo.getNextSyncTime() != null) {
            return !now.isAfter(syncInfo.getNextSyncTime());
        }

        // 根据同步间隔检查是否应该同步
        return !now.isAfter(syncInfo.getLastSyncTime().plusNanos(syncInfo.getSyncInterval() * 1000000));
    }

    public void enableSync(String syncType, boolean enabled) {
        SyncInfo syncInfo = getSyncInfo(syncType);
        syncInfo.setEnabled(enabled);
        syncInfoRepository.save(syncInfo);
    }

    public void setSyncInterval(String syncType, long interval) {
        SyncInfo syncInfo = getSyncInfo(syncType);
        syncInfo.setSyncInterval(interval);
        syncInfoRepository.save(syncInfo);
    }

    public void saveSyncInfo(SyncInfo syncInfo) {
        syncInfoRepository.save(syncInfo);
    }
} 