package xyz.mxue.lazycatapp.model;

import lombok.Data;

@Data
public class R<T> {

    private boolean success;
    private String message;
    private T data;

    private R(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> R<T> success(T data) {
        return new R<>(true, "success", data);
    }

    public static <T> R<T> error(String message) {
        return new R<>(false, message, null);
    }

    public static <T> R<T> error(String message, Throwable t) {
        return new R<>(false, message, null);
    }
} 