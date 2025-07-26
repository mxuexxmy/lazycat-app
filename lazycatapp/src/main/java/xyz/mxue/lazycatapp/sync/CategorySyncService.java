package xyz.mxue.lazycatapp.sync;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.entity.Category;
import xyz.mxue.lazycatapp.entity.SyncInfo;
import xyz.mxue.lazycatapp.repository.CategoryRepository;
import xyz.mxue.lazycatapp.sync.api.LazyCatInterfaceInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategorySyncService {

    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;
    private final SyncService syncService;

    @PostConstruct
    public void init() {
        // 检查是否需要首次同步
        if (categoryRepository.count() == 0) {
            log.info("No categories found, performing initial sync");
            syncCategories();
        }
    }

    // @Scheduled(fixedRate = 3600000) // 每1小时执行一次
    public void syncCategories() {
        if (syncService.shouldSync(SyncService.SYNC_TYPE_CATEGORY)) {
            return;
        }

        log.info("开始同步分类信息...");
        try {
            // 同步中文分类
            Category[] chineseCategories = getCategoriesFromUrl(LazyCatInterfaceInfo.CATEGORY_URL_ZH);
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
            Category[] englishCategories = getCategoriesFromUrl(LazyCatInterfaceInfo.CATEGORY_URL_EN);
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
            HttpResponse execute = HttpRequest.get(url).execute();
            if (execute.getStatus() == 200) {
                return objectMapper.readValue(execute.body(), Category[].class);
            } else {
                String error = "获取分类失败: " + execute.body();
                log.error(error);
            }
            return null;
        } catch (Exception e) {
            String error = "获取分类时发生错误: " + e.getMessage();
            log.error(error, e);
            throw new RuntimeException(error);
        }
    }

}
