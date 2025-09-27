package com.manal.expensemanager.auth.refresh;

import com.manal.expensemanager.model.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=200)
    private String token;

    @ManyToOne(optional=false)
    private User user;

    @Column(nullable=false)
    private Instant expiresAt;

    @Column(nullable=false)
    private boolean revoked;
}
