package com.hkuh.complaint.service;

import java.util.Map;

public interface SmsService {

    SendResult sendSms(String phone, String templateCode, Map<String, String> params);

    void sendComplaintAcceptNotice(String phone, String feedbackNo, String title);

    void sendComplaintProgressNotice(String phone, String feedbackNo, String action, String content);

    void sendComplaintCompleteNotice(String phone, String feedbackNo, String result);

    void sendSatisfactionInvite(String phone, String feedbackNo, String surveyUrl);

    SendResult sendVerificationCode(String phone, String code);

    boolean verifyCode(String phone, String code);

    boolean checkSendFrequency(String phone);

    void recordSmsSend(String phone, String templateCode, SendResult result);
}
