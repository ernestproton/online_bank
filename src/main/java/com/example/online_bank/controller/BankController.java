    package com.example.online_bank.controller;

    import com.example.online_bank.domain.dto.BuyCurrencyDto;
    import com.example.online_bank.domain.dto.FinanceOperationDto;
    import com.example.online_bank.domain.dto.OperationInfoDto;
    import com.example.online_bank.service.BankService;
    import com.example.online_bank.service.BuyCurrencyService;
    import io.swagger.v3.oas.annotations.Operation;
    import io.swagger.v3.oas.annotations.media.Content;
    import io.swagger.v3.oas.annotations.media.Schema;
    import io.swagger.v3.oas.annotations.responses.ApiResponse;
    import io.swagger.v3.oas.annotations.tags.Tag;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.access.prepost.PreAuthorize;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestBody;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RestController;

    import java.math.BigDecimal;
    import java.util.List;

    @RestController()
    @RequestMapping("/api/operation")
    @RequiredArgsConstructor
    @Tag(name = "Контроллер финансовых операций")
    public class    BankController {
        private final BankService bankService;
        private final BuyCurrencyService buyCurrencyService;

        /**
         * Пополнить счет
         *
         * @param dto количество денег для пополнения по пользовательскому счету, описание к операции
         * @return делает зачисление по этому счету.
         */
        @Operation(summary = "Пополнить счёт пользователя по номеру счёта")
        @ApiResponse(
                responseCode = "200",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = OperationInfoDto.class)
                )
        )
        @PreAuthorize("@accountSecurity.isOwner(#dto.accountNumber(), authentication.principal.uuid)")
        @PostMapping("/receive")
        public ResponseEntity<OperationInfoDto> receive(@RequestBody FinanceOperationDto dto) {
            return ResponseEntity.status(201).body(bankService.makeDeposit(dto));
        }

        /**
         * Снять деньги с банкомата(списать)
         *
         * @param dto количество денег для платежа по пользовательскому счету, описание к операции
         * @return делает платеж по пользовательскому счёту
         */
        @Operation(summary = "Сделать платеж по пользовательскому счёту/Списать деньги с пользовательского счёта")
        @ApiResponse(
                responseCode = "202",
                content = @Content(mediaType = "text/plain")
        )
        @PreAuthorize("@accountSecurity.isOwner(#dto.accountNumber(), authentication.principal.uuid)")
        @PostMapping("/withdraw")
        public ResponseEntity<OperationInfoDto> withdraw(@RequestBody FinanceOperationDto dto) {
            return ResponseEntity.status(201).body(bankService.makePayment(dto));
        }

        @PreAuthorize("""
                  @accountSecurity.isOwnsBothAccounts(
                  #dto.baseAccountNumber(),
                   #dto.targetAccountNumber(),
                    authentication.principal.uuid
                )
                """)
        @PostMapping("/buy-currency")
        @Operation(summary = "Купить валюту с одного счёта на другой")
        @ApiResponse(responseCode = "200", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = BigDecimal.class)))
        public List<OperationInfoDto> buyCurrency(@RequestBody BuyCurrencyDto dto) {
            return buyCurrencyService.buyCurrency(dto);
        }
    }
