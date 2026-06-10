package com.example.online_bank.service;

import com.example.online_bank.OnlineBankApplication;
import com.example.online_bank.domain.dto.BonusAccountDto;
import com.example.online_bank.domain.dto.OperationInfoDto;
import com.example.online_bank.domain.entity.Account;
import com.example.online_bank.domain.entity.BonusAccount;
import com.example.online_bank.domain.entity.User;
import com.example.online_bank.enums.CurrencyCode;
import com.example.online_bank.exception.ConvertBonusException;
import com.example.online_bank.repository.AccountRepository;
import com.example.online_bank.repository.BankPartnerRepository;
import com.example.online_bank.repository.BonusAccountRepository;
import com.example.online_bank.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.TWO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@RequiredArgsConstructor
@ContextConfiguration(classes = OnlineBankApplication.class)
@SpringBootTest(classes = OnlineBankApplication.class)
class BonusAccountServiceTest {
    @Autowired
    BonusAccountService bonusAccountService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    BonusAccountRepository bonusAccountRepository;
    @Autowired
    BankPartnerRepository bankPartnerRepository;
    private UUID uuid;
    private User userMock;
    private Account accountMock1;
    private Account accountMock2;
    private BonusAccount bonusAccount1;
    private BonusAccount bonusAccount2;

    @BeforeEach
    @Transactional
    void init() {
        bankPartnerRepository.deleteAll();
        bonusAccountRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
        uuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
        //Создаем пользователя
        userMock = User.builder()
                .uuid(uuid)
                .build();

        userRepository.save(userMock);
        //Создаем аккаунты
        accountMock1 = Account.builder()
                .holder(userMock)
                .currencyCode(CurrencyCode.RUB)
                .accountNumber("0000011")
                .balance(TEN)
                .build();
        accountMock2 = Account.builder()
                .holder(userMock)
                .currencyCode(CurrencyCode.RUB)
                .accountNumber("0000022")
                .balance(TWO)
                .build();

        userMock.getAccounts().addAll(List.of(accountMock1, accountMock2));

        bonusAccount1 = BonusAccount.builder()
                .account(accountMock1)
                .points(BigDecimal.valueOf(100))
                .user(userMock)
                .build();

        bonusAccount2 = BonusAccount.builder()
                .account(accountMock2)
                .points(BigDecimal.valueOf(150))
                .user(userMock)
                .build();
        bonusAccountRepository.saveAll(List.of(bonusAccount1, bonusAccount2));
        int countRows = bonusAccountRepository.findAll().size();
        log.info("saved entities - {}", countRows);
    }

    @Test
    void successFindAllAccountsByUser() {
        List<BonusAccountDto> resultList = bonusAccountService.findAllAccountsByUser(uuid);
        log.info("resultList - {}", resultList);
        assertEquals(2, resultList.size());
        var filterResult = resultList.stream()
                .map(BonusAccountDto::accountNumber)
                .filter(s -> s.equals(accountMock2.getAccountNumber()))
                .toList();
        assertEquals(1, filterResult.size());
    }

    @Test
    void successFindBonusAccountByAccountNumber() {
        BonusAccount result = bonusAccountService.findBonusAccountByAccountNumber("0000011");
        Assertions.assertEquals(new BigDecimal("100.00"), result.getPoints());
    }

    @Test
    void failureFindBonusAccountByAccountNumber() {
        bonusAccountRepository.deleteAll();
        assertThrows(EntityNotFoundException.class, () -> bonusAccountService.findBonusAccountByAccountNumber("0000011"));
    }

    @Test
    void failureConvertPoints_AccountNumberNotExists() {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> bonusAccountService.convertPoints("00002222", TEN));
        Assertions.assertEquals("Счет не существует", exception.getMessage());
    }

    @Test
    void failureConvertPoints_NotEnoughPoints() {
        ConvertBonusException exception = assertThrows(ConvertBonusException.class, () -> bonusAccountService.convertPoints("0000011", BigDecimal.valueOf(1000)));
        Assertions.assertEquals("Не хватает бонусов для выполнения операции.", exception.getMessage());
    }

    @Test
    @Transactional
    void successConvertPoints() {
        Account account = accountRepository.findByAccountNumber("0000011").orElseThrow(RuntimeException::new);
        log.info("account найден, {}", account);
        OperationInfoDto operationInfoDto = bonusAccountService.convertPoints("0000011", BigDecimal.valueOf(100));
        Assertions.assertEquals(new BigDecimal("50.0"), operationInfoDto.amount());
        Assertions.assertEquals("Обмен бонусов", operationInfoDto.description());
        BonusAccount bonusAccount = bonusAccountService.findBonusAccountByAccountNumber("0000011");
        Assertions.assertEquals(BigDecimal.ZERO, bonusAccount.getPoints());
    }

    @Test
    void successDepositBonus() {
        bonusAccountService.depositBonus("0000011", BigDecimal.valueOf(100));
        BonusAccount bonusAccount = bonusAccountService.findBonusAccountByAccountNumber("0000011");
        Assertions.assertEquals(new BigDecimal("200.00"), bonusAccount.getPoints());
    }

}