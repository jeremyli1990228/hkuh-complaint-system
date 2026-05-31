package com.hkuh.complaint.security;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hkuh.complaint.utils.JwtUtil;
import com.hkuh.complaint.utils.RedisUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String USER_TOKEN_PREFIX = "user:token:";
    private static final int MAX_CONCURRENT_TOKENS = 3;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);

            if (StrUtil.isBlank(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            if (isTokenBlacklisted(token)) {
                sendUnauthorizedResponse(response, "Token已失效");
                return;
            }

            if (!jwtUtil.validateToken(token)) {
                sendUnauthorizedResponse(response, "Token无效或已过期");
                return;
            }

            String userId = jwtUtil.getUserIdFromToken(token);
            String redisToken = redisTemplate.opsForValue().get(USER_TOKEN_PREFIX + userId);

            if (StrUtil.isBlank(redisToken) || !redisToken.equals(token)) {
                sendUnauthorizedResponse(response, "Token验证失败，请重新登录");
                return;
            }

            if (!checkConcurrentSession(userId, token)) {
                sendUnauthorizedResponse(response, "登录会话数超限，请重新登录");
                return;
            }

            List<String> roles = jwtUtil.getRolesFromToken(token);
            List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userId, null, authorities);
            
            authentication.setDetails(new LoginUserDetail(
                jwtUtil.getUsernameFromToken(token),
                jwtUtil.getDeptIdFromToken(token),
                roles
            ));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            updateTokenExpireTime(userId);
            
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("JWT认证过滤器异常: {}", e.getMessage(), e);
            sendUnauthorizedResponse(response, "认证失败，请稍后重试");
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StrUtil.isNotBlank(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token));
    }

    private boolean checkConcurrentSession(String userId, String currentToken) {
        String sessionKey = USER_TOKEN_PREFIX + userId + ":sessions";
        Long currentSize = redisTemplate.opsForSet().size(sessionKey);
        
        if (currentSize == null) {
            return true;
        }

        if (currentSize >= MAX_CONCURRENT_TOKENS) {
            String oldestToken = redisTemplate.opsForSet().pop(sessionKey);
            if (oldestToken != null) {
                redisTemplate.opsForValue().getAndDelete(TOKEN_BLACKLIST_PREFIX + oldestToken);
                redisTemplate.opsForSet().add(sessionKey, currentToken);
            }
            return true;
        }

        redisTemplate.opsForSet().add(sessionKey, currentToken);
        redisTemplate.expire(sessionKey, 24, TimeUnit.HOURS);
        return true;
    }

    private void updateTokenExpireTime(String userId) {
        String tokenKey = USER_TOKEN_PREFIX + userId;
        Long expire = redisTemplate.getExpire(tokenKey);
        if (expire != null && expire > 0) {
            redisTemplate.expire(tokenKey, expire, TimeUnit.SECONDS);
        }
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("message", message);
        result.put("data", null);
        
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/") 
            || path.startsWith("/api/h5/feedback/create")
            || path.startsWith("/api/h5/satisfaction/")
            || path.startsWith("/api/h5/faq/")
            || path.startsWith("/doc.html")
            || path.startsWith("/swagger-ui")
            || path.startsWith("/v3/api-docs")
            || path.equals("/favicon.ico")
            || path.startsWith("/actuator/");
    }

    public static class LoginUserDetail {
        private String username;
        private Long deptId;
        private List<String> roles;

        public LoginUserDetail(String username, Long deptId, List<String> roles) {
            this.username = username;
            this.deptId = deptId;
            this.roles = roles;
        }

        public String getUsername() {
            return username;
        }

        public Long getDeptId() {
            return deptId;
        }

        public List<String> getRoles() {
            return roles;
        }
    }
}
