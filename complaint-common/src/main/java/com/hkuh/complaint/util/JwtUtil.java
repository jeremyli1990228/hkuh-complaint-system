package com.hkuh.complaint.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * JWT工具类
 * 提供JWT Token的生成、解析、验证以及Redis Token管理功能
 * 支持访问令牌和刷新令牌分离，配合登录失败锁定机制提升安全性
 *
 * @author HKUH Complaint System
 * @version 1.0.0
 */
@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final String issuer;
    private final String header;
    private final String prefix;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final StringRedisTemplate redisTemplate;

    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";
    private static final String REDIS_KEY_ACCESS_TOKEN = "token:access:";
    private static final String REDIS_KEY_REFRESH_TOKEN = "token:refresh:";
    private static final String REDIS_KEY_LOGIN_FAILURE = "login:fail:";
    private static final String REDIS_KEY_LOGIN_LOCKED = "login:locked:";
    private static final int MAX_LOGIN_FAILURE_COUNT = 10;
    private static final long LOGIN_FAILURE_TTL = 30; // 分钟
    private static final long ACCOUNT_LOCK_TTL = 30; // 分钟

    /**
     * 构造函数，初始化JWT配置参数
     *
     * @param secret JWT签名密钥（至少256位）
     * @param issuer JWT签发者
     * @param header Authorization请求头名称
     * @param prefix Token前缀（Bearer）
     * @param accessTokenExpiration 访问令牌过期时间（毫秒）
     * @param refreshTokenExpiration 刷新令牌过期时间（毫秒）
     * @param redisTemplate Redis模板
     */
    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.header}") String header,
            @Value("${jwt.prefix}") String prefix,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration,
            StringRedisTemplate redisTemplate) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.header = header;
        this.prefix = prefix;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成访问令牌（Access Token）
     * 包含用户ID、用户名、角色列表等信息，有效期2小时
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param roles 用户角色列表
     * @return JWT访问令牌字符串
     */
    public String generateAccessToken(Long userId, String username, List<String> roles) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpiration);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roles", roles);
        claims.put("type", TOKEN_TYPE_ACCESS);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(userId))
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成刷新令牌（Refresh Token）
     * 包含用户ID、用户名信息，有效期90天
     *
     * @param userId 用户ID
     * @param username 用户名
     * @return JWT刷新令牌字符串
     */
    public String generateRefreshToken(Long userId, String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshTokenExpiration);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", TOKEN_TYPE_REFRESH);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(userId))
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析JWT Token
     * 返回Token中的Claims信息
     *
     * @param token JWT令牌字符串
     * @return Token中的Claims对象
     * @throws ExpiredJwtException 如果Token已过期
     * @throws JwtException 如果Token格式或签名无效
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.error("Token解析失败: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 验证Token是否为访问令牌
     *
     * @param token JWT令牌字符串
     * @return true表示是访问令牌，false表示不是
     */
    public boolean isAccessToken(String token) {
        try {
            Claims claims = parseToken(token);
            return TOKEN_TYPE_ACCESS.equals(claims.get("type"));
        } catch (Exception e) {
            log.error("验证访问令牌失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证Token是否为刷新令牌
     *
     * @param token JWT令牌字符串
     * @return true表示是刷新令牌，false表示不是
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            return TOKEN_TYPE_REFRESH.equals(claims.get("type"));
        } catch (Exception e) {
            log.error("验证刷新令牌失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证Token有效性
     * 包括签名验证和过期时间验证
     *
     * @param token JWT令牌字符串
     * @return true表示Token有效，false表示无效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token JWT令牌字符串
     * @return 用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        } else if (userId instanceof Long) {
            return (Long) userId;
        } else if (userId instanceof String) {
            return Long.parseLong((String) userId);
        }
        return null;
    }

    /**
     * 从Token中获取用户名
     *
     * @param token JWT令牌字符串
     * @return 用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("username");
    }

    /**
     * 存储访问令牌到Redis
     * Key格式: token:access:{userId}
     * Value: 完整的JWT Token
     * TTL: 2小时（与Token过期时间一致）
     *
     * @param userId 用户ID
     * @param token JWT访问令牌
     */
    public void storeAccessToken(Long userId, String token) {
        String key = REDIS_KEY_ACCESS_TOKEN + userId;
        redisTemplate.opsForValue().set(key, token, accessTokenExpiration, TimeUnit.MILLISECONDS);
        log.debug("存储访问令牌到Redis: userId={}, key={}", userId, key);
    }

    /**
     * 存储刷新令牌到Redis
     * Key格式: token:refresh:{userId}
     * Value: 完整的JWT Token
     * TTL: 90天（与Token过期时间一致）
     *
     * @param userId 用户ID
     * @param token JWT刷新令牌
     */
    public void storeRefreshToken(Long userId, String token) {
        String key = REDIS_KEY_REFRESH_TOKEN + userId;
        redisTemplate.opsForValue().set(key, token, refreshTokenExpiration, TimeUnit.MILLISECONDS);
        log.debug("存储刷新令牌到Redis: userId={}, key={}", userId, key);
    }

    /**
     * 删除用户所有Token
     * 包括访问令牌和刷新令牌
     * 用于用户退出登录时清除所有Token
     *
     * @param userId 用户ID
     */
    public void removeToken(Long userId) {
        String accessKey = REDIS_KEY_ACCESS_TOKEN + userId;
        String refreshKey = REDIS_KEY_REFRESH_TOKEN + userId;
        redisTemplate.delete(Arrays.asList(accessKey, refreshKey));
        log.info("删除用户所有Token: userId={}", userId);
    }

    /**
     * 验证Redis中的Token是否匹配
     * 用于多设备登录控制或Token强制失效场景
     *
     * @param userId 用户ID
     * @param token 要验证的JWT令牌
     * @return true表示Token在Redis中存在且匹配，false表示不匹配或不存在
     */
    public boolean isTokenValidInRedis(Long userId, String token) {
        String accessKey = REDIS_KEY_ACCESS_TOKEN + userId;
        String storedToken = redisTemplate.opsForValue().get(accessKey);
        return token.equals(storedToken);
    }

    /**
     * 记录登录失败次数
     * 使用Redis INCR命令实现原子性递增
     * TTL设置为30分钟，超时自动重置
     * 当失败次数达到10次时自动锁定账户
     *
     * @param userId 用户ID
     */
    public void recordLoginFailure(Long userId) {
        String key = REDIS_KEY_LOGIN_FAILURE + userId;
        Long count = redisTemplate.opsForValue().increment(key);

        // 首次失败时设置过期时间
        if (count != null && count == 1) {
            redisTemplate.expire(key, LOGIN_FAILURE_TTL, TimeUnit.MINUTES);
        }

        log.warn("记录登录失败: userId={}, count={}", userId, count);

        // 失败次数达到阈值时锁定账户
        if (count != null && count >= MAX_LOGIN_FAILURE_COUNT) {
            lockAccount(userId);
            log.error("账户因连续登录失败已被锁定: userId={}, count={}", userId, count);
        }
    }

    /**
     * 获取登录失败次数
     *
     * @param userId 用户ID
     * @return 当前登录失败次数
     */
    public int getLoginFailureCount(Long userId) {
        String key = REDIS_KEY_LOGIN_FAILURE + userId;
        String countStr = redisTemplate.opsForValue().get(key);
        if (countStr == null) {
            return 0;
        }
        try {
            return Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            log.error("解析登录失败次数失败: userId={}, countStr={}", userId, countStr);
            return 0;
        }
    }

    /**
     * 重置登录失败计数
     * 用户成功登录后调用此方法清除失败记录
     *
     * @param userId 用户ID
     */
    public void resetLoginFailure(Long userId) {
        String key = REDIS_KEY_LOGIN_FAILURE + userId;
        redisTemplate.delete(key);
        log.info("重置登录失败计数: userId={}", userId);
    }

    /**
     * 判断账户是否已被锁定
     *
     * @param userId 用户ID
     * @return true表示账户已被锁定，false表示未锁定
     */
    public boolean isAccountLocked(Long userId) {
        String lockedKey = REDIS_KEY_LOGIN_LOCKED + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockedKey));
    }

    /**
     * 锁定账户
     * 在Redis中设置锁定标记，TTL为30分钟
     * 同时清除登录失败计数
     *
     * @param userId 用户ID
     */
    public void lockAccount(Long userId) {
        String lockedKey = REDIS_KEY_LOGIN_LOCKED + userId;
        String failureKey = REDIS_KEY_LOGIN_FAILURE + userId;

        redisTemplate.opsForValue().set(lockedKey, "locked", ACCOUNT_LOCK_TTL, TimeUnit.MINUTES);
        redisTemplate.delete(failureKey);

        log.warn("账户已被锁定: userId={}, 锁定时长={}分钟", userId, ACCOUNT_LOCK_TTL);
    }

    /**
     * 解锁账户
     * 删除Redis中的锁定标记
     *
     * @param userId 用户ID
     */
    public void unlockAccount(Long userId) {
        String lockedKey = REDIS_KEY_LOGIN_LOCKED + userId;
        redisTemplate.delete(lockedKey);
        log.info("账户已解锁: userId={}", userId);
    }

    /**
     * 获取JWT配置的头部名称
     *
     * @return HTTP请求头名称（如：Authorization）
     */
    public String getHeader() {
        return header;
    }

    /**
     * 获取JWT配置的Token前缀
     *
     * @return Token前缀（如：Bearer）
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * 从请求头中提取Token
     * 自动去除Bearer前缀
     *
     * @param authorizationHeader Authorization请求头的值
     * @return 纯Token字符串，如果格式不正确返回null
     */
    public String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(prefix + " ")) {
            return authorizationHeader.substring(prefix.length() + 1);
        }
        return null;
    }
}
