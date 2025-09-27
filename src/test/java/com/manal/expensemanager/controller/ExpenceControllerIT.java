package com.manal.expensemanager.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.*;
import org.springframework.http.HttpHeaders;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manal.expensemanager.model.Category;
import com.manal.expensemanager.model.Expense;
import com.manal.expensemanager.model.Role;
import com.manal.expensemanager.model.User;
import com.manal.expensemanager.repository.CategoryRepository;
import com.manal.expensemanager.repository.ExpenseRepository;
import com.manal.expensemanager.repository.UserRepository;
import com.manal.expensemanager.testsupport.PostgresITBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class ExpenseControllerIT extends PostgresITBase {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @Autowired ExpenseRepository expenseRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder encoder;

    User alice; // USER
    User bob;   // USER
    Category food;
    Category travel;

    @BeforeEach
    void setup() {
        expenseRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        alice = userRepository.save(User.builder()
                .fullName("Alice Doe").email("alice@test.io")
                .password(encoder.encode("Password123")).role(Role.USER).build());
        bob = userRepository.save(User.builder()
                .fullName("Bob Roe").email("bob@test.io")
                .password(encoder.encode("Password123")).role(Role.USER).build());

        food = categoryRepository.save(Category.builder().name("Food").build());
        travel = categoryRepository.save(Category.builder().name("Travel").build());

        // Alice: 2 expenses (Aug & Sep)
        expenseRepository.save(Expense.builder()
                .title("Lunch").amount(15.0).date(LocalDate.of(2025, 8, 22))
                .user(alice).category(food).build());
        expenseRepository.save(Expense.builder()
                .title("Train").amount(35.0).date(LocalDate.of(2025, 9, 2))
                .user(alice).category(travel).build());

        // Bob: 1 expense (Sep)
        expenseRepository.save(Expense.builder()
                .title("Groceries").amount(60.0).date(LocalDate.of(2025, 9, 5))
                .user(bob).category(food).build());
    }

    @Test
    @WithMockUser(username = "alice@test.io", roles = "USER")
    void create_shouldCreate_forAuthenticatedUser() throws Exception {
        var payload = """
                {
                  "title":"Coffee",
                  "description":"morning",
                  "amount": 3.5,
                  "date":"2025-09-10",
                  "categoryId": %d
                }
                """.formatted(food.getId());

        mvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Coffee"));

        assertThat(expenseRepository.findByUser(alice))
                .extracting(Expense::getTitle)
                .anyMatch("Coffee"::equals);
    }

    @Test
    @WithMockUser(username = "alice@test.io", roles = "USER")
    void getMine_shouldReturnOnlyCurrentUsersExpenses() throws Exception {
        mvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)) // Alice has 2 from setup
                .andExpect(jsonPath("$[?(@.title=='Lunch')]").exists())
                .andExpect(jsonPath("$[?(@.title=='Train')]").exists());
    }

    @Test
    @WithMockUser(username = "alice@test.io", roles = "USER")
    void monthly_shouldReturnPagedStats_forCurrentUser() throws Exception {
        mvc.perform(get("/api/expenses/monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[?(@.month=='2025-08')]").exists())
                .andExpect(jsonPath("$.content[?(@.month=='2025-09')]").exists());
    }

    @Test
    @WithMockUser(username = "admin@test.io", roles = "ADMIN")
    void adminMonthly_shouldReturnStats_forRequestedUser() throws Exception {
        // ask for Bob’s stats
        mvc.perform(get("/api/expenses/admin/monthly")
                        .param("userId", bob.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].month").value("2025-09"));
    }

    @Test
    @WithMockUser(username = "alice@test.io", roles = "USER")
    void adminMonthly_shouldBeForbidden_forNonAdmin() throws Exception {
        mvc.perform(get("/api/expenses/admin/monthly")
                        .param("userId", bob.getId().toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "alice@test.io", roles = "USER")
    void total_shouldReturnSum_forCurrentUser() throws Exception {
        mvc.perform(get("/api/expenses/total"))
                .andExpect(status().isOk())
                .andExpect(content().string("50.0")); // 15 + 35
    }

    @Test
    @WithMockUser(username = "admin@test.io", roles = "ADMIN")
    void adminTotal_shouldReturnSum_forRequestedUser() throws Exception {
        mvc.perform(get("/api/expenses/admin/total").param("userId", bob.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("60.0"));
    }

    @Test
    @WithMockUser(username = "alice@test.io", roles = "USER")
    void byCategory_shouldReturnPaged_forCurrentUser() throws Exception {
        var result = mvc.perform(get("/api/expenses/by-category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(2))) // Food + Travel
                .andReturn();

        // quick smoke on names & amounts without relying on exact sort order
        var body = result.getResponse().getContentAsString();
        assertThat(body).contains("Food");
        assertThat(body).contains("Travel");
        assertThat(body).contains("15");  // Food total
        assertThat(body).contains("35");  // Travel total
    }

    @Test
    @WithMockUser(username = "admin@test.io", roles = "ADMIN")
    void adminByCategory_shouldReturnPaged_forRequestedUser() throws Exception {
        var result = mvc.perform(get("/api/expenses/admin/by-category")
                        .param("userId", bob.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(1))) // Bob only has Food
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThat(body).contains("Food");
        assertThat(body).contains("60");
    }


    @Test
    @WithMockUser(username = "alice@test.io", roles = "USER")
    void annualExport_shouldReturnCsv_forCurrentUser() throws Exception {
        var result = mvc.perform(get("/api/expenses/annual/export"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("attachment; filename=annual-stats.csv")))
                .andExpect(content().contentType("text/csv"))
                .andReturn();

        var csv = result.getResponse().getContentAsString();
        assertThat(csv).contains("Year,Total");
        assertThat(csv).contains("2025,50.00");
    }

    @Test
    @WithMockUser(username = "admin@test.io", roles = "ADMIN")
    void adminAnnualExport_shouldReturnCsv_forRequestedUser() throws Exception {
        var result = mvc.perform(get("/api/expenses/admin/annual/export")
                        .param("userId", bob.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        allOf(containsString("attachment;"),
                                containsString("annual-stats-" + bob.getId() + ".csv"))))
                .andExpect(content().contentType("text/csv"))
                .andReturn();

        var csv = result.getResponse().getContentAsString();
        assertThat(csv).contains("Year,Total");
        assertThat(csv).contains("2025,60.00");
    }

    @Test
    @WithMockUser(username = "alice@test.io", roles = "USER")
    void adminEndpoints_shouldBeForbidden_forNonAdmin_onTotalsAndAnnualAndByCategory() throws Exception {
        mvc.perform(get("/api/expenses/admin/total").param("userId", bob.getId().toString()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/expenses/admin/by-category").param("userId", bob.getId().toString()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/expenses/admin/annual").param("userId", bob.getId().toString()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/expenses/admin/annual/export").param("userId", bob.getId().toString()))
                .andExpect(status().isForbidden());
    }
}
