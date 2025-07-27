package xyz.mxue.lazycatapp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SyncStatusEnum {
    // 开始
    START(1, "开始"),

    // 同步中
    SYNCING(2, "同步中"),

    // 同步完成
    COMPLETE(3, "同步完成"),

    // 同步失败
    FAILED(4, "同步失败"),
    ;

    private final int code;

    private final String name;
}
