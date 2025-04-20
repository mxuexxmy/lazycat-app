package xyz.mxue.lazycatapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import javax.sql.DataSource;

@Configuration
public class SQLiteDatabaseConfig {
    @Bean
    public DataSource dataSource() {
        SQLiteConfig config = new SQLiteConfig();
        config.setPragma(SQLiteConfig.Pragma.JOURNAL_MODE, "WAL");
        config.setPragma(SQLiteConfig.Pragma.SHARED_CACHE, "true");
        
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setConfig(config);
        dataSource.setUrl("jdbc:sqlite:./data/lazycatapp.db");
        return dataSource;
    }
} 