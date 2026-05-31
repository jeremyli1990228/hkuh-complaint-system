package com.hkuh.complaint.util;

import com.hkuh.complaint.exception.DecryptException;
import com.hkuh.complaint.exception.EncryptException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

@Slf4j
@Component
public class AesEncryptUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final int KEY_LENGTH = 32;
    private static final Pattern BASE64_PATTERN = Pattern.compile("^[A-Za-z0-9+/]+=*$");

    @Value("${aes.key}")
    private String aesKeyBase64;

    @Value("${aes.iv}")
    private String aesIvBase64;

    private SecretKey secretKey;
    private byte[] defaultIv;

    private void initKey() {
        if (secretKey == null) {
            byte[] keyBytes = Base64.getDecoder().decode(aesKeyBase64);
            if (keyBytes.length != KEY_LENGTH) {
                throw new EncryptException("AES密钥长度必须为32字节，当前长度：" + keyBytes.length);
            }
            secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
        }
    }

    private void initIv() {
        if (defaultIv == null) {
            defaultIv = Base64.getDecoder().decode(aesIvBase64);
            if (defaultIv.length != IV_LENGTH) {
                throw new EncryptException("AES IV长度必须为12字节，当前长度：" + defaultIv.length);
            }
        }
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            initKey();
            initIv();

            byte[] iv = generateIv();
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] plainBytes = plainText.getBytes(StandardCharsets.UTF_8);
            byte[] cipherBytes = cipher.doFinal(plainBytes);

            byte[] combined = new byte[iv.length + cipherBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherBytes, 0, combined, iv.length, cipherBytes.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (EncryptException e) {
            throw e;
        } catch (Exception e) {
            throw new EncryptException("AES加密失败", e);
        }
    }

    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }

        if (!isEncrypted(cipherText)) {
            return cipherText;
        }

        try {
            initKey();

            byte[] combined = Base64.getDecoder().decode(cipherText);

            if (combined.length < IV_LENGTH + GCM_TAG_LENGTH / 8) {
                throw new DecryptException("密文长度不足");
            }

            byte[] iv = new byte[IV_LENGTH];
            byte[] cipherBytes = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            System.arraycopy(combined, IV_LENGTH, cipherBytes, 0, cipherBytes.length);

            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] plainBytes = cipher.doFinal(cipherBytes);
            return new String(plainBytes, StandardCharsets.UTF_8);

        } catch (DecryptException e) {
            throw e;
        } catch (Exception e) {
            throw new DecryptException("AES解密失败：" + e.getMessage(), e);
        }
    }

    public boolean isEncrypted(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        if (text.length() < 32) {
            return false;
        }

        String cleanText = text.replace("\n", "").replace("\r", "").trim();

        if (!BASE64_PATTERN.matcher(cleanText).matches()) {
            return false;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(cleanText);
            return decoded.length >= IV_LENGTH + 16;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public String encryptField(String fieldName, String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        try {
            String encrypted = encrypt(value);
            log.info("字段[{}]加密成功", fieldName);
            return encrypted;
        } catch (EncryptException e) {
            log.error("字段[{}]加密失败：{}", fieldName, e.getMessage());
            throw e;
        }
    }

    public String decryptField(String fieldName, String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        try {
            String decrypted = decrypt(value);
            log.info("字段[{}]解密成功", fieldName);
            return decrypted;
        } catch (DecryptException e) {
            log.error("字段[{}]解密失败：{}", fieldName, e.getMessage());
            throw e;
        }
    }

    private byte[] generateIv() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    public String encryptWithCustomIv(String plainText, String ivBase64) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            initKey();

            byte[] iv = Base64.getDecoder().decode(ivBase64);
            if (iv.length != IV_LENGTH) {
                throw new EncryptException("IV长度必须为12字节");
            }

            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] plainBytes = plainText.getBytes(StandardCharsets.UTF_8);
            byte[] cipherBytes = cipher.doFinal(plainBytes);

            return Base64.getEncoder().encodeToString(cipherBytes);

        } catch (EncryptException e) {
            throw e;
        } catch (Exception e) {
            throw new EncryptException("AES加密失败", e);
        }
    }

    public String decryptWithCustomIv(String cipherText, String ivBase64) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }

        try {
            initKey();

            byte[] iv = Base64.getDecoder().decode(ivBase64);
            if (iv.length != IV_LENGTH) {
                throw new DecryptException("IV长度必须为12字节");
            }

            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] cipherBytes = Base64.getDecoder().decode(cipherText);
            byte[] plainBytes = cipher.doFinal(cipherBytes);

            return new String(plainBytes, StandardCharsets.UTF_8);

        } catch (DecryptException e) {
            throw e;
        } catch (Exception e) {
            throw new DecryptException("AES解密失败：" + e.getMessage(), e);
        }
    }
}
