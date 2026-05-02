package com.example.online_bank.service;


import com.example.online_bank.domain.dto.AccountDtoResponse;
import com.example.online_bank.domain.entity.Account;
import com.example.online_bank.domain.entity.BonusAccount;
import com.example.online_bank.domain.entity.User;
import com.example.online_bank.enums.CurrencyCode;
import com.example.online_bank.exception.EmptyDataException;
import com.example.online_bank.exception.NegativeAccountBalance;
import com.example.online_bank.mapper.AccountMapper;
import com.example.online_bank.repository.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.example.online_bank.util.CodeGeneratorUtil.generateAccountNumber;
import static java.math.BigDecimal.ZERO;


/**
 * Сервис по работе со счетами
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final UserService userService;

    /**
     * Создать счет для пользователя
     *
     * @param userUuid     Пользователь
     * @param currencyCode Код валюты
     */
    @Transactional()
    public AccountDtoResponse createAccountForUser(UUID userUuid, CurrencyCode currencyCode) {
        Arrays.stream(CurrencyCode.values())
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Переданный код валюты не найден"));

        User user = userService.findByUuid(userUuid).orElseThrow(EntityNotFoundException::new);

        Account account = Account
                .builder()
                .balance(ZERO)
                .holder(user)
                .accountNumber(generateAccountNumber(currencyCode))
                .currencyCode(currencyCode)
                .isBlocked(false)
                .build();
        account.setBonusAccount(
                BonusAccount.builder()
                        .user(user)
                        .account(account)
                        .points(ZERO)
                        .build());

        accountRepository.save(account);
        return accountMapper.toDtoResponse(account);
    }

    /**
     * Занести деньги на счет. Увеличивает остаток счета. Если счета не существует - ошибка.
     *
     * @param accountNumber Номер счета
     * @param amount        Сумма пополнения
     */
    @Transactional()
    public void depositMoney(String accountNumber, BigDecimal amount) {
        Account account = findByAccountNumber(accountNumber);
        account.setBalance(account.getBalance().add(amount));

    }

    /**
     * Уменьшает остаток счета. Остаток после операции не может быть отрицательный.
     * Если счета не существует - ошибка.
     *
     * @param accountNumber      Номер счета
     * @param countWithdrawMoney количество денег для списания
     */
    @Transactional
    public void withdrawMoney(String accountNumber, BigDecimal countWithdrawMoney) {
        Account account = findByAccountNumber(accountNumber);
        if (account.getBalance().compareTo(countWithdrawMoney) < 0) {
            throw new NegativeAccountBalance(countWithdrawMoney, account.getBalance());
        }
        account.setBalance(account.getBalance().subtract(countWithdrawMoney));
    }

    /**
     * Найти все счета пользователя
     *
     * @param holderUuid пользователь
     * @return Список всех счетов пользователя
     */
    @Transactional(readOnly = true)
    public List<AccountDtoResponse> findAllByHolder(UUID holderUuid) {
        List<Account> allAccounts = accountRepository.findAllByHolderUuid(holderUuid);
        if (allAccounts.isEmpty()) {
            log.warn("Нет счетов у пользователя {}", holderUuid);
            throw new EmptyDataException("Нет счетов у данного пользователя");
        }

        return allAccounts.stream()
                .map(accountMapper::toDtoResponse)
                .toList();
    }

    /**
     * Найти баланс по счету
     *
     * @param accountNumber Номер счета
     * @return Сумма баланса на счете
     */
    @Transactional(readOnly = true)
    public BigDecimal getBalance(String accountNumber) {
        return accountRepository.findBalanceByAccountNumber(accountNumber)
                .orElseThrow(() -> new EmptyDataException("Счета с номером %s не найден".formatted(accountNumber)));
    }

    /**
     * Найти по номеру счета
     *
     * @param accountNumber Номер счета
     * @return Сущность Account или выбрасывает ошибку если не удалось найти сущность.
     */
    @Transactional(readOnly = true)
    public Account findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Счет с номером %s не найден".formatted(accountNumber)));
    }

    public CurrencyCode findCurrencyCode(String accountNumber) {
        return accountRepository.findCurrencyCodeByAccountNumber(accountNumber)
                .orElseThrow(() -> new EmptyDataException(("Счет с номером %s не найден".formatted(accountNumber))));
    }
}