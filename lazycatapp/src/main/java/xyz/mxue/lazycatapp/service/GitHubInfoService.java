package xyz.mxue.lazycatapp.service;

import xyz.mxue.lazycatapp.entity.GitHubInfo;

import java.util.*;

public interface GitHubInfoService {


    /**
     * 获取指定用户的 GitHub 信息
     *
     * @param userId 用户ID
     * @return GitHub 信息，如果不存在则返回 null
     */
    GitHubInfo getGitHubInfo(Long userId);

    /**
     * 获取多个用户的 GitHub 信息
     *
     * @param userIds 用户ID列表
     * @return 用户ID到GitHub信息的映射
     */
    Map<Long, GitHubInfo> getGitHubInfos(List<Long> userIds);

}