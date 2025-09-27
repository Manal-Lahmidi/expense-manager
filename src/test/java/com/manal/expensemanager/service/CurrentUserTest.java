package com.manal.expensemanager.service;

import com.manal.expensemanager.model.Role;
import com.manal.expensemanager.model.User;
import com.manal.expensemanager.repository.UserRepository;
import com.manal.expensemanager.security.CurrentUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

class CurrentUserTest {

    private UserRepository repo;
    private CurrentUser currentUser;

    @BeforeEach
    void setup() {
        repo = Mockito.mock(UserRepository.class);
        currentUser = new CurrentUser(repo);

        // put a fake authentication in the security context
        var auth = new UsernamePasswordAuthenticationToken("alice@test.io", "password");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void get_shouldReturnUserFromRepository() {
        var user = User.builder()
                .id(1L)
                .email("alice@test.io")
                .fullName("Alice Doe")
                .role(Role.USER)
                .build();
        given(repo.findByEmail("alice@test.io")).willReturn(Optional.of(user));

        var result = currentUser.get();

        assertThat(result.getEmail()).isEqualTo("alice@test.io");
        verify(repo).findByEmail("alice@test.io");
    }

    @Test
    void get_shouldThrow_whenUserNotFound() {
        given(repo.findByEmail("alice@test.io")).willReturn(Optional.empty());

        assertThatThrownBy(() -> currentUser.get())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Authenticated user not found");
    }

    @Test
    void id_shouldReturnUserId() {
        var user = User.builder().id(42L).email("alice@test.io").role(Role.USER).build();
        given(repo.findByEmail("alice@test.io")).willReturn(Optional.of(user));

        assertThat(currentUser.id()).isEqualTo(42L);
    }

    @Test
    void email_shouldReturnUserEmail() {
        var user = User.builder().id(42L).email("alice@test.io").role(Role.USER).build();
        given(repo.findByEmail("alice@test.io")).willReturn(Optional.of(user));

        assertThat(currentUser.email()).isEqualTo("alice@test.io");
    }
}
