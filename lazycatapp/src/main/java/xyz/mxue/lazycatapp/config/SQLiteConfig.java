package xyz.mxue.lazycatapp.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Slf4j
@Component
public class SQLiteConfig {

    private final DataSource dataSource;

    public SQLiteConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void enableWALMode() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            log.info("Current journal mode: {}",
                    stmt.executeQuery("PRAGMA journal_mode;").getString(1));

            stmt.execute("PRAGMA journal_mode=WAL;");
            stmt.execute("PRAGMA synchronous=NORMAL;");
            stmt.execute("PRAGMA wal_autocheckpoint=1000;");
            stmt.execute("PRAGMA busy_timeout=5000;");

            log.info("WAL mode enabled successfully");
        } catch (Exception e) {
            log.error("Failed to enable WAL mode", e);
        }
    }
}