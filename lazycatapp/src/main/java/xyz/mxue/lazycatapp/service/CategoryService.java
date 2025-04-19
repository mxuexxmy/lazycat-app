package xyz.mxue.lazycatapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.entity.Category;
import xyz.mxue.lazycatapp.repository.CategoryRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    private static final String CATEGORY_URL_ZH = "https://dl.lazycatmicroserver.com/appstore/metarepo/zh/categories.json";
    private static final String CATEGORY_URL_EN = "https://dl.lazycatmicroserver.com/appstore/metarepo/en/categories.json";
    
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void syncCategories() {
        updateChineseCategories();
        updateEnglishCategories();
    }
    
    public void updateChineseCategories() {
        log.info("开始更新中文分类信息");
        try {
            Request request = new Request.Builder()
                    .url(CATEGORY_URL_ZH)
                    .get()
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("获取中文分类信息失败: {}", response);
                    return;
                }
                
                String responseBody = response.body().string();
                List<Category> newCategories = objectMapper.readValue(responseBody, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Category.class));
                
                if (newCategories != null && !newCategories.isEmpty()) {
                    // 获取现有分类
                    List<Category> existingCategories = categoryRepository.findAll();
                    Map<Integer, Category> existingMap = existingCategories.stream()
                            .collect(Collectors.toMap(Category::getId, category -> category));
                    
                    // 更新或添加新分类
                    for (Category newCategory : newCategories) {
                        Category existingCategory = existingMap.get(newCategory.getId());
                        if (existingCategory != null) {
                            // 更新现有分类
                            existingCategory.setName(newCategory.getName());
                            existingCategory.setIcon(newCategory.getIcon());
                        } else {
                            // 添加新分类
                            categoryRepository.save(newCategory);
                        }
                    }
                    
                    // 保存更新后的分类
                    categoryRepository.saveAll(existingCategories);
                    log.info("成功更新中文分类信息");
                }
            }
        } catch (IOException e) {
            log.error("更新中文分类信息时发生错误", e);
        }
    }
    
    public void updateEnglishCategories() {
        log.info("开始更新英文分类信息");
        try {
            Request request = new Request.Builder()
                    .url(CATEGORY_URL_EN)
                    .get()
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("获取英文分类信息失败: {}", response);
                    return;
                }
                
                String responseBody = response.body().string();
                List<Category> englishCategories = objectMapper.readValue(responseBody, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Category.class));
                
                if (englishCategories != null && !englishCategories.isEmpty()) {
                    // 将英文分类信息转换为Map，以id为key
                    Map<Integer, String> englishNameMap = englishCategories.stream()
                            .collect(Collectors.toMap(Category::getId, Category::getName));
                    
                    // 获取所有中文分类
                    List<Category> chineseCategories = categoryRepository.findAll();
                    
                    // 更新每个中文分类的英文名称
                    for (Category category : chineseCategories) {
                        String englishName = englishNameMap.get(category.getId());
                        if (englishName != null) {
                            category.setEnglishName(englishName);
                        }
                    }
                    
                    categoryRepository.saveAll(chineseCategories);
                    log.info("成功更新英文分类信息");
                }
            }
        } catch (IOException e) {
            log.error("更新英文分类信息时发生错误", e);
        }
    }
    
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }
    
    public Category findById(Integer id) {
        return categoryRepository.findById(id).orElse(null);
    }
} 