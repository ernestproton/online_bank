package com.example.online_bank.controller;

import com.example.online_bank.domain.dto.RegenerateVerifiedCodeDto;
import com.example.online_bank.service.VerificationManager;
import com.example.online_bank.service.domain.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/verification-code")
@RequiredArgsConstructor
public class VerificationCodeController {
    private final VerificationManager verificationManager;
    private final VerificationCodeService verificationCodeService;

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteOldCode() {
        verificationCodeService.clearOldCodes();
    }

    @PatchMapping("/update")
    public ResponseEntity<Void> regenerateCode(@RequestBody RegenerateVerifiedCodeDto dto) {
        verificationManager.regenerateOtp(dto);
        return ResponseEntity.ok().build();
    }
}
