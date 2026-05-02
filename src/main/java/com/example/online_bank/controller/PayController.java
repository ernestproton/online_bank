package com.example.online_bank.controller;

import com.example.online_bank.domain.dto.OperationInfoDto;
import com.example.online_bank.domain.dto.PayDtoRequest;
import com.example.online_bank.security.userdetails.JwtUserDetails;
import com.example.online_bank.service.PayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PayController {
    private final PayService payService;

    @PostMapping
    public ResponseEntity<OperationInfoDto> pay(
            @RequestBody PayDtoRequest dto,
            @AuthenticationPrincipal JwtUserDetails userDetails
    ) {
        OperationInfoDto body = payService.pay(dto, UUID.fromString(userDetails.getUuid()));
        return ResponseEntity.ok().body(body);
    }
}
