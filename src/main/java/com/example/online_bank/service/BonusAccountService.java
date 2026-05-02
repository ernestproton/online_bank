package com.example.online_bank.service;

import com.example.online_bank.domain.dto.BonusAccountDto;
import com.example.online_bank.domain.dto.FinanceOperationDto;
import com.example.online_bank.domain.dto.OperationInfoDto;
import com.example.online_bank.domain.entity.BonusAccount;
import com.example.online_bank.exception.ConvertBonusException;
import com.example.online_bank.repository.BonusAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.example.online_bank.enums.CurrencyCode.RUB;

@Service
@RequiredArgsConstructor
@Slf4j
public class BonusAccountService {
    public static final String DESCRIPTION = "Пополнение бонусов";
    private static final String ERR_MSG = "Не хватает бонусов для выполнения операции.";
    private final BonusAccountRepository bonusAccountRepository;
    private final BankService bankService;

    public List<BonusAccountDto> findAllAccountsByUser(UUID userUuid) {
        return bonusAccountRepository.findAllByUser_Uuid((userUuid)).stream()
                .map(bonusAccount -> new BonusAccountDto(
                        bonusAccount.getAccount().getAccountNumber(),
                        bonusAccount.getPoints()))
                .toList();
    }

    public BonusAccount getBonusAccountByAccountNumber(String accountNumber) {
        return bonusAccountRepository.findByAccount_AccountNumber(accountNumber)
                .orElseThrow(EntityNotFoundException::new);
    }

    public OperationInfoDto convertPoints(String accountNumber, BigDecimal amount) {
        BonusAccount bonusAccount = bonusAccountRepository.findByAccount_AccountNumber(accountNumber).orElseThrow(
                () -> new EntityNotFoundException("Счет не существует")
        );

        if (amount.compareTo(bonusAccount.getPoints()) > 0) {
            throw new ConvertBonusException(ERR_MSG);
        }

        BigDecimal convertResult = amount.multiply(BigDecimal.valueOf(0.5));
        FinanceOperationDto operationDto = new FinanceOperationDto(
                accountNumber,
                convertResult,
                DESCRIPTION,
                RUB
        );
        return bankService.makeDeposit(operationDto);
    }

    public void depositBonus(String accountNumber, Integer points) {
        BonusAccount bonusAccount = getBonusAccountByAccountNumber(accountNumber);
        bonusAccount.setPoints(BigDecimal.valueOf(points));

        bonusAccountRepository.save(bonusAccount);
        log.info("deposit bonus account: {}", bonusAccount);
    }
}
