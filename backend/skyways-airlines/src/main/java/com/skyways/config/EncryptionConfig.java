package com.skyways.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import java.util.Base64;

@Component
public class EncryptionConfig {

    private static final Logger logger =
        LoggerFactory.getLogger(EncryptionConfig.class);

    private static final String ALGORITHM = "DESede";
    private static final String SECRET_KEY = 
        "SkyWaysAirlinesSecretKey2026!!??";

    private SecretKey getSecretKey() throws Exception {
        DESedeKeySpec keySpec = new DESedeKeySpec(
            SECRET_KEY.getBytes());
        SecretKeyFactory keyFactory = 
            SecretKeyFactory.getInstance(ALGORITHM);
        return keyFactory.generateSecret(keySpec);
    }

    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
            byte[] encrypted = cipher.doFinal(
                data.getBytes());
            return Base64.getEncoder()
                .encodeToString(encrypted);
        } catch (Exception e) {
            logger.error("Encryption failed: {}", 
                e.getMessage());
            return data;
        }
    }

    public String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
            byte[] decrypted = cipher.doFinal(
                Base64.getDecoder().decode(encryptedData));
            return new String(decrypted);
        } catch (Exception e) {
            logger.error("Decryption failed: {}", 
                e.getMessage());
            return encryptedData;
        }
    }
}