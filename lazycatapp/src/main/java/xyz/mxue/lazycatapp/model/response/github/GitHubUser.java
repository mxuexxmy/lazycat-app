package xyz.mxue.lazycatapp.model.response.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GitHubUser {

    private Long id;
    private String username;
    @JsonProperty("isUsernameSet")
    private boolean usernameSet;
    private String nickname;
    private String avatar;
    private String description;
    private int status;
    private String githubUsername;
    @JsonProperty("isCurrentLoginUser")
    private boolean currentLoginUser;

}
