package xyz.mxue.lazycatapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.mxue.lazycatapp.service.KeywordService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/apps")
public class KeywordController {
    private final KeywordService keywordService;

    public KeywordController(KeywordService keywordService) {
        this.keywordService = keywordService;
    }

    @GetMapping("/keywords")
    public List<Map<String, Object>> getKeywords() {
        return keywordService.getKeywordStats();
    }
} 