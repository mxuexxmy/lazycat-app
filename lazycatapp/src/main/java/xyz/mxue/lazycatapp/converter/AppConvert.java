package xyz.mxue.lazycatapp.converter;

import xyz.mxue.lazycatapp.entity.App;
import xyz.mxue.lazycatapp.model.response.app.AppItemInfo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppConvert {

    public static App convert(AppItemInfo appItemInfo) {
        App app = new App();

        app.setId(appItemInfo.getId());
        app.setPackageName(appItemInfo.getPackageName());
        app.setName(appItemInfo.getInformation().getName());
        app.setPkgPath(appItemInfo.getVersion().getPkgPath());
        app.setPkgHash(appItemInfo.getVersion().getPkgHash());
        app.setDescription(appItemInfo.getInformation().getDescription());
        app.setBrief(appItemInfo.getInformation().getBrief());
        app.setKindIds(appItemInfo.getKindIds());
        app.setIconPath(appItemInfo.getVersion().getIconPath());
        app.setKeywords(appItemInfo.getInformation().getKeywords());
        app.setVersion(appItemInfo.getVersion().getName());
        app.setCreator(appItemInfo.getInformation().getCreateUserId());
        app.setCreatorId(appItemInfo.getInformation().getCreateUserId());
        app.setUpdateId(appItemInfo.getVersion().getCreateUserId());
        app.setAuthor(appItemInfo.getInformation().getSourceAuthor());
        app.setPrice("0");
        app.setSource(appItemInfo.getInformation().getSource());
        app.setDownloadCount(appItemInfo.getCount().getDownloads());
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 应用创建时间
        app.setAppCreateTime(LocalDateTime.parse(appItemInfo.getCreatedAt(), timeFormatter));
        // 应用更新时间
        app.setAppUpdateTime(LocalDateTime.parse(appItemInfo.getUpdatedAt(), timeFormatter));

        return app;
    }

}
