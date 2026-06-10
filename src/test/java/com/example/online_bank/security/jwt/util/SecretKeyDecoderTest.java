package com.example.online_bank.security.jwt.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.FileReader;
import java.io.IOException;

@ExtendWith(MockitoExtension.class)
class SecretKeyDecoderTest {
    
    private static final String PATH = "jwt_secret_file_name";

    @Test
    void decode() throws IOException {
        FileReader fileReader = new FileReader(PATH);
        String key = SecretKeyReader.readKeyFromFile(fileReader);
        SecretKeyDecoder.decode(key);
        Assertions.assertDoesNotThrow(() -> SecretKeyDecoder.decode(key));
        fileReader.close();


    }
}