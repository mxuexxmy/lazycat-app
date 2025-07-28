package xyz.mxue.lazycatapp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SyncTypeEnum {

    /**
     * 应用
     */
    APP("APP", "应用", "应用信息同步，应用会关联同步应用评论、应用评分"),

    /**
     * 分类
     */
    CATEGORY("CATEGORY", "分类", "应用分类信息同步"),

    /**
     * 用户
     */
    USER("USER", "用户", "用户信息同步、用户会关联同步社区用户信息、GitHub 信息、攻略信息"),
    ;

    private final String code;
    private final String name;
    private final String desc;

    public static String getNameByCode(String code) {
        for (SyncTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value.name;
            }
        }
        return null;
    }
}
