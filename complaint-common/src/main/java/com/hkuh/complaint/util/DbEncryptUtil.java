package com.hkuh.complaint.util;

import lombok.extern.slf4j.Slf4j;
import org.jasypt.commons.CommonUtils;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.jasypt.util.text.AES256TextEncryptor;
import org.jasypt.util.text.TextEncryptor;

import java.util.Scanner;

/**
 * 数据库密码加密工具类
 * 使用Jasypt进行密码加密和解密
 */
@Slf4j
public class DbEncryptUtil {

    private static final String ALGORITHM_MD5_DES = "PBEWithMD5AndDES";
    private static final String ALGORITHM_SHA512_AES = "PBEWITHHMACSHA512ANDAES_256";

    private static final int POOL_SIZE = 1;
    private static final String KEY_OBTENTION_ITERATIONS = "1000";

    private DbEncryptUtil() {
    }

    public static String encryptWithMD5DES(String plainText, String password) {
        if (plainText == null || password == null) {
            throw new IllegalArgumentException("明文密码和密钥不能为空");
        }

        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(password);
        config.setAlgorithm(ALGORITHM_MD5_DES);
        config.setKeyObtentionIterations(KEY_OBTENTION_ITERATIONS);
        config.setPoolSize(POOL_SIZE);
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.NoIvGenerator");
        config.setStringOutputType(CommonUtils.STRING_OUTPUT_TYPE_BASE64);

        encryptor.setConfig(config);
        String encrypted = encryptor.encrypt(plainText);
        encryptor.destroy();

        log.info("使用MD5_DES算法加密成功");
        return encrypted;
    }

    public static String decryptWithMD5DES(String encryptedText, String password) {
        if (encryptedText == null || password == null) {
            throw new IllegalArgumentException("密文和密钥不能为空");
        }

        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(password);
        config.setAlgorithm(ALGORITHM_MD5_DES);
        config.setKeyObtentionIterations(KEY_OBTENTION_ITERATIONS);
        config.setPoolSize(POOL_SIZE);
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.NoIvGenerator");
        config.setStringOutputType(CommonUtils.STRING_OUTPUT_TYPE_BASE64);

        encryptor.setConfig(config);
        String decrypted = encryptor.decrypt(encryptedText);
        encryptor.destroy();

        return decrypted;
    }

    public static String encryptWithSHA512AES(String plainText, String password) {
        if (plainText == null || password == null) {
            throw new IllegalArgumentException("明文密码和密钥不能为空");
        }

        AES256TextEncryptor encryptor = new AES256TextEncryptor();
        encryptor.setPassword(password);

        String encrypted = encryptor.encrypt(plainText);
        log.info("使用AES256算法加密成功");

        return encrypted;
    }

    public static String decryptWithSHA512AES(String encryptedText, String password) {
        if (encryptedText == null || password == null) {
            throw new IllegalArgumentException("密文和密钥不能为空");
        }

        AES256TextEncryptor encryptor = new AES256TextEncryptor();
        encryptor.setPassword(password);

        return encryptor.decrypt(encryptedText);
    }

    public static String encrypt(String plainText, String password) {
        return encryptWithSHA512AES(plainText, password);
    }

    public static String decrypt(String encryptedText, String password) {
        return decryptWithSHA512AES(encryptedText, password);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("===========================================");
        System.out.println("   港大医院投诉系统 - 数据库密码加密工具   ");
        System.out.println("===========================================");
        System.out.println();

        String password;
        while (true) {
            System.out.print("请输入加密密钥 (JASYPT_ENCRYPTOR_PASSWORD): ");
            password = scanner.nextLine().trim();
            if (password.length() >= 8) {
                break;
            }
            System.out.println("密钥长度至少8位，请重新输入");
        }

        System.out.print("请输入要加密的明文密码: ");
        String plainText = scanner.nextLine();

        System.out.println();
        System.out.println("加密算法: " + ALGORITHM_SHA512_AES);
        System.out.println("------------------------------------------");

        try {
            String encrypted = encrypt(plainText, password);
            System.out.println("加密结果:");
            System.out.println("ENC(" + encrypted + ")");
            System.out.println();

            System.out.println("===========================================");
            System.out.println("   使用说明");
            System.out.println("===========================================");
            System.out.println();
            System.out.println("1. 将上方加密结果复制到 application.yml:");
            System.out.println("   password: ENC(" + encrypted + ")");
            System.out.println();
            System.out.println("2. 设置环境变量:");
            System.out.println("   export JASYPT_ENCRYPTOR_PASSWORD=" + password);
            System.out.println();
            System.out.println("3. 或者在启动参数中指定:");
            System.out.println("   java -Djasypt.encryptor.password=" + password + " -jar app.jar");
            System.out.println();

            System.out.print("是否测试解密? (y/n): ");
            String confirm = scanner.nextLine();
            if ("y".equalsIgnoreCase(confirm)) {
                System.out.println();
                String decrypted = decrypt(encrypted, password);
                System.out.println("解密结果: " + decrypted);

                if (plainText.equals(decrypted)) {
                    System.out.println("✓ 解密验证成功!");
                } else {
                    System.out.println("✗ 解密验证失败!");
                }
            }

        } catch (Exception e) {
            System.err.println("加密失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
