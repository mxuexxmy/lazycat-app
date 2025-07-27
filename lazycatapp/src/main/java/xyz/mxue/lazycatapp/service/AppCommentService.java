package xyz.mxue.lazycatapp.service;

import java.util.List;
import java.util.Map;

public interface AppCommentService {
    List<Map<String, Object>> getAllComments();

    List<Map<String, Object>> getLatestComments();
}
