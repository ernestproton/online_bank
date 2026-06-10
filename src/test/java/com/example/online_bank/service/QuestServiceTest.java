package com.example.online_bank.service;

import com.example.online_bank.domain.dto.QuestResponseDto;
import com.example.online_bank.domain.dto.UserQuestResponseDto;
import com.example.online_bank.domain.entity.Quest;
import com.example.online_bank.domain.entity.User;
import com.example.online_bank.domain.entity.UserCategoryStats;
import com.example.online_bank.domain.entity.UserQuest;
import com.example.online_bank.enums.PartnerCategory;
import com.example.online_bank.mapper.QuestMapper;
import com.example.online_bank.repository.QuestRepository;
import com.example.online_bank.repository.UserQuestRepository;
import com.example.online_bank.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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
import java.util.*;

import static com.example.online_bank.enums.PartnerCategory.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class QuestServiceTest {
    @InjectMocks
    private QuestService questService;
    @Mock
    private QuestRepository questRepository;
    @Mock
    private Random random;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserQuestRepository userQuestRepository;
    @Mock
    private QuestMapper questMapper;
    @Mock
    private UserCategoryStatsService userStatsService;
    @Mock
    private UserService userService;

    private final User userMock = User.builder()
            .uuid(UUID.fromString("11111111-1111-1111-1111-111111111111"))
            .build();

    @Test
    void successCreate() {
        Quest questMock = Quest.builder()
                .pointReward(BigDecimal.valueOf(400L))
                .category(PartnerCategory.MEDICINE)
                .dateOfExpiry(LocalDate.of(2026, 6, 30))
                .build();

        User userMock1 = User.builder()
                .isVerified(true).build();
        User userMock2 = User.builder()
                .isVerified(true).build();
        User userMock3 = User.builder()
                .isVerified(true).build();
        List<User> users = List.of(userMock1, userMock2, userMock3);

        when(random.nextInt(1, 11))
                .thenReturn(8); //потом умножиться на 50 и станет 400

        when(questRepository.save(Mockito.any(Quest.class)))
                .thenReturn(questMock);
        when(userRepository.findAllIsVerified()).thenReturn(users);
        when(userQuestRepository.saveAll(anyList())).thenReturn(Collections.emptyList());


        Quest quest = questService.create(PartnerCategory.MEDICINE);
        log.info("quest- {}", quest);
        Assertions.assertEquals(BigDecimal.valueOf(400), quest.getPointReward());
        Assertions.assertEquals(PartnerCategory.MEDICINE, quest.getCategory());
    }

    @Test
    void successCreateRandomQuest() {
        Quest questMock1 = Quest.builder()
                .pointReward(BigDecimal.valueOf(200L))
                .category(PartnerCategory.FOOD)
                .dateOfExpiry(LocalDate.of(2026, 6, 30))
                .build();

        Quest questMock2 = Quest.builder()
                .pointReward(BigDecimal.valueOf(100L))
                .category(ENTERTAINMENT)
                .dateOfExpiry(LocalDate.of(2026, 6, 30))
                .build();

        Quest questMock3 = Quest.builder()
                .pointReward(BigDecimal.valueOf(150L))
                .category(PartnerCategory.MEDICINE)
                .dateOfExpiry(LocalDate.of(2026, 6, 30))
                .build();

        QuestResponseDto dto1 = new QuestResponseDto(
                PartnerCategory.FOOD,
                questMock1.getDateOfExpiry(),
                Integer.valueOf(String.valueOf(questMock1.getPointReward())),
                0
        );
        QuestResponseDto dto2 = new QuestResponseDto(
                ENTERTAINMENT,
                questMock2.getDateOfExpiry(),
                Integer.valueOf(String.valueOf(questMock2.getPointReward())),
                0
        );
        QuestResponseDto dto3 = new QuestResponseDto(
                PartnerCategory.MEDICINE,
                questMock3.getDateOfExpiry(),
                Integer.valueOf(String.valueOf(questMock3.getPointReward())),
                0
        );

        Mockito.when(random.nextInt(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(0, 1, 2);

        Mockito.when(questRepository.save(Mockito.any(Quest.class)))
                .thenReturn(questMock1, questMock2, questMock3);

        Mockito.when(questMapper.toDto(Mockito.any(Quest.class)))
                .thenReturn(dto1, dto2, dto3);

        List<QuestResponseDto> result = questService.createRandomQuest();
        Assertions.assertNotNull(result);
        Assertions.assertEquals(3, result.size());
    }


    @Test
    void failureFindAllByUserQuest() {
        when(userService.findByUuid(userMock.getUuid())).thenThrow(EntityNotFoundException.class);
        Assertions.assertThrows(EntityNotFoundException.class, () -> questService.findAllByUserQuest(userMock.getUuid()));

    }

    @Test
    void successFindAllByUserQuest() {

        Quest questMock1 = Quest.builder()
                .pointReward(BigDecimal.valueOf(200L))
                .category(PartnerCategory.FOOD)
                .dateOfExpiry(LocalDate.of(2026, 6, 30))
                .build();

        Quest questMock2 = Quest.builder()
                .pointReward(BigDecimal.valueOf(100L))
                .category(ENTERTAINMENT)
                .dateOfExpiry(LocalDate.of(2026, 6, 30))
                .build();

        Quest questMock3 = Quest.builder()
                .pointReward(BigDecimal.valueOf(150L))
                .category(PartnerCategory.MEDICINE)
                .dateOfExpiry(LocalDate.of(2026, 6, 30))
                .build();

        UserCategoryStats userCategoryStats1 = UserCategoryStats.builder()
                .user(userMock)
                .category(FOOD)
                .totalSpend(BigDecimal.valueOf(300))
                .build();

        UserCategoryStats userCategoryStats2 = UserCategoryStats.builder()
                .user(userMock)
                .category(ENTERTAINMENT)
                .totalSpend(BigDecimal.valueOf(150))
                .build();

        UserCategoryStats userCategoryStats3 = UserCategoryStats.builder()
                .user(userMock)
                .category(MEDICINE)
                .totalSpend(BigDecimal.valueOf(200))
                .build();

        UserQuest userQuestMock1 = UserQuest.builder()
                .user(userMock)
                .quest(questMock1)
                .isComplete(false)
                .build();

        UserQuest userQuestMock2 = UserQuest.builder()
                .user(userMock)
                .quest(questMock2)
                .isComplete(false)
                .build();

        UserQuest userQuestMock3 = UserQuest.builder()
                .user(userMock)
                .quest(questMock3)
                .isComplete(false)
                .build();

        List<UserQuest> userQuestMockList = List.of(userQuestMock1, userQuestMock2, userQuestMock3);


        List<UserCategoryStats> stats = List.of(userCategoryStats1, userCategoryStats2, userCategoryStats3);

        when(userService.findByUuid(userMock.getUuid())).thenReturn(Optional.of(userMock));
        when(userStatsService.findAllByUser(userMock))
                .thenReturn(stats);
        when(userQuestRepository.findAllByUser_Uuid(userMock.getUuid()))
                .thenReturn(userQuestMockList);

        List<UserQuestResponseDto> result = questService.findAllByUserQuest(userMock.getUuid());
        Assertions.assertEquals(3, result.size());
    }

    @Test
    void successFindAllAvailable(){
        Quest questMock1 = Quest.builder()
                .pointReward(BigDecimal.valueOf(200L))
                .category(PartnerCategory.FOOD)
                .dateOfExpiry(LocalDate.of(2026, 6, 30))
                .build();

        Quest questMock2 = Quest.builder()
                .pointReward(BigDecimal.valueOf(100L))
                .category(ENTERTAINMENT)
                .dateOfExpiry(LocalDate.of(2026, 6, 30))
                .build();

        Quest questMock3 = Quest.builder()
                .pointReward(BigDecimal.valueOf(150L))
                .category(PartnerCategory.MEDICINE)
                .dateOfExpiry(LocalDate.of(2026, 6, 30))
                .build();

        LocalDate now = LocalDate.of(2026, 6, 10);
        when(questRepository.findAllByDateOfExpiryIsAfter(now))
                .thenReturn(List.of(questMock1, questMock2, questMock3));

        List<Quest> result = questService.findAllAvailable(now);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(3, result.size());

    }

}