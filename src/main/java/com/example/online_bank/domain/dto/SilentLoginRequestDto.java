package com.example.online_bank.domain.dto;

public record SilentLoginRequestDto(
        String refreshToken,
        String deviceId
) {
}
