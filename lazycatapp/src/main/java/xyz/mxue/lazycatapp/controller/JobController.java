package xyz.mxue.lazycatapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.mxue.lazycatapp.model.R;
import xyz.mxue.lazycatapp.sync.AppSyncService;

@Tag(name = "定时任务管理", description = "定时任务管理")
@RestController
@RequestMapping("/api/job")
@RequiredArgsConstructor
public class JobController {

    private final AppSyncService appSyncService;

    @Operation(summary = "同步应用", description = "同步应用")
    @GetMapping("/syncApps")
    public R<String> syncApps() {
        appSyncService.syncApps();
        return R.success("同步完成");
    }

}
