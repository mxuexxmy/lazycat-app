package xyz.mxue.lazycatapp.model.response.comment;

import lombok.Data;
import xyz.mxue.lazycatapp.service.impl.AppServiceImpl;

@Data
public class CommentApiResponse {

    private int errorCode;
    private String message;
    private CommentData data;
    private boolean success;

}
