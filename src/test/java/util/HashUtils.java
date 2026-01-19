package util;

import lombok.extern.log4j.Log4j2;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.math.BigInteger;

@Log4j2
public class HashUtils {
    public static String hashString(String input) {
        try {
            // Создаем объект MessageDigest для алгоритма SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // Преобразуем входную строку в байты
            byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            // Преобразуем байты в строку в шестнадцатеричном формате
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        // Преобразуем байты в строку в шестнадцатеричном формате
        BigInteger number = new BigInteger(1, hash);
        StringBuilder hexString = new StringBuilder(number.toString(16));
        // Заполняем нулями до длины 64 символа
        while (hexString.length() < 64) {
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }
}