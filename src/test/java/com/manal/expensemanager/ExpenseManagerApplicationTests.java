package com.manal.expensemanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                // Turn off anything that would try to touch a DB
                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration",
                // Stop Spring Boot 3.3 testcontainers auto-service-connection magic
                "spring.testcontainers.enabled=false"
        }
)
// DO NOT extend PostgresITBase here
// DO NOT set @ActiveProfiles here
class ExpenseManagerApplicationTests {

    @Test
    void contextLoads() {}
}
