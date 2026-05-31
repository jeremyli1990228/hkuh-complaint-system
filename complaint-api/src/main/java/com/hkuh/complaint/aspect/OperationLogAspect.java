package com.hkuh.complaint.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hkuh.complaint.annotation.OperationLog;
import com.hkuh.complaint.entity.SysOperationLog;
import com.hkuh.complaint.security.JwtAuthenticationFilter.LoginUserDetail;
import com.hkuh.complaint.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Aspect
@Component
@Slf4j
public class OperationLogAspect {

    @Autowired
    private ObjectMapper objectMapper;

    private static final String MASKED_VALUE = "***";

    @Pointcut("@annotation(com.hkuh.complaint.annotation.OperationLog)")
    public void operationLogPointcut() {
    }

    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog operationLog = method.getAnnotation(OperationLog.class);

        if (operationLog == null) {
            return joinPoint.proceed();
        }

        SysOperationLog logEntity = new SysOperationLog();
        logEntity.setModule(operationLog.module());
        logEntity.setOperation(operationLog.operation());
        logEntity.setMethod(joinPoint.getTarget().getClass().getName() + "." + method.getName());
        logEntity.setStatus(SysOperationLog.STATUS_SUCCESS);
        logEntity.setCreateTime(LocalDateTime.now());

        HttpServletRequest request = getHttpServletRequest();
        if (request != null) {
            logEntity.setRequestUrl(request.getRequestURI());
            logEntity.setRequestMethod(request.getMethod());
            logEntity.setOperatorIp(IpUtil.getClientIp(request));
        }

        fillOperatorInfo(logEntity);

        if (operationLog.recordParams()) {
            logEntity.setRequestParams(getRequestParams(joinPoint, operationLog.excludeParams()));
        }

        Object result = null;
        Throwable exception = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            exception = e;
            logEntity.setStatus(SysOperationLog.STATUS_FAILURE);
            logEntity.setErrorMsg(truncateErrorMsg(e.getMessage()));
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            logEntity.setCostTime(costTime);

            if (operationLog.recordResult() && result != null) {
                logEntity.setResponseData(getResponseData(result));
            }

            saveOperationLogAsync(logEntity);
        }
    }

    private HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private void fillOperatorInfo(SysOperationLog logEntity) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() != null) {
                Object principal = authentication.getPrincipal();
                
                if (principal instanceof LoginUserDetail) {
                    LoginUserDetail userDetail = (LoginUserDetail) principal;
                    logEntity.setOperatorName(userDetail.getUsername());
                } else if (principal instanceof String) {
                    String username = (String) principal;
                    if (!"anonymousUser".equals(username)) {
                        logEntity.setOperatorName(username);
                    }
                }

                if (authentication.getDetails() instanceof LoginUserDetail) {
                    LoginUserDetail userDetail = (LoginUserDetail) authentication.getDetails();
                    if (logEntity.getOperatorName() == null) {
                        logEntity.setOperatorName(userDetail.getUsername());
                    }
                }
            }
        } catch (Exception e) {
            log.debug("获取操作者信息失败：{}", e.getMessage());
        }
    }

    private String getRequestParams(ProceedingJoinPoint joinPoint, String[] excludeParams) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            if (paramNames == null || args == null || paramNames.length == 0) {
                return null;
            }

            Map<String, Object> paramsMap = new HashMap<>();
            Set<String> excludeSet = excludeParams != null ? 
                Set.of(Arrays.stream(excludeParams).map(String::toLowerCase).toArray(String[]::new)) : 
                Set.of();

            for (int i = 0; i < paramNames.length; i++) {
                String paramName = paramNames[i].toLowerCase();
                if (excludeSet.contains(paramName)) {
                    paramsMap.put(paramNames[i], MASKED_VALUE);
                } else {
                    Object arg = args[i];
                    if (arg != null && isSerializable(arg)) {
                        paramsMap.put(paramNames[i], arg);
                    } else if (arg != null) {
                        paramsMap.put(paramNames[i], arg.toString());
                    }
                }
            }

            return objectMapper.writeValueAsString(paramsMap);
        } catch (Exception e) {
            log.warn("获取请求参数失败：{}", e.getMessage());
            return null;
        }
    }

    private String getResponseData(Object result) {
        try {
            String json = objectMapper.writeValueAsString(result);
            return json.length() > 2000 ? json.substring(0, 2000) + "...(truncated)" : json;
        } catch (Exception e) {
            log.warn("序列化响应结果失败：{}", e.getMessage());
            return null;
        }
    }

    private String truncateErrorMsg(String message) {
        if (message == null) {
            return null;
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }

    private boolean isSerializable(Object obj) {
        return obj instanceof String || 
               obj instanceof Number || 
               obj instanceof Boolean ||
               obj instanceof Map ||
               obj instanceof Iterable;
    }

    @Async
    public void saveOperationLogAsync(SysOperationLog logEntity) {
        try {
            log.info("操作日志 - 模块：{}，操作：{}，耗时：{}ms，状态：{}",
                    logEntity.getModule(),
                    logEntity.getOperation(),
                    logEntity.getCostTime(),
                    logEntity.getStatus() == 1 ? "成功" : "失败");

        } catch (Exception e) {
            log.error("保存操作日志失败：{}", e.getMessage(), e);
        }
    }
}
