package xyz.mxue.lazycatapp.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import xyz.mxue.lazycatapp.entity.CommunityUser;
import xyz.mxue.lazycatapp.entity.User;

@Schema(description = "用户信息VO")
@Data
public class UserVO {

    @Schema(description = "用户信息")
    private User userInfo;

    @Schema(description = "用户社区信息")
    private CommunityUser communityUser;

}
