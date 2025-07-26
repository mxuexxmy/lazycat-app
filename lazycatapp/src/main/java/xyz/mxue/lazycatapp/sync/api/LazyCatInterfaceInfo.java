package xyz.mxue.lazycatapp.sync.api;

public interface LazyCatInterfaceInfo {

    /**
     * 应用列表接口
     */
    String APP_LIST_URL = "https://appstore.api.lazycat.cloud/api/v3/user/app/list";

    /**
     * 分类列表接口 - 中文
     */
    String CATEGORY_URL_ZH = "https://dl.lazycatmicroserver.com/appstore/metarepo/zh/categories.json";

    /**
     * 分类列表接口 - 英文
     */
    String CATEGORY_URL_EN = "https://dl.lazycatmicroserver.com/appstore/metarepo/en/categories.json";

    /**
     * 用户列表接口
     */
    String DEVELOPER_LIST = "https://appstore.api.lazycat.cloud/api/v3/user/developer/list";

    /**
     * Github 列表接口
     */
    String GITHUB_INFO_URL = "https://playground.api.lazycat.cloud/api/github/info/";

    /**
     * 用户信息接口
     */
    String USER_INFO_URL = "https://playground.api.lazycat.cloud/api/user/info/";

    /**
     * 下载次数
     */
    String DOWNLOAD_COUNT_URL = "https://appstore.api.lazycat.cloud/api/counting";

    /**
     * 评分接口
     */
    String SCORE_API_URL = "https://appstore.api.lazycat.cloud/api/comment/score/";

    /**
     * 评论列表接口
     */
    String COMMENT_LIST_URL = "https://appstore.api.lazycat.cloud/api/comment/list/";

    /**
     * 动态接口
     * 例子：https://playground.api.lazycat.cloud/api/user/dynamic/332?size=5&sort=-createdAt&page=1
     */
    String DYNAMIC_URL = "https://playground.api.lazycat.cloud/api/user/dynamic/";



}
