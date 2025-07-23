package xyz.mxue.lazycatapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class LazyCatAppApplication {

    public static void main(String[] args) {
        // 创建必要的目录
        createDirectories();
        SpringApplication.run(LazyCatAppApplication.class, args);
    }

    private static void createDirectories() {
        // 创建 data 目录
        File dataDir = new File("data");

        if (!dataDir.exists()) {
           boolean mkdirSuccess =  dataDir.mkdirs();
           if (mkdirSuccess) {
               log.info("创建 data 目录成功");
           } else {
               log.info("创建 data 目录失败");
           }
        }
        
        // 创建 logs 目录
        File logsDir = new File("logs");
        if (!logsDir.exists()) {
            boolean mkdirSuccess = logsDir.mkdirs();
            if (mkdirSuccess) {
                log.info("创建 logs 目录成功");
            } else {
                log.info("创建 logs 目录失败");
            }

        }
    }
}
