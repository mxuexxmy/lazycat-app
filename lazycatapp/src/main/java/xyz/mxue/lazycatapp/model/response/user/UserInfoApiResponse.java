package xyz.mxue.lazycatapp.model.response.user;

import lombok.Data;

import java.util.List;

@Data
public class UserInfoApiResponse {

    /**
     * 用户信息集合
     */
    private List<UserInfo> items;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页数量
     */
    private Integer size;

    /**
     * 总数
     */
    private Integer total;

}
