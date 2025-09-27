package com.manal.expensemanager.testsupport;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Shared DB config for Integration Tests.
 * - Local (default): Testcontainers Postgres
 * - CI: external Postgres (provided by GitHub Actions service)
 */
@ActiveProfiles("test")
public abstract class PostgresITBase {

    private static PostgreSQLContainer<?> POSTGRES;
    private static final boolean USE_EXTERNAL =
            System.getenv("IT_DB_URL") != null;  // set only in CI

    @BeforeAll
    static void start() {
        if (!USE_EXTERNAL) {
            POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");
            POSTGRES.start();
        }
    }

    @AfterAll
    static void stop() {
        if (POSTGRES != null) {
            POSTGRES.stop();
        }
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry r) {
        if (USE_EXTERNAL) {
            r.add("spring.datasource.url", () -> System.getenv("IT_DB_URL"));
            r.add("spring.datasource.username", () -> System.getenv("IT_DB_USERNAME"));
            r.add("spring.datasource.password", () -> System.getenv("IT_DB_PASSWORD"));
        } else {
            r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
            r.add("spring.datasource.username", POSTGRES::getUsername);
            r.add("spring.datasource.password", POSTGRES::getPassword);
        }

        // sensible test defaults
        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        r.add("spring.jpa.show-sql", () -> "false");
        r.add("spring.test.database.replace", () -> "NONE");
    }
}
