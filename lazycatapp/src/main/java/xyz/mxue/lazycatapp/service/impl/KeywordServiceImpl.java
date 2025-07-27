package xyz.mxue.lazycatapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.repository.AppRepository;
import xyz.mxue.lazycatapp.service.KeywordService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordServiceImpl implements KeywordService {

    private final AppRepository appRepository;

    @Override
    public List<Map<String, Object>> getKeywordStats() {
        // 获取所有应用的关键词
        List<String> allKeywords = appRepository.findAll().stream()
                .flatMap(app -> {
                    List<String> keywords = new ArrayList<>();
                    // 添加应用名称作为关键词
                    keywords.add(app.getName());
                    // 添加应用标签作为关键词
                    if (app.getTags() != null) {
                        keywords.addAll(Arrays.asList(app.getTags().split(",")));
                    }
                    // 添加应用keywords作为关键词
                    if (app.getKeywords() != null) {
                        keywords.addAll(Arrays.asList(app.getKeywords().split(",")));
                    }
                    return keywords.stream();
                })
                .map(String::trim)
                .filter(keyword -> !keyword.isEmpty())
                .toList();

        // 统计关键词频率
        Map<String, Long> keywordCount = allKeywords.stream()
                .collect(Collectors.groupingBy(
                        keyword -> keyword,
                        Collectors.counting()
                ));

        // 转换为前端需要的格式并按频率排序
        return keywordCount.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> keywordData = new HashMap<>();
                    keywordData.put("keyword", entry.getKey());
                    keywordData.put("count", entry.getValue());
                    return keywordData;
                })
                .sorted((a, b) -> Long.compare(
                        (Long) b.get("count"),
                        (Long) a.get("count")
                ))
                .limit(50) // 限制返回前50个最常见的关键词
                .collect(Collectors.toList());
    }

}
