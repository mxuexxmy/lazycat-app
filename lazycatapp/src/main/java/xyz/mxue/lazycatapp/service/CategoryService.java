package xyz.mxue.lazycatapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.entity.Category;
import xyz.mxue.lazycatapp.entity.SyncInfo;
import xyz.mxue.lazycatapp.repository.CategoryRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final SyncService syncService;
    
    private static final String CATEGORY_URL_ZH = "https://dl.lazycatmicroserver.com/appstore/metarepo/zh/categories.json";
    private static final String CATEGORY_URL_EN = "https://dl.lazycatmicroserver.com/appstore/metarepo/en/categories.json";

    @PostConstruct
    public void init() {
        // 检查是否需要首次同步
        if (categoryRepository.count() == 0) {
            log.info("No categories found, performing initial sync");
            syncCategories();
        }
    }
    
    @Scheduled(fixedRate = 3600000) // 每1小时执行一次
    public void syncCategories() {
        if (!syncService.shouldSync(SyncService.SYNC_TYPE_CATEGORY)) {
            return;
        }

        log.info("开始同步分类信息...");
        try {
            // 同步中文分类
            Category[] chineseCategories = getCategoriesFromUrl(CATEGORY_URL_ZH);
            if (chineseCategories == null || chineseCategories.length == 0) {
                log.info("没有需要更新的分类");
                syncService.updateSyncInfo(SyncService.SYNC_TYPE_CATEGORY, true, null);
                return;
            }

            // 更新总数量到 SyncInfo
            SyncInfo syncInfo = syncService.getSyncInfo(SyncService.SYNC_TYPE_CATEGORY);
            if (syncInfo != null) {
                syncInfo.setTotalCount((long) chineseCategories.length);
                syncService.saveSyncInfo(syncInfo);
            }

            // 获取现有分类
            List<Category> existingCategories = categoryRepository.findAll();
            Map<Integer, Category> existingCategoryMap = existingCategories.stream()
                    .collect(Collectors.toMap(Category::getId, category -> category));

            // 同步英文分类
            Category[] englishCategories = getCategoriesFromUrl(CATEGORY_URL_EN);
            Map<Integer, Category> englishCategoryMap = new HashMap<>();
            if (englishCategories != null) {
                for (Category category : englishCategories) {
                    englishCategoryMap.put(category.getId(), category);
                }
            }

            // 合并中英文分类信息
            for (Category chineseCategory : chineseCategories) {
                Integer id = chineseCategory.getId();
                Category englishCategory = englishCategoryMap.get(id);

                Category existingCategory = existingCategoryMap.get(id);
                Category categoryToSave = existingCategory != null ? existingCategory : new Category();

                // 设置基本信息
                categoryToSave.setId(id);
                categoryToSave.setName(chineseCategory.getName()); // 中文名称
                categoryToSave.setIcon(chineseCategory.getIcon());

                // 设置英文名称
                if (englishCategory != null) {
                    categoryToSave.setEnglishName(englishCategory.getName());
                }

                categoryRepository.save(categoryToSave);
                log.info("同步分类: {} (中文: {}, 英文: {})", 
                    id, 
                    chineseCategory.getName(),
                    englishCategory != null ? englishCategory.getName() : "N/A");
            }

            log.info("分类信息同步完成");
            syncService.updateSyncInfo(SyncService.SYNC_TYPE_CATEGORY, true, null);
        } catch (Exception e) {
            String error = "同步分类信息时发生错误: " + e.getMessage();
            log.error(error, e);
            syncService.updateSyncInfo(SyncService.SYNC_TYPE_CATEGORY, false, error);
        }
    }
    
    private Category[] getCategoriesFromUrl(String url) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String error = "获取分类失败: " + response;
                    log.error(error);
                    throw new RuntimeException(error);
                }

                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, Category[].class);
            }
        } catch (Exception e) {
            String error = "获取分类时发生错误: " + e.getMessage();
            log.error(error, e);
            throw new RuntimeException(error);
        }
    }
    
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    public Category getCategoryById(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }
    
    public long count() {
        return categoryRepository.count();
    }
    
    public long getTotalCategoriesCount() {
        return categoryRepository.count();
    }

} 