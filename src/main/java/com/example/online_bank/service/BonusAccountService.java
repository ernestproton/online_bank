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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.example.online_bank.enums.CurrencyCode.RUB;

@Service
@RequiredArgsConstructor
@Slf4j
public class BonusAccountService {
    public static final String DESCRIPTION = "Обмен бонусов";
    private static final String ERR_MSG = "Не хватает бонусов для выполнения операции.";
    public static final double CONVERT_COEFFICIENT = 0.5;

    private final BonusAccountRepository bonusAccountRepository;
    private final BankService bankService;

    public List<BonusAccountDto> findAllAccountsByUser(UUID userUuid) {
        return bonusAccountRepository.findAllByUser_Uuid((userUuid)).stream()
                .map(bonusAccount -> new BonusAccountDto(
                        bonusAccount.getAccount().getAccountNumber(),
                        bonusAccount.getPoints()))
                .toList();
    }

    public BonusAccount findBonusAccountByAccountNumber(String accountNumber) {
        return bonusAccountRepository.findByAccount_AccountNumber(accountNumber)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Transactional
    public OperationInfoDto convertPoints(String accountNumber, BigDecimal amountPoints) {
        BonusAccount bonusAccount = bonusAccountRepository.findByAccount_AccountNumber(accountNumber).orElseThrow(
                () -> new EntityNotFoundException("Счет не существует")
        );

        if (amountPoints.compareTo(bonusAccount.getPoints()) > 0) {
            log.warn("Не хватает бонусов для снятия");
            throw new ConvertBonusException(ERR_MSG);
        }

        BigDecimal convertResult = amountPoints.multiply(BigDecimal.valueOf(CONVERT_COEFFICIENT));
        FinanceOperationDto operationDto = new FinanceOperationDto(
                accountNumber,
                convertResult,
                DESCRIPTION,
                RUB
        );
        withdrawBonus(accountNumber, amountPoints);
        return bankService.makeDeposit(operationDto);
    }

    @Transactional
    public void depositBonus(String accountNumber, BigDecimal points) {
        BonusAccount bonusAccount = findBonusAccountByAccountNumber(accountNumber);
        bonusAccount.setPoints(bonusAccount.getPoints().add(points));

        bonusAccountRepository.save(bonusAccount);
    }

    private void withdrawBonus(String accountNumber, BigDecimal points) {
        BonusAccount bonusAccount = findBonusAccountByAccountNumber(accountNumber);
        bonusAccount.setPoints(bonusAccount.getPoints().subtract(points));

        bonusAccountRepository.save(bonusAccount);
    }
}
