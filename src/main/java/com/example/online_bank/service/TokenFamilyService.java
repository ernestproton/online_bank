package com.example.online_bank.service;

import com.example.online_bank.domain.entity.TokenFamily;
import com.example.online_bank.domain.entity.TrustedDevice;
import com.example.online_bank.domain.entity.User;
import com.example.online_bank.repository.TokenFamilyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenFamilyService {
    private final TokenFamilyRepository tokenFamilyRepository;

    public void save(TokenFamily tokenFamily) {
        tokenFamilyRepository.save(tokenFamily);
    }

    public TokenFamily create(TrustedDevice trustedDevice, User user) {
        TokenFamily tokenFamily = TokenFamily.builder()
                .trustedDevice(trustedDevice)
                .build();
        save(tokenFamily);
        return tokenFamily;
    }
}
