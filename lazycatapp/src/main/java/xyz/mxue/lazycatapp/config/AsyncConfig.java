package xyz.mxue.lazycatapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AsyncConfig {

    @Bean(name = "taskExecutor") // Bean 名称用于 @Async 指定
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(8);
        // 最大线程数
        executor.setMaxPoolSize(16);
        // 队列容量
        executor.setQueueCapacity(200);
        // 线程空闲存活时间（秒）
        executor.setKeepAliveSeconds(60);
        // 线程名前缀
        executor.setThreadNamePrefix("Async-Executor-");
        // 拒绝策略：由调用线程处理该任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 初始化
        executor.initialize();
        return executor;
    }

}
