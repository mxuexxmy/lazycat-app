package xyz.mxue.lazycatapp.service;

import xyz.mxue.lazycatapp.entity.Category;

import java.util.List;

public interface CategoryService {

    List<Category> getAllCategories();

    Category getCategoryById(Long id);

    long count();
} 