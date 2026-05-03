package com.example.online_bank.controller;

import com.example.online_bank.domain.dto.EmptyDeviceResponseDto;
import com.example.online_bank.domain.dto.VerifyRequiredResponseDto;
import com.example.online_bank.exception.*;
import jakarta.persistence.EntityNotFoundException;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class AdviceController {

    /**
     * @param e Обработка ошибки при ненахождении курса
     * @return 404 HTTP статус
     */
    @ExceptionHandler(CurrencyPairsNotFoundException.class)
    public ResponseEntity<String> handleCurrencyPairsNotFoundException(Exception e) {
        return new ResponseEntity<>(e.getMessage(), NOT_FOUND);
    }

    @ExceptionHandler(EmptyDataException.class)
    public ResponseEntity<String> handleEmptyOperationsException(Exception e) {
        return new ResponseEntity<>(e.getMessage(), NO_CONTENT);
    }

    /**
     * @param e Обработка ошибки если сущность уже существует
     * @return 409 статус
     */
    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ResponseEntity<String> handleEntityAlreadyExistsException(Exception e) {
        return new ResponseEntity<>(e.getMessage(), CONFLICT);
    }

    /**
     * @param e обработка ошибки при нулевом балансе
     * @return 400 HTTP статус
     */
    @ExceptionHandler(NegativeAccountBalance.class)
    public ResponseEntity<String> handleNegativeAccountBalanceException(Exception e) {
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    /**
     * @param e Обработка ошибки в случае возникновения ошибки отправке запроса
     * @return 500 HTTP статус
     */
    @ExceptionHandler(TransferException.class)
    public ResponseEntity<String> handleTransferException(Exception e) {
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(Exception e) {
        return new ResponseEntity<>(e.getMessage(), NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleBadRequestException(Exception e) {
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

//    /**
//     * @param e обработка ошибки когда произошла неизвестная ошибка
//     * @return 503 HTTP статус
//     */
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<String> handleApiException(Exception e) {
//        return ResponseEntity.status(SERVICE_UNAVAILABLE)
//                .body("Сервис временно не работает, но мы работаем над этим");
//    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentialsException(Exception e) {
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(VerificationOtpException.class)
    public ResponseEntity<String> handleVerificationOtpException(Exception e) {
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(Exception e) {
        return new ResponseEntity<>(e.getMessage(), FORBIDDEN);
    }

    @ExceptionHandler(SendEmailException.class)
    public ResponseEntity<String> handleSendEmailException(Exception e) {
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            String objectName = error.getField();
            String message = error.getDefaultMessage();
            errors.put(objectName, message);
        });
        return new ResponseEntity<>(errors, BAD_REQUEST);
    }

    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<String> handleDeviceNotFoundException(Exception e) {
        return new ResponseEntity<>(e.getMessage(), FORBIDDEN);
    }

    @ExceptionHandler(DeviceIdIsBlankException.class)
    public ResponseEntity<EmptyDeviceResponseDto> handleDeviceIdIsBlankException(DeviceIdIsBlankException e) {
        return ResponseEntity.status(FORBIDDEN).body(new EmptyDeviceResponseDto(e.getDeviceId(), e.getMessage()));
    }

    @ExceptionHandler(UserAgentNotEqualException.class)
    public ResponseEntity<VerifyRequiredResponseDto> handleUserAgentNotEqualException(UserAgentNotEqualException e) {
        VerifyRequiredResponseDto responseDto = new VerifyRequiredResponseDto(e.getMessage(), "VERIFY_REQUIRED");
        return ResponseEntity.status(OK).body(responseDto);
    }

    @ExceptionHandler(InvertedRateNotFoundException.class)
    public ResponseEntity<String> handleInvertedRateNotFoundException(Exception e) {
        return ResponseEntity.status(NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(ReuseDetectionException.class)
    public ResponseEntity<String> handleReuseDetectionException(Exception e) {
        return ResponseEntity.status(BAD_REQUEST).body(e.getMessage());
    }
}
