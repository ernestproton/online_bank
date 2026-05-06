package com.example.online_bank.service.listener;

import com.example.online_bank.domain.entity.UserCategoryStats;
import com.example.online_bank.domain.event.UpdateUserQuestEvent;
import com.example.online_bank.domain.event.UpdateUserStatEvent;
import com.example.online_bank.service.UserCategoryStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateUserStatEventListener {
    private final UserCategoryStatsService userCategoryStatsService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Async
    @EventListener
    public void updateUserStat(UpdateUserStatEvent event) {
        log.info("Обновление статистики");
        //1. Обновление статистики пользователя
        UserCategoryStats userCategoryStats = userCategoryStatsService.updateUserStat(event);

        //2. Отправка события для обновления таблицы user_quest
        UpdateUserQuestEvent updateUserQuestEvent = new UpdateUserQuestEvent(
                userCategoryStats.getCategory(),
                userCategoryStats.getUser(),
                userCategoryStats.getLastSpendDate(),
                event.userAccount(),
                userCategoryStats.getTotalSpend()
        );
        log.info("Отправляю ивент UserQuest");
        applicationEventPublisher.publishEvent(updateUserQuestEvent);
    }
}
