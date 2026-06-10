package com.example.online_bank.security.jwt.service;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static com.example.online_bank.security.jwt.util.SecretKeyDecoder.decode;
import static com.example.online_bank.security.jwt.util.SecretKeyEncoder.encode;
import static com.example.online_bank.security.jwt.util.SecretKeyReader.readKeyFromFile;
import static com.example.online_bank.security.jwt.util.SecretKeyWriter.writeKeyToFile;

@Component
@RequiredArgsConstructor
public class SecretKeyManager {

    /**
     * Считывает преобразованные байты с файла (readKeyFromFile) и преобразует в SecretKey(decode)
     *
     * @param fileName имя файла
     * @return секретный ключ
     */
    public SecretKey decodeFile(String fileName) throws IOException {
        String base64encodedKey = readKeyFromFile(new FileReader(fileName));
        return decode(base64encodedKey);
    }

    /**
     * 1) получаем байты секретного ключа и создаем строку из массива байтов
     * <p>
     * 2) записываем строку в указанный файл
     */
    public void encodeAndWriteKey(FileWriter writer, SecretKey secretKey) throws IOException {
        String encoded = encode(secretKey);
        writeKeyToFile(writer, encoded);
    }

    /**
     * Создает секретный ключ
     */
    public SecretKey createSecretKey() {
        return Jwts
                .SIG
                .HS256
                .key()
                .build(); // генерируем секретный ключ
    }
}
