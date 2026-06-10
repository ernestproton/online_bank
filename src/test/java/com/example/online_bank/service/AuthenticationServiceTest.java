package com.example.online_bank.service;

import com.example.online_bank.domain.dto.*;
import com.example.online_bank.domain.entity.*;
import com.example.online_bank.domain.event.RelatableUserToQuestEvent;
import com.example.online_bank.enums.CodeType;
import com.example.online_bank.enums.Roles;
import com.example.online_bank.enums.TokenStatus;
import com.example.online_bank.exception.DeviceNotFoundException;
import com.example.online_bank.exception.ReuseDetectionException;
import com.example.online_bank.mapper.UserMapper;
import com.example.online_bank.security.userdetails.CustomUserDetails;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.example.online_bank.enums.AuthenticationResponseKey.*;
import static com.example.online_bank.enums.CodeType.EMAIL_VERIFICATION;
import static com.example.online_bank.enums.SecurityMessage.HACKING_ATTEMPT_DETECTED;
import static com.example.online_bank.enums.TestUserData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(SpringExtension.class)
class AuthenticationServiceTest {
    @InjectMocks
    private AuthenticationService authenticationService;
    @Mock
    private TokenService tokenService;
    @Mock
    private TrustedDeviceService trustedDeviceService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    TokenFamilyService tokenFamilyService;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserAgentService userAgentService;
    @Mock
    private VerificationManager verificationManager;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private DeviceChallengeService deviceChallengeService;
    @Mock
    private DeviceFlowService deviceFlowService;
    @Mock
    private AuthenticationManager authenticationManager;

    private static VerificationRequestDto verificationRequestDto;

    private static VerificationResponseDto verificationResponseDtoMock;
    private static TrustedDevice trustedDeviceMock;
    private final TokenFamily tokenFamilyMock = new TokenFamily();
    private static Authentication authenticationMock;

    private static LoginRequestDto loginRequestDto;
    private static LoginRequestDto loginRequestDtoWithAnotherBrowserVersion;


    private static final User userMock = User.builder()
            .id(1L)
            .roles(List.of(
                    Role.builder()
                            .name(Roles.ROLE_USER.getValue())
                            .build())
            )
            .name(NAME.getValue())
            .email(EMAIL.getValue())
            .build();

    @BeforeAll()
    static void init() {
        String uuid = UUID.randomUUID().toString();

        verificationRequestDto = new VerificationRequestDto(
                EMAIL.getValue(),
                VERIFICATION_CODE.getValue(),
                DEVICE_NAME.getValue(),
                CHROME_USER_AGENT.getValue(),
                uuid
        );

        loginRequestDto = new LoginRequestDto(
                EMAIL.getValue(),
                PASSWORD.getValue(),
                uuid,
                DEVICE_NAME.getValue(),
                CHROME_USER_AGENT.getValue()
        );

        verificationResponseDtoMock = new VerificationResponseDto(
                User.builder()
                        .isVerified(true)
                        .build()
        );
        authenticationMock = new UsernamePasswordAuthenticationToken(new CustomUserDetails(userMock), null);
        trustedDeviceMock = TrustedDevice.builder()
                .user(userMock)
                .deviceId(loginRequestDto.deviceId())
                .deviceName(loginRequestDto.deviceName())
                .userAgent(CHROME_USER_AGENT.getValue())
                .build();

        loginRequestDtoWithAnotherBrowserVersion = new LoginRequestDto(
                EMAIL.getValue(),
                PASSWORD.getValue(),
                uuid,
                DEVICE_NAME.getValue(),
                UPDATED_CHROME_USER_AGENT.getValue()
        );
    }

    @Test
    void successFirstVerification() {
        when(verificationManager.verifyUserByEmail(
                verificationRequestDto.verificationCode(),
                verificationRequestDto.email(),
                EMAIL_VERIFICATION))
                .thenReturn(verificationResponseDtoMock);

        when(trustedDeviceService.create(anyString(), anyString(), any(User.class), anyString()))
                .thenReturn(trustedDeviceMock);

        AuthenticationResponseDto result = authenticationService
                .firstVerification(verificationRequestDto);

        assertNotNull(result);
        assertTrue(result.tokens().containsKey(ACCESS_TOKEN.getValue()));
        assertTrue(result.tokens().containsKey(ID_TOKEN.getValue()));
        assertTrue(result.tokens().containsKey(REFRESH_TOKEN.getValue()));

        verify(trustedDeviceService).create(eq(verificationRequestDto.deviceName()),
                eq(verificationRequestDto.deviceId()),
                eq(verificationResponseDtoMock.verifiedUser()),
                eq(verificationRequestDto.userAgent())
        );
        verify(tokenFamilyService).create(
                any(TrustedDevice.class),
                eq(verificationResponseDtoMock.verifiedUser())
        );
        verify(applicationEventPublisher).publishEvent(any(RelatableUserToQuestEvent.class));
    }

    @Test
    void successDefaultVerification() {
        log.info("verificationResponseDtoMock - {}", verificationResponseDtoMock);
        when(verificationManager.verifyUserByEmail(
                eq(verificationRequestDto.verificationCode()),
                eq(verificationRequestDto.email()),
                eq(EMAIL_VERIFICATION)
        )).thenReturn(verificationResponseDtoMock);

        doNothing().when(deviceChallengeService).existsByParameters(
                verificationRequestDto.deviceName(),
                verificationRequestDto.deviceId(),
                verificationRequestDto.userAgent(),
                verificationResponseDtoMock.verifiedUser());

        when(trustedDeviceService.create(
                eq(verificationRequestDto.deviceName()),
                eq(verificationRequestDto.deviceId()),
                eq(verificationResponseDtoMock.verifiedUser()),
                eq(verificationRequestDto.userAgent())
        )).thenReturn(trustedDeviceMock);

        when(tokenFamilyService.create(any(TrustedDevice.class), any(User.class))).thenReturn(tokenFamilyMock);

        AuthenticationResponseDto result = authenticationService.defaultVerification(verificationRequestDto);

        assertNotNull(result);
        assertTrue(result.tokens().containsKey(ACCESS_TOKEN.getValue()));
        assertTrue(result.tokens().containsKey(ID_TOKEN.getValue()));
        assertTrue(result.tokens().containsKey(REFRESH_TOKEN.getValue()));

        verify(deviceChallengeService, atLeastOnce()).existsByParameters(verificationRequestDto.deviceName(),
                verificationRequestDto.deviceId(),
                verificationRequestDto.userAgent(),
                verificationResponseDtoMock.verifiedUser());

        verify(trustedDeviceService, atLeastOnce()).create(
                eq(verificationRequestDto.deviceName()),
                eq(verificationRequestDto.deviceId()),
                eq(verificationResponseDtoMock.verifiedUser()),
                eq(verificationRequestDto.userAgent()));
        verify(tokenFamilyService, atLeastOnce()).create(
                any(TrustedDevice.class), eq(verificationResponseDtoMock.verifiedUser()));
    }

    @Test
    void successLogin() {
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequestDto.email(),
                loginRequestDto.password()))
        ).thenReturn(authenticationMock);

        when(trustedDeviceService.findByParam(
                eq(loginRequestDto.deviceName()),
                eq(loginRequestDto.deviceId()),
                eq(userMock)
        )).thenReturn(Optional.of(trustedDeviceMock));

        doNothing().when(userAgentService)
                .checkUserAgent(loginRequestDto.userAgent(), trustedDeviceMock.getUserAgent());

        when(userAgentService.checkBrowserVersion(
                eq(loginRequestDto.userAgent()),
                eq(trustedDeviceMock.getUserAgent()
                ))).thenReturn(true);

        when(tokenFamilyService.create(eq(trustedDeviceMock), eq(userMock))).thenReturn(tokenFamilyMock);

        AuthenticationResponseDto result = authenticationService.login(loginRequestDto);
        assertNotNull(result);
        assertTrue(result.tokens().containsKey(ACCESS_TOKEN.getValue()));

        assertTrue(result.tokens().containsKey(ID_TOKEN.getValue()));
        assertTrue(result.tokens().containsKey(REFRESH_TOKEN.getValue()));

        verify(trustedDeviceService, never()).updateUserAgent(anyString(), any(TrustedDevice.class));
        //  verify(tokenFamilyService, atLeastOnce()).create(any(), any());
    }

    @Test
    void successLoginWithAnotherBrowserVersion() {
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequestDtoWithAnotherBrowserVersion.email(),
                loginRequestDtoWithAnotherBrowserVersion.password()))
        ).thenReturn(authenticationMock);

        when(trustedDeviceService.findByParam(
                eq(loginRequestDtoWithAnotherBrowserVersion.deviceName()),
                eq(loginRequestDtoWithAnotherBrowserVersion.deviceId()),
                eq(userMock)
        )).thenReturn(Optional.of(trustedDeviceMock));

        doNothing().when(userAgentService)
                .checkUserAgent(loginRequestDtoWithAnotherBrowserVersion.userAgent(), trustedDeviceMock.getUserAgent());

        when(userAgentService.checkBrowserVersion(
                eq(loginRequestDtoWithAnotherBrowserVersion.userAgent()),
                eq(trustedDeviceMock.getUserAgent()
                ))).thenReturn(false);

        AuthenticationResponseDto result = authenticationService.login(loginRequestDtoWithAnotherBrowserVersion);
        assertNotNull(result);
        assertTrue(result.tokens().containsKey(ACCESS_TOKEN.getValue()));
        assertTrue(result.tokens().containsKey(ID_TOKEN.getValue()));
        assertTrue(result.tokens().containsKey(REFRESH_TOKEN.getValue()));

        log.info("request user agent - {}", loginRequestDtoWithAnotherBrowserVersion.userAgent());
        log.info("device user agent - {}", trustedDeviceMock.getUserAgent());
        verify(trustedDeviceService, atLeastOnce()).updateUserAgent(
                eq(loginRequestDtoWithAnotherBrowserVersion.userAgent()),
                eq(trustedDeviceMock));
        //   verify(tokenFamilyService, atLeastOnce()).create(any(), any());
    }

    @Test
    void successLogin_VerificationRequired() {
        VerificationCode verificationCodeMock = VerificationCode.builder()
                .user(userMock)
                .verificationCode(VERIFICATION_CODE.getValue())
                .codeType(CodeType.EMAIL_AUTHENTICATION)
                .build();
        log.info("verificationCodeMock - {}", verificationCodeMock);

        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequestDto.email(),
                loginRequestDto.password()))
        ).thenReturn(authenticationMock);

        when(trustedDeviceService.findByParam(
                eq(loginRequestDto.deviceName()),
                eq(loginRequestDto.deviceId()),
                eq(userMock)
        )).thenReturn(Optional.empty());

        doNothing().when(deviceFlowService).handleNewUserDevice(loginRequestDto, userMock);

        assertThrows(DeviceNotFoundException.class, () -> authenticationService.login(loginRequestDto));

        verify(deviceFlowService).handleNewUserDevice(loginRequestDto, userMock);
        verify(userAgentService, never()).checkUserAgent(anyString(), anyString());
        verify(userAgentService, never()).checkBrowserVersion(anyString(), anyString());
        verify(trustedDeviceService, never()).updateUserAgent(any(), any());
        verify(tokenFamilyService, never()).create(any(), any());
    }

    @Test
    void successSilentLogin() {
        String oldTokenUuid = "oldTokenUuid";
        Claims claimsMock = mock(Claims.class);
        RefreshToken refreshTokenMock = RefreshToken.builder()
                .uuid(oldTokenUuid)
                .status(TokenStatus.CREATED)
                .build();
        TokenFamily tokenFamilyMock = TokenFamily.builder()
                .trustedDevice(trustedDeviceMock)
                .refreshTokens(List.of(refreshTokenMock))
                .build();
        refreshTokenMock.setFamily(tokenFamilyMock);


        var silentLoginRequestDto = new SilentLoginRequestDto("refreshToken", UUID.randomUUID().toString());
        when(jwtService.getPayload(anyString())).thenReturn(claimsMock);
        when(jwtService.getUuid(eq(claimsMock))).thenReturn(oldTokenUuid);
        when(refreshTokenService.findByUuid(eq(oldTokenUuid))).thenReturn(refreshTokenMock);
        AuthenticationResponseDto result = authenticationService.silentLogin(silentLoginRequestDto);
        assertNotNull(result);
        assertTrue(result.tokens().containsKey(ACCESS_TOKEN.getValue()));
        assertTrue(result.tokens().containsKey(ID_TOKEN.getValue()));
        assertTrue(result.tokens().containsKey(REFRESH_TOKEN.getValue()));
    }

    @Test
    void failureSilentLogin() {
        String oldTokenUuid = "oldTokenUuid";
        Claims claimsMock = mock(Claims.class);
        RefreshToken refreshTokenMock = RefreshToken.builder()
                .uuid(oldTokenUuid)
                .status(TokenStatus.REVOKED)
                .build();
        TokenFamily tokenFamilyMock = TokenFamily.builder()
                .trustedDevice(trustedDeviceMock)
                .refreshTokens(List.of(refreshTokenMock))
                .build();
        refreshTokenMock.setFamily(tokenFamilyMock);

        User userMock = User.builder()
                .build();

        var silentLoginRequestDto = new SilentLoginRequestDto("refreshToken", UUID.randomUUID().toString());
        when(jwtService.getPayload(anyString())).thenReturn(claimsMock);
        when(jwtService.getUuid(eq(claimsMock))).thenReturn(oldTokenUuid);
        when(refreshTokenService.findByUuid(eq(oldTokenUuid))).thenReturn(refreshTokenMock);
        ReuseDetectionException reuseDetectionException = assertThrows(
                ReuseDetectionException.class, () -> authenticationService.silentLogin(silentLoginRequestDto));
        assertEquals(HACKING_ATTEMPT_DETECTED.getValue(), reuseDetectionException.getMessage());

        verify(refreshTokenService).revokeAllByFamily(tokenFamilyMock);
        verify(trustedDeviceService).deleteByUserAndDeviceId(eq(silentLoginRequestDto.deviceId()), any());
    }

    @Test
    void successLogOut() {
        Claims claimsMock = mock(Claims.class);
        String oldTokenUuid = "oldTokenUuid";
        var logoutRequestDto = new LogoutRequestDto(oldTokenUuid, UUID.randomUUID().toString());
        RefreshToken refreshTokenMock = RefreshToken.builder()
                .uuid(oldTokenUuid)
                .status(TokenStatus.CREATED)
                .build();
        TokenFamily tokenFamilyMock = TokenFamily.builder()
                .refreshTokens(List.of(refreshTokenMock))
                .trustedDevice(trustedDeviceMock)
                .build();
        refreshTokenMock.setFamily(tokenFamilyMock);

        when(jwtService.getPayload(anyString())).thenReturn(claimsMock);
        when(jwtService.getUuid(eq(claimsMock))).thenReturn(oldTokenUuid);
        when(refreshTokenService.findByUuid(eq(oldTokenUuid))).thenReturn(refreshTokenMock);
        doNothing().when(refreshTokenService).revoke(eq(refreshTokenMock));

        assertDoesNotThrow(() -> authenticationService.logout(logoutRequestDto));
    }

    @Test
    void failureLogout() {
        //Подготовка данных
        Claims claimsMock = mock(Claims.class);
        String oldTokenUuid = "oldTokenUuid";
        var logoutRequestDto = new LogoutRequestDto(oldTokenUuid, UUID.randomUUID().toString());
        RefreshToken revocedRefreshToken = RefreshToken.builder()
                .uuid(oldTokenUuid)
                .status(TokenStatus.REVOKED)
                .build();
        TokenFamily tokenFamilyMock = TokenFamily.builder()
                .trustedDevice(trustedDeviceMock)
                .refreshTokens(List.of(revocedRefreshToken))
                .build();
        revocedRefreshToken.setFamily(tokenFamilyMock);

        when(jwtService.getPayload(anyString())).thenReturn(claimsMock);
        when(jwtService.getUuid(eq(claimsMock))).thenReturn(oldTokenUuid);
        when(refreshTokenService.findByUuid(eq(oldTokenUuid))).thenReturn(revocedRefreshToken);

        ReuseDetectionException reuseDetectionException = assertThrows(
                ReuseDetectionException.class, () -> authenticationService.logout(logoutRequestDto));
        assertEquals(HACKING_ATTEMPT_DETECTED.getValue(), reuseDetectionException.getMessage());

        verify(refreshTokenService).revokeAllByFamily(tokenFamilyMock);
        verify(trustedDeviceService).deleteByUserAndDeviceId(eq(logoutRequestDto.deviceId()), any());
    }

}