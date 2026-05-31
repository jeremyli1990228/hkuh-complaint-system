package com.hkuh.complaint.service.impl;

import com.hkuh.complaint.service.SmsService;
import com.hkuh.complaint.util.DesensitizeUtil;
import com.hkuh.complaint.vo.SendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${sms.provider:aliyun}")
    private String smsProvider;

    @Value("${sms.aliyun.sign-name:港大医院}")
    private String signName;

    @Value("${sms.aliyun.template-code.complaint-accept:SMS_001}")
    private String complaintAcceptTemplateCode;

    @Value("${sms.aliyun.template-code.complaint-progress:SMS_002}")
    private String complaintProgressTemplateCode;

    @Value("${sms.aliyun.template-code.complaint-complete:SMS_003}")
    private String complaintCompleteTemplateCode;

    @Value("${sms.aliyun.template-code.satisfaction-invite:SMS_004}")
    private String satisfactionInviteTemplateCode;

    @Value("${sms.aliyun.template-code.verification-code:SMS_005}")
    private String verificationCodeTemplateCode;

    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final String SMS_FREQUENCY_PREFIX = "sms:frequency:";
    private static final String SMS_HOURLY_PREFIX = "sms:hourly:";
    private static final int VERIFY_CODE_EXPIRE_MINUTES = 5;
    private static final int FREQUENCY_LIMIT_MINUTES = 1;
    private static final int HOURLY_LIMIT_COUNT = 5;

    @Override
    public SendResult sendSms(String phone, String templateCode, Map<String, String> params) {
        long startTime = System.currentTimeMillis();

        if (!validatePhone(phone)) {
            return SendResult.failure("手机号格式不正确");
        }

        try {
            log.info("发送短信 - 手机号: {}, 模板: {}, 参数: {}",
                    DesensitizeUtil.maskPhone(phone), templateCode, params);

            String messageId = sendByProvider(phone, templateCode, params);

            long costTime = System.currentTimeMillis() - startTime;

            recordSmsSend(phone, templateCode, SendResult.success(messageId, costTime));

            return SendResult.success(messageId, costTime);

        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("发送短信失败 - 手机号: {}, 错误: {}",
                    DesensitizeUtil.maskPhone(phone), e.getMessage());

            recordSmsSend(phone, templateCode, SendResult.failure(e.getMessage()));

            return SendResult.failure(e.getMessage());
        }
    }

    private String sendByProvider(String phone, String templateCode, Map<String, String> params) throws Exception {
        switch (smsProvider.toLowerCase()) {
            case "aliyun":
                return sendByAliyun(phone, templateCode, params);
            case "tencent":
                return sendByTencent(phone, templateCode, params);
            case "huawei":
                return sendByHuawei(phone, templateCode, params);
            default:
                return sendByAliyun(phone, templateCode, params);
        }
    }

    private String sendByAliyun(String phone, String templateCode, Map<String, String> params) throws Exception {
        log.debug("使用阿里云发送短信 - templateCode: {}", templateCode);
        return "ALIYUN_" + System.currentTimeMillis();
    }

    private String sendByTencent(String phone, String templateCode, Map<String, String> params) throws Exception {
        log.debug("使用腾讯云发送短信 - templateCode: {}", templateCode);
        return "TENCENT_" + System.currentTimeMillis();
    }

    private String sendByHuawei(String phone, String templateCode, Map<String, String> params) throws Exception {
        log.debug("使用华为云发送短信 - templateCode: {}", templateCode);
        return "HUAWEI_" + System.currentTimeMillis();
    }

    @Override
    public void sendComplaintAcceptNotice(String phone, String feedbackNo, String title) {
        if (!checkSendFrequency(phone)) {
            log.warn("短信发送频率超限 - 手机号: {}", DesensitizeUtil.maskPhone(phone));
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("feedbackNo", feedbackNo);
        params.put("title", truncateString(title, 20));
        params.put("hospitalName", signName);

        SendResult result = sendSms(phone, complaintAcceptTemplateCode, params);
        if (!result.isSuccess()) {
            log.error("发送投诉受理通知失败 - 手机号: {}, 错误: {}",
                    DesensitizeUtil.maskPhone(phone), result.getErrorMsg());
        }
    }

    @Override
    public void sendComplaintProgressNotice(String phone, String feedbackNo, String action, String content) {
        if (!checkSendFrequency(phone)) {
            log.warn("短信发送频率超限 - 手机号: {}", DesensitizeUtil.maskPhone(phone));
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("feedbackNo", feedbackNo);
        params.put("action", action);
        params.put("content", truncateString(content, 50));

        SendResult result = sendSms(phone, complaintProgressTemplateCode, params);
        if (!result.isSuccess()) {
            log.error("发送处理进度通知失败 - 手机号: {}, 错误: {}",
                    DesensitizeUtil.maskPhone(phone), result.getErrorMsg());
        }
    }

    @Override
    public void sendComplaintCompleteNotice(String phone, String feedbackNo, String result) {
        if (!checkSendFrequency(phone)) {
            log.warn("短信发送频率超限 - 手机号: {}", DesensitizeUtil.maskPhone(phone));
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("feedbackNo", feedbackNo);
        params.put("result", truncateString(result, 100));

        SendResult sendResult = sendSms(phone, complaintCompleteTemplateCode, params);
        if (!sendResult.isSuccess()) {
            log.error("发送投诉完成通知失败 - 手机号: {}, 错误: {}",
                    DesensitizeUtil.maskPhone(phone), sendResult.getErrorMsg());
        }
    }

    @Override
    public void sendSatisfactionInvite(String phone, String feedbackNo, String surveyUrl) {
        if (!checkSendFrequency(phone)) {
            log.warn("短信发送频率超限 - 手机号: {}", DesensitizeUtil.maskPhone(phone));
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("feedbackNo", feedbackNo);
        params.put("surveyUrl", truncateString(surveyUrl, 50));

        SendResult result = sendSms(phone, satisfactionInviteTemplateCode, params);
        if (!result.isSuccess()) {
            log.error("发送满意度调查邀请失败 - 手机号: {}, 错误: {}",
                    DesensitizeUtil.maskPhone(phone), result.getErrorMsg());
        }
    }

    @Override
    public SendResult sendVerificationCode(String phone, String code) {
        if (!validatePhone(phone)) {
            return SendResult.failure("手机号格式不正确");
        }

        if (!checkSendFrequency(phone)) {
            return SendResult.failure("发送频率超限，请稍后再试");
        }

        try {
            String codeKey = SMS_CODE_PREFIX + phone;
            redisTemplate.opsForValue().set(codeKey, code, VERIFY_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

            Map<String, String> params = new HashMap<>();
            params.put("code", code);
            params.put("expireMinutes", String.valueOf(VERIFY_CODE_EXPIRE_MINUTES));

            SendResult result = sendSms(phone, verificationCodeTemplateCode, params);

            if (result.isSuccess()) {
                log.info("验证码发送成功 - 手机号: {}", DesensitizeUtil.maskPhone(phone));
            } else {
                redisTemplate.delete(codeKey);
                log.error("验证码发送失败 - 手机号: {}, 错误: {}",
                        DesensitizeUtil.maskPhone(phone), result.getErrorMsg());
            }

            return result;

        } catch (Exception e) {
            log.error("发送验证码异常 - 手机号: {}, 错误: {}",
                    DesensitizeUtil.maskPhone(phone), e.getMessage());
            return SendResult.failure("发送失败：" + e.getMessage());
        }
    }

    @Override
    public boolean verifyCode(String phone, String code) {
        if (!validatePhone(phone) || code == null || code.isEmpty()) {
            return false;
        }

        String codeKey = SMS_CODE_PREFIX + phone;
        String storedCode = redisTemplate.opsForValue().get(codeKey);

        if (storedCode == null) {
            log.debug("验证码已过期或不存在 - 手机号: {}", DesensitizeUtil.maskPhone(phone));
            return false;
        }

        if (storedCode.equals(code)) {
            redisTemplate.delete(codeKey);
            log.info("验证码验证成功 - 手机号: {}", DesensitizeUtil.maskPhone(phone));
            return true;
        }

        log.debug("验证码错误 - 手机号: {}, 输入: {}, 存储: {}",
                DesensitizeUtil.maskPhone(phone), code, storedCode);
        return false;
    }

    @Override
    public boolean checkSendFrequency(String phone) {
        String frequencyKey = SMS_FREQUENCY_PREFIX + phone;
        String hourlyKey = SMS_HOURLY_PREFIX + phone;

        Boolean hasRecentSend = redisTemplate.hasKey(frequencyKey);
        if (Boolean.TRUE.equals(hasRecentSend)) {
            log.debug("1分钟内已发送过短信 - 手机号: {}", DesensitizeUtil.maskPhone(phone));
            return false;
        }

        Long hourlyCount = redisTemplate.opsForValue().increment(hourlyKey);
        if (hourlyCount == null) {
            hourlyCount = 1L;
        }

        if (hourlyCount > HOURLY_LIMIT_COUNT) {
            log.debug("1小时内发送次数超限 - 手机号: {}, 次数: {}",
                    DesensitizeUtil.maskPhone(phone), hourlyCount);
            return false;
        }

        redisTemplate.opsForValue().set(frequencyKey, "1", FREQUENCY_LIMIT_MINUTES, TimeUnit.MINUTES);
        redisTemplate.expire(hourlyKey, 1, TimeUnit.HOURS);

        return true;
    }

    @Override
    public void recordSmsSend(String phone, String templateCode, SendResult result) {
        log.info("短信发送记录 - 手机号: {}, 模板: {}, 成功: {}, messageId: {}, 错误: {}",
                DesensitizeUtil.maskPhone(phone),
                templateCode,
                result.isSuccess(),
                result.getMessageId(),
                result.getErrorMsg());
    }

    private boolean validatePhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        String cleanPhone = phone.replaceAll("[\\s-]", "");
        return cleanPhone.matches("^1[3-9]\\d{9}$");
    }

    private String truncateString(String str, int maxLength) {
        if (str == null) {
            return "";
        }
        return str.length() > maxLength ? str.substring(0, maxLength) + "..." : str;
    }
}
