package xyz.mxue.lazycatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.mxue.lazycatapp.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
} 