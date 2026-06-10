package com.example.online_bank.service;

import com.example.online_bank.domain.dto.AccountDtoResponse;
import com.example.online_bank.domain.entity.Account;
import com.example.online_bank.domain.entity.User;
import com.example.online_bank.enums.CurrencyCode;
import com.example.online_bank.exception.EmptyDataException;
import com.example.online_bank.exception.NegativeAccountBalance;
import com.example.online_bank.mapper.AccountMapper;
import com.example.online_bank.repository.AccountRepository;
import com.example.online_bank.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.example.online_bank.enums.CurrencyCode.CNY;
import static com.example.online_bank.enums.CurrencyCode.RUB;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserService userService;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;

    @Mock
    private UserRepository userRepository;

    @Test
    void createAccountForUser() {
        //Arrange: Подготовка данных
        UUID uuidMock = UUID.fromString("11111111-1111-1111-1111-111111111111");
        User userMock = User.builder()
                .id(1L)
                .uuid(uuidMock)
                .build();
        when(userService.findByUuid(uuidMock)).thenReturn(Optional.of(userMock));

        Account accountMock = Account.builder()
                .id(1L)
                .balance(BigDecimal.ZERO)
                .holder(userMock)
                .build();
        when(accountRepository.save(any(Account.class))).thenReturn(accountMock);

        //Act: вызываем метод, который тестируем
        AccountDtoResponse dtoMock = new AccountDtoResponse(
                accountMock.getAccountNumber(),
                accountMock.getCurrencyCode(),
                accountMock.getBalance(),
                accountMock.getHolder().getName(),
                accountMock.getHolder().getSurname()
        );
        when(accountMapper.toDtoResponse(any(Account.class))).thenReturn(dtoMock);
        AccountDtoResponse result = accountService.createAccountForUser(uuidMock, CNY);

        //Assert: проверяем результат
        assertEquals(AccountDtoResponse.class, result.getClass());
        Assertions.assertNotNull(result);
    }

    @Test
    void depositMoney() {
        //Подготовка данных
        String accountNumber = "12345";
        BigDecimal initialBalance = TEN;
        BigDecimal deposit = BigDecimal.valueOf(5);
        Account account = Account.builder()
                .id(1L)
                .accountNumber(accountNumber)
                .balance(initialBalance)
                .build();

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));

        //Сверяем данные
        accountService.depositMoney(accountNumber, deposit);
        assertEquals(initialBalance.add(deposit), account.getBalance());
    }

    @Test
    void successWithdrawMoney() {
        String accountNumber = "12345";
        BigDecimal initialBalance = TEN;
        BigDecimal withdraw = BigDecimal.valueOf(5);
        Account account = Account.builder().id(1L).accountNumber(accountNumber).balance(initialBalance).build();

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
        accountService.withdrawMoney(accountNumber, withdraw);
        assertEquals(initialBalance.subtract(withdraw), account.getBalance());
    }

    @Test
    void failWithdrawMoney() {
        String accountNumber = "12345";
        BigDecimal withdraw = BigDecimal.valueOf(100);
        Account account = Account.builder().id(1L).accountNumber(accountNumber).balance(TEN).build();

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
        assertThrows(NegativeAccountBalance.class, () -> accountService.withdrawMoney(accountNumber, withdraw));
    }

    @Test
    void successFindAllByHolder() {
        String uuid = "11111111-1111-1111-1111-111111111111";
        User user = User.builder()
                .id(1L)
                .uuid(UUID.fromString(uuid))
                .name("John")
                .surname("Doe")
                .build();

        Account account = Account.builder()
                .id(1L)
                .accountNumber("67890")
                .holder(user)
                .balance(BigDecimal.valueOf(100))
                .currencyCode(RUB)
                .build();

        Account account2 = Account.builder()
                .id(2L)
                .accountNumber("12345")
                .holder(user)
                .balance(BigDecimal.valueOf(200))
                .currencyCode(CNY)
                .build();
        List<Account> accounts = List.of(account, account2);
        user.setAccounts(accounts);
        log.info(user.getAccounts().toString());

        when(accountRepository.findAllByHolderUuid(UUID.fromString(uuid))).thenReturn(accounts);

        List<AccountDtoResponse> result = accountService.findAllByHolder(UUID.fromString(uuid));
        log.info("result is {}", result.toString());
        assertNotNull(result);
        Assertions.assertArrayEquals(accounts.stream().map(accountMapper::toDtoResponse).toArray(), result.toArray());
    }

    @Test
    void failFindAllByHolder() {
        String uuid = "11111111-1111-1111-1111-111111111111";
        Assertions.assertEquals(Collections.EMPTY_LIST, accountService.findAllByHolder(UUID.fromString(uuid)));
    }

    @Test
    void successGetBalance() {
        String accountNumber = "12345";

        when(accountRepository.findBalanceByAccountNumber(accountNumber)).thenReturn(Optional.of(BigDecimal.valueOf(100)));

        BigDecimal balance = accountService.getBalance(accountNumber);
        assertDoesNotThrow(() -> accountService.getBalance(accountNumber));
        assertEquals(BigDecimal.valueOf(100), balance);
    }

    @Test
    void failGetBalance() {
        String accountNumber = "12345";

        when(accountRepository.findBalanceByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        assertThrows(EmptyDataException.class, () -> accountService.getBalance(accountNumber));
    }

    @Test
    void successFindByAccountNumber() {
        String accountNumber = "12345";
        Account account = Account.builder().id(1L).accountNumber(accountNumber).build();
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.ofNullable(account));

        Account result = accountService.findByAccountNumber(accountNumber);

        assertDoesNotThrow(() -> accountService.findByAccountNumber(accountNumber));
        assertNotNull(account);
        assertEquals(account.getAccountNumber(), result.getAccountNumber());
    }

    @Test
    void failFindByAccountNumber() {
        String accountNumber = "12345";

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> accountService.findByAccountNumber(accountNumber));
    }

    @Test
    void successFindCurrencyCode() {
        String accountNumber = "12345";
        Account account = Account.builder().id(1L).accountNumber(accountNumber).currencyCode(RUB).build();
        when(accountRepository.findCurrencyCodeByAccountNumber(accountNumber)).thenReturn(Optional.of(RUB));

        CurrencyCode result = accountService.findCurrencyCode(accountNumber);

        assertNotNull(result);
        assertDoesNotThrow(() -> accountService.findCurrencyCode(accountNumber));
        assertEquals(account.getCurrencyCode(), result);
    }

    @Test
    void failFindCurrencyCode() {
        String accountNumber = "12345";
        when(accountRepository.findCurrencyCodeByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        assertThrows(EmptyDataException.class, () -> accountService.findCurrencyCode(accountNumber));
    }
}