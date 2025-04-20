package xyz.mxue.lazycatapp.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import xyz.mxue.lazycatapp.entity.UserInfo;

import java.time.LocalDateTime;
import java.util.List;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
    @Query("SELECT u FROM UserInfo u ORDER BY u.updatedAt DESC")
    List<UserInfo> findActiveUsers(Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM user_info WHERE DATE(created_at) = CURRENT_DATE", nativeQuery = true)
    long countNewUsersToday();

    @Query(value = "SELECT COUNT(*) FROM user_info WHERE created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY)", nativeQuery = true)
    long countNewUsersThisWeek();

    @Query(value = "SELECT COUNT(*) FROM user_info WHERE created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY)", nativeQuery = true)
    long countNewUsersThisMonth();
} 