package xyz.mxue.lazycatapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.mxue.lazycatapp.service.AppCommentService;

import java.util.List;
import java.util.Map;

@Tag(name = "应用评论管理", description = "应用评论管理")
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class AppCommentController {

    private final AppCommentService appCommentService;

    @Operation(summary = "获取所有评论", description = "获取所有评论")
    @GetMapping("/all")
    public List<Map<String, Object>> getAllComments() {
        return appCommentService.getAllComments();
    }

    /**
     * 最新的5个评论
     */
    @Operation(summary = "获取最新的5个评论", description = "获取最新的5个评论")
    @GetMapping("/latest")
    public List<Map<String, Object>> getLatestComments() {
        return appCommentService.getLatestComments();
    }


}
