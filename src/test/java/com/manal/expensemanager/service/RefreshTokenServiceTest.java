package com.manal.expensemanager.service;


import com.manal.expensemanager.auth.refresh.RefreshToken;
import com.manal.expensemanager.auth.refresh.RefreshTokenRepository;
import com.manal.expensemanager.auth.refresh.RefreshTokenService;
import com.manal.expensemanager.model.Role;
import com.manal.expensemanager.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    RefreshTokenRepository repo;

    @InjectMocks
    RefreshTokenService service;

    @Captor
    ArgumentCaptor<RefreshToken> tokenCaptor;

    User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .fullName("Alice Doe")
                .email("alice@test.io")
                .role(Role.USER)
                .password("x") // not used here
                .build();
    }

    @Test
    void create_shouldPersistWithGeneratedTokenAndExpiry() {
        // repo.save echoes what it gets but attaches an id
        when(repo.save(any(RefreshToken.class))).thenAnswer(inv -> {
            RefreshToken rt = inv.getArgument(0);
            rt.setId(100L);
            return rt;
        });

        RefreshToken saved = service.create(user);

        assertThat(saved.getId()).isEqualTo(100L);
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.isRevoked()).isFalse();
        assertThat(saved.getToken()).isNotBlank();
        // token looks like a UUID (best-effort)
        assertThatCode(() -> UUID.fromString(saved.getToken())).doesNotThrowAnyException();

        // expires in the future (at least a few minutes)
        assertThat(saved.getExpiresAt()).isAfter(Instant.now().plusSeconds(60));

        verify(repo).save(tokenCaptor.capture());
        RefreshToken toPersist = tokenCaptor.getValue();
        assertThat(toPersist.getUser()).isEqualTo(user);
        assertThat(toPersist.isRevoked()).isFalse();
        assertThat(toPersist.getToken()).isNotBlank();
        assertThat(toPersist.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void validateUsable_shouldReturnToken_whenNotRevokedAndNotExpired() {
        RefreshToken rt = RefreshToken.builder()
                .id(5L)
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        when(repo.findByToken(rt.getToken())).thenReturn(Optional.of(rt));

        RefreshToken result = service.validateUsable(rt.getToken());

        assertThat(result).isSameAs(rt);
        verify(repo).findByToken(rt.getToken());
    }

    @Test
    void validateUsable_shouldThrow_whenTokenNotFound() {
        when(repo.findByToken("nope")).thenReturn(Optional.empty());

        assertThatIllegalArgumentException()
                .isThrownBy(() -> service.validateUsable("nope"))
                .withMessageContaining("Invalid refresh token");
    }

    @Test
    void validateUsable_shouldThrow_whenExpired() {
        RefreshToken rt = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().minusSeconds(1))
                .revoked(false)
                .build();

        when(repo.findByToken(rt.getToken())).thenReturn(Optional.of(rt));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> service.validateUsable(rt.getToken()))
                .withMessageContaining("expired");
    }

    @Test
    void validateUsable_shouldThrow_whenRevoked() {
        RefreshToken rt = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(true)
                .build();

        when(repo.findByToken(rt.getToken())).thenReturn(Optional.of(rt));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> service.validateUsable(rt.getToken()))
                .withMessageContaining("revoked");
    }

    @Test
    void revoke_shouldMarkRevokedAndSave() {
        RefreshToken rt = RefreshToken.builder()
                .id(9L)
                .user(user)
                .token("T-123")
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        when(repo.findByToken("T-123")).thenReturn(Optional.of(rt));
        when(repo.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        service.revoke("T-123");

        verify(repo).findByToken("T-123");
        verify(repo).save(tokenCaptor.capture());

        RefreshToken saved = tokenCaptor.getValue();
        assertThat(saved.isRevoked()).isTrue();
        assertThat(saved.getId()).isEqualTo(9L);
    }
}

