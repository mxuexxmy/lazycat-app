package xyz.mxue.lazycatapp.model.response.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CommentData {

    private List<Comment> list;

    @JsonProperty("isNext")
    private boolean isNext;

}
