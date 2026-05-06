package com.example.online_bank.service;


import com.example.online_bank.domain.dto.*;
import com.example.online_bank.domain.entity.RefreshToken;
import com.example.online_bank.domain.entity.TokenFamily;
import com.example.online_bank.domain.entity.TrustedDevice;
import com.example.online_bank.domain.entity.User;
import com.example.online_bank.domain.event.RelatableUserToQuestEvent;
import com.example.online_bank.exception.DeviceNotFoundException;
import com.example.online_bank.exception.ReuseDetectionException;
import com.example.online_bank.mapper.UserMapper;
import com.example.online_bank.security.userdetails.CustomUserDetails;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;

import static com.example.online_bank.enums.AuthenticationResponseKey.*;
import static com.example.online_bank.enums.CodeType.EMAIL_AUTHENTICATION;
import static com.example.online_bank.enums.CodeType.EMAIL_VERIFICATION;
import static com.example.online_bank.enums.SecurityMessage.CONFIRM_LOGIN_MESSAGE;
import static com.example.online_bank.enums.SecurityMessage.HACKING_ATTEMPT_DETECTED;
import static com.example.online_bank.enums.TokenStatus.REVOKED;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final TrustedDeviceService trustedDeviceService;
    private final RefreshTokenService refreshTokenService;
    private final TokenFamilyService tokenFamilyService;
    private final JwtService jwtService;
    private final UserAgentService userAgentService;
    private final VerificationManager verificationManager;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DeviceChallengeService deviceChallengeService;
    private final DeviceFlowService deviceFlowService;

    /**
     * Первый вход
     * userEmail
     * → OTP
     * → подтверждение OTP
     * → create TrustedDevice
     * → create TokenFamily
     * → create RefreshToken
     * → return access + refresh + deviceId
     */
    @Transactional
    public AuthenticationResponseDto firstVerification(VerificationRequestDto requestDto) {
        // 2. Верифицируем otp и пользователя.
        VerificationResponseDto verificationResponseDto = verificationManager.verifyUserByEmail(
                requestDto.verificationCode(),
                requestDto.email(),
                EMAIL_VERIFICATION
        );

        TrustedDevice trustedDevice = trustedDeviceService.create(
                requestDto.deviceName(),
                requestDto.deviceId(),
                verificationResponseDto.verifiedUser(),
                requestDto.userAgent()
        );
        TokenFamily tokenFamily = tokenFamilyService.create(trustedDevice, verificationResponseDto.verifiedUser());

        //Запускается событие для инициализации квестов.
        var event = new RelatableUserToQuestEvent(verificationResponseDto.verifiedUser());
        applicationEventPublisher.publishEvent(event);

        return handleTokenProcessCreating(verificationResponseDto.verifiedUser(), tokenFamily);
    }

    @Transactional
    public AuthenticationResponseDto defaultVerification(VerificationRequestDto verificationRequestDto) {
        // 2. Верифицируем otp и пользователя
        VerificationResponseDto verificationResponseDto = verificationManager.verifyUserByEmail(
                verificationRequestDto.verificationCode(),
                verificationRequestDto.email(),
                EMAIL_AUTHENTICATION
        );
        log.info("Начало проверки device challenge");
        deviceChallengeService.existsByParameters(
                verificationRequestDto.deviceName(),
                verificationRequestDto.deviceId(),
                verificationRequestDto.userAgent(),
                verificationResponseDto.verifiedUser()
        );

        TrustedDevice trustedDevice = trustedDeviceService.create(
                verificationRequestDto.deviceName(),
                verificationRequestDto.deviceId(),
                verificationResponseDto.verifiedUser(),
                verificationRequestDto.userAgent()
        );
        TokenFamily tokenFamily = tokenFamilyService.create(trustedDevice, verificationResponseDto.verifiedUser());
        return handleTokenProcessCreating(verificationResponseDto.verifiedUser(), tokenFamily);
    }

    @Transactional
    public AuthenticationResponseDto login(LoginRequestDto loginRequest) {
        //Проверка пароля
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
        );
        CustomUserDetails details = (CustomUserDetails) authenticate.getPrincipal();
        User user = details.user();

        //Ищем устройство по пользователю и fingerPrint устройства
        TrustedDevice trustedDevice = trustedDeviceService.findByParam(
                loginRequest.deviceName(),
                loginRequest.deviceId(),
                user
        ).orElseThrow(() -> {
            deviceFlowService.handleNewUserDevice(loginRequest, user);
            return new DeviceNotFoundException(CONFIRM_LOGIN_MESSAGE.getValue());
        });

        //Проверка userAgent
        userAgentService.checkUserAgent(loginRequest.userAgent(), trustedDevice.getUserAgent());

        if (!userAgentService.checkBrowserVersion(loginRequest.userAgent(), trustedDevice.getUserAgent())) {
            trustedDeviceService.updateUserAgent(loginRequest.userAgent(), trustedDevice);
        }

        TokenFamily tokenFamily = tokenFamilyService.create(trustedDevice, user);
        return handleTokenProcessCreating(user, tokenFamily);
    }

    /**
     * Тихий вход (refresh rotation)
     * access expired
     * → refresh
     * <p>
     * 1. refresh найден?
     * нет → 401
     * <p>
     * 2. Приведенный incomeRefreshToken был отозван? refresh.status == REVOKED ?
     * → reuse detected
     * → block TokenFamily
     * → revoke ALL refresh in family
     * → REQUIRE OTP
     * <p>
     * 3. family.isBlocked == true ?
     * → REQUIRE OTP
     * <p>
     * 4. refresh.expiresAt < now ?
     * → 401 (expired)
     * <p>
     * 5. OK:
     * → revoke old refresh
     * → create new refresh
     * → return access + refresh
     */
    @Transactional
    public AuthenticationResponseDto silentLogin(SilentLoginRequestDto dto) {
        //1. Парсим токен
        Claims claims = jwtService.getPayload(dto.refreshToken());
        String oldTokenUuid = jwtService.getUuid(claims);
        //2. Ищем токен
        RefreshToken refreshTokenEntity = refreshTokenService.findByUuid(oldTokenUuid);
        TokenFamily family = refreshTokenEntity.getFamily();
        User user = family.getUser();
        checkReuseDetection(refreshTokenEntity, family, dto.deviceId(), user);

        log.debug("start revoke old refreshToken");
        refreshTokenService.revoke(refreshTokenEntity);
        log.info("Ротация токенов произошла успешно");
        return handleTokenProcessCreating(user, family);
    }

    @Transactional
    public void logout(LogoutRequestDto dto) {
        //1. Парсим токен
        Claims claims = jwtService.getPayload(dto.token());
        String oldTokenUuid = jwtService.getUuid(claims);
        RefreshToken refreshTokenEntity = refreshTokenService.findByUuid(oldTokenUuid);
        TokenFamily family = refreshTokenEntity.getFamily();

        checkReuseDetection(refreshTokenEntity, family, dto.deviceId(), family.getUser());
        tokenFamilyService.blockFamily(family);
        refreshTokenService.revoke(refreshTokenEntity);
    }

    /*
     * Вспомогательные методы
     * */
    public AuthenticationResponseDto handleTokenProcessCreating(User user, TokenFamily tokenFamily) {
        UserContainer userContainer = userMapper.toUserContainer(user);

        String accessToken = tokenService.getAccessToken(userContainer);
        String idToken = tokenService.getIdToken(userContainer);
        String refreshToken = tokenService.getRefreshToken(userContainer);

        Claims refreshTokenClaims = jwtService.getPayload(refreshToken);

        String tokenUuid = jwtService.getUuid(refreshTokenClaims);
        LocalDateTime createdDate = jwtService.getCreatedDate(refreshTokenClaims);
        LocalDateTime expDate = jwtService.getExpDate(refreshTokenClaims);

        refreshTokenService.create(tokenUuid, refreshToken, tokenFamily, expDate, createdDate);
        return createAuthResponse(accessToken, idToken, refreshToken);
    }

    private AuthenticationResponseDto createAuthResponse(String accessToken, String idToken, String refreshToken) {
        HashMap<String, String> tokens = new HashMap<>();

        tokens.put(ACCESS_TOKEN.getValue(), accessToken);
        tokens.put(ID_TOKEN.getValue(), idToken);
        tokens.put(REFRESH_TOKEN.getValue(), refreshToken);
        return new AuthenticationResponseDto(tokens);
    }

    private void checkReuseDetection(
            RefreshToken refreshToken,
            TokenFamily family,
            String deviceId,
            User user
    ) {
        if (refreshToken.getStatus().equals(REVOKED)) {
            log.warn("Reuse detected");
            tokenFamilyService.blockFamily(family);
            refreshTokenService.revokeAllByFamily(family);
            trustedDeviceService.deleteByUserAndDeviceId(deviceId, user);
            throw new ReuseDetectionException(HACKING_ATTEMPT_DETECTED.getValue());
        }
    }
}