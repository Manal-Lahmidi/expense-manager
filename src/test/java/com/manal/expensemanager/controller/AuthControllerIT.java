package com.manal.expensemanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manal.expensemanager.auth.dto.AuthRequest;
import com.manal.expensemanager.auth.dto.SignupRequest;
import com.manal.expensemanager.auth.refresh.RefreshTokenRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class AuthControllerIT extends PostgresITBase {

    @Autowired MockMvc mvc;
    @Autowired UserRepository userRepository;
    @Autowired RefreshTokenRepository refreshTokenRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired ObjectMapper om;

    private String json(Object o) throws Exception { return om.writeValueAsString(o); }

    @BeforeEach
    void clean() {
        // IMPORTANT: delete children first to satisfy FKs
        refreshTokenRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    void signup_shouldCreateUser_andReturnTokens() throws Exception {
        var req = SignupRequest.builder()
                .fullName("Alice Doe")
                .email("alice@test.io")
                .password("Password123")
                .role("USER")
                .build();

        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void login_shouldAuthenticateUser() throws Exception {
        userRepository.save(User.builder()
                .fullName("Alice Doe")
                .email("alice@test.io")
                .password(passwordEncoder.encode("Password123"))
                .role(Role.USER)
                .build());

        var req = AuthRequest.builder()
                .email("alice@test.io")
                .password("Password123")
                .build();

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }
}
