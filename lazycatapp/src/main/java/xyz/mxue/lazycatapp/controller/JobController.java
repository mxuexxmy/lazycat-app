package xyz.mxue.lazycatapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.mxue.lazycatapp.model.R;
import xyz.mxue.lazycatapp.sync.AppSyncService;
import xyz.mxue.lazycatapp.sync.CategorySyncService;
import xyz.mxue.lazycatapp.sync.UserSyncService;

@Tag(name = "定时任务管理", description = "定时任务管理")
@RestController
@RequestMapping("/api/job")
@RequiredArgsConstructor
public class JobController {

    private final AppSyncService appSyncService;

    private final CategorySyncService categorySyncService;

    private final UserSyncService userSyncService;

    @Operation(summary = "全量同步应用", description = "全量同步应用")
    @GetMapping("/syncApps")
    public R<String> syncApps() {
        appSyncService.syncApps(true);
        return R.success("在后台进行同步，请 30 分钟后进行查看");
    }

    @Operation(summary = "全量同步分类", description = "全量同步分类")
    @GetMapping("/syncCategories")
    public R<String> syncCategories() {
        categorySyncService.syncCategories(true);
        return R.success("在后台进行同步，请 1 分钟后进行查看");
    }

    @Operation(summary = "全量同步用户", description = "全量同步用户")
    @GetMapping("/syncUsers")
    public R<String> syncUsers() {
        userSyncService.syncDevelopers(true);
        return R.success("在后台进行同步，请 10 分钟后进行查看");
    }

    @Operation(summary = "增量同步应用", description = "增量全量同步应用")
    @GetMapping("/incrementalSyncApps")
    public R<String> incrementalSyncApps() {
        appSyncService.syncAppsIncremental(true);
        return R.success("在后台进行同步，请 10 分钟后进行查看");
    }

    @Operation(summary = "增量同步分类", description = "增量同步分类")
    @GetMapping("/incrementalSyncCategories")
    public R<String> incrementalSyncCategories() {
        categorySyncService.syncCategoriesIncremental(true);
        return R.success("在后台进行同步，请 1 分钟后进行查看");
    }

    @Operation(summary = "增量同步用户", description = "增量同步用户")
    @GetMapping("/incrementalSyncUsers")
    public R<String> incrementalSyncUsers() {
        userSyncService.syncUsersIncremental(true);
        return R.success("在后台进行同步，请 1 分钟后进行查看");
    }


}
