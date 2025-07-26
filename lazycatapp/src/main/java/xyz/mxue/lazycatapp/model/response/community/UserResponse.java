package xyz.mxue.lazycatapp.model.response.community;

import lombok.Data;

@Data
public class UserResponse {

    private Long uid;
    private UserData user;
    private Integer receiveThumbs;
    private Integer follows;
    private Integer fans;
    private Boolean followed;
    private Integer guidelineCounts;
    private Boolean hideFollows;
    private Boolean hideFans;
    private Boolean hideThumbs;
    private Boolean onlyFollowedAtMe;
    private Boolean onlyFollowedCommentMe;

}
