package xyz.mxue.lazycatapp.converter;

import xyz.mxue.lazycatapp.entity.AppScore;
import xyz.mxue.lazycatapp.model.response.app.AppItemInfo;

import java.time.LocalDateTime;

public class AppScoreConvert {

    public static AppScore convert(AppItemInfo appItemInfo)
    {
        AppScore appScore = new AppScore();
        appScore.setPkgId(appItemInfo.getPackageName());
        appScore.setScore(appItemInfo.getRating().getScore());
        appScore.setTotalReviews(appItemInfo.getRating().getStatistics().getTotal());
        appScore.setOneStarCount(appItemInfo.getRating().getStatistics().getOne());
        appScore.setTwoStarCount(appItemInfo.getRating().getStatistics().getTwo());
        appScore.setThreeStarCount(appItemInfo.getRating().getStatistics().getThree());
        appScore.setFourStarCount(appItemInfo.getRating().getStatistics().getFour());
        appScore.setFiveStarCount(appItemInfo.getRating().getStatistics().getFive());
        appScore.setTotalReviews(appItemInfo.getRating().getStatistics().getTotal());
        appScore.setLastSyncTime(LocalDateTime.now());

        return appScore;
    }

}
