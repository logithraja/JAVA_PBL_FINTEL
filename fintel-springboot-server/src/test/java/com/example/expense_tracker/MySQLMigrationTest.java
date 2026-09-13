package com.example.expense_tracker;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.EnabledIf;

import java.net.InetSocketAddress;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates Flyway database migrations directly against a live MySQL instance.
 * Automatically runs if a local MySQL instance is reachable on port 3306;
 * skips gracefully if no live MySQL instance is listening.
 */
@EnabledIf(expression = "#{T(com.example.expense_tracker.MySQLMigrationTest).isMySQLAvailable()}", loadContext = false)
@SpringBootTest(properties = {
        "spring.datasource.url=${MYSQL_TEST_URL:jdbc:mysql://localhost:3306/fintel_test_migration_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}",
        "spring.datasource.username=${MYSQL_TEST_USER:root}",
        "spring.datasource.password=${MYSQL_TEST_PASSWORD:9786@Raja}",
        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect",
        "spring.flyway.enabled=true",
        "spring.flyway.clean-disabled=false"
})
public class MySQLMigrationTest {

    private static final Logger log = LoggerFactory.getLogger(MySQLMigrationTest.class);

    @Autowired(required = false)
    private Flyway flyway;

    public static boolean isMySQLAvailable() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", 3306), 600);
            return true;
        } catch (Exception e) {
            log.info("No live MySQL instance detected on localhost:3306. Skipping MySQLMigrationTest.");
            return false;
        }
    }

    @Test
    void testFlywayMigrationAgainstRealMySQL() {
        assertNotNull(flyway, "Flyway should be configured");
        var info = flyway.info().current();
        assertNotNull(info, "Migration should be applied");
        log.info("Current migration version on MySQL: {}", info.getVersion().getVersion());
        assertTrue(info.getState().isApplied(), "Migrations must be applied");
        assertTrue(info.getVersion().compareTo(org.flywaydb.core.api.MigrationVersion.fromVersion("1")) >= 0);
    }
}
