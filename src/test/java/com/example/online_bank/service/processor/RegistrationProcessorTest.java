package com.example.online_bank.service.processor;

import com.example.online_bank.domain.dto.RegistrationDtoRequest;
import com.example.online_bank.domain.entity.User;
import com.example.online_bank.domain.entity.VerificationCode;
import com.example.online_bank.domain.event.SendVerificationCodeEvent;
import com.example.online_bank.enums.BodyMessage;
import com.example.online_bank.enums.CodeType;
import com.example.online_bank.enums.SubjectMessage;
import com.example.online_bank.exception.EntityAlreadyExistsException;
import com.example.online_bank.mapper.UserMapper;
import com.example.online_bank.service.RoleService;
import com.example.online_bank.service.UserService;
import com.example.online_bank.service.domain.VerificationCodeService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;

import static com.example.online_bank.enums.BodyMessage.VERIFICATION_BODY;
import static com.example.online_bank.enums.CodeType.EMAIL_VERIFICATION;
import static com.example.online_bank.enums.SubjectMessage.VERIFICATION;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class RegistrationProcessorTest {
    @InjectMocks
    private RegistrationProcessor registrationProcessor;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock
    private RoleService roleService;
    @Mock
    private VerificationCodeService verificationCodeService;
    @Mock
    private UserService userService;
    @Mock
    private UserMapper userMapper;
    private final RegistrationDtoRequest request = new RegistrationDtoRequest(
            "testName",
            "testSurname",
            "testPatronymic",
            "89608052696",
            "wass",
            "myemail@test.com"
    );

    @Test
    void successRegisterToUser() {
        //arrange подготовка
        User userMock = User.builder()
                .name(request.name())
                .surname(request.surname())
                .phoneNumber(request.phone())
                .email(request.email())
                .build();

        VerificationCode verificationCodeMock = VerificationCode.builder()
                .verificationCode("1234")
                .user(userMock)
                .createdAt(LocalDateTime.now())
                .codeType(EMAIL_VERIFICATION)
                .build();

        var expectedResult = new SendVerificationCodeEvent(request.email(), "1234", VERIFICATION.getValue(), VERIFICATION_BODY.getValue());

        //обучение моков
        when(userService.existsByPhoneNumber(request.phone())).thenReturn(false);
        when(userService.existsByEmail(request.email())).thenReturn(false);
        when(userMapper.toUser(eq(request), any(), any())).thenReturn(userMock);
        when(verificationCodeService.create(
                eq(userMock),
                eq(CodeType.EMAIL_VERIFICATION),
                eq(SubjectMessage.VERIFICATION),
                eq(BodyMessage.VERIFICATION_BODY),
                eq(false)))
                .thenReturn(verificationCodeMock);

        //act действие
        SendVerificationCodeEvent actualResult = registrationProcessor.register(request, userMapper::toUser);

        //assert проверка
        Assertions.assertNotNull(actualResult);
        Assertions.assertEquals(expectedResult.userEmail(), actualResult.userEmail());

        verify(userMapper).toUser(eq(request), any(RoleService.class), any(BCryptPasswordEncoder.class));
        verify(userService).save(eq(userMock));
    }

    @Test
    void successRegisterToAdmin() {
        //arrange подготовка
        User adminMock = User.builder()
                .name(request.name())
                .surname(request.surname())
                .phoneNumber(request.phone())
                .email(request.email())
                .build();

        VerificationCode verificationCodeMock = VerificationCode.builder()
                .verificationCode("1234")
                .user(adminMock)
                .createdAt(LocalDateTime.now())
                .codeType(EMAIL_VERIFICATION)
                .build();

        SendVerificationCodeEvent expectedResult = new SendVerificationCodeEvent(
                request.email(),
                "1234",
                VERIFICATION.getValue(),
                VERIFICATION_BODY.getValue()
        );

        //обучение моков
        when(userService.existsByPhoneNumber(request.phone())).thenReturn(false);
        when(userService.existsByEmail(request.email())).thenReturn(false);
        when(userMapper.toAdmin(eq(request), any(), any())).thenReturn(adminMock);
        when(verificationCodeService.create(
                eq(adminMock),
                eq(EMAIL_VERIFICATION),
                eq(VERIFICATION),
                eq(VERIFICATION_BODY),
                eq(false))).thenReturn(verificationCodeMock);
        //act действие
        SendVerificationCodeEvent actualResult = registrationProcessor.register(request, userMapper::toAdmin);
        log.info("result {}", actualResult);

        //assert проверка
        Assertions.assertNotNull(actualResult);
        Assertions.assertEquals(expectedResult.userEmail(), actualResult.userEmail());

        verify(userMapper).toAdmin(eq(request), any(RoleService.class), any(BCryptPasswordEncoder.class));
        verify(userService).save(eq(adminMock));
    }

    @Test
    void failRegister_UserExistWithProvidedEmail() {
        //1. Arrange (подготовка)
        when(userService.existsByEmail(request.email())).thenReturn(true);

        //2. Act & assert (действие и проверка)
        assertThrows(
                EntityAlreadyExistsException.class,
                () -> registrationProcessor.register(request, userMapper::toUser)
        );

        // 3. Assert (проверка)
        verify(userService, never()).save(any());
        verifyNoInteractions(userMapper, verificationCodeService);
    }

    @Test
    void failRegister_UserExistWithProvidedPhone() {
        //1. Arrange (подготовка)
        when(userService.existsByPhoneNumber(request.phone())).thenReturn(true);
        //2. Act & assert (действие и проверка)
        Assertions.assertThrows(EntityAlreadyExistsException.class,
                () -> registrationProcessor.register(request, userMapper::toUser)
        );
        //3. Assert (проверка)
        verify(userService, never()).save(any());
        verifyNoInteractions(userMapper, verificationCodeService);
    }

    @Test
    void failureRegister_UserExistWithProvidedEmailButNotWithPhone() {
        //1. Arrange (подготовка)
        when(userService.existsByPhoneNumber(request.phone())).thenReturn(false);
        when(userService.existsByEmail(request.email())).thenReturn(true);
        //2. Act & assert (действие и проверка)
        Assertions.assertThrows(EntityAlreadyExistsException.class,
                () -> registrationProcessor.register(request, userMapper::toUser)
        );
        //3. Assert (проверка)
        verify(userService).existsByEmail(eq(request.email()));
        verify(userService, never()).save(any());
        verifyNoInteractions(userMapper, verificationCodeService);
    }
}