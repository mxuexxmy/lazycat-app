package xyz.mxue.lazycatapp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SyncStrategyEnum {

    FULL("FULL", "全量"),
    INCREMENTAL("INCREMENTAL", "增量");;

    private final String code;
    private final String name;

    public static String getNameByCode(String code) {
        for (SyncStrategyEnum value : values()) {
            if (value.code.equals(code)) {
                return value.name;
            }
        }
        return null;
    }
}
