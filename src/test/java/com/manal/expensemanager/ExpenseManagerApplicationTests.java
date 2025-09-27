package com.manal.expensemanager;

import com.manal.expensemanager.testsupport.PostgresITBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ExpenseManagerApplicationTests extends PostgresITBase {

    @Test
    void contextLoads() {
        // boots the Spring context with the Testcontainers Postgres from PostgresITBase
    }
}
