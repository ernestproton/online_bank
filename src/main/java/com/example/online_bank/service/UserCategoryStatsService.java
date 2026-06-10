package com.example.online_bank.service;

import com.example.online_bank.domain.entity.User;
import com.example.online_bank.domain.entity.UserCategoryStats;
import com.example.online_bank.domain.event.UpdateUserStatEvent;
import com.example.online_bank.repository.UserCategoryStatsRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;

import static java.math.BigDecimal.ZERO;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCategoryStatsService {
    private final UserCategoryStatsRepository userCategoryStatsRepository;
    private final UserService userService;

    @Transactional
    public UserCategoryStats updateUserStat(UpdateUserStatEvent event) {
        Month month = event.operationDate().getMonth();
        YearMonth yearMonth = YearMonth.of(event.operationDate().getYear(), event.operationDate().getMonthValue());
        LocalDate startDate = LocalDate.of(event.operationDate().getYear(), month, 1);
        LocalDate endDate = LocalDate.of(event.operationDate().getYear(), month, yearMonth.lengthOfMonth());
        log.debug("Категория от ивента - {}",event.partnerCategory().toString());
        //1. ищем пользовательскую статистику между началом и концом месяца из переданного события
        UserCategoryStats userCategoryStats = userCategoryStatsRepository
                .findByUser_UuidAndCategoryAndLastSpendDateBetween(
                event.userUuid(),
                event.partnerCategory(),
                startDate,
                endDate
        ).orElseGet(() -> {
                    //2. если не нашли, то создаем ее
                    log.info("Статистика по пользователю не была найдена, поэтому будет создана");
                    return create(event);
                }
        );
        log.trace("Категория от статистики {}", userCategoryStats.getCategory());
        //3. увеличиваем общую потраченную сумму
        log.trace("Пользователь потратил в событие - {}", event.spendAmount());
        log.trace("Потрачено в статистике - {}", userCategoryStats.getTotalSpend());
        userCategoryStats.setTotalSpend(userCategoryStats.getTotalSpend().add(event.spendAmount()));
        log.debug("Пользователь потратил после обновления данных - {}", userCategoryStats.getTotalSpend());
        //4. увеличиваем количество трат в этом месяце по категории
        userCategoryStats.setCountSpendInMonth(userCategoryStats.getCountSpendInMonth() + 1);
        //5. изменяем последнюю дату траты
        userCategoryStats.setLastSpendDate(event.operationDate());
        //6. сохраняем изменения
        userCategoryStatsRepository.save(userCategoryStats);
        //7. Возвращаем измененную статистику
        return userCategoryStats;
    }

    private UserCategoryStats create(UpdateUserStatEvent event) {
        User user = userService.findByUuid(event.userUuid())
                .orElseThrow(EntityNotFoundException::new);

        return UserCategoryStats.builder()
                .category(event.partnerCategory())
                .user(user)
                .lastSpendDate(event.operationDate())
                .countSpendInMonth(0)
                .totalSpend(ZERO)
                .build();
    }

    public List<UserCategoryStats> findAllByUser(User user) {
        return userCategoryStatsRepository.findAllByUser(user);
    }
}
