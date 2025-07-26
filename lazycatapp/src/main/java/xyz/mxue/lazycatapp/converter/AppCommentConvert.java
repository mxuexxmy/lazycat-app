package xyz.mxue.lazycatapp.converter;

import xyz.mxue.lazycatapp.entity.AppComment;
import xyz.mxue.lazycatapp.model.response.comment.Comment;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class AppCommentConvert {

    public static AppComment convert(Comment comment) {
        AppComment appComment = new AppComment();
        appComment.setCommentId(comment.getCommentId());
        appComment.setPkgId(comment.getAppId());
        appComment.setUserId(comment.getUserid());
        appComment.setNickname(comment.getNickname());
        appComment.setAvatar(comment.getAvatar());
        appComment.setScore(comment.getScore());
        appComment.setContent(comment.getContent());
        appComment.setLiked(comment.getLiked());
        appComment.setLikeCounts(comment.getLikeCounts());
        appComment.setCreatedAt(OffsetDateTime.parse(comment.getCreatedAt()).toLocalDateTime());
        appComment.setUpdatedAt(OffsetDateTime.parse(comment.getUpdatedAt()).toLocalDateTime());
        return appComment;
    }
}
