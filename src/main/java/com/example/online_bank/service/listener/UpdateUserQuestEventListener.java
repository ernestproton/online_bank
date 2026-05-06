package com.example.online_bank.service.listener;

import com.example.online_bank.domain.entity.UserQuest;
import com.example.online_bank.domain.event.UpdateBonusAccountEvent;
import com.example.online_bank.domain.event.UpdateUserQuestEvent;
import com.example.online_bank.repository.UserQuestRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateUserQuestEventListener {
    private final UserQuestRepository userQuestRepository;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    @Async
    public void handle(UpdateUserQuestEvent event) {
        log.info("Проверка выполнения квеста");
        //1. Находим квест с категорией и пользователем.
        UserQuest userQuest = userQuestRepository.findByUserAndQuest_CategoryAndQuest_DateOfExpiryIsAfter(
                event.user(),
                event.category(),
                event.lastSpendDate()
        ).orElseThrow(() -> {
            log.error("Квесты пользователя не были найдены");
            return new EntityNotFoundException("Прогресс пользователя по данным квестам не найден");
        });
        log.info("Потрачено в событие - {}", event.totalSpendAmount());
        log.info("Сколько необходимо потратить для квеста - {}", userQuest.getQuest().getPointReward());

        //2. Если потраченного >= чем требуется для данного квеста, то создаем событие на обновление счета
        if (event.totalSpendAmount().compareTo(userQuest.getQuest().getPointReward()) >= 0) {
            userQuest.setIsComplete(true);
            log.info("Квест выполнен");
            userQuestRepository.save(userQuest);

            BigDecimal points = userQuest.getQuest().getPointReward();
            String userAccountNumber = event.userAccountNumber();

            eventPublisher.publishEvent(new UpdateBonusAccountEvent(points, userAccountNumber));
        }
    }
}
