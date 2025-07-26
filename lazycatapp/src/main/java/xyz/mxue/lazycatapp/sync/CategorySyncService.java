package xyz.mxue.lazycatapp.sync;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.converter.CategoryConvert;
import xyz.mxue.lazycatapp.entity.Category;
import xyz.mxue.lazycatapp.entity.SyncInfo;
import xyz.mxue.lazycatapp.repository.CategoryRepository;
import xyz.mxue.lazycatapp.sync.api.LazyCatInterfaceInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Async("taskExecutor")
    public void syncCategories() {
        if (syncService.shouldSync(SyncService.SYNC_TYPE_CATEGORY)) {
            return;
        }

        log.info("开始同步分类信息...");
        try {
            // 同步中文分类
            List<Category> chineseCategories = getCategoriesFromUrl(LazyCatInterfaceInfo.CATEGORY_URL_ZH);
            if (CollectionUtil.isEmpty(chineseCategories)) {
                log.info("没有需要更新的分类");
                syncService.updateSyncInfo(SyncService.SYNC_TYPE_CATEGORY, true, null);
                return;
            }

            // 更新总数量到 SyncInfo
            SyncInfo syncInfo = syncService.getSyncInfo(SyncService.SYNC_TYPE_CATEGORY);
            if (syncInfo != null) {
                syncInfo.setTotalCount((long) chineseCategories.size());
                syncService.saveSyncInfo(syncInfo);
            }

            // 同步英文分类
            List<Category> englishCategories = getCategoriesFromUrl(LazyCatInterfaceInfo.CATEGORY_URL_EN);
            Map<Long, Category> englishCategoryMap = new HashMap<>();
            if (englishCategories != null) {
                for (Category category : englishCategories) {
                    englishCategoryMap.put(category.getId(), category);
                }
            }

            // 合并中英文分类信息
            List<Category> mergedCategories = new ArrayList<>();
            for (Category chineseCategory : chineseCategories) {
                Category saveCategory = CategoryConvert.convert(chineseCategory, englishCategoryMap.get(chineseCategory.getId()));
                mergedCategories.add(saveCategory);
            }
            categoryRepository.saveAll(mergedCategories);
            log.info("分类信息同步完成");
            syncService.updateSyncInfo(SyncService.SYNC_TYPE_CATEGORY, true, null);
        } catch (Exception e) {
            String error = "同步分类信息时发生错误: " + e.getMessage();
            log.error(error, e);
            syncService.updateSyncInfo(SyncService.SYNC_TYPE_CATEGORY, false, error);
        }
    }

    private List<Category> getCategoriesFromUrl(String url) {
        try {
            HttpResponse execute = HttpRequest.get(url).execute();
            if (execute.getStatus() == 200) {
                return objectMapper.readValue(execute.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, Category.class));
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

    /**
     * 查询懒猫应用商店中应用数量
     *
     * @return 应用数量
     */
    public long getTotalAppsCount() {
        long total = 0;
        List<Category> chineseCategories = getCategoriesFromUrl(LazyCatInterfaceInfo.CATEGORY_URL_ZH);
        if (chineseCategories != null) {
            total = chineseCategories.size();
        }
        return total;
    }

}
