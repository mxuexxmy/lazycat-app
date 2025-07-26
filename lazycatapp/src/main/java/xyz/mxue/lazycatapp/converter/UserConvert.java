package xyz.mxue.lazycatapp.converter;

import xyz.mxue.lazycatapp.entity.User;
import xyz.mxue.lazycatapp.model.response.user.UserInfo;

public class UserConvert {

    public static User convert(UserInfo userInfo) {
        User result = new User();
        result.setId(userInfo.getId());
        result.setDeveloperId(userInfo.getDeveloperId());
        result.setNickname(userInfo.getNickname());
        result.setUsername(userInfo.getUsername());
        result.setDescription(userInfo.getDescription());
        result.setAvatar(userInfo.getAvatar());
        result.setGithubUsername(userInfo.getGithubUsername());
        result.setContinuousSubmissionDayCount(userInfo.getContinuousSubmissionDayCount());
        result.setIsOfficial(userInfo.getIsOfficial());
        result.setAppTotalCount(userInfo.getAppTotalCount());
        return result;
    }

}
