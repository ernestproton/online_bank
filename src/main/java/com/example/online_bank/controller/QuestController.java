package com.example.online_bank.controller;

import com.example.online_bank.domain.dto.QuestResponseDto;
import com.example.online_bank.domain.dto.UserQuestResponseDto;
import com.example.online_bank.security.userdetails.JwtUserDetails;
import com.example.online_bank.service.QuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/quest")
@RequiredArgsConstructor
public class QuestController {
    private final QuestService questService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<List<QuestResponseDto>> createRandomQuest() {
        return ResponseEntity.ok(questService.createRandomQuest());
    }

    @GetMapping("/load-user")
    public ResponseEntity<List<UserQuestResponseDto>> findAllUserQuest(
            @AuthenticationPrincipal
            JwtUserDetails userDetails
    ) {
        return ResponseEntity.ok(questService.findAllByUserQuest(UUID.fromString(userDetails.getUuid())));
    }
}
