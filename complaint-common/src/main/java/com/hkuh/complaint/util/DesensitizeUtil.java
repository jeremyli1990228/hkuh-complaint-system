package com.hkuh.complaint.util;

import com.hkuh.complaint.annotation.Desensitize;
import com.hkuh.complaint.constant.DesensitizeType;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DesensitizeUtil {

    private static final String MASK_CHAR = "*";

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern LANDLINE_PATTERN = Pattern.compile("^0\\d{2,3}-?\\d{7,8}$");

    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        int length = name.length();
        if (length == 1) {
            return name;
        }

        if (length == 2) {
            return name.charAt(0) + "*";
        }

        StringBuilder result = new StringBuilder();
        result.append(name.charAt(0));

        for (int i = 1; i < length - 1; i++) {
            char c = name.charAt(i);
            if (c >= '\u4e00' && c <= '\u9fa5') {
                result.append("*");
            } else {
                result.append("*");
            }
        }

        result.append(name.charAt(length - 1));
        return result.toString();
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }

        String cleanPhone = phone.replaceAll("[\\s-]", "");

        if (PHONE_PATTERN.matcher(cleanPhone).matches()) {
            int length = cleanPhone.length();
            return cleanPhone.substring(0, 3) + "****" + cleanPhone.substring(length - 4);
        }

        if (LANDLINE_PATTERN.matcher(cleanPhone).matches()) {
            if (cleanPhone.contains("-")) {
                return cleanPhone.substring(0, 4) + "****" + cleanPhone.substring(cleanPhone.length() - 2);
            } else {
                return cleanPhone.substring(0, 4) + "****" + cleanPhone.substring(cleanPhone.length() - 2);
            }
        }

        return maskCustom(phone, 3, 4);
    }

    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.isEmpty()) {
            return idCard;
        }

        String cleanIdCard = idCard.replaceAll("[\\sXx]", "");

        int length = cleanIdCard.length();
        if (length < 8) {
            return idCard;
        }

        return cleanIdCard.substring(0, 3) + "**********" + cleanIdCard.substring(length - 4);
    }

    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return email;
        }

        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (username.length() <= 1) {
            return username + maskString(domain.length() - 1) + domain;
        }

        return username.charAt(0) + maskString(username.length() - 1) + domain;
    }

    public static String maskAddress(String address) {
        if (address == null || address.isEmpty()) {
            return address;
        }

        String[] provinceKeywords = {"省", "市", "自治区"};
        int provinceEnd = 0;

        for (String keyword : provinceKeywords) {
            int index = address.indexOf(keyword);
            if (index > 0) {
                provinceEnd = index + keyword.length();
                break;
            }
        }

        String[] cityKeywords = {"市", "区", "县"};
        int cityEnd = provinceEnd;

        if (provinceEnd > 0) {
            String remaining = address.substring(provinceEnd);
            for (String keyword : cityKeywords) {
                int index = remaining.indexOf(keyword);
                if (index > 0) {
                    cityEnd = provinceEnd + index + keyword.length();
                    break;
                }
            }
        } else {
            for (String keyword : cityKeywords) {
                int index = address.indexOf(keyword);
                if (index > 0) {
                    cityEnd = index + keyword.length();
                    break;
                }
            }
        }

        if (cityEnd > 0 && cityEnd < address.length()) {
            return address.substring(0, cityEnd) + "***";
        }

        if (provinceEnd > 0 && provinceEnd < address.length()) {
            return address.substring(0, provinceEnd) + "***";
        }

        return maskCustom(address, 4, 0);
    }

    public static String maskBankCard(String bankCard) {
        if (bankCard == null || bankCard.isEmpty()) {
            return bankCard;
        }

        String cleanCard = bankCard.replaceAll("[\\s-]", "");
        int length = cleanCard.length();

        if (length < 12) {
            return maskCustom(bankCard, 2, 4);
        }

        return cleanCard.substring(0, 4) + maskString(length - 8) + cleanCard.substring(length - 4);
    }

    public static String maskCustom(String text, int prefixLen, int suffixLen) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        int length = text.length();
        int totalKeep = prefixLen + suffixLen;

        if (totalKeep >= length) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        result.append(text.substring(0, prefixLen));
        result.append(maskString(length - totalKeep));
        result.append(text.substring(length - suffixLen));

        return result.toString();
    }

    public static <T> T desensitize(T obj) {
        if (obj == null) {
            return null;
        }

        Class<?> clazz = obj.getClass();

        if (obj instanceof String) {
            return obj;
        }

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(Desensitize.class)) {
                continue;
            }

            field.setAccessible(true);
            Desensitize annotation = field.getAnnotation(Desensitize.class);

            try {
                Object value = field.get(obj);
                if (value == null) {
                    continue;
                }

                String original = value.toString();
                String masked = desensitizeByType(original, annotation.type(),
                        annotation.prefixLen(), annotation.suffixLen());
                field.set(obj, masked);

            } catch (IllegalAccessException e) {
                log.warn("字段[{}]脱敏失败：{}", field.getName(), e.getMessage());
            }
        }

        return obj;
    }

    public static String desensitizeByType(String value, DesensitizeType type,
                                            int prefixLen, int suffixLen) {
        if (value == null) {
            return null;
        }

        switch (type) {
            case NAME:
                return maskName(value);
            case PHONE:
                return maskPhone(value);
            case ID_CARD:
                return maskIdCard(value);
            case EMAIL:
                return maskEmail(value);
            case ADDRESS:
                return maskAddress(value);
            case BANK_CARD:
                return maskBankCard(value);
            case CUSTOM:
            default:
                if (prefixLen <= 0 && suffixLen <= 0) {
                    return maskCustom(value, 3, 4);
                }
                return maskCustom(value, prefixLen, suffixLen);
        }
    }

    private static String maskString(int length) {
        if (length <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(MASK_CHAR);
        }
        return sb.toString();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        String cleanPhone = phone.replaceAll("[\\s-]", "");
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }

    public static boolean isValidIdCard(String idCard) {
        if (idCard == null || idCard.isEmpty()) {
            return false;
        }
        String cleanIdCard = idCard.replaceAll("[\\sXx]", "");
        return cleanIdCard.length() == 15 || cleanIdCard.length() == 18;
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return email.contains("@") && email.indexOf("@") > 0;
    }
}
