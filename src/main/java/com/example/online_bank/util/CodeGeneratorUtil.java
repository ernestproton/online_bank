package com.example.online_bank.util;

import com.example.online_bank.enums.CurrencyCode;
import lombok.experimental.UtilityClass;

import java.util.Random;

@UtilityClass
public class CodeGeneratorUtil {
    private final Random RANDOM = new Random();
    private static final int ACCOUNT_NUMBER_FORMAT = 100_000;
    private static final int OTP_FORMAT = 10_000;

    /**
     * Генерация номера счета. Счет состоит из 5 случайных и трех добавочных(код валюты).
     * При генерации счетов добавляется в начале код валюты: рубль - 810, юань - 378, доллар 840.
     *
     * @example было: "000001"
     * стало: "810000001"
     */
    public String generateAccountNumber(CurrencyCode currencyCode) {
        return String.format(currencyCode.getCode() + "%06d", RANDOM.nextInt(ACCOUNT_NUMBER_FORMAT));
    }

    /**
     * Устаревший метод генерации номера счета.
     */
    @Deprecated
    public String generateAccountNumber() {
        return String.format("%06d", RANDOM.nextInt(ACCOUNT_NUMBER_FORMAT));
    }

    /**
     * Генерация otp кода из 4 цифр
     */
    public String generateVerificationCode() {
        return String.format("%04d", RANDOM.nextInt(OTP_FORMAT));
    }
}