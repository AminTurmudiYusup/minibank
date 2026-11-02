package com.example.minibank.auth.dto;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}
