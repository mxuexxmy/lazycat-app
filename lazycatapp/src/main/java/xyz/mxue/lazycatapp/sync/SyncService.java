package xyz.mxue.lazycatapp.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.entity.SyncInfo;
import xyz.mxue.lazycatapp.enums.SyncStatusEnum;
import xyz.mxue.lazycatapp.enums.SyncStrategyEnum;
import xyz.mxue.lazycatapp.enums.SyncTypeEnum;
import xyz.mxue.lazycatapp.repository.SyncInfoRepository;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {
    private final SyncInfoRepository syncInfoRepository;

    public SyncInfo getSyncInfo(SyncTypeEnum syncType) {
        return syncInfoRepository.findBySyncType(syncType.getCode()).orElseGet(() -> createDefaultSyncInfo(syncType));
    }

    private SyncInfo createDefaultSyncInfo(SyncTypeEnum syncType) {
        SyncInfo syncInfo = new SyncInfo();
        syncInfo.setSyncType(syncType.getCode());
        syncInfo.setInitialSyncCompleted(false);
        syncInfo.setSyncStrategy(SyncStrategyEnum.FULL.getCode());
        syncInfo.setSyncStatus(SyncStatusEnum.START.getCode());

        // 设置默认同步间隔
        switch (syncType) {
            case APP:
                syncInfo.setDescription(syncType.getDesc());
                syncInfo.setFullSyncInterval(43200000L); // 12小时
                syncInfo.setIncrementalSyncInterval(3600000L); // 30 分钟
            case CATEGORY:
                syncInfo.setDescription(syncType.getDesc());
                syncInfo.setFullSyncInterval(43200000L); // 12小时
                syncInfo.setIncrementalSyncInterval(14400000L); // 2 小时
                break;
            case USER:
                syncInfo.setDescription(syncType.getDesc());
                syncInfo.setFullSyncInterval(43200000L); // 12小时
                syncInfo.setIncrementalSyncInterval(3600000L); // 30 分钟
                break;
            default:
                syncInfo.setDescription(syncType.getDesc());
                syncInfo.setFullSyncInterval(86400000L); // 24小时
                syncInfo.setIncrementalSyncInterval(28800000L); // 4 小时
                break;
        }
        // 转换为纳秒
        syncInfo.setFullNextSyncTime(LocalDateTime.now().plusNanos(syncInfo.getFullSyncInterval() * 1000000));
        // 增量下一次同步时间
        syncInfo.setIncrementalNextSyncTime(LocalDateTime.now().plusNanos(syncInfo.getIncrementalSyncInterval() * 1000000));
        syncInfo.setEnabled(true);
        syncInfo.setRetryCount(0);
        return syncInfoRepository.save(syncInfo);
    }

    public void updateSyncInfo(SyncTypeEnum syncType, SyncStrategyEnum syncStrategyEnum, boolean success, String error) {
        SyncInfo syncInfo = getSyncInfo(syncType);

        if (success) {
            syncInfo.setRetryCount(0);
            syncInfo.setLastError(null);
            syncInfo.setSyncStatus(SyncStatusEnum.COMPLETE.getCode());

            // 如果是全量同步且成功，则标记为已完成初始同步
            if (syncStrategyEnum.getCode().equals(SyncStrategyEnum.FULL.getCode())) {
                syncInfo.setInitialSyncCompleted(true);
                syncInfo.setIncrementalNextSyncTime(LocalDateTime.now().plusNanos(syncInfo.getIncrementalSyncInterval() * 1000000));
                // 全量同步成功后，切换为增量同步
                syncInfo.setSyncStrategy(SyncStrategyEnum.INCREMENTAL.getCode());
            }
            // 如果是增量同步且成功，则更新下次同步时间
            else if (syncStrategyEnum.getCode().equals(SyncStrategyEnum.INCREMENTAL.getCode())) {
                // 如果是增量同步，更新下次同步时间
                syncInfo.setIncrementalNextSyncTime(LocalDateTime.now().plusNanos(syncInfo.getIncrementalSyncInterval() * 1000000));
                // 如果增量下一次同步时间大于全量下一次同步时间，则切换为全量同步
                if (syncInfo.getIncrementalNextSyncTime().isAfter(syncInfo.getFullNextSyncTime())) {
                    syncInfo.setSyncStrategy(SyncStrategyEnum.FULL.getCode());
                } else {
                    syncInfo.setSyncStrategy(SyncStrategyEnum.INCREMENTAL.getCode());
                }

            }

        } else {
            syncInfo.setLastError(error);
            syncInfo.setRetryCount(syncInfo.getRetryCount() + 1);
            syncInfo.setSyncStatus(SyncStatusEnum.FAILED.getCode());

            // 如果重试次数过多，可以选择禁用同步或增加同步间隔
            if (syncInfo.getRetryCount() >= 3) {
                if (SyncStrategyEnum.FULL.getCode().equals(syncInfo.getSyncStrategy())) {
                    syncInfo.setFullSyncInterval(syncInfo.getFullSyncInterval() * 2);
                    syncInfo.setSyncStrategy(SyncStrategyEnum.FULL.getCode());
                } else {
                    syncInfo.setIncrementalSyncInterval(syncInfo.getIncrementalSyncInterval() * 2);
                    syncInfo.setSyncStrategy(SyncStrategyEnum.INCREMENTAL.getCode());
                }
                log.warn("同步 {} 失败次数过多，增加同步间隔至 {} 毫秒", syncType, syncInfo.getFullSyncInterval());
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
    public boolean isSync(SyncTypeEnum syncType, SyncStrategyEnum syncStrategyEnum, boolean forceSync) {
        if (forceSync) {
            return true;
        }
        SyncInfo syncInfo = getSyncInfo(syncType);

        if (!syncInfo.isEnabled()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        // 如果最新同步时间是null，则进行同步
        if (syncInfo.getLastSyncTime() == null) {
            return true;
        }

        // 如果状态是没有完成的，则不同步
        if (!Objects.equals(syncInfo.getSyncStatus(), SyncStatusEnum.COMPLETE.getCode())) {
            return false;
        }

        if (syncInfo.getSyncStrategy().equals(SyncStrategyEnum.INCREMENTAL.getCode())) {
            // 增量同步
            return now.isAfter(syncInfo.getIncrementalNextSyncTime());
        } else {
            // 全量同步
            return now.isAfter(syncInfo.getFullNextSyncTime());
        }
    }

    public void enableSync(SyncTypeEnum syncType, boolean enabled) {
        SyncInfo syncInfo = getSyncInfo(syncType);
        syncInfo.setEnabled(enabled);
        syncInfoRepository.save(syncInfo);
    }

    public void updateSyncStatus(SyncTypeEnum syncType, SyncStatusEnum syncStatusEnum) {
        SyncInfo syncInfo = getSyncInfo(syncType);
        syncInfo.setSyncStatus(syncStatusEnum.getCode());
        // 如果状态是同步中，更改最近的同步时间
        if (syncStatusEnum == SyncStatusEnum.SYNCING) {
            syncInfo.setLastSyncTime(LocalDateTime.now());
        }
        syncInfoRepository.save(syncInfo);
    }

    public void setSyncInterval(SyncTypeEnum syncType, long interval) {
        SyncInfo syncInfo = getSyncInfo(syncType);
        syncInfo.setFullSyncInterval(interval);
        syncInfoRepository.save(syncInfo);
    }

    public void saveSyncInfo(SyncInfo syncInfo) {
        syncInfoRepository.save(syncInfo);
    }
} 