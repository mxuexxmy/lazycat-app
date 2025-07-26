package xyz.mxue.lazycatapp.sync;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.converter.AppCommentConvert;
import xyz.mxue.lazycatapp.entity.AppComment;
import xyz.mxue.lazycatapp.model.response.comment.Comment;
import xyz.mxue.lazycatapp.model.response.comment.CommentApiResponse;
import xyz.mxue.lazycatapp.repository.AppCommentRepository;
import xyz.mxue.lazycatapp.sync.api.LazyCatInterfaceInfo;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class AppCommentSyncService {

    private final AppCommentRepository appCommentRepository;

    private final ObjectMapper objectMapper;

    public void syncAppComments(String packageName) {
        int page = 1;
        int limit = 500;
        boolean hasNext = true;

        while (hasNext) {
            try {
                Map<String, Object> queryParam = new HashMap<>();
                queryParam.put("page", page);
                queryParam.put("limit", limit);
                HttpResponse execute = HttpRequest.get(LazyCatInterfaceInfo.COMMENT_LIST_URL + packageName)
                        .form(queryParam)
                        .execute();

                if (execute.getStatus() != 200) {
                    log.error("获取应用评论失败: {}", execute.body());
                    return;
                }

                CommentApiResponse commentResponse = objectMapper.readValue(execute.body(), CommentApiResponse.class);

                if (commentResponse.getErrorCode() == 0 && commentResponse.getData() != null) {
                    // 保存或更新评论
                    for (Comment comment : commentResponse.getData().getList()) {
                        AppComment appComment = AppCommentConvert.convert(comment);
                        appCommentRepository.save(appComment);
                    }

                    hasNext = commentResponse.getData().isNext();

                    page++;

                    if (!hasNext) {
                        log.info("成功同步应用 {} 的所有评论信息", packageName);
                    }
                }
            } catch (Exception e) {
                log.info("处理应用评论失败：{}", e.getMessage());
            }
        }
    }

}
