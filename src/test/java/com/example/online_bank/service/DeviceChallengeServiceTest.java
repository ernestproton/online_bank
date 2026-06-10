package com.example.online_bank.service;

import com.example.online_bank.domain.entity.DeviceChallenge;
import com.example.online_bank.domain.entity.User;
import com.example.online_bank.exception.DeviceChallengesNotFoundException;
import com.example.online_bank.repository.DeviceChallengesRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceChallengeServiceTest {
    public static final String DEVICE_NAME = "windows 10";
    public static final String DEVICE_ID = "11111111-1111-1111-1111-111111111111";
    public static final String USER_AGENT = "some userAgent";
    @InjectMocks
    private DeviceChallengeService deviceChallengeService;
    @Mock
    private DeviceChallengesRepository deviceChallengesRepository;
    private final User userMock = User.builder()
            .name("Kolya")
            .build();
    private final DeviceChallenge deviceChallengeMock = DeviceChallenge.builder()
            .deviceId(DEVICE_ID)
            .deviceName(DEVICE_NAME)
            .userAgent(USER_AGENT)
            .build();

    @Test
    void successCreate() {


        assertDoesNotThrow(() -> deviceChallengeService.create(
                userMock,
                DEVICE_NAME,
                DEVICE_ID,
                USER_AGENT
        ));


        Mockito.verify(deviceChallengesRepository).save(Mockito.any(DeviceChallenge.class));

    }

    @Test
    void successExistsByParameters() {

        when(deviceChallengesRepository.existsByDeviceNameAndDeviceIdAndUserAgentAndUser(
                DEVICE_NAME, DEVICE_ID, USER_AGENT, userMock))
                .thenReturn(true);

        assertDoesNotThrow(() -> deviceChallengeService.existsByParameters(DEVICE_NAME, DEVICE_ID, USER_AGENT, userMock));
    }

    @Test
    void failureExistsByParameters() {
        when(deviceChallengesRepository.existsByDeviceNameAndDeviceIdAndUserAgentAndUser(
                DEVICE_NAME, DEVICE_ID, USER_AGENT, userMock))
                .thenReturn(false);

        Assertions.assertThrows(
                DeviceChallengesNotFoundException.class,
                () -> deviceChallengeService.existsByParameters(DEVICE_NAME, DEVICE_ID, USER_AGENT, userMock));

    }
}