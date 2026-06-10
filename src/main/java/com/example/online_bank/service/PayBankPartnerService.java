package com.example.online_bank.service;

import com.example.online_bank.domain.dto.FinanceOperationDto;
import com.example.online_bank.domain.dto.OperationInfoDto;
import com.example.online_bank.domain.dto.PayDtoRequest;
import com.example.online_bank.domain.event.UpdateUserStatEvent;
import com.example.online_bank.enums.PartnerCategory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayBankPartnerService {
    private final BankService bankService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final BankPartnerService bankPartnerService;

    @Transactional
    public OperationInfoDto pay(PayDtoRequest payDtoRequest, UUID userUuid) {
        //1. снимаем деньги со счета пользователя
        FinanceOperationDto senderDto = createSenderDto(payDtoRequest);
        OperationInfoDto senderOperationResponse = bankService.makePayment(senderDto);

        //2. пополняем счет сервиса
        FinanceOperationDto serviceDto = createRecipientDto(payDtoRequest);
        OperationInfoDto partnerOperationResponse = bankService.makeDeposit(serviceDto);
        PartnerCategory category = bankPartnerService.findPartnerCategoryByName(payDtoRequest.serviceInfo().partnerName());

        //3. обновление статистики пользователя
        UpdateUserStatEvent updateUserStatEvent = new UpdateUserStatEvent(
                userUuid,
                partnerOperationResponse.amount(),
                LocalDate.now(),
                senderDto.accountNumber(),
                category
        );
                applicationEventPublisher.publishEvent(updateUserStatEvent);

        return senderOperationResponse;
    }

    private FinanceOperationDto createSenderDto(PayDtoRequest payDtoRequest){
        return new FinanceOperationDto(
                payDtoRequest.senderInfo().accountNumberFrom(),
                payDtoRequest.serviceRequestAmount(),
                createDescription(payDtoRequest.serviceInfo().partnerName()),
                bankPartnerService.findAccountCurrencyCodeByName(payDtoRequest.serviceInfo().partnerName())
        );
    }

    private FinanceOperationDto createRecipientDto(PayDtoRequest payDtoRequest) {
        return new FinanceOperationDto(
                bankPartnerService.findAccountNumber(payDtoRequest.serviceInfo().partnerName()),
                payDtoRequest.serviceRequestAmount(),
                createDescription(payDtoRequest.serviceInfo().partnerName()),
                bankPartnerService.findAccountCurrencyCodeByName(payDtoRequest.serviceInfo().partnerName())
        );
    }

    private String createDescription(String partnerName){
        return  "Оплата сервиса %s".formatted(partnerName);
    }
}
