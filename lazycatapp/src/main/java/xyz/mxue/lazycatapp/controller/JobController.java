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

    @Operation(summary = "同步应用", description = "同步应用")
    @GetMapping("/syncApps")
    public R<String> syncApps() {
        appSyncService.syncApps();
        return R.success("同步完成");
    }

    @Operation(summary = "同步分类", description = "同步分类")
    @GetMapping("/syncCategories")
    public R<String> syncCategories() {
        categorySyncService.syncCategories();
        return R.success("同步完成");
    }

    @Operation(summary = "同步用户", description = "同步用户")
    @GetMapping("/syncUsers")
    public R<String> syncUsers() {
        userSyncService.syncDevelopers();
        return R.success("同步完成");
    }

}
