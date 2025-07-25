package xyz.mxue.lazycatapp.job;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class Job {

    @Value("${custom.job}")
    private Boolean jobEnable;

    public void syncApps() {
        // 获取所有App
        // 遍历App，同步App信息
        // 遍历App，同步App评分
        // 遍历App，同步App评论
        // 遍历App，同步App下载数
        // 遍历App，同步App分类
        // 遍历App，同步App开发者信息
        // 遍历App，同步App开发者评分
        // 遍历App，同步App开发者评论
        // 遍历App，同步App开发者下载数
        // 遍历App，同步App开发者分类
    }

}
