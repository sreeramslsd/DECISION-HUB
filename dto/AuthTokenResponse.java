package com.decisionhub.dto;

public record AuthTokenResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn
) {}
