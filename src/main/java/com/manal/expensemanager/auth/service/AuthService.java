package com.manal.expensemanager.auth.service;

import com.manal.expensemanager.auth.dto.AuthRequest;
import com.manal.expensemanager.auth.dto.AuthResponse;
import com.manal.expensemanager.auth.dto.SignupRequest;
import com.manal.expensemanager.model.Role;
import com.manal.expensemanager.model.User;
import com.manal.expensemanager.repository.UserRepository;
import com.manal.expensemanager.security.jwt.JwtService;
import com.manal.expensemanager.auth.refresh.RefreshToken;
import com.manal.expensemanager.auth.refresh.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        Role role = Role.valueOf(request.getRole().toUpperCase());

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        User saved = userRepository.save(user);

        String access = jwtService.generateToken(saved.getEmail());
        RefreshToken rt = refreshTokenService.create(saved);

        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(rt.getToken())
                .build();
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String access = jwtService.generateToken(user.getEmail());
        RefreshToken rt = refreshTokenService.create(user);

        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(rt.getToken())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(String refreshToken) {
        RefreshToken rt = refreshTokenService.validateUsable(refreshToken);
        String newAccess = jwtService.generateToken(rt.getUser().getEmail());

        // (optional) rotate RT for stronger security:
        // refreshTokenService.revoke(refreshToken);
        // String newRt = refreshTokenService.create(rt.getUser()).getToken();

        return AuthResponse.builder()
                .accessToken(newAccess)
                .refreshToken(refreshToken) // or newRt if you rotate
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }
}
