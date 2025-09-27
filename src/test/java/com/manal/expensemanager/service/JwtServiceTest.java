package com.manal.expensemanager.service;

import com.manal.expensemanager.security.jwt.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    @Test
    void generateToken_and_extractUsername_shouldWork() {
        String token = jwtService.generateToken("alice@test.io");

        String extracted = jwtService.extractUsername(token);

        assertThat(extracted).isEqualTo("alice@test.io");
    }

    @Test
    void isTokenValid_shouldReturnTrue_forValidToken() {
        String token = jwtService.generateToken("bob@test.io");

        boolean valid = jwtService.isTokenValid(token, "bob@test.io");

        assertThat(valid).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalse_forWrongUser() {
        String token = jwtService.generateToken("bob@test.io");

        boolean valid = jwtService.isTokenValid(token, "alice@test.io");

        assertThat(valid).isFalse();
    }

    @Test
    void extractClaim_shouldReturnSubject() {
        String token = jwtService.generateToken("carol@test.io");

        String sub = jwtService.extractClaim(token, c -> c.getSubject());

        assertThat(sub).isEqualTo("carol@test.io");
    }

    @Test
    void expiredToken_shouldThrowException() {
        // create a token with 1 ms expiry
        JwtService shortLived = new JwtService() {
            @Override
            public String generateToken(String email) {
                return io.jsonwebtoken.Jwts.builder()
                        .setSubject(email)
                        .setExpiration(new java.util.Date(System.currentTimeMillis() - 1000)) // already expired
                        .signWith(getSignInKey(), io.jsonwebtoken.SignatureAlgorithm.HS256)
                        .compact();
            }
        };

        String token = shortLived.generateToken("expired@test.io");

        assertThatThrownBy(() -> shortLived.extractUsername(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
