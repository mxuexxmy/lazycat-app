package xyz.mxue.lazycatapp.sync;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.converter.TutorialConvert;
import xyz.mxue.lazycatapp.entity.Tutorial;
import xyz.mxue.lazycatapp.model.response.tutorial.TutorialInfo;
import xyz.mxue.lazycatapp.model.response.tutorial.TutorialResponse;
import xyz.mxue.lazycatapp.repository.TutorialRepository;
import xyz.mxue.lazycatapp.sync.api.LazyCatInterfaceInfo;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TutorialSyncService {

    private final TutorialRepository tutorialRepository;

    private final ObjectMapper objectMapper;

    public void syncTutorials(Long userId) {
        int page = 1;
        int size = 10;
        boolean hasNext = true;
        int totalProcessed = 0;
        while (hasNext) {
            try {
                Map<String, Object> queryParam = new HashMap<>();
                queryParam.put("page", page);
                queryParam.put("sort", "createdAt");
                queryParam.put("size", size);
                HttpResponse execute = HttpRequest.get(LazyCatInterfaceInfo.DYNAMIC_URL + userId)
                        .form(queryParam)
                        .execute();

                if (execute.getStatus() != 200) {
                    log.error("获取教程失败: {}", execute.body());
                    return;
                }

                TutorialResponse tutorialResponse = objectMapper.readValue(execute.body(), TutorialResponse.class);

                if (CollectionUtil.isEmpty(tutorialResponse.getItems())) {
                    log.info("没有教程");
                    hasNext = false;
                } else {
                    for (TutorialInfo tutorialInfo : tutorialResponse.getItems()) {
                        Tutorial tutorial = TutorialConvert.convert(tutorialInfo);
                        tutorialRepository.save(tutorial);
                    }
                    page++;
                    totalProcessed += tutorialResponse.getItems().size();
                    hasNext = totalProcessed < tutorialResponse.getTotal();
                    if (!hasNext) {
                        log.info("成功同步 {} 用户的全部动态", userId);
                    }
                }

            } catch (Exception e) {
                log.error("处理教程失败: {}", e.getMessage());
            }
        }
    }

}
