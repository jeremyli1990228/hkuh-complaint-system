package com.hkuh.complaint.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class IpUtil {

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    private static final int IP_LENGTH = 15;

    private static final String[] IP_HEADER_CANDIDATES = {
        "X-Forwarded-For",
        "X-Real-IP",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR",
        "HTTP_X_FORWARDED",
        "HTTP_X_CLUSTER_CLIENT_IP",
        "HTTP_CLIENT_IP",
        "HTTP_FORWARDED_FOR",
        "HTTP_FORWARDED",
        "HTTP_VIA",
        "REMOTE_ADDR"
    };

    private IpUtil() {
    }

    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String ip = null;

        for (String header : IP_HEADER_CANDIDATES) {
            ip = request.getHeader(header);
            if (isValidIp(ip)) {
                break;
            }
        }

        if (!isValidIp(ip)) {
            ip = request.getRemoteAddr();
        }

        if (LOCALHOST_IPV6.equals(ip)) {
            ip = LOCALHOST_IPV4;
        }

        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        if (LOCALHOST_IPV4.equals(ip)) {
            ip = getServerIp(request);
        }

        return ip != null ? ip : UNKNOWN;
    }

    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !UNKNOWN.equalsIgnoreCase(ip);
    }

    private static String getServerIp(HttpServletRequest request) {
        try {
            String serverIp = request.getLocalAddr();
            if (isValidIp(serverIp)) {
                return serverIp;
            }

            InetAddress address = InetAddress.getLocalHost();
            return address.getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("获取服务器IP失败：{}", e.getMessage());
            return LOCALHOST_IPV4;
        }
    }

    public static boolean isInternalIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        if (LOCALHOST_IPV4.equals(ip) || LOCALHOST_IPV6.equals(ip)) {
            return true;
        }

        if (ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.")) {
            return true;
        }

        byte[] addr = textToNumericFormatV4(ip);
        if (addr != null) {
            final byte b0 = addr[0];
            final byte b1 = addr[1];

            if (b0 == (byte) 10) {
                return true;
            } else if (b0 == (byte) 172 && b1 >= (byte) 16 && b1 <= (byte) 31) {
                return true;
            } else if (b0 == (byte) 192 && b1 == (byte) 168) {
                return true;
            }
        }

        return false;
    }

    private static byte[] textToNumericFormatV4(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        String[] parts = text.split("\\.");
        if (parts.length != 4) {
            return null;
        }

        byte[] bytes = new byte[4];
        try {
            for (int i = 0; i < 4; i++) {
                int value = Integer.parseInt(parts[i]);
                if (value < 0 || value > 255) {
                    return null;
                }
                bytes[i] = (byte) value;
            }
        } catch (NumberFormatException e) {
            return null;
        }

        return bytes;
    }

    public static String getIpProvince(String ip) {
        if (ip == null || ip.isEmpty() || UNKNOWN.equals(ip) || isInternalIp(ip)) {
            return "内网IP";
        }
        return "广东省深圳市";
    }

    public static String maskIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return ip;
        }

        if (ip.contains(".")) {
            String[] parts = ip.split("\\.");
            if (parts.length >= 4) {
                return parts[0] + "." + parts[1] + ".*.*";
            }
        }

        if (ip.contains(":")) {
            return ip.substring(0, ip.indexOf(":")) + ":****";
        }

        return ip;
    }
}
