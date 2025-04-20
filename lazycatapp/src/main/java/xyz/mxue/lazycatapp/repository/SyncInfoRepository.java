package xyz.mxue.lazycatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mxue.lazycatapp.entity.SyncInfo;
import java.util.Optional;

public interface SyncInfoRepository extends JpaRepository<SyncInfo, Long> {
    Optional<SyncInfo> findBySyncType(String syncType);
} 