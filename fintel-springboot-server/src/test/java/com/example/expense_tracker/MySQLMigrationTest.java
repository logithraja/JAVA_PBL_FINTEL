package com.example.expense_tracker;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:mysql://localhost:3306/fintel_test_migration_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
        "spring.datasource.username=root",
        "spring.datasource.password=9786@Raja",
        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect",
        "spring.flyway.enabled=true",
        "spring.flyway.clean-disabled=false"
})
public class MySQLMigrationTest {

    @Autowired(required = false)
    private Flyway flyway;

    @Test
    void testFlywayMigrationAgainstRealMySQL() {
        assertNotNull(flyway, "Flyway should be configured");
        var info = flyway.info().current();
        assertNotNull(info, "Migration should be applied");
        System.out.println("Current migration version on MySQL: " + info.getVersion());
        assertTrue(info.getState().isApplied(), "V1 migration must be applied");
    }
}
