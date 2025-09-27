package com.manal.expensemanager.auth.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
}
