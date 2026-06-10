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
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.DOWN;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class QuestService {
    private final QuestRepository questRepository;
    private final Random random;
    private final UserRepository userRepository;
    private final UserQuestRepository userQuestRepository;
    private final QuestMapper questMapper;
    private final UserCategoryStatsService userStatsService;
    private final UserService userService;

    public Quest create(PartnerCategory category) {
        BigDecimal randomPoint = generateRandomPoint();
        LocalDate expDate = createExpDate();
        Quest quest = Quest.builder()
                .pointReward(randomPoint)
                .category(category)
                .dateOfExpiry(expDate)
                .build();
        questRepository.save(quest);
        List<User> verifiedUsers = userRepository.findAllIsVerified();

        List<UserQuest> userQuests = verifiedUsers.stream()
                .map(user ->
                        UserQuest.builder()
                                .isComplete(false)
                                .user(user)
                                .quest(quest)
                                .build()
                )
                .toList();
        userQuestRepository.saveAll(userQuests);
        return quest;
    }

    @Scheduled(cron = "0 0 0 1 * *")
    public List<QuestResponseDto> createRandomQuest() {
        PartnerCategory[] categories = PartnerCategory.values();
        Set<PartnerCategory> randomCategories = Stream
                .generate(() -> random.nextInt(0, PartnerCategory.values().length))
                .limit(3)
                .map(e -> categories[e])
                .collect(Collectors.toSet());

        return randomCategories.stream()
                .map(this::create)
                .map(questMapper::toDto)
                .toList();
    }

    public List<UserQuestResponseDto> findAllByUserQuest(UUID userUuid) {
        //1. Ищем пользователя
        User user = userService.findByUuid(userUuid)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        //2. Ищем статистику пользователя
        List<UserCategoryStats> allUserStats = userStatsService.findAllByUser(user);

        //3. Создаем мапу с категориями и количеством потраченного
        Map<PartnerCategory, BigDecimal> categoryPointsByStat = allUserStats.stream()
                .collect(toMap(UserCategoryStats::getCategory, UserCategoryStats::getTotalSpend));

        List<UserQuest> allUserQuests = userQuestRepository.findAllByUser_Uuid(userUuid);

        return allUserQuests.stream().map(
                userQuest -> {
                    Quest quest = userQuest.getQuest(); //получаем квест
                    //Получаем трату в месяц
                    BigDecimal totalSpendInMonth = categoryPointsByStat.getOrDefault(quest.getCategory(), ZERO);
                    //Делаем маппинг
                    return createUserProgress(userQuest, quest, totalSpendInMonth);
                }
        ).toList();
    }

    public List<Quest> findAllAvailable(LocalDate now) {
        //find all Quest where now before now.последнийДень
        return questRepository.findAllByDateOfExpiryIsAfter(now);
    }

    private BigDecimal generateRandomPoint() {
        return BigDecimal.valueOf(random.nextInt(1, 11) * 50L);
    }

    private UserQuestResponseDto createUserProgress(UserQuest userQuest, Quest quest, BigDecimal totalSpendInMonth) {
        var progress = calcProgressInPercent(quest, totalSpendInMonth);
        return new UserQuestResponseDto(
                generateQuestName(quest),
                quest.getCategory(),
                quest.getDateOfExpiry(),
                quest.getPointReward(),
                totalSpendInMonth,
                userQuest.getIsComplete(),
                progress
        );
    }

    private BigDecimal calcProgressInPercent(Quest quest, BigDecimal totalSpendInMonth) {
        return (totalSpendInMonth.multiply(BigDecimal.valueOf(100))).divide(quest.getPointReward(), 2, DOWN);
    }

    private String generateQuestName(Quest quest) {
        return "Квест № %s".formatted(quest.getId());
    }

    private LocalDate createExpDate() {
        YearMonth currentYearMonth = YearMonth.now();
        int lastDayOfMonth = currentYearMonth.lengthOfMonth();
        return LocalDate.of(currentYearMonth.getYear(), currentYearMonth.getMonth(), lastDayOfMonth);
    }
}
