package xyz.mxue.lazycatapp.converter;

import xyz.mxue.lazycatapp.entity.CommunityUser;
import xyz.mxue.lazycatapp.entity.User;
import xyz.mxue.lazycatapp.model.response.community.UserResponse;

public class CommunityUserConvert {

   public static CommunityUser convert(UserResponse userResponse) {
      CommunityUser communityUser = new CommunityUser();

       communityUser.setUid(userResponse.getUid());
       communityUser.setReceiveThumbs(userResponse.getReceiveThumbs());
       communityUser.setFollows(userResponse.getFollows());
       communityUser.setFans(userResponse.getFans());
       communityUser.setFollowed(userResponse.getFollowed());
       communityUser.setGuidelineCounts(userResponse.getGuidelineCounts());
       communityUser.setHideFollows(userResponse.getHideFollows());
       communityUser.setHideFans(userResponse.getHideFans());
       communityUser.setHideThumbs(userResponse.getHideThumbs());
       communityUser.setOnlyFollowedAtMe(userResponse.getOnlyFollowedAtMe());
       communityUser.setOnlyFollowedCommentMe(userResponse.getOnlyFollowedCommentMe());

      return communityUser;
   }

   public static void convert(UserResponse userResponse, User userInfo) {
       userInfo.setId(userResponse.getUser().getId());
       userInfo.setUsername(userResponse.getUser().getUsername());
       userInfo.setNickname(userResponse.getUser().getNickname());
       userInfo.setAvatar(userResponse.getUser().getAvatar());
       userInfo.setDescription(userResponse.getUser().getDescription());
       userInfo.setGithubUsername(userResponse.getUser().getGithubUsername());
   }

}
