package xyz.mxue.lazycatapp.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.mxue.lazycatapp.converter.AppScoreConvert;
import xyz.mxue.lazycatapp.entity.AppScore;
import xyz.mxue.lazycatapp.model.response.app.AppItemInfo;
import xyz.mxue.lazycatapp.repository.AppScoreRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppScoreSyncService {

    private final AppScoreRepository appScoreRepository;

    public void syncAppScore(AppItemInfo appItemInfo) {
        AppScore appScore = AppScoreConvert.convert(appItemInfo);
        appScoreRepository.save(appScore);
        log.info("同步得分: {}", appItemInfo);
    }

}
