package com.manal.expensemanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.testcontainers.enabled=false",
                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration"
        }
)
class ExpenseManagerApplicationTests {
    @org.springframework.boot.test.mock.mockito.MockBean
    com.manal.expensemanager.repository.UserRepository userRepository;
    @org.junit.jupiter.api.Test void contextLoads() {}
}
