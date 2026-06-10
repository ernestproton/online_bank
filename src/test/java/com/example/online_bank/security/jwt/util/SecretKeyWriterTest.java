package com.example.online_bank.security.jwt.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;

class SecretKeyWriterTest {

    @Test
    void successWriteKeyToFile() throws IOException {
        String filename = "src/test/resources/testsecretfile.txt";
        String content = "test-content";

        try (FileWriter fw = new FileWriter(filename)) {
            Assertions.assertDoesNotThrow(() -> SecretKeyWriter.writeKeyToFile(fw, content));
        }
    }
}