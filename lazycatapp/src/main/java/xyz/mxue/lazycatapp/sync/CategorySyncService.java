package xyz.mxue.lazycatapp.sync;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.converter.CategoryConvert;
import xyz.mxue.lazycatapp.entity.Category;
import xyz.mxue.lazycatapp.entity.SyncInfo;
import xyz.mxue.lazycatapp.enums.SyncStrategyEnum;
import xyz.mxue.lazycatapp.enums.SyncTypeEnum;
import xyz.mxue.lazycatapp.repository.CategoryRepository;
import xyz.mxue.lazycatapp.sync.api.LazyCatInterfaceInfo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Async("taskExecutor")
    public void syncCategories(boolean forceSync) {
        log.info("开始同步分类信息-{}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        if (syncService.isSync(SyncTypeEnum.CATEGORY, SyncStrategyEnum.FULL, forceSync)) {
            log.info("进行同步分类信息-{}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            try {
                // 更改同步状态- 同步中
                log.info("开始同步分类信息...");
                // 同步中文分类
                List<Category> chineseCategories = getCategoriesFromUrl(LazyCatInterfaceInfo.CATEGORY_URL_ZH);
                if (CollectionUtil.isEmpty(chineseCategories)) {
                    log.info("没有需要更新的分类");
                    syncService.updateSyncInfo(SyncTypeEnum.CATEGORY, SyncStrategyEnum.FULL, true, null);
                    return;
                }

                // 更新总数量到 SyncInfo
                SyncInfo syncInfo = syncService.getSyncInfo(SyncTypeEnum.CATEGORY);
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
                syncService.updateSyncInfo(SyncTypeEnum.CATEGORY, SyncStrategyEnum.FULL, true, null);
                // 更改同步状态- 完成
            } catch (Exception e) {
                String error = "同步分类信息时发生错误: " + e.getMessage();
                log.error(error, e);
                syncService.updateSyncInfo(SyncTypeEnum.CATEGORY, SyncStrategyEnum.FULL, false, error);
                // 更改同步状态- 失败
            }
        }
    }

    /**
     * 增量同步
     */
    public void syncCategoriesIncremental(boolean forceSync) {
        if (syncService.isSync(SyncTypeEnum.CATEGORY, SyncStrategyEnum.INCREMENTAL, forceSync)) {
            long localCount = categoryRepository.count();
            long remoteCount = getTotalCategoryCount();
            if (localCount < remoteCount) {
                try {
                    // 更改同步状态- 同步中
                    log.info("开始同步分类信息...");
                    // 同步中文分类
                    List<Category> chineseCategories = getCategoriesFromUrl(LazyCatInterfaceInfo.CATEGORY_URL_ZH);
                    if (CollectionUtil.isEmpty(chineseCategories)) {
                        log.info("没有需要更新的分类");
                        syncService.updateSyncInfo(SyncTypeEnum.CATEGORY, SyncStrategyEnum.INCREMENTAL, true, null);
                        return;
                    }

                    // 更新总数量到 SyncInfo
                    SyncInfo syncInfo = syncService.getSyncInfo(SyncTypeEnum.CATEGORY);
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
                    syncService.updateSyncInfo(SyncTypeEnum.CATEGORY, SyncStrategyEnum.INCREMENTAL, true, null);
                    // 更改同步状态- 完成
                } catch (Exception e) {
                    String error = "同步分类信息时发生错误: " + e.getMessage();
                    log.error(error, e);
                    syncService.updateSyncInfo(SyncTypeEnum.CATEGORY, SyncStrategyEnum.INCREMENTAL, false, error);
                    // 更改同步状态- 失败
                }
            }
        }
    }

    private List<Category> getCategoriesFromUrl(String url) {
        try {
            HttpResponse execute = HttpRequest.get(url).execute();
            if (execute.getStatus() == 200) {
                log.error("分类信息：{}", execute);
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
    public long getTotalCategoryCount() {
        long total = 0;
        List<Category> chineseCategories = getCategoriesFromUrl(LazyCatInterfaceInfo.CATEGORY_URL_ZH);
        if (chineseCategories != null) {
            total = chineseCategories.size();
        }
        return total;
    }

}
