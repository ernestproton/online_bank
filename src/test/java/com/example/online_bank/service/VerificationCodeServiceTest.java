package com.example.online_bank.service;

import com.example.online_bank.domain.entity.User;
import com.example.online_bank.domain.entity.VerificationCode;
import com.example.online_bank.enums.CodeType;
import com.example.online_bank.exception.VerificationOtpException;
import com.example.online_bank.repository.VerificationCodeRepository;
import com.example.online_bank.service.domain.VerificationCodeService;
import com.example.online_bank.util.CodeGeneratorUtil;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
class VerificationCodeServiceTest {
    @Mock
    private VerificationCodeRepository verificationCodeRepository;
    @InjectMocks
    private VerificationCodeService verificationCodeService;

    @Test
    void successDeleteAllByUserVerificationCodes() {
        Mockito.doNothing().when(verificationCodeRepository).deleteAllByIsVerifiedTrueAndUser_id(1L);
        Assertions.assertDoesNotThrow(() -> verificationCodeService.deleteAllUserVerificationCodes(1L));
    }

    @Test
    void successClearOldCodes() {
        VerificationCode codeMock1 = VerificationCode.builder()
                .verificationCode("0011")
                .build();

        VerificationCode codeMock2 = VerificationCode.builder()
                .verificationCode("0022")
                .build();

        VerificationCode codeMock3 = VerificationCode.builder()
                .verificationCode("0033")
                .build();
        List<VerificationCode> verificationCodes = List.of(codeMock1, codeMock2, codeMock3);
        LocalDateTime now = LocalDateTime.of(2026, 6, 10, 4, 10);
        Mockito.when(verificationCodeRepository.findAllByExpiresAtBefore(now))
                .thenReturn(verificationCodes);

        Mockito.doNothing().when(verificationCodeRepository).deleteAll(verificationCodes);
        verificationCodeService.clearOldCodes();
    }

    @Test
    void failureFindCodeByUser() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 10, 4, 10);
        Mockito.when(verificationCodeRepository.findByVerificationCodeAndUser_IdAndCodeTypeAndIsVerifiedIsFalseAndExpiresAtAfter
                        ("000", 1L, CodeType.EMAIL_VERIFICATION, now))
                .thenThrow(VerificationOtpException.class);

        VerificationOtpException e = assertThrows(VerificationOtpException.class,
                () -> verificationCodeService.findCodeByUser("000", User.builder().id(1L).build(), CodeType.EMAIL_VERIFICATION));
        Assertions.assertEquals("Ошибка верификации. Запросите новый код.", e.getMessage());
    }

    @Test
    @Disabled
    void successFindCodeByUser() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 10, 4, 10);
        LocalDateTime expTime = LocalDateTime.of(2026, 6, 10, 5, 10);
        User userMock = User.builder().id(1L).build();
        VerificationCode verificationCodeMock = VerificationCode.builder()
                .user(userMock)
                .verificationCode("0000")
                .codeType(CodeType.EMAIL_VERIFICATION)
                .expiresAt(expTime)
                .build();

        Mockito.when(verificationCodeRepository
                        .findByVerificationCodeAndUser_IdAndCodeTypeAndIsVerifiedIsFalseAndExpiresAtAfter(
                                "0000",
                                1L,
                                CodeType.EMAIL_VERIFICATION,
                                now))
                .thenReturn(Optional.of(verificationCodeMock));

        Assertions.assertDoesNotThrow(() -> verificationCodeService.findCodeByUser(
                "0000",
                userMock,
                CodeType.EMAIL_VERIFICATION));
    }

    @Test
    @Disabled
    void successUpdateVerificationCode() {
        LocalDateTime oldExpTime = LocalDateTime.of(2026, 6, 10, 5, 10);
        LocalDateTime newExpTime = LocalDateTime.of(2026, 6, 10, 5, 12);

        Mockito.when(CodeGeneratorUtil.generateVerificationCode())
                .thenReturn("0000");
        Mockito.doNothing().when(verificationCodeRepository)
                .updateVerifiedCodeByUser_Email("testemail@domain.com", "0000", newExpTime);
        String newCode = verificationCodeService.updateVerificationCode("testemail@domain.com");
        Assertions.assertEquals("0000", newCode);
    }

    @Test
    @Disabled
    void successVerifyCode() {
        VerificationCode verificationCodeMock = VerificationCode.builder()
                .isVerified(false)
                .build();

        Mockito.doNothing().when(verificationCodeRepository).save(Mockito.any(VerificationCode.class));

        verificationCodeService.verifyCode(verificationCodeMock);
        verificationCodeService.verifyCode(verificationCodeMock);
    }

    @Test
    void failureFindCodeByUserEmail() {
        Mockito.when(verificationCodeRepository.findByUser_Email("testemail@domain.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> verificationCodeService.findCodeByUserEmail("testemail@domain.com"));
    }

    @Test
    void successFindCodeByUserEmail() {
        VerificationCode verificationCodeMock = VerificationCode.builder()
                .isVerified(false)
                .build();

        Mockito.when(verificationCodeRepository.findByUser_Email("testemail@domain.com"))
                .thenReturn(Optional.of(verificationCodeMock));

        Assertions.assertDoesNotThrow(() -> verificationCodeService.findCodeByUserEmail("testemail@domain.com"));
    }

}