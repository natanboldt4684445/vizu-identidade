package com.vizu.identidade.auth.dto;

public record TokenResponse(String accessToken, String refreshToken, long expiresIn) {
}
