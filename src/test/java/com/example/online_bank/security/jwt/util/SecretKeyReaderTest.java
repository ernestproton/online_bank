package com.example.online_bank.security.jwt.util;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileReader;

class SecretKeyReaderTest {

    @Test
    @SneakyThrows
    void successReadKeyFromFile() {
        String filename = "src/test/resources/testsecretfile.txt";
        String exceptedContent = "test-content";
        try (FileReader fr = new FileReader(filename)) {
            String result = SecretKeyReader.readKeyFromFile(fr);
            Assertions.assertEquals(exceptedContent, result);
        }
    }
}