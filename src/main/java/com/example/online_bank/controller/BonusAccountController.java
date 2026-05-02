package com.example.online_bank.controller;

import com.example.online_bank.domain.dto.BonusAccountDto;
import com.example.online_bank.domain.dto.ConvertBonusDto;
import com.example.online_bank.domain.dto.OperationInfoDto;
import com.example.online_bank.security.userdetails.JwtUserDetails;
import com.example.online_bank.service.BonusAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bonus")
@RequiredArgsConstructor
@Tag(name = "Контроллер финансовых операций")
public class BonusAccountController {
    private final BonusAccountService bonusAccountService;

    @PostMapping("/convert")
    @Operation(summary = "Конвертировать бонусы в рубли")
    @ApiResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = OperationInfoDto.class)
            )
    )
    @PreAuthorize("@accountSecurity.isOwner(#dto.accountNumber(), authentication.principal.uuid)")
    public ResponseEntity<OperationInfoDto> convert(@RequestBody ConvertBonusDto dto) {
        return ResponseEntity.status(200)
                .body(bonusAccountService.convertPoints(dto.accountNumber(), dto.points()));
    }

    @GetMapping()
    @Operation(summary = "Запросить все бонусные счета пользователя")
    @ApiResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BonusAccountDto.class)
            )
    )
    public List<BonusAccountDto> findAllByUser(@AuthenticationPrincipal JwtUserDetails userDetails){
        return bonusAccountService.findAllAccountsByUser(UUID.fromString(userDetails.getUuid()));
    }
}
