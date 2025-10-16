package com.manal.expensemanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        // don’t auto-configure a DataSource for this smoke test
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
class ExpenseManagerApplicationTests {

    @Test
    void contextLoads() {}
}
