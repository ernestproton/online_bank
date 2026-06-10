package com.example.online_bank.service;

import com.example.online_bank.domain.dto.BankPartnerDto;
import com.example.online_bank.domain.entity.Account;
import com.example.online_bank.domain.entity.BankPartner;
import com.example.online_bank.enums.PartnerCategory;
import com.example.online_bank.repository.AccountRepository;
import com.example.online_bank.repository.BankPartnerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.example.online_bank.enums.CurrencyCode.RUB;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankPartnerServiceTest {
    @InjectMocks
    private BankPartnerService bankPartnerService;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private BankPartnerRepository bankPartnerRepository;
    private static final String TEST_BANK_PARTNER_NAME = "testBankPartnerName";

    @Test
    void successCreate() {
        bankPartnerService.create(TEST_BANK_PARTNER_NAME, PartnerCategory.FOOD);
        verify(accountRepository, times(2)).save(Mockito.any(Account.class));
        verify(bankPartnerRepository, times(1)).save(Mockito.any(BankPartner.class));
    }

    @Test()
    void successFindAccountCurrencyCodeByName() {
        when(bankPartnerRepository.findCurrencyCodeByPartnerName(TEST_BANK_PARTNER_NAME))
                .thenReturn(Optional.of(RUB));

        Assertions.assertDoesNotThrow(() -> bankPartnerService.findAccountCurrencyCodeByName(TEST_BANK_PARTNER_NAME));
    }

    @Test
    void failureFindAccountCurrencyCodeByName() {
        when(bankPartnerRepository.findCurrencyCodeByPartnerName(TEST_BANK_PARTNER_NAME))
                .thenReturn(Optional.empty());
        EntityNotFoundException e = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> bankPartnerService.findAccountCurrencyCodeByName(TEST_BANK_PARTNER_NAME));

        assertEquals("Партнер банка не найден", e.getMessage());
    }

    @Test
    void successFindAccountNumber() {

        when(bankPartnerRepository.findAccountNumberByPartnerName(TEST_BANK_PARTNER_NAME))
                .thenReturn(Optional.of("00001"));

        Assertions.assertDoesNotThrow(() -> bankPartnerService.findAccountNumber(TEST_BANK_PARTNER_NAME));
    }

    @Test
    void failureFindAccountNumber() {
        when(bankPartnerRepository.findAccountNumberByPartnerName(TEST_BANK_PARTNER_NAME))
                .thenReturn(Optional.empty());
        EntityNotFoundException e = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> bankPartnerService.findAccountNumber(TEST_BANK_PARTNER_NAME));

        assertEquals("Партнер банка не найден", e.getMessage());
    }

    @Test
    void successFindAll() {
        BankPartner bankPartner1 = BankPartner.builder()
                .name("bank partner 1")
                .partnerCategory(PartnerCategory.FOOD)
                .build();

        BankPartner bankPartner2 = BankPartner.builder()
                .name("bank partner 2")
                .partnerCategory(PartnerCategory.ENTERTAINMENT)
                .build();

        BankPartner bankPartner3 = BankPartner.builder()
                .name("bank partner 3")
                .partnerCategory(PartnerCategory.MEDICINE)
                .build();

        when(bankPartnerRepository.findAll())
                .thenReturn(List.of(bankPartner1, bankPartner2, bankPartner3));

        List<BankPartnerDto> resultList = bankPartnerService.getAll();
        Assertions.assertEquals(3, resultList.size());
    }

    @Test
    void successFindPartnerCategoryByName() {

        when(bankPartnerRepository.findByNamePartnerCategory(TEST_BANK_PARTNER_NAME))
                .thenReturn(Optional.of(PartnerCategory.MEDICINE));

        Assertions.assertDoesNotThrow(() -> bankPartnerService.findPartnerCategoryByName(TEST_BANK_PARTNER_NAME));
    }

    @Test
    void failureFindPartnerCategoryByName() {
        when(bankPartnerRepository.findByNamePartnerCategory(TEST_BANK_PARTNER_NAME))
                .thenReturn(Optional.empty());

        EntityNotFoundException e = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> bankPartnerService.findPartnerCategoryByName(TEST_BANK_PARTNER_NAME));

        assertEquals("Партнер банка не найден", e.getMessage());
    }

    @Test
    void findPartnerByNameContaining(){
        BankPartner bankPartner1 = BankPartner.builder()
                .name("bank partner 1")
                .partnerCategory(PartnerCategory.FOOD)
                .build();

        BankPartner bankPartner2 = BankPartner.builder()
                .name("bank partner 2")
                .partnerCategory(PartnerCategory.ENTERTAINMENT)
                .build();

        BankPartner bankPartner3 = BankPartner.builder()
                .name("bank partner 3")
                .partnerCategory(PartnerCategory.MEDICINE)
                .build();

        when(bankPartnerRepository.findTop5ByNameContainingIgnoreCase("bank"))
                .thenReturn(List.of(bankPartner1, bankPartner2, bankPartner3));

        List<BankPartnerDto> result = bankPartnerService.findPartnerByNameContaining("bank");
        Assertions.assertEquals(3, result.size());
    }


}