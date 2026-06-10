package com.example.online_bank.service;

import com.example.online_bank.domain.dto.*;
import com.example.online_bank.domain.event.UpdateUserStatEvent;
import com.example.online_bank.enums.CurrencyCode;
import com.example.online_bank.enums.OperationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.math.BigDecimal.TEN;

@ExtendWith(MockitoExtension.class)
class PayBankPartnerServiceTest {
    @InjectMocks
    PayBankPartnerService payBankPartnerService;
    @Mock
    BankService bankService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private BankPartnerService bankPartnerService;

    private static final UUID userUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private final SenderInfo senderInfo = new SenderInfo("000111");
    private final ServiceInfo serviceInfo = new ServiceInfo("TestName");
    private final PayDtoRequest payDtoRequest = new PayDtoRequest(senderInfo, serviceInfo, TEN);

    @Test
    void successPay() {
        Mockito.when(bankPartnerService.findAccountCurrencyCodeByName("TestName"))
                .thenReturn(CurrencyCode.RUB);
        Mockito.when(bankPartnerService.findAccountNumber("TestName"))
                .thenReturn("000222");
        OperationInfoDto senderOperationInfoDto = new OperationInfoDto(
                1L,
                LocalDateTime.of(2026, 6, 10, 4, 0),
                payDtoRequest.senderInfo().accountNumberFrom(),
                OperationType.WITHDRAW,
                payDtoRequest.serviceRequestAmount(),
                "Оплата сервиса TestName",
                CurrencyCode.RUB);

        OperationInfoDto partnerOperationInfoDto = new OperationInfoDto(
                1L,
                LocalDateTime.of(2026, 6, 10, 4, 0),
                payDtoRequest.senderInfo().accountNumberFrom(),
                OperationType.DEPOSIT,
                payDtoRequest.serviceRequestAmount(),
                "Оплата сервиса TestName",
                CurrencyCode.RUB);
        Mockito.when(bankService.makePayment(Mockito.any(FinanceOperationDto.class))).thenReturn(senderOperationInfoDto);
        Mockito.when(bankService.makeDeposit(Mockito.any(FinanceOperationDto.class))).thenReturn(partnerOperationInfoDto);
        Mockito.doNothing().when(applicationEventPublisher).publishEvent(Mockito.any(UpdateUserStatEvent.class));

        OperationInfoDto result = payBankPartnerService.pay(payDtoRequest, userUuid);
        Assertions.assertEquals(new BigDecimal("10"), result.amount());
        Assertions.assertEquals("Оплата сервиса TestName", result.description());
    }
}