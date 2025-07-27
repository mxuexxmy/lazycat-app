package xyz.mxue.lazycatapp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SyncTypeEnum {

    /**
     * 应用
     */
    APP("APP", "应用"),

    /**
     * 分类
     */
    CATEGORY("CATEGORY", "分类"),

    /**
     * 用户
     */
    USER("USER", "用户"),
    ;

    private final String code;
    private final String name;

    public static String getNameByCode(String code) {
        for (SyncTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value.name;
            }
        }
        return null;
    }
}
