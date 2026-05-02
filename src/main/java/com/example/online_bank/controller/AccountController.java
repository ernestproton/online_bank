package com.example.online_bank.controller;

import com.example.online_bank.domain.dto.AccountDtoResponse;
import com.example.online_bank.domain.entity.Account;
import com.example.online_bank.enums.CurrencyCode;
import com.example.online_bank.security.userdetails.JwtUserDetails;
import com.example.online_bank.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Tag(name = "Account", description = "Методы по работе со счетами пользователя")
public class AccountController {
    private final AccountService accountService;

    /**
     * Создать счет для пользователя
     *
     * @param currencyCode Код валюты
     * @return Информацию об счете
     */
    @PostMapping()
    @Operation(summary = "Создать счёт для пользователя")
    @ApiResponse(
            responseCode = "201",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = String.class)
            )
    )
    public ResponseEntity<AccountDtoResponse> createAccountForUser(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @Parameter(description = "Код валюты", example = "USD")
            @RequestParam CurrencyCode currencyCode
    ) {
        return ResponseEntity.status(CREATED)
                .body(accountService.createAccountForUser(UUID.fromString(userDetails.getUuid()), currencyCode));
    }

    /**
     * Просмотреть баланс по счету
     *
     * @param accountNumber номер счета
     * @return возвращает баланс пользователя по номеру счёта
     */
    @GetMapping(value = {"/{accountNumber}"})
    @Operation(summary = "Просмотреть баланс по номеру счёта")
    @ApiResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "text/plain",
                    schema = @Schema(implementation = BigDecimal.class)
            )
    )
    @PreAuthorize("@accountSecurity.isOwner(#accountNumber, authentication.principal.uuid)")
    public BigDecimal getBalance(
            @Parameter(description = "Номер счёта с валютным кодом", example = "810097622")
            @PathVariable(value = "accountNumber") String accountNumber) {
        return accountService.getBalance(accountNumber);
    }

    /**
     * Найти все счета пользователя
     *
     * @return возвращает список всех счетов пользователя
     */
    @ApiResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Account.class)
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно получен счёт пользователя"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @Operation(summary = "Просмотреть все счета пользователя")
    @GetMapping(value = "/find-all-by-holder")
    public List<AccountDtoResponse> findAllByHolder(@AuthenticationPrincipal JwtUserDetails userDetails) {
        return accountService.findAllByHolder(UUID.fromString(userDetails.getUuid()));
    }
}
