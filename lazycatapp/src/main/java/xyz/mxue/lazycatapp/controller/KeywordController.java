package xyz.mxue.lazycatapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.mxue.lazycatapp.service.KeywordService;

import java.util.List;
import java.util.Map;

@Tag(name = "关键词管理", description = "关键词管理")
@RestController
@RequestMapping("/api/apps")
public class KeywordController {
    private final KeywordService keywordService;

    public KeywordController(KeywordService keywordService) {
        this.keywordService = keywordService;
    }

    @Operation(summary = "获取关键词统计", description = "获取关键词统计")
    @GetMapping("/keywords")
    public List<Map<String, Object>> getKeywords() {
        return keywordService.getKeywordStats();
    }
} 