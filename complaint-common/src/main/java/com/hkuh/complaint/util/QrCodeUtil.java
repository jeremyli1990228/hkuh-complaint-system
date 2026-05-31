package com.hkuh.complaint.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class QrCodeUtil {

    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 300;
    private static final int QRCODE_SIZE = 20;
    private static final String QRCODE_FORMAT = "PNG";
    private static final int BLACK_COLOR = 0xFF000000;
    private static final int WHITE_COLOR = 0xFFFFFFFF;

    private QrCodeUtil() {
    }

    public static byte[] generateQrCode(String content, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = createHints();
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            MatrixToImageConfig config = new MatrixToImageConfig(BLACK_COLOR, WHITE_COLOR);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix, config);

            return bufferedImageToBytes(bufferedImage, QRCODE_FORMAT);
        } catch (WriterException | IOException e) {
            log.error("生成二维码失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成二维码失败", e);
        }
    }

    public static byte[] generateQrCode(String content) {
        return generateQrCode(content, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public static String generateQrCodeBase64(String content, int width, int height) {
        byte[] qrCodeBytes = generateQrCode(content, width, height);
        return Base64.getEncoder().encodeToString(qrCodeBytes);
    }

    public static String generateQrCodeBase64(String content) {
        return generateQrCodeBase64(content, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public static byte[] generateQrCodeWithLogo(String content, int width, int height, String logoPath) {
        try {
            byte[] qrCodeBytes = generateQrCode(content, width, height);
            BufferedImage qrCodeImage = ImageIO.read(new java.io.ByteArrayInputStream(qrCodeBytes));

            File logoFile = new File(logoPath);
            if (!logoFile.exists()) {
                log.warn("Logo文件不存在: {}", logoPath);
                return qrCodeBytes;
            }

            BufferedImage logoImage = ImageIO.read(logoFile);
            int logoSize = width / 5;
            int logoX = (width - logoSize) / 2;
            int logoY = (height - logoSize) / 2;

            BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = combined.createGraphics();

            g2d.drawImage(qrCodeImage, 0, 0, null);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            RoundRectangle2D.Float roundedRectangle = new RoundRectangle2D.Float(
                    logoX - 5, logoY - 5, logoSize + 10, logoSize + 10, 10, 10);
            g2d.setColor(Color.WHITE);
            g2d.fill(roundedRectangle);

            g2d.drawImage(logoImage.getScaledInstance(logoSize, logoSize, Image.SCALE_SMOOTH),
                    logoX, logoY, null);

            g2d.dispose();

            return bufferedImageToBytes(combined, QRCODE_FORMAT);
        } catch (IOException e) {
            log.error("生成带Logo二维码失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成带Logo二维码失败", e);
        }
    }

    public static byte[] generateQrCodeWithLogo(String content, String logoPath) {
        return generateQrCodeWithLogo(content, DEFAULT_WIDTH, DEFAULT_HEIGHT, logoPath);
    }

    public static byte[] generateFeedbackQrCode(String feedbackNo, String baseUrl) {
        String content = baseUrl + "/h5/feedback/detail/" + feedbackNo;
        return generateQrCode(content);
    }

    public static byte[] generateDeptQrCode(Long deptId, String deptName, String baseUrl) {
        String content = baseUrl + "/h5/submit?deptId=" + deptId;
        return generateQrCode(content);
    }

    public static byte[] generateWardQrCode(Long wardId, String wardName, String baseUrl) {
        String content = baseUrl + "/h5/submit?wardId=" + wardId;
        return generateQrCode(content);
    }

    public static void generateQrCodeToFile(String content, String filePath, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = createHints();
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            Path path = new File(filePath).toPath();
            MatrixToImageWriter.writeToPath(bitMatrix, QRCODE_FORMAT, path);

            log.info("二维码已保存到: {}", filePath);
        } catch (WriterException | IOException e) {
            log.error("保存二维码到文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("保存二维码失败", e);
        }
    }

    public static void generateQrCodeToFile(String content, String filePath) {
        generateQrCodeToFile(content, filePath, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public static BufferedImage generateQrCodeImage(String content, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = createHints();
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            MatrixToImageConfig config = new MatrixToImageConfig(BLACK_COLOR, WHITE_COLOR);
            return MatrixToImageWriter.toBufferedImage(bitMatrix, config);
        } catch (WriterException e) {
            log.error("生成二维码图片失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成二维码图片失败", e);
        }
    }

    public static byte[] generateColoredQrCode(String content, int width, int height, int foregroundColor, int backgroundColor) {
        try {
            Map<EncodeHintType, Object> hints = createHints();
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            MatrixToImageConfig config = new MatrixToImageConfig(foregroundColor, backgroundColor);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix, config);

            return bufferedImageToBytes(bufferedImage, QRCODE_FORMAT);
        } catch (WriterException | IOException e) {
            log.error("生成彩色二维码失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成彩色二维码失败", e);
        }
    }

    public static byte[] generateFeedbackQrCodeWithLogo(String feedbackNo, String baseUrl, String logoPath) {
        String content = baseUrl + "/h5/feedback/detail/" + feedbackNo;
        return generateQrCodeWithLogo(content, logoPath);
    }

    public static byte[] generateDeptQrCodeWithLogo(Long deptId, String deptName, String baseUrl, String logoPath) {
        String content = baseUrl + "/h5/submit?deptId=" + deptId;
        return generateQrCodeWithLogo(content, logoPath);
    }

    private static Map<EncodeHintType, Object> createHints() {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2);
        hints.put(EncodeHintType.DISABLE_ECI, true);
        return hints;
    }

    private static byte[] bufferedImageToBytes(BufferedImage bufferedImage, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, format, baos);
        return baos.toByteArray();
    }

    public static boolean isValidQrCodeContent(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        if (content.length() > 2048) {
            return false;
        }
        return true;
    }

    public static String buildFeedbackUrl(String baseUrl, String feedbackNo) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "https://m.hkuh.hk";
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        return baseUrl + "h5/feedback/detail/" + feedbackNo;
    }

    public static String buildSubmitUrl(String baseUrl, Long deptId, Long wardId) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "https://m.hkuh.hk";
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        StringBuilder url = new StringBuilder(baseUrl).append("h5/submit?");

        if (deptId != null) {
            url.append("deptId=").append(deptId);
        }
        if (wardId != null) {
            if (deptId != null) {
                url.append("&");
            }
            url.append("wardId=").append(wardId);
        }

        return url.toString();
    }
}
