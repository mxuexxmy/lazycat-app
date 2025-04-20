package xyz.mxue.lazycatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import xyz.mxue.lazycatapp.entity.AppScore;
import java.util.List;

public interface AppScoreRepository extends JpaRepository<AppScore, String> {
    
    // 查询评分为5星的应用
    @Query("SELECT a FROM AppScore a WHERE a.score = 5.0")
    List<AppScore> findFiveStarApps();
    
    // 查询评论数最多的应用
    List<AppScore> findTop10ByOrderByTotalReviewsDesc();
    
    // 按评分降序排序
    List<AppScore> findAllByOrderByScoreDesc();
}