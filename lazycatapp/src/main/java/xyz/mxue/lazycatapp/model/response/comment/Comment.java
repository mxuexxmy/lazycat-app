package xyz.mxue.lazycatapp.model.response.comment;

import lombok.Data;

@Data
public class Comment {

    private Long commentId;
    private String appId;
    private Long userid;
    private String nickname;
    private String avatar;
    private int score;
    private String content;
    private Boolean liked;
    private int likeCounts;
    private String createdAt;
    private String updatedAt;

}
