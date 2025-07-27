package xyz.mxue.lazycatapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.entity.App;
import xyz.mxue.lazycatapp.entity.AppComment;
import xyz.mxue.lazycatapp.repository.AppCommentRepository;
import xyz.mxue.lazycatapp.repository.AppRepository;
import xyz.mxue.lazycatapp.service.AppCommentService;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppCommentServiceImpl implements AppCommentService {

    private final AppCommentRepository appCommentRepository;

    private final AppRepository appRepository;

    @Override
    public List<Map<String, Object>> getAllComments() {
        List<AppComment> comments = appCommentRepository.findAll();
        return getMaps(comments);
    }

    @Override
    public List<Map<String, Object>> getLatestComments() {
        // 查询最新的5个评论
        List<AppComment> comments = appCommentRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream().limit(5).toList();
        return getMaps(comments);
    }

    private List<Map<String, Object>> getMaps(List<AppComment> comments) {
        return comments.stream().map(comment -> {
            Map<String, Object> commentMap = new HashMap<>();
            commentMap.put("commentId", comment.getCommentId());
            commentMap.put("pkgId", comment.getPkgId());
            commentMap.put("userId", comment.getUserId());
            commentMap.put("nickname", comment.getNickname());
            commentMap.put("avatar", comment.getAvatar());
            commentMap.put("score", comment.getScore());
            commentMap.put("content", comment.getContent());
            commentMap.put("liked", comment.getLiked());
            commentMap.put("likeCounts", comment.getLikeCounts());
            commentMap.put("createdAt", comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            commentMap.put("updatedAt", comment.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // 获取应用信息
            App queryApp = new App();
            queryApp.setPackageName(comment.getPkgId());
            Example<App> example = Example.of(queryApp);
            App app = appRepository.findOne(example).orElse(null);
            if (app != null) {
                commentMap.put("appName", app.getName());
                commentMap.put("appIcon", app.getIconPath());
                commentMap.put("brief", app.getBrief());
            }

            return commentMap;
        }).collect(Collectors.toList());
    }
}
