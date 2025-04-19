package xyz.mxue.lazycatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mxue.lazycatapp.entity.GitHubInfo;
import java.util.Optional;

public interface GitHubInfoRepository extends JpaRepository<GitHubInfo, Long> {
    Optional<GitHubInfo> findByUserId(Long userId);
} 