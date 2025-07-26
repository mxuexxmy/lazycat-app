package xyz.mxue.lazycatapp.model.response.community;

import lombok.Data;

@Data
public class UserData {

    private Long id;
    private String username;
    private Boolean isUsernameSet;
    private String nickname;
    private String avatar;
    private String description;
    private Integer status;
    private String githubUsername;
    private Boolean isCurrentLoginUser;

}
