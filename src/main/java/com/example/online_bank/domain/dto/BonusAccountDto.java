package com.example.online_bank.domain.dto;

import java.math.BigDecimal;

public record BonusAccountDto(
        String accountNumber,
        BigDecimal points
) {
}
