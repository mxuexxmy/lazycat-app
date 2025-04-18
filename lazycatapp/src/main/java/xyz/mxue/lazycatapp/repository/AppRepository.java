package xyz.mxue.lazycatapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import xyz.mxue.lazycatapp.entity.App;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppRepository extends JpaRepository<App, String> {
    Page<App> findByNameContainingOrDescriptionContaining(String name, String description, Pageable pageable);
    
    @Query(value = "SELECT * FROM apps WHERE category LIKE %:category%", nativeQuery = true)
    Page<App> findByCategoryContaining(@Param("category") String category, Pageable pageable);
    
    @Query(value = "SELECT * FROM apps ORDER BY last_updated DESC LIMIT ?1", nativeQuery = true)
    List<App> findTopNByOrderByLastUpdatedDesc(int limit);
    
    @Query(value = "SELECT * FROM apps ORDER BY update_id DESC LIMIT ?1", nativeQuery = true)
    List<App> findTopNByOrderByUpdateIdDesc(int limit);
    
    @Query("SELECT a FROM App a WHERE (:platform = 'PC' AND a.supportPC = true) OR (:platform = 'Mobile' AND a.supportMobile = true)")
    List<App> findByPlatformSupport(@Param("platform") String platform);
    
    List<App> findByNameContainingOrDescriptionContaining(String name, String description);
    
    @Query(value = "SELECT * FROM apps ORDER BY download_count DESC LIMIT ?1", nativeQuery = true)
    List<App> findTopNByOrderByDownloadCountDesc(int limit);

    @Query("SELECT DISTINCT a.creatorId FROM App a WHERE a.creatorId IS NOT NULL")
    List<Long> findDistinctCreatorIds();

    List<App> findByCreatorId(Long creatorId);
} 