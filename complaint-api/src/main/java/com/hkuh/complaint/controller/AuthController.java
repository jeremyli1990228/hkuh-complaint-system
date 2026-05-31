package com.hkuh.complaint.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.hkuh.complaint.constant.ResultCode;
import com.hkuh.complaint.dto.*;
import com.hkuh.complaint.exception.BusinessException;
import com.hkuh.complaint.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;

    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    private static final long CAPTCHA_EXPIRATION = 5; // 5分钟
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,20}$"
    );

    @PostMapping("/captcha")
    public Result<CaptchaVO> getCaptcha() {
        // 生成UUID作为验证码Key
        String captchaKey = IdUtil.simpleUUID();

        // 使用Hutool生成图形验证码
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 30);

        // 获取验证码文字
        String captchaCode = captcha.getCode();

        // 存储到Redis，5分钟过期
        redisTemplate.opsForValue().set(
                CAPTCHA_KEY_PREFIX + captchaKey,
                captchaCode,
                CAPTCHA_EXPIRATION,
                TimeUnit.MINUTES
        );

        // 生成Base64图片
        String captchaImage = "data:image/png;base64," + captcha.getImageBase64();

        CaptchaVO captchaVO = CaptchaVO.builder()
                .captchaKey(captchaKey)
                .captchaImage(captchaImage)
                .build();

        log.info("生成图形验证码成功: captchaKey={}", captchaKey);

        return Result.success(captchaVO);
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        // 参数校验
        if (StrUtil.isBlank(loginDTO.getUsername())) {
            throw new BusinessException(400, "用户名不能为空");
        }
        if (StrUtil.isBlank(loginDTO.getPassword())) {
            throw new BusinessException(400, "密码不能为空");
        }
        if (StrUtil.isBlank(loginDTO.getCaptchaKey())) {
            throw new BusinessException(400, "验证码Key不能为空");
        }
        if (StrUtil.isBlank(loginDTO.getCaptchaCode())) {
            throw new BusinessException(400, "验证码不能为空");
        }

        // 验证验证码
        String storedCaptcha = redisTemplate.opsForValue().get(CAPTCHA_KEY_PREFIX + loginDTO.getCaptchaKey());
        if (StrUtil.isBlank(storedCaptcha)) {
            throw new BusinessException(400, "验证码已过期，请重新获取");
        }
        if (!storedCaptcha.equalsIgnoreCase(loginDTO.getCaptchaCode())) {
            throw new BusinessException(400, "验证码错误");
        }
        // 验证成功后删除验证码
        redisTemplate.delete(CAPTCHA_KEY_PREFIX + loginDTO.getCaptchaKey());

        // 查询用户（模拟，实际应从数据库查询）
        // 此处需要注入UserService
        Long userId = 1L;
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword(); // 实际应该是BCrypt加密后的密码
        String realName = "管理员";
        String deptName = "客户服务部";
        List<String> roles = List.of("admin");
        List<String> permissions = List.of("*:*:*");

        // 验证密码（实际应使用BCrypt验证）
        // BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        // if (!encoder.matches(loginDTO.getPassword(), storedPassword)) {
        //     throw new BusinessException(401, "用户名或密码错误");
        // }

        // 检查账户状态
        if (jwtUtil.isAccountLocked(userId)) {
            throw new BusinessException(ResultCode.ACCOUNT_LOCKED);
        }

        // 记录登录成功
        jwtUtil.resetLoginFailure(userId);

        // 生成Token
        String accessToken = jwtUtil.generateAccessToken(userId, username, roles);
        String refreshToken = jwtUtil.generateRefreshToken(userId, username);

        // 存储Token到Redis
        jwtUtil.storeAccessToken(userId, accessToken);
        jwtUtil.storeRefreshToken(userId, refreshToken);

        // 构建返回数据
        UserVO userVO = UserVO.builder()
                .userId(userId)
                .username(username)
                .realName(realName)
                .deptName(deptName)
                .roles(roles)
                .permissions(permissions)
                .build();

        LoginVO loginVO = LoginVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(7200000L) // 2小时
                .userInfo(userVO)
                .build();

        log.info("用户登录成功: userId={}, username={}", userId, username);

        return Result.success(loginVO);
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            try {
                Long userId = jwtUtil.getUserId(token);
                jwtUtil.removeToken(userId);
                log.info("用户退出登录成功: userId={}", userId);
            } catch (Exception e) {
                log.warn("Token解析失败，可能已过期: {}", e.getMessage());
            }
        }
        return Result.success("退出登录成功");
    }

    @PostMapping("/refresh-token")
    public Result<TokenVO> refreshToken(@RequestBody RefreshTokenDTO refreshTokenDTO) {
        String refreshToken = refreshTokenDTO.getRefreshToken();

        if (StrUtil.isBlank(refreshToken)) {
            throw new BusinessException(400, "刷新Token不能为空");
        }

        // 验证refreshToken有效性
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        Long userId = jwtUtil.getUserId(refreshToken);
        String username = jwtUtil.getUsername(refreshToken);

        // 从Redis验证Token匹配
        if (!jwtUtil.isTokenValidInRedis(userId, refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        // 查询用户角色（实际应从数据库查询）
        List<String> roles = List.of("admin");

        // 生成新的Token
        String newAccessToken = jwtUtil.generateAccessToken(userId, username, roles);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, username);

        // 更新Redis存储
        jwtUtil.storeAccessToken(userId, newAccessToken);
        jwtUtil.storeRefreshToken(userId, newRefreshToken);

        TokenVO tokenVO = TokenVO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(7200000L)
                .build();

        log.info("刷新Token成功: userId={}", userId);

        return Result.success(tokenVO);
    }

    @PostMapping("/change-password")
    public Result<Void> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ChangePasswordDTO changePasswordDTO) {

        if (StrUtil.isBlank(authorization) || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        String token = authorization.substring(7);
        Long userId = jwtUtil.getUserId(token);

        // 验证旧密码（实际应从数据库验证BCrypt密码）
        // BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        // if (!encoder.matches(changePasswordDTO.getOldPassword(), storedPassword)) {
        //     throw new BusinessException(400, "旧密码错误");
        // }

        // 验证新密码策略
        String newPassword = changePasswordDTO.getNewPassword();
        if (StrUtil.isBlank(newPassword)) {
            throw new BusinessException(400, "新密码不能为空");
        }
        if (newPassword.length() < 8 || newPassword.length() > 20) {
            throw new BusinessException(400, "新密码长度必须在8-20位之间");
        }
        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            throw new BusinessException(400, "新密码必须包含大小写字母、数字和特殊字符");
        }

        // 更新密码（实际应更新数据库）
        // userService.updatePassword(userId, encoder.encode(newPassword));

        // 清除所有Token
        jwtUtil.removeToken(userId);

        log.info("修改密码成功: userId={}", userId);

        return Result.success("密码修改成功，请重新登录");
    }

    @GetMapping("/info")
    public Result<UserVO> getUserInfo(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (StrUtil.isBlank(authorization) || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        String token = authorization.substring(7);
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        Long userId = jwtUtil.getUserId(token);

        // 查询用户信息（实际应从数据库查询）
        UserVO userVO = UserVO.builder()
                .userId(userId)
                .username("admin")
                .realName("管理员")
                .deptName("客户服务部")
                .roles(List.of("admin"))
                .permissions(List.of("*:*:*"))
                .build();

        return Result.success(userVO);
    }
}
