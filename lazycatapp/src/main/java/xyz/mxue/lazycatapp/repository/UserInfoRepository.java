package xyz.mxue.lazycatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mxue.lazycatapp.entity.UserInfo;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
} 