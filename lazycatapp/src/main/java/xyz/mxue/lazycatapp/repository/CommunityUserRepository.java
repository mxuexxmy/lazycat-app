package xyz.mxue.lazycatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mxue.lazycatapp.entity.CommunityUser;

public interface CommunityUserRepository extends JpaRepository<CommunityUser, Long> {
} 