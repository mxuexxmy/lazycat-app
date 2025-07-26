package xyz.mxue.lazycatapp.service;

import xyz.mxue.lazycatapp.entity.CommunityUser;
import xyz.mxue.lazycatapp.entity.User;

import java.util.List;
import java.util.Map;


public interface UserService {


    User getUserInfo(Long userId);

    CommunityUser getCommunityUser(Long userId);

    /**
     * 获取所有用户个人信息
     *
     * @return 用户个人信息列表
     */
    List<User> getAllUserInfos();

    /**
     * 获取所有用户社区信息
     *
     * @return 用户社区信息列表
     */
    List<CommunityUser> getAllCommunityUsers();

    long count();

    List<Map<String, Object>> getActiveUsers(int limit);

    Map<String, Object> getUserGrowthStats();

} 