package xyz.mxue.lazycatapp.model.response.github;

import lombok.Data;

import java.util.Map;

/**
 * GitHub用户响应
 */
@Data
public class GitHubUserResponse {

    private Long id;
    private String createdAt;
    private String updatedAt;
    private Long uid;
    private GitHubUser user;
    private Map<String, GitHubLanguage> topLangs;
    private GitHubSummary summary;

}
