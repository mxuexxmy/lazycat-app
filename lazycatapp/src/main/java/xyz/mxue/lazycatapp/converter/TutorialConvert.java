package xyz.mxue.lazycatapp.converter;

import xyz.mxue.lazycatapp.entity.Tutorial;
import xyz.mxue.lazycatapp.model.response.tutorial.TutorialInfo;

import java.time.OffsetDateTime;

public class TutorialConvert {
    public static Tutorial convert(TutorialInfo tutorialInfo) {
        Tutorial tutorial = new Tutorial();
        tutorial.setId(tutorialInfo.getId());
        tutorial.setUid(tutorialInfo.getUid());
        tutorial.setTitle(tutorialInfo.getTitle());
        tutorial.setType(tutorialInfo.getType());
        tutorial.setCover(tutorialInfo.getCover());
        tutorial.setViews(tutorialInfo.getViews());
        tutorial.setThumbCount(tutorialInfo.getThumbCount());
        tutorial.setCommentCount(tutorialInfo.getCommentCount());
        tutorial.setCreatedAt(OffsetDateTime.parse(tutorialInfo.getCreatedAt()).toLocalDateTime());
        tutorial.setUpdatedAt(OffsetDateTime.parse(tutorialInfo.getUpdatedAt()).toLocalDateTime());
        tutorial.setContent(tutorialInfo.getContent());
        return tutorial;
    }
}
