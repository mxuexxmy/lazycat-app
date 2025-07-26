package xyz.mxue.lazycatapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.entity.App;
import xyz.mxue.lazycatapp.entity.CommunityUser;
import xyz.mxue.lazycatapp.entity.User;
import xyz.mxue.lazycatapp.repository.AppRepository;
import xyz.mxue.lazycatapp.repository.CommunityUserRepository;
import xyz.mxue.lazycatapp.repository.UserRepository;
import xyz.mxue.lazycatapp.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CommunityUserRepository communityUserRepository;
    private final AppRepository appRepository;

    @Override
    public User getUserInfo(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public CommunityUser getCommunityUser(Long userId) {
        return communityUserRepository.findById(userId).orElse(null);
    }

    /**
     * 获取所有用户个人信息
     *
     * @return 用户个人信息列表
     */
    @Override
    public List<User> getAllUserInfos() {
        return userRepository.findAll();
    }

    /**
     * 获取所有用户社区信息
     *
     * @return 用户社区信息列表
     */
    @Override
    public List<CommunityUser> getAllCommunityUsers() {
        return communityUserRepository.findAll();
    }

    @Override
    public long count() {
        return userRepository.count();
    }

    @Override
    public List<Map<String, Object>> getActiveUsers(int limit) {
        List<User> users = userRepository.findActiveUsers(PageRequest.of(0, limit));
        return users.stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("nickname", user.getNickname());
            userMap.put("avatar", user.getAvatar());
            // userMap.put("lastActive", user.getUpdatedAt());

            // 获取该用户的所有应用并计算总下载量
            List<App> userApps = appRepository.findByCreatorId(user.getId());
            int totalDownloads = userApps.stream().mapToInt(app -> app.getDownloadCount() != null ? app.getDownloadCount() : 0).sum();

            userMap.put("downloads", totalDownloads);
            return userMap;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getUserGrowthStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("daily", userRepository.countNewUsersToday());
        stats.put("weekly", userRepository.countNewUsersThisWeek());
        stats.put("monthly", userRepository.countNewUsersThisMonth());
        return stats;
    }

}
