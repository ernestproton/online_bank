package com.example.online_bank.service;

import com.example.online_bank.domain.dto.BankPartnerDto;
import com.example.online_bank.domain.entity.Account;
import com.example.online_bank.domain.entity.BankPartner;
import com.example.online_bank.enums.CurrencyCode;
import com.example.online_bank.enums.PartnerCategory;
import com.example.online_bank.repository.AccountRepository;
import com.example.online_bank.repository.BankPartnerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.online_bank.enums.CurrencyCode.RUB;
import static com.example.online_bank.util.CodeGeneratorUtil.generateAccountNumber;
import static java.math.BigDecimal.ZERO;

@Service
@RequiredArgsConstructor
public class BankPartnerService {
    public static final String PARTNER_NOT_FOUND_MSG = "Партнер банка не найден";
    private final AccountRepository accountRepository;
    private final BankPartnerRepository bankPartnerRepository;

    //fixme используется хардкод в коде
    @Transactional
    public void create(String name, PartnerCategory category) {
        Account partnerAccount = Account.builder()
                .balance(ZERO)
                .accountNumber(generateAccountNumber(RUB))
                .isBlocked(false)
                .currencyCode(RUB)
                .build();

        accountRepository.save(partnerAccount);

        BankPartner bankPartner = BankPartner.builder()
                .name(name)
                .partnerCategory(category)
                .account(partnerAccount)
                .build();
        bankPartnerRepository.save(bankPartner);
        partnerAccount.setBankPartner(bankPartner);
        accountRepository.save(partnerAccount);
    }

    public CurrencyCode findAccountCurrencyCodeByName(String partnerName) {
        return bankPartnerRepository.findCurrencyCodeByPartnerName(partnerName)
                .orElseThrow(() -> new EntityNotFoundException(PARTNER_NOT_FOUND_MSG));
    }

    public String findAccountNumber(String partnerName) {
        return bankPartnerRepository.findAccountNumberByPartnerName(partnerName).orElseThrow(
                () -> new EntityNotFoundException(PARTNER_NOT_FOUND_MSG)
        );
    }

    public List<BankPartnerDto> getAll() {
        //todo переделать на маппер
        return bankPartnerRepository.findAll().stream()
                .map(e -> new BankPartnerDto(e.getName(), e.getPartnerCategory()))
                .toList();
    }

    public PartnerCategory findPartnerCategoryByName(String name) {
        return bankPartnerRepository.findByNamePartnerCategory(name)
                .orElseThrow(() -> new EntityNotFoundException(PARTNER_NOT_FOUND_MSG));
    }

    public List<BankPartnerDto> findPartnerByNameContaining(String name) {
       return bankPartnerRepository.findTop5ByNameContainingIgnoreCase(name).stream()
                .map(bankPartnerCategory -> new BankPartnerDto(bankPartnerCategory.getName(),
                                bankPartnerCategory.getPartnerCategory()))
                .toList();
    }
}
