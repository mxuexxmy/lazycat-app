package xyz.mxue.lazycatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mxue.lazycatapp.entity.AppComment;

import java.util.List;

public interface AppCommentRepository extends JpaRepository<AppComment, Long> {
    List<AppComment> findByPkgId(String pkgId);

    void deleteByPkgId(String pkgId);
}