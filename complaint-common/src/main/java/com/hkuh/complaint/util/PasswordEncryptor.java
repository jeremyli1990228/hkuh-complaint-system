package com.hkuh.complaint.util;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.jasypt.util.text.AES256TextEncryptor;

/**
 * 密码加密器
 * 用于命令行加密敏感配置
 *
 * 使用方法:
 * 1. 直接运行: java -cp complaint-common.jar com.hkuh.complaint.util.PasswordEncryptor
 * 2. 带参数运行: java -cp complaint-common.jar com.hkuh.complaint.util.PasswordEncryptor <明文密码>
 */
public class PasswordEncryptor {

    private static final String ALGORITHM = "PBEWITHHMACSHA512ANDAES_256";

    private PasswordEncryptor() {
    }

    /**
     * 使用AES256加密密码
     *
     * @param plainText 明文密码
     * @param password  加密密钥
     * @return 加密后的密文
     */
    public static String encrypt(String plainText, String password) {
        if (plainText == null || plainText.isEmpty()) {
            throw new IllegalArgumentException("明文密码不能为空");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("加密密钥不能为空");
        }

        AES256TextEncryptor encryptor = new AES256TextEncryptor();
        encryptor.setPassword(password);
        return encryptor.encrypt(plainText);
    }

    /**
     * 使用AES256解密密码
     *
     * @param encryptedText 密文
     * @param password      解密密钥
     * @return 解密后的明文
     */
    public static String decrypt(String encryptedText, String password) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            throw new IllegalArgumentException("密文不能为空");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("解密密钥不能为空");
        }

        AES256TextEncryptor encryptor = new AES256TextEncryptor();
        encryptor.setPassword(password);
        return encryptor.decrypt(encryptedText);
    }

    /**
     * 使用PBEWithMD5AndDES加密（兼容旧版本）
     *
     * @param plainText 明文密码
     * @param password  加密密钥
     * @return 加密后的密文
     */
    public static String encryptWithPBE(String plainText, String password) {
        if (plainText == null || plainText.isEmpty()) {
            throw new IllegalArgumentException("明文密码不能为空");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("加密密钥不能为空");
        }

        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(password);
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize(1);
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.NoIvGenerator");
        config.setStringOutputType("base64");

        encryptor.setConfig(config);
        return encryptor.encrypt(plainText);
    }

    /**
     * 使用PBEWithMD5AndDES解密（兼容旧版本）
     *
     * @param encryptedText 密文
     * @param password     解密密钥
     * @return 解密后的明文
     */
    public static String decryptWithPBE(String encryptedText, String password) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            throw new IllegalArgumentException("密文不能为空");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("解密密钥不能为空");
        }

        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(password);
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize(1);
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.NoIvGenerator");
        config.setStringOutputType("base64");

        encryptor.setConfig(config);
        return encryptor.decrypt(encryptedText);
    }

    /**
     * 命令行入口
     */
    public static void main(String[] args) {
        String password;
        String plainText;

        if (args.length >= 2) {
            password = args[0];
            plainText = args[1];
        } else if (args.length == 1) {
            plainText = args[0];
            password = readPassword("请输入加密密钥: ");
        } else {
            printUsage();
            password = readPassword("请输入加密密钥: ");
            plainText = readPassword("请输入要加密的明文密码: ");
        }

        try {
            System.out.println("\n========================================");
            System.out.println("  密码加密工具");
            System.out.println("========================================\n");
            System.out.println("算法: " + ALGORITHM);
            System.out.println("--------------------------------------\n");

            String encrypted = encrypt(plainText, password);
            System.out.println("加密结果:");
            System.out.println("ENC(" + encrypted + ")\n");

            System.out.println("========================================");
            System.out.println("  使用说明");
            System.out.println("========================================\n");
            System.out.println("1. 将上方加密结果填入配置文件:");
            System.out.println("   password: ENC(" + encrypted + ")");
            System.out.println();
            System.out.println("2. 设置环境变量:");
            System.out.println("   export JASYPT_ENCRYPTOR_PASSWORD=" + password);
            System.out.println();
            System.out.println("3. 或在启动参数中指定:");
            System.out.println("   java -Djasypt.encryptor.password=" + password + " -jar app.jar");
            System.out.println();

            String verifyInput = readPassword("是否验证解密? (y/n): ");
            if ("y".equalsIgnoreCase(verifyInput.trim())) {
                String decrypted = decrypt(encrypted, password);
                System.out.println("\n解密结果: " + decrypted);
                if (plainText.equals(decrypted)) {
                    System.out.println("✓ 验证成功!");
                } else {
                    System.out.println("✗ 验证失败!");
                }
            }

        } catch (Exception e) {
            System.err.println("\n加密失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printUsage() {
        System.out.println("\n========================================");
        System.out.println("  密码加密工具 - 使用说明");
        System.out.println("========================================\n");
        System.out.println("用法:");
        System.out.println("  java -cp complaint-common.jar com.hkuh.complaint.util.PasswordEncryptor");
        System.out.println("  java -cp complaint-common.jar com.hkuh.complaint.util.PasswordEncryptor <密钥>");
        System.out.println("  java -cp complaint-common.jar com.hkuh.complaint.util.PasswordEncryptor <密钥> <明文密码>");
        System.out.println();
        System.out.println("示例:");
        System.out.println("  java -cp complaint-common.jar com.hkuh.complaint.util.PasswordEncryptor mySecretKey");
        System.out.println("  java -cp complaint-common.jar com.hkuh.complaint.util.PasswordEncryptor mySecretKey dbPassword123");
        System.out.println();
    }

    private static String readPassword(String prompt) {
        System.out.print(prompt);
        try {
            return new String(System.console().readPassword());
        } catch (Exception e) {
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            return scanner.nextLine();
        }
    }
}
