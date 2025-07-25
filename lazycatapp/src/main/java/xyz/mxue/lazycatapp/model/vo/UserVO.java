package xyz.mxue.lazycatapp.model.vo;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.Data;
import xyz.mxue.lazycatapp.entity.CommunityUser;
import xyz.mxue.lazycatapp.entity.User;

@ApiResponse(description = "用户信息")
@Data
public class UserVO {

    private User userInfo;

    private CommunityUser communityUser;

}
