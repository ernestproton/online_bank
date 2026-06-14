package com.example.online_bank.service;


import com.example.online_bank.domain.dto.OperationInfoDto;
import com.example.online_bank.domain.entity.Account;
import com.example.online_bank.domain.entity.Operation;
import com.example.online_bank.enums.CurrencyCode;
import com.example.online_bank.enums.OperationType;
import com.example.online_bank.mapper.OperationMapper;
import com.example.online_bank.repository.AccountRepository;
import com.example.online_bank.repository.OperationRepository;
import com.example.online_bank.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationService {
    private final OperationRepository operationRepository;
    private final OperationMapper operationMapper;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountService accountService;

    /**
     * Создать операцию
     *
     * @param createdAt     время операции
     * @param operationType тип операции
     * @param amount        количество денег
     * @param description   описание
     * @param accountNumber номер счета
     * @param currencyCode  код валюты
     */
    @Transactional()
    public Operation createOperation(
            @NonNull LocalDateTime createdAt,
            @NonNull OperationType operationType,
            @NonNull BigDecimal amount,
            @NonNull String description,
            @NonNull String accountNumber,
            @NonNull CurrencyCode currencyCode) {
        Account accountNumberEntity = accountService.findByAccountNumber(accountNumber);
        System.out.println(accountNumberEntity);
        log.info("{}", accountNumberEntity);
        Operation operation = Operation.builder()
                .operationType(operationType)
                .amount(amount)
                .description(description)
                .currencyCode(currencyCode)
                .createdAt(createdAt)
                .account(accountNumberEntity)
                .build();

        operationRepository.save(operation);
        return operation;
    }

    /**
     * Найти список операций по номеру счета(порционно)
     *
     * @param accountNumber Номер счёта
     * @return Список операций, отсортированных по дате(desc)
     */
    @Transactional(readOnly = true)
    public List<OperationInfoDto> findAllByAccount(String accountNumber, int page, int size) {
        if (!accountRepository.existsByAccountNumber(accountNumber)) {
            throw new EntityNotFoundException("Счет %s не существует".formatted(accountNumber));
        }
        log.info("Показ операций по счету {} (начало с индекса - {}, размер - {})", accountNumber, page, size);

        return operationRepository.findAllByAccount_AccountNumber(
                        accountNumber,
                        createPageRequest(page, size))
                .stream()
                .map(operationMapper::toOperationInfoDto)
                .toList();
    }

    /**
     * Найти все операции по пользователю(порционно)
     *
     * @param userUuid Uuid пользователя
     * @param page     индекс отображения
     * @param size     размер
     * @return список отфильтрованных операций(порционно)
     */
    @Transactional(readOnly = true)
    public List<OperationInfoDto> findAllByUserPaged(UUID userUuid, int page, int size) {
        if (!userRepository.existsByUuid(userUuid)) {
            throw new EntityNotFoundException("Пользователь с uuid %s не существует".formatted(userUuid));
        }
        log.info("Поиск операций по пользователю {}. Начало с индекса {}, размер {}", userUuid.toString(), page, size);

        return operationRepository.findAllByAccount_Holder_Uuid(userUuid, createPageRequest(page, size)).stream()
                .map(operationMapper::toOperationInfoDto)
                .toList();
    }

    private PageRequest createPageRequest(int page, int size) {
        return PageRequest.of(page, size, Sort.by(DESC, "createdAt"));
    }

    public Operation findById(Long id) {
        return operationRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }
}