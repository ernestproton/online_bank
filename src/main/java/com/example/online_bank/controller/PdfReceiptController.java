package com.example.online_bank.controller;

import com.example.online_bank.security.userdetails.JwtUserDetails;
import com.example.online_bank.service.PDFWriterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/receipt")
public class PdfReceiptController {
    private final PDFWriterService pdfWriterService;

    @GetMapping("/download/{operationId}")
    public ResponseEntity<byte[]> getReceipt(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @PathVariable Long operationId
    ) {
        byte[] receipt = pdfWriterService.write(operationId, UUID.fromString(userDetails.getUuid()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"receipt.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(receipt);
    }
}
