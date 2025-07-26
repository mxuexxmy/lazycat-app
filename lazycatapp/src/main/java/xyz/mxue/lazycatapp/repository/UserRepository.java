package xyz.mxue.lazycatapp.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import xyz.mxue.lazycatapp.entity.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u ")
    List<User> findActiveUsers(Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM User ")
    long countNewUsersToday();

    @Query(value = "SELECT COUNT(*) FROM User ")
    long countNewUsersThisWeek();

    @Query(value = "SELECT COUNT(*) FROM User")
    long countNewUsersThisMonth();
} 