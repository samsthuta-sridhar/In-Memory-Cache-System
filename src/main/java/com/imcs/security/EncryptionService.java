package com.imcs.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class EncryptionService {

    @Value("${security.aes-secret:imcs1234567890ab}")
    private String secret;

    private SecretKeySpec buildKey() {
        byte[] raw = secret.getBytes();
        byte[] key = new byte[16];
        System.arraycopy(raw, 0, key, 0, Math.min(raw.length, 16));
        return new SecretKeySpec(key, "AES");
    }

    public String encrypt(String plainText) {
        try {
            Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, buildKey());
            return Base64.getEncoder().encodeToString(c.doFinal(plainText.getBytes("UTF-8")));
        } catch (Exception e) {
            System.err.println("Encryption failed for: " + plainText);
            return plainText; // fallback — return as is
        }
    }

    public String decrypt(String cipherText) {
        try {
            Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE, buildKey());
            return new String(c.doFinal(Base64.getDecoder().decode(cipherText)), "UTF-8");
        } catch (Exception e) {
            System.err.println("Decryption failed — returning raw value");
            return cipherText; // fallback — return as is
        }
    }

    public boolean isEncrypted(String value) {
        try {
            Base64.getDecoder().decode(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}