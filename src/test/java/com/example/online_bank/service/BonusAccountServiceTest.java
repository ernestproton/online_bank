package com.example.online_bank.service;

import com.example.online_bank.OnlineBankApplication;
import com.example.online_bank.domain.dto.BonusAccountDto;
import com.example.online_bank.domain.entity.Account;
import com.example.online_bank.domain.entity.BonusAccount;
import com.example.online_bank.domain.entity.User;
import com.example.online_bank.repository.AccountRepository;
import com.example.online_bank.repository.BonusAccountRepository;
import com.example.online_bank.repository.UserRepository;
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
    private UUID uuid;
    private User userMock;
    private Account accountMock1;
    private Account accountMock2;
    private BonusAccount bonusAccount1;
    private BonusAccount bonusAccount2;

    @BeforeEach
    void init() {
        uuid = UUID.randomUUID();
        userMock = User.builder()
                .uuid(uuid)
                .build();
        userRepository.save(userMock);
        accountMock1 = Account.builder()
                .holder(userMock)
                .accountNumber("0000011")
                .balance(TEN)
                .build();
        accountMock2 = Account.builder()
                .holder(userMock)
                .accountNumber("0000022")
                .balance(TWO)
                .build();
        accountRepository.saveAll(List.of(accountMock1, accountMock2));

        userMock.getAccounts().addAll(List.of(accountMock1, accountMock2));
        userRepository.save(userMock);

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
       log.info("saved entities- {}", countRows);
    }

    @Test
    @Transactional
    void successFindAllAccountsByUser() {
        List<BonusAccountDto> resultList = bonusAccountService.findAllAccountsByUser(uuid);
        log.info("resultList - {}", resultList);
        Assertions.assertEquals(2, resultList.size());
        var filterResult = resultList.stream()
                .map(BonusAccountDto::accountNumber)
                .filter(s -> s.equals(accountMock2.getAccountNumber()))
                .toList();
        Assertions.assertEquals(1, filterResult.size());
    }
}