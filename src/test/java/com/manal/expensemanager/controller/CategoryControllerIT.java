package com.manal.expensemanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manal.expensemanager.model.Category;
import com.manal.expensemanager.repository.CategoryRepository;
import com.manal.expensemanager.repository.ExpenseRepository;
import com.manal.expensemanager.testsupport.PostgresITBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class CategoryControllerIT extends PostgresITBase {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @Autowired CategoryRepository categoryRepository;
    @Autowired ExpenseRepository expenseRepository;

    @BeforeEach
    void clean() {
        expenseRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
    }

    @Test
    @WithMockUser // any authenticated user
    void list_shouldReturnAll_forAnyAuthenticatedUser() throws Exception {
        categoryRepository.save(Category.builder().name("Food").build());
        categoryRepository.save(Category.builder().name("Travel").build());

        mvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.name=='Food')]").exists())
                .andExpect(jsonPath("$[?(@.name=='Travel')]").exists());
    }

    @Test
    @WithMockUser // creation currently allowed (no admin guard)
    void create_shouldReturn201_andBody() throws Exception {
        mvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Food\"}"))
                .andExpect(status().isCreated()) // 201
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Food"));

        assertThat(categoryRepository.existsByName("Food")).isTrue();
    }
}
