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
                syncInfo.setSyncInterval(21600000L); // 6小时
            case CATEGORY:
                syncInfo.setSyncInterval(3600000L); // 1小时
                break;
            case USER:
                syncInfo.setSyncInterval(21600000L); // 默认 30 分钟
                break;
            default:
                syncInfo.setSyncInterval(3600000L); // 默认1小时
                break;
        }
        // 转换为纳秒
        syncInfo.setNextSyncTime(LocalDateTime.now().plusNanos(syncInfo.getSyncInterval() * 1000000));
        syncInfo.setEnabled(true);
        syncInfo.setRetryCount(0);
        return syncInfoRepository.save(syncInfo);
    }

    public void updateSyncInfo(SyncTypeEnum syncType, boolean success, String error) {
        SyncInfo syncInfo = getSyncInfo(syncType);

        if (success) {
            syncInfo.setLastSyncTime(LocalDateTime.now());
            // 转换为纳秒
            syncInfo.setNextSyncTime(LocalDateTime.now().plusNanos(syncInfo.getSyncInterval() * 1000000));
            syncInfo.setRetryCount(0);
            syncInfo.setLastError(null);
            syncInfo.setSyncStatus(SyncStatusEnum.COMPLETE.getCode());

            // 如果是全量同步且成功，则标记为已完成初始同步
            if (SyncStrategyEnum.FULL.getCode().equals(syncInfo.getSyncStrategy())) {
                syncInfo.setInitialSyncCompleted(true);
                syncInfo.setSyncStrategy(SyncStrategyEnum.INCREMENTAL.getCode());
            }
        } else {
            syncInfo.setLastError(error);
            syncInfo.setRetryCount(syncInfo.getRetryCount() + 1);
            syncInfo.setSyncStatus(SyncStatusEnum.FAILED.getCode());

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
    public boolean isSync(SyncTypeEnum syncType, boolean forceSync) {
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

        // 如果设置了下次同步时间，检查是否到达
        if (syncInfo.getNextSyncTime() != null) {
            return now.isAfter(syncInfo.getNextSyncTime());
        }

        return now.isAfter(syncInfo.getLastSyncTime().plusNanos(syncInfo.getSyncInterval() * 1000000));
    }

    public void enableSync(SyncTypeEnum syncType, boolean enabled) {
        SyncInfo syncInfo = getSyncInfo(syncType);
        syncInfo.setEnabled(enabled);
        syncInfoRepository.save(syncInfo);
    }

    public void updateSyncStatus(SyncTypeEnum syncType, SyncStatusEnum syncStatusEnum) {
        SyncInfo syncInfo = getSyncInfo(syncType);
        syncInfo.setSyncStatus(syncStatusEnum.getCode());
        syncInfoRepository.save(syncInfo);
    }

    public void setSyncInterval(SyncTypeEnum syncType, long interval) {
        SyncInfo syncInfo = getSyncInfo(syncType);
        syncInfo.setSyncInterval(interval);
        syncInfoRepository.save(syncInfo);
    }

    public void saveSyncInfo(SyncInfo syncInfo) {
        syncInfoRepository.save(syncInfo);
    }
} 