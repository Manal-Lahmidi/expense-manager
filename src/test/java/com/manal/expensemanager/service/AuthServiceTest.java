package com.manal.expensemanager.service;


import com.manal.expensemanager.auth.dto.AuthRequest;
import com.manal.expensemanager.auth.dto.AuthResponse;
import com.manal.expensemanager.auth.dto.SignupRequest;
import com.manal.expensemanager.auth.refresh.RefreshToken;
import com.manal.expensemanager.auth.refresh.RefreshTokenService;
import com.manal.expensemanager.auth.service.AuthService;
import com.manal.expensemanager.model.Role;
import com.manal.expensemanager.model.User;
import com.manal.expensemanager.repository.UserRepository;
import com.manal.expensemanager.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private RefreshTokenService refreshTokenService;

    @InjectMocks private AuthService authService;

    // ====== SIGNUP ======

    @Test
    void signup_shouldCreateUser_andReturnTokens() {
        // given
        SignupRequest req = SignupRequest.builder()
                .fullName("Alice Doe")
                .email("alice@test.io")
                .password("secret123")
                .role("USER")
                .build();

        given(userRepository.existsByEmail("alice@test.io")).willReturn(false);
        given(passwordEncoder.encode("secret123")).willReturn("ENCODED");
        User saved = User.builder()
                .id(1L).fullName("Alice Doe").email("alice@test.io")
                .password("ENCODED").role(Role.USER)
                .build();
        given(userRepository.save(any(User.class))).willReturn(saved);
        given(jwtService.generateToken("alice@test.io")).willReturn("jwt-token");
        given(refreshTokenService.create(saved)).willReturn(
                RefreshToken.builder()
                        .id(10L)
                        .token("rt-123")
                        .user(saved)
                        .expiresAt(Instant.now().plusSeconds(3600))
                        .revoked(false)
                        .build()
        );

        // when
        AuthResponse res = authService.signup(req);

        // then
        assertThat(res.getAccessToken()).isEqualTo("jwt-token");
        assertThat(res.getRefreshToken()).isEqualTo("rt-123");
        verify(userRepository).save(any(User.class));
        verify(refreshTokenService).create(saved);
    }

    @Test
    void signup_shouldFail_whenEmailAlreadyUsed() {
        // given
        SignupRequest req = SignupRequest.builder()
                .fullName("Bob")
                .email("bob@test.io")
                .password("passw0rd")
                .role("USER")
                .build();
        given(userRepository.existsByEmail("bob@test.io")).willReturn(true);

        // expect
        assertThatThrownBy(() -> authService.signup(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already in use");

        verify(userRepository, never()).save(any());
    }

    // ====== LOGIN ======

    @Test
    void login_shouldAuthenticate_andReturnTokens() {
        // given
        AuthRequest req = AuthRequest.builder()
                .email("alice@test.io")
                .password("secret123")
                .build();

        // authentication succeeds
        given(authenticationManager.authenticate(any()))
                .willReturn(new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

        User user = User.builder()
                .id(1L).email("alice@test.io").password("ENC").role(Role.USER).fullName("Alice")
                .build();
        given(userRepository.findByEmail("alice@test.io")).willReturn(Optional.of(user));
        given(jwtService.generateToken("alice@test.io")).willReturn("jwt2");
        given(refreshTokenService.create(user))
                .willReturn(RefreshToken.builder().token("rt2").user(user).expiresAt(Instant.now().plusSeconds(3600)).revoked(false).build());

        // when
        AuthResponse res = authService.login(req);

        // then
        assertThat(res.getAccessToken()).isEqualTo("jwt2");
        assertThat(res.getRefreshToken()).isEqualTo("rt2");
        verify(authenticationManager).authenticate(any());
        verify(userRepository).findByEmail("alice@test.io");
    }

    @Test
    void login_shouldThrowBadCredentials_whenAuthenticationFails() {
        // given
        AuthRequest req = AuthRequest.builder()
                .email("nope@test.io")
                .password("bad")
                .build();

        // simulate any underlying failure
        given(authenticationManager.authenticate(any())).willThrow(new RuntimeException("x"));

        // expect
        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
    }

    // ====== REFRESH ======

    @Test
    void refresh_shouldReturnNewAccessToken() {
        // given
        User user = User.builder().id(1L).email("alice@test.io").role(Role.USER).password("ENC").fullName("Alice").build();
        RefreshToken rt = RefreshToken.builder()
                .token("rt-abc")
                .user(user)
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        given(refreshTokenService.validateUsable("rt-abc")).willReturn(rt);
        given(jwtService.generateToken("alice@test.io")).willReturn("new-access");

        // when
        AuthResponse res = authService.refresh("rt-abc");

        // then
        assertThat(res.getAccessToken()).isEqualTo("new-access");
        assertThat(res.getRefreshToken()).isEqualTo("rt-abc"); // unchanged (no rotation)
        verify(refreshTokenService).validateUsable("rt-abc");
    }

    // ====== LOGOUT ======

    @Test
    void logout_shouldRevokeRefreshToken() {
        // when
        authService.logout("rt-to-revoke");

        // then
        verify(refreshTokenService).revoke("rt-to-revoke");
    }
}
