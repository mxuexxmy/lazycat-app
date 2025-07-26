package xyz.mxue.lazycatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mxue.lazycatapp.entity.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
}
