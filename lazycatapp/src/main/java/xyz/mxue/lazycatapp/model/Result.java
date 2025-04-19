package xyz.mxue.lazycatapp.model;

import lombok.Data;

@Data
public class Result {
    private boolean success;
    private String message;
    private Object data;

    private Result(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static Result success(Object data) {
        return new Result(true, "success", data);
    }

    public static Result error(String message) {
        return new Result(false, message, null);
    }
} 