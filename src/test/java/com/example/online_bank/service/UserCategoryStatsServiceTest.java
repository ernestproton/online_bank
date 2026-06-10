package com.example.online_bank.service;

import com.example.online_bank.domain.entity.User;
import com.example.online_bank.domain.entity.UserCategoryStats;
import com.example.online_bank.domain.event.UpdateUserStatEvent;
import com.example.online_bank.enums.PartnerCategory;
import com.example.online_bank.repository.UserCategoryStatsRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
class UserCategoryStatsServiceTest {
    @InjectMocks
    private UserCategoryStatsService userCategoryStatsService;
    @Mock
    private UserCategoryStatsRepository userCategoryStatsRepository;
    @Mock
    private UserService userService;

    @Test
    void successUpdateUserStat() {
        User userMock = User.builder()
                .uuid(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .build();

        UserCategoryStats userCategoryStatsMock = UserCategoryStats.builder()
                .category(PartnerCategory.ENTERTAINMENT)
                .user(userMock)
                .lastSpendDate(LocalDate.of(2026, 6, 9))
                .countSpendInMonth(1)
                .totalSpend(BigDecimal.valueOf(100))
                .build();

        UpdateUserStatEvent event = new UpdateUserStatEvent(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                BigDecimal.TEN,
                LocalDate.of(2026, 6, 10),
                "000111",
                PartnerCategory.ENTERTAINMENT
        );

        when(userCategoryStatsRepository
                .findByUser_UuidAndCategoryAndLastSpendDateBetween(
                        eq(event.userUuid()),
                        eq(event.partnerCategory()),
                        Mockito.any(LocalDate.class),
                        Mockito.any(LocalDate.class)
                )
        ).thenReturn(Optional.of(userCategoryStatsMock));

        UserCategoryStats result = userCategoryStatsService.updateUserStat(event);
        Assertions.assertNotNull(result);

        assertEquals(2, result.getCountSpendInMonth());
        assertEquals(LocalDate.of(2026, 6, 10), result.getLastSpendDate());
        assertEquals(BigDecimal.valueOf(110), result.getTotalSpend());
    }

    @Test
    void successUpdateStat_InitNewStatistic(){
        User userMock = User.builder()
                .uuid(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .build();

        UpdateUserStatEvent event = new UpdateUserStatEvent(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                BigDecimal.TEN,
                LocalDate.of(2026, 6, 10),
                "000111",
                PartnerCategory.ENTERTAINMENT
        );

        when(userCategoryStatsRepository
                .findByUser_UuidAndCategoryAndLastSpendDateBetween(
                        eq(event.userUuid()),
                        eq(event.partnerCategory()),
                        Mockito.any(LocalDate.class),
                        Mockito.any(LocalDate.class)
                )
        ).thenReturn(Optional.empty());

        when(userService.findByUuid(userMock.getUuid()))
                .thenReturn(Optional.of(userMock));

        UserCategoryStats result = userCategoryStatsService.updateUserStat(event);
        Assertions.assertNotNull(result);

        assertEquals(1, result.getCountSpendInMonth());
        assertEquals(LocalDate.of(2026, 6, 10), result.getLastSpendDate());
        assertEquals(BigDecimal.valueOf(10), result.getTotalSpend());
    }

    @Test
    void successFindAllByUser(){
        User userMock = User.builder()
                .uuid(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .build();

        UserCategoryStats userCategoryStatsMock1 = UserCategoryStats.builder()
                .category(PartnerCategory.ENTERTAINMENT)
                .user(userMock)
                .lastSpendDate(LocalDate.of(2026, 6, 9))
                .countSpendInMonth(1)
                .totalSpend(BigDecimal.valueOf(100))
                .build();

        UserCategoryStats userCategoryStatsMock2 = UserCategoryStats.builder()
                .category(PartnerCategory.FOOD)
                .user(userMock)
                .lastSpendDate(LocalDate.of(2026, 6, 11))
                .countSpendInMonth(5)
                .totalSpend(BigDecimal.valueOf(100))
                .build();

        when(userCategoryStatsRepository.findAllByUser(userMock))
                .thenReturn((List<UserCategoryStats>) List.of(userCategoryStatsMock1, userCategoryStatsMock2));

        assertDoesNotThrow(() -> userCategoryStatsService.findAllByUser(userMock));
    }
}