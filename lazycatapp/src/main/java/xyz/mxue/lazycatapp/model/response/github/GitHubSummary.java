package xyz.mxue.lazycatapp.model.response.github;

import lombok.Data;

@Data
public class GitHubSummary {

    private String name;
    private int totalPRs;
    private int totalCommits;
    private int totalIssues;
    private int contributedTo;
    private GitHubRank rank;

}
