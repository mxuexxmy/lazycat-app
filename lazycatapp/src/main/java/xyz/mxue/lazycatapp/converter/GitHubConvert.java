package xyz.mxue.lazycatapp.converter;

import cn.hutool.json.JSONUtil;
import xyz.mxue.lazycatapp.entity.GitHubInfo;
import xyz.mxue.lazycatapp.model.response.github.GitHubRank;
import xyz.mxue.lazycatapp.model.response.github.GitHubSummary;
import xyz.mxue.lazycatapp.model.response.github.GitHubUser;
import xyz.mxue.lazycatapp.model.response.github.GitHubUserResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GitHubConvert {

    public static GitHubInfo convert(Long userId, GitHubUserResponse githubResponse) {
        // 获取或创建 GitHub 信息
        GitHubInfo githubInfo = new GitHubInfo();

        // 设置基本信息
        githubInfo.setUserId(userId);
        githubInfo.setUid(githubResponse.getUid());

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        githubInfo.setCreatedAt(LocalDateTime.parse(githubResponse.getCreatedAt(), timeFormatter));
        githubInfo.setUpdatedAt(LocalDateTime.parse(githubResponse.getUpdatedAt(), timeFormatter));

        // 设置用户信息
        GitHubUser user = githubResponse.getUser();
        githubInfo.setUsername(user.getUsername());
        githubInfo.setNickname(user.getNickname());
        githubInfo.setAvatar(user.getAvatar());
        githubInfo.setDescription(user.getDescription());
        githubInfo.setGithubUsername(user.getGithubUsername());

        // 设置统计信息
        GitHubSummary summary = githubResponse.getSummary();
        githubInfo.setTotalPRs(summary.getTotalPRs());
        githubInfo.setTotalCommits(summary.getTotalCommits());
        githubInfo.setTotalIssues(summary.getTotalIssues());
        githubInfo.setContributedTo(summary.getContributedTo());

        // 设置排名信息
        GitHubRank rank = summary.getRank();
        githubInfo.setRankLevel(rank.getLevel());
        githubInfo.setRankScore(rank.getScore());

        // 设置编程语言信息
        githubInfo.setTopLangs(JSONUtil.toJsonPrettyStr(githubResponse.getTopLangs()));

        // 设置最后同步时间
        githubInfo.setLastSyncTime(LocalDateTime.now());
        return githubInfo;
    }

}
