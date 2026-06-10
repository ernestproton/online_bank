package com.example.online_bank.service.it;

import com.example.online_bank.OnlineBankApplication;
import com.example.online_bank.config.JwtConfig;
import com.example.online_bank.domain.entity.User;
import com.example.online_bank.domain.entity.VerificationCode;
import com.example.online_bank.repository.VerificationCodeRepository;
import com.example.online_bank.service.MailService;
import com.example.online_bank.service.UserService;
import com.example.online_bank.service.domain.VerificationCodeService;
import com.example.online_bank.service.impl.DefaultEmailNotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.online_bank.enums.BodyMessage.VERIFICATION_BODY;
import static com.example.online_bank.enums.CodeType.EMAIL_VERIFICATION;
import static com.example.online_bank.enums.SubjectMessage.VERIFICATION;
import static org.junit.jupiter.api.Assertions.*;


@RequiredArgsConstructor
@ContextConfiguration(classes = OnlineBankApplication.class)
@SpringBootTest(classes = OnlineBankApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Slf4j
class VerificationCodeServiceTestIT {
    @Autowired
    private VerificationCodeService verificationCodeService;
    @Autowired
    private VerificationCodeRepository verificationCodeRepository;
    @Autowired
    private UserService userService;
    @MockBean
    private DefaultEmailNotificationService emailNotificationService;
    @MockBean
    private MailService mailService;
    @MockBean
    private JwtConfig jwtConfig;

    @Test
    @Transactional
    @DisplayName("Успешное создание верификационного кода")
    void successCreateVerifiedCode() {
        //подготовка данных arr
        User userMock = User.builder().build();
        userService.save(userMock);

        VerificationCode verificationCode = verificationCodeService.create(
                userMock,
                EMAIL_VERIFICATION,
                VERIFICATION,
                VERIFICATION_BODY,
                false
        );

        //act
        assertDoesNotThrow(() -> verificationCodeRepository.findVerifiedCodeByVerificationCode(verificationCode.getVerificationCode())
                .orElseThrow(EntityNotFoundException::new));

        assertNotNull(verificationCode.getVerificationCode());
        assertFalse(verificationCode.getIsVerified());
        assertNotNull(verificationCode.getCreatedAt());
        assertTrue(verificationCode.getExpiresAt().isAfter(verificationCode.getCreatedAt()));
    }

    @Test
    @Transactional
    @DisplayName("Успешное удаление всех истекших кодов")
    void successfulRemoveExpiredOtpCode() {
        verificationCodeService.clearOldCodes();
        List<VerificationCode> allOtp = verificationCodeRepository.findAll();
        assertTrue(allOtp.isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("Успешное удаление истекших кодов пользователя")
    void successfulRemoveExpiredOtpCodeByUser() {
        //подготовка данных arr
        User userMock = User.builder().build();
        userService.save(userMock);

        LocalDateTime now = LocalDateTime.now();
        verificationCodeService.create(
                userMock,
                EMAIL_VERIFICATION,
                VERIFICATION,
                VERIFICATION_BODY,
                false
        );

        verificationCodeService.deleteAllUserVerificationCodes(userMock.getId());
        //act
        assertTrue(verificationCodeRepository.findAllByExpiresAtBeforeAndUser_Id(now, userMock.getId()).isEmpty());
    }

    @Test
    @DisplayName("Найти не истекший код для пользователя и поставить ему isVerified = true")
    @Transactional
    void validateCode() {
        //подготовка данных arr
        User userMock = User.builder()
                .isVerified(false)
                .build();
        userService.save(userMock);

        verificationCodeService.create(userMock, EMAIL_VERIFICATION, VERIFICATION, VERIFICATION_BODY, false);

        //act
        VerificationCode verificationCode = assertDoesNotThrow(() -> {
            return verificationCodeRepository.findVerifiedCodeByVerificationCode("7777")
                    .orElseThrow(EntityNotFoundException::new);
        });
        log.debug(verificationCode.toString());
        assertTrue(verificationCode.getIsVerified());
    }
}