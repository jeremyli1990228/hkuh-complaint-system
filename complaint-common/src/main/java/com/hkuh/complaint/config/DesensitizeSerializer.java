package com.hkuh.complaint.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.hkuh.complaint.util.DesensitizeUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class DesensitizeSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null || value.isEmpty()) {
            gen.writeString(value);
            return;
        }

        try {
            String desensitized = detectAndDesensitize(value);
            gen.writeString(desensitized);
        } catch (Exception e) {
            log.warn("JSON序列化脱敏失败，返回原始值：{}", e.getMessage());
            gen.writeString(value);
        }
    }

    private String detectAndDesensitize(String value) {
        if (isPhoneNumber(value)) {
            return DesensitizeUtil.maskPhone(value);
        }

        if (isIdCard(value)) {
            return DesensitizeUtil.maskIdCard(value);
        }

        if (isEmail(value)) {
            return DesensitizeUtil.maskEmail(value);
        }

        if (isBankCard(value)) {
            return DesensitizeUtil.maskBankCard(value);
        }

        if (isName(value)) {
            return DesensitizeUtil.maskName(value);
        }

        return DesensitizeUtil.maskCustom(value, 3, 4);
    }

    private boolean isPhoneNumber(String value) {
        String clean = value.replaceAll("[\\s-]", "");
        return clean.matches("^1[3-9]\\d{9}$") || clean.matches("^0\\d{2,3}-?\\d{7,8}$");
    }

    private boolean isIdCard(String value) {
        String clean = value.replaceAll("[\\sXx]", "");
        return (clean.length() == 15 || clean.length() == 18) && clean.matches("^\\d+X?$");
    }

    private boolean isEmail(String value) {
        return value.contains("@") && value.indexOf("@") > 0 && value.indexOf("@") < value.length() - 1;
    }

    private boolean isBankCard(String value) {
        String clean = value.replaceAll("[\\s-]", "");
        return clean.matches("^\\d{13,19}$");
    }

    private boolean isName(String value) {
        if (value.length() < 2) {
            return false;
        }

        int chineseCount = 0;
        for (char c : value.toCharArray()) {
            if (c >= '\u4e00' && c <= '\u9fa5') {
                chineseCount++;
            }
        }

        return chineseCount >= 2;
    }
}
