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

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    private static final String CATEGORY_URL = "https://dl.lazycatmicroserver.com/appstore/metarepo/zh/categories.json";
    
   // @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void updateCategories() {
        log.info("开始更新分类信息");
        try {
            Request request = new Request.Builder()
                    .url(CATEGORY_URL)
                    .get()
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("获取分类信息失败: {}", response);
                    return;
                }
                
                String responseBody = response.body().string();
                List<Category> categories = objectMapper.readValue(responseBody, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Category.class));
                
                if (categories != null && !categories.isEmpty()) {
                    categoryRepository.saveAll(categories);
                    log.info("成功更新分类信息");
                }
            }
        } catch (IOException e) {
            log.error("更新分类信息时发生错误", e);
        }
    }
    
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }
    
    public Category findById(Integer id) {
        return categoryRepository.findById(id).orElse(null);
    }
} 