package xyz.mxue.lazycatapp.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.repository.AppCommentRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppCommentSyncService {

    private final AppCommentRepository appCommentRepository;

    public void sync(String packageName) {
        log.info("开始同步评论 {}", packageName);
    }

}
