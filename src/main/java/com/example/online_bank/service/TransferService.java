package com.example.online_bank.service;

import com.example.online_bank.domain.dto.FinanceOperationDto;
import com.example.online_bank.domain.dto.OperationInfoDto;
import com.example.online_bank.domain.dto.TransferDto;
import com.example.online_bank.domain.model.AbstractBank;
import com.example.online_bank.enums.CurrencyCode;
import com.example.online_bank.exception.TransferException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;


@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {
    private final RestTemplate restTemplate;
    private final AbstractBank bank;
    private static final String POSTFIX_URL = "/api/operation/receive";
    private final AccountService accountService;
    private final BankService bankService;

    /**
     * Получить информацию о текущем банке
     */
    public String getBankInfo() {
        return bank.getName();
    }

    /**
     * Перевод в партнерский банк(на другом порту или по одному)
     * <p>
     * На вход - имя банка, сумма, описание, фио от кого пришло.
     *
     * @param transferDto Содержит информацию об отправителе и получателе,
     *                    количестве отправленных денег, описание и время операции.
     *                    <p>
     *                    Если названия банков отправителя и получателя одинаковы - то выполняется пополнение.
     *                    <p>
     *                    Если нет - метод отправит POST запрос по адресу - bank.partner.url + /operations/receive.
     *                    Делает списание со счета отправителя и начисление на счет получателя.
     */
    @Transactional
    public OperationInfoDto transferMoney(
            TransferDto transferDto) {
        CurrencyCode recipientCurrencyCode = accountService.findCurrencyCode(transferDto.recipientInfo().accountNumberTo());

        //снимаем деньги со счета отправителя
        OperationInfoDto senderOperationResponse = bankService.makePayment(
                new FinanceOperationDto(
                        transferDto.senderInfo().accountNumberFrom(),
                        transferDto.recipientRequestAmount(),
                        transferDto.description(),
                        recipientCurrencyCode)
        );

        FinanceOperationDto recipientDto = createRecipientDto(transferDto, recipientCurrencyCode);
        sendRequest(recipientDto);
        return senderOperationResponse;
    }

    private void sendRequest(FinanceOperationDto dto) {
        String url = createTransferUrl(bank);
        try {
            RequestEntity<FinanceOperationDto> request = RequestEntity.post(url) //создаем запрос
                    .body(dto);
            restTemplate.exchange(request, OperationInfoDto.class);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new TransferException("Ошибка при отправке перевода %s".formatted(e.getMessage()));
        }
    }

    /**
     * Формирует URL
     */
    private String createTransferUrl(AbstractBank bank) {
        String partnerBankPrefixUrl = bank.getPrefixUrl();
        return partnerBankPrefixUrl + POSTFIX_URL;
    }

    private FinanceOperationDto createRecipientDto(TransferDto transferDto, CurrencyCode recipientCurrencyCode) {
        return new FinanceOperationDto(transferDto.recipientInfo().accountNumberTo(),
                transferDto.recipientRequestAmount(),
                transferDto.description(),
                recipientCurrencyCode);
    }
}