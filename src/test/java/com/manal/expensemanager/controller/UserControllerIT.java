package com.manal.expensemanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manal.expensemanager.dto.UserRequestDTO;
import com.manal.expensemanager.model.Role;
import com.manal.expensemanager.model.User;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserController (admin-only endpoints).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIT extends PostgresITBase {

    @Autowired MockMvc mvc;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder encoder;
    @Autowired ObjectMapper om;

    @BeforeEach
    void clean() {
        userRepository.deleteAllInBatch();
    }

    @Test
    @WithMockUser(roles = "USER")
    void list_shouldBeForbidden_forNonAdmin() throws Exception {
        mvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void list_shouldReturnAll_whenAdmin() throws Exception {
        userRepository.save(User.builder()
                .fullName("Alice Doe").email("alice@test.io")
                .password(encoder.encode("Password123")).role(Role.USER).build());
        userRepository.save(User.builder()
                .fullName("Bob Roe").email("bob@test.io")
                .password(encoder.encode("Password123")).role(Role.USER).build());

        mvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.email=='alice@test.io')]").exists())
                .andExpect(jsonPath("$[?(@.email=='bob@test.io')]").exists());
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_shouldBeForbidden_forNonAdmin() throws Exception {
        var dto = UserRequestDTO.builder()
                .fullName("Charlie")
                .email("charlie@test.io")
                .role("USER")
                .build();

        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

}
