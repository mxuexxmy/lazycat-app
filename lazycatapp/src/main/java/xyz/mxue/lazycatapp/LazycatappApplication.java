package xyz.mxue.lazycatapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import xyz.mxue.lazycatapp.service.CategoryService;

import java.io.File;
import java.nio.file.Paths;

@SpringBootApplication
@EnableScheduling
public class LazycatappApplication {

    public static void main(String[] args) {
        // 创建必要的目录
        createDirectories();
        SpringApplication.run(LazycatappApplication.class, args);
    }

    private static void createDirectories() {
        // 创建 data 目录
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        
        // 创建 logs 目录
        File logsDir = new File("logs");
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }
    }

    @Bean
    public CommandLineRunner initCategories(CategoryService categoryService) {
        return args -> {
            // 在应用启动时执行一次分类同步
            categoryService.updateChineseCategories();
            categoryService.updateEnglishCategories();
        };
    }
}
