package com.example.online_bank.service;

import com.example.online_bank.domain.entity.RefreshToken;
import com.example.online_bank.domain.entity.TokenFamily;
import com.example.online_bank.enums.TokenStatus;
import com.example.online_bank.repository.RefreshTokenRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.online_bank.enums.TokenStatus.CREATED;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public void create(
            String tokenUuid,
            String token,
            TokenFamily tokenFamily,
            LocalDateTime expiredAt,
            LocalDateTime createdAt
    ) {
        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(encodeToken(token))
                .expiresAt(expiredAt)
                .revokedAt(null)
                .createdAt(createdAt)
                .status(CREATED)
                .uuid(tokenUuid)
                .family(tokenFamily)
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    private String encodeToken(String token) {
        return bCryptPasswordEncoder.encode(token);
    }

    @Transactional
    public void revoke(RefreshToken refreshToken) {
        refreshToken.setStatus(TokenStatus.REVOKED);
        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);
    }

    public void revokeAllByFamily(TokenFamily family) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByFamily(family);
        tokens.forEach(token -> {
            token.setRevokedAt(LocalDateTime.now());
            token.setStatus(TokenStatus.REVOKED);
            refreshTokenRepository.save(token);
        });
    }

    public RefreshToken findByUuid(String uuid) {
        return refreshTokenRepository.findByUuid(uuid).orElseThrow(() -> {
            log.error("Token with provided not found");
            return new EntityNotFoundException();
        });
    }
}
