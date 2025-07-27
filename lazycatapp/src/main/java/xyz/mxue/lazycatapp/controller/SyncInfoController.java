package xyz.mxue.lazycatapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.mxue.lazycatapp.model.R;
import xyz.mxue.lazycatapp.model.vo.SyncDataSourceVO;
import xyz.mxue.lazycatapp.model.vo.SyncInfoVO;
import xyz.mxue.lazycatapp.service.SyncInfoService;

import java.util.List;

@Tag(name = "同步管理", description = "同步管理")
@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncInfoController {

    private final SyncInfoService syncInfoService;

    @Operation(summary = "同步信息", description = "同步信息")
    @GetMapping("/info")
    public R<List<SyncInfoVO>> syncInfo() {
        return R.success(syncInfoService.syncInfo());
    }

    /**
     * 每个数据源同步情况
     */
    @Operation(summary = "数据源同步情况", description = "数据源同步情况")
    @GetMapping("/source")
    public R<List<SyncDataSourceVO>> syncSource() {
        return R.success(syncInfoService.syncDataSource());
    }


}
