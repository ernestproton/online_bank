package com.example.online_bank.service;

import com.example.online_bank.domain.dto.BuyCurrencyDto;
import com.example.online_bank.domain.dto.ConvertCurrencyResponseDto;
import com.example.online_bank.domain.dto.FinanceOperationDto;
import com.example.online_bank.domain.dto.OperationInfoDto;
import com.example.online_bank.enums.CurrencyCode;
import com.example.online_bank.mapper.OperationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.online_bank.enums.OperationType.DEPOSIT;
import static com.example.online_bank.enums.OperationType.WITHDRAW;

@Service
@RequiredArgsConstructor
public class BankService {
    private final AccountService accountService;
    private final OperationService operationService;
    private final OperationMapper operationMapper;
    private final CurrencyConversionService currencyConversionService;

    /**
     * Делать платеж:
     * <p>
     * Проверяем что счет принадлежит пользователю.
     * Производит списание со счета. Записывает операцию в историю
     *
     * @param dto Номер счета, выбранный код валюты, описание, количестве денег
     * @return Возвращает информацию об операции списания со счета
     */
    @Transactional()
    public OperationInfoDto makePayment(FinanceOperationDto dto) {
        CurrencyCode accountCurrencyCode = accountService.findCurrencyCode(dto.accountNumber());

        ConvertCurrencyResponseDto convertedResult = currencyConversionService.convert(
                dto.selectedCurrencyCode(),
                accountCurrencyCode,
                dto.amount()
        );

        accountService.withdrawMoney(dto.accountNumber(), convertedResult.targetConvertedAmount());


        return operationMapper.toOperationInfoDto(operationService.createOperation(
                LocalDateTime.now(),
                WITHDRAW,
                convertedResult.targetConvertedAmount(),
                dto.description(),
                dto.accountNumber(),
                dto.selectedCurrencyCode())
        );
    }

    /**
     * Делать зачисление: на вход - номер счета, сумма, описание.
     * Зачисляет на банковский счет деньги и записывает операцию в историю.
     *
     * @param dto Содержит информацию о номере счета, код валюты, описание, количестве денег
     * @return Возвращает информацию об операции пополнении счета
     */

    @Transactional()
    public OperationInfoDto makeDeposit(FinanceOperationDto dto) {
         CurrencyCode accountCurrencyCode = accountService.findCurrencyCode(dto.accountNumber());

        ConvertCurrencyResponseDto convertedResult = currencyConversionService.convert(
                dto.selectedCurrencyCode(),
                accountCurrencyCode,
                dto.amount()
        );

        accountService.depositMoney(dto.accountNumber(), convertedResult.targetConvertedAmount());

        //TODO перевести на ивенты
        return operationMapper.toOperationInfoDto(operationService.createOperation(
                LocalDateTime.now(),
                DEPOSIT,
                convertedResult.targetConvertedAmount(),
                dto.description(),
                dto.accountNumber(),
                dto.selectedCurrencyCode())
        );
    }

    /**
     * Покупка валюты. Производит списание суммы со счета {@code dto.baseTargetAccount},
     * делает конвертацию в валюту {@code dto.targetAccountNumber}
     */
    @Transactional()
    public List<OperationInfoDto> buyCurrency(BuyCurrencyDto dto) {
        CurrencyCode targetCurrencyCode = accountService.findCurrencyCode(dto.targetAccountNumber());

        List<String> descriptions = createDescriptions(dto);

        final String paymentDescription = descriptions.getFirst();
        final String depositDescription = descriptions.getLast();

        OperationInfoDto paymentOperation = makePayment(
                new FinanceOperationDto(dto.baseAccountNumber(),
                        dto.amount(),
                        paymentDescription,
                        targetCurrencyCode)
        );

        OperationInfoDto depositOperation = makeDeposit(new FinanceOperationDto(
                dto.targetAccountNumber(),
                dto.amount(),
                depositDescription,
                targetCurrencyCode
        ));
        return List.of(paymentOperation, depositOperation);
    }

    private List<String> createDescriptions(BuyCurrencyDto dto) {
        String baseAccountPostfix = "валюты со счета %s".formatted(dto.baseAccountNumber());
        String targetAccountPostfix = "валюты со счета %s".formatted(dto.targetAccountNumber());
        return List.of("Продажа %s".formatted(baseAccountPostfix), "Покупка %s".formatted(targetAccountPostfix));
    }
}