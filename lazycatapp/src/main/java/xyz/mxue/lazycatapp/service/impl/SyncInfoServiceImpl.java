package xyz.mxue.lazycatapp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.entity.SyncInfo;
import xyz.mxue.lazycatapp.enums.SyncStatusEnum;
import xyz.mxue.lazycatapp.enums.SyncTypeEnum;
import xyz.mxue.lazycatapp.model.vo.SyncDataSourceVO;
import xyz.mxue.lazycatapp.model.vo.SyncInfoVO;
import xyz.mxue.lazycatapp.repository.*;
import xyz.mxue.lazycatapp.service.SyncInfoService;
import xyz.mxue.lazycatapp.sync.AppSyncService;
import xyz.mxue.lazycatapp.sync.CategorySyncService;
import xyz.mxue.lazycatapp.sync.UserSyncService;

import java.util.ArrayList;
import java.util.List;

import static xyz.mxue.lazycatapp.sync.SyncService.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncInfoServiceImpl implements SyncInfoService {

    private final SyncInfoRepository syncInfoRepository;

    private final AppRepository appRepository;

    private final AppCommentRepository appCommentRepository;

    private final AppScoreRepository appScoreRepository;

    private final CategoryRepository categoryRepository;

    private final CommunityUserRepository communityUserRepository;

    private final GitHubInfoRepository gitHubInfoRepository;

    private final UserRepository userRepository;

    private final TutorialRepository tutorialRepository;

    private final AppSyncService appSyncService;

    private final CategorySyncService categorySyncService;

    private final UserSyncService userSyncService;

    @Override
    public List<SyncInfoVO> syncInfo() {
        List<SyncInfo> syncInfoList = syncInfoRepository.findAll();
        List<SyncInfoVO> res = new ArrayList<>();

        if (CollectionUtil.isNotEmpty(syncInfoList)) {
            syncInfoList.forEach(entity -> {
                SyncInfoVO vo = new SyncInfoVO();
                vo.setId(entity.getId());
                vo.setSyncType(entity.getSyncType());
                vo.setLastSyncTime(entity.getLastSyncTime());
                vo.setInitialSyncCompleted(entity.isInitialSyncCompleted());
                vo.setInitialSyncCompletedName(entity.isInitialSyncCompleted() ? "是" : "否");
                vo.setSyncStatus(entity.getSyncStatus());
                vo.setSyncStatusName(SyncStatusEnum.getNameByCode(entity.getSyncStatus()));
                vo.setSyncStrategy(entity.getSyncStrategy());
                vo.setSyncInterval(entity.getSyncInterval());
                vo.setLastError(entity.getLastError());
                vo.setNextSyncTime(entity.getNextSyncTime());
                vo.setRetryCount(entity.getRetryCount());
                vo.setEnabled(entity.isEnabled());
                vo.setTotalCount(entity.getTotalCount());
                vo.setSyncTypeName(SyncTypeEnum.getNameByCode(entity.getSyncType()));
                vo.setInitialSyncCompletedName(SyncStatusEnum.getNameByCode(entity.isInitialSyncCompleted() ? 1 : 0));
                res.add(vo);
            });
        }

        return res;
    }

    @Override
    public List<SyncDataSourceVO> syncDataSource() {
        List<SyncDataSourceVO> res = new ArrayList<>();
        List<SyncInfo> syncInfoAll = syncInfoRepository.findAll();
        long appTargetCount = 0;
        long categoryTargetCount = 0;
        long userTargetCount = 0;
        if (CollectionUtil.isNotEmpty(syncInfoAll)) {
            for (SyncInfo entity : syncInfoAll) {
                if (entity.getSyncType().equals(SyncTypeEnum.APP.getCode())) {
                    appTargetCount = entity.getTotalCount();
                } else if (entity.getSyncType().equals(SyncTypeEnum.CATEGORY.getCode())) {
                    categoryTargetCount = entity.getTotalCount();
                } else if (entity.getSyncType().equals(SyncTypeEnum.USER.getCode())) {
                    userTargetCount = entity.getTotalCount();
                }
            }
        }

        if (appTargetCount == 0) {
            appTargetCount = appSyncService.getTotalAppsCount();
        }

        if (categoryTargetCount == 0) {
            categoryTargetCount = categorySyncService.getTotalCategoryCount();
        }

        if (userTargetCount == 0) {
            userTargetCount = userSyncService.getTotalUserCount();
        }

        // 应用
        long appTotal = appRepository.count();
        res.add(buildInfo("应用", appTotal, appTargetCount));

        // 应用评论
        long appCommentTotal = appCommentRepository.count();
        res.add(buildInfo("应用评论", appCommentTotal, appCommentTotal));

        // 应用评分
        long appScoreTotal = appScoreRepository.count();
        res.add(buildInfo("应用评分", appScoreTotal, appTargetCount));

        // 应用分类
        long categoryTotal = categoryRepository.count();
        res.add(buildInfo("应用分类", categoryTotal, categoryTargetCount));

        // 社区用户信息
        long communityUserTotal = communityUserRepository.count();
        res.add(buildInfo("社区用户信息", communityUserTotal, communityUserTotal));

        // GitHub 信息
        long gitHubInfoTotal = gitHubInfoRepository.count();
        res.add(buildInfo("GitHub 信息", gitHubInfoTotal, gitHubInfoTotal));

        // 用户
        res.add(buildInfo("用户", userRepository.count(), userTargetCount));

        // 教程
        long tutorialTotal = tutorialRepository.count();
        res.add(buildInfo("教程", tutorialTotal, tutorialTotal));

        return res;
    }

    /**
     * 构建同步数据源信息
     *
     * @param dataName    数据名称
     * @param dataCount   数据数量
     * @param targetCount 目标数量
     * @return 同步数据源信息
     */
    private SyncDataSourceVO buildInfo(String dataName, Long dataCount, Long targetCount) {
        SyncDataSourceVO syncDataSourceVO = new SyncDataSourceVO();
        syncDataSourceVO.setDataName(dataName);
        syncDataSourceVO.setDataCount(dataCount);
        syncDataSourceVO.setTargetCount(targetCount);
        return syncDataSourceVO;
    }

}
