package com.hkuh.complaint.service;

import me.chanjar.weixin.mp.bean.result.WxMpUser;

import java.util.Map;

public interface WechatMpService {

    String generateOAuthUrl(String redirectUri, String state, String scope);

    String generateOAuthUrl(String redirectUri, String state);

    WechatOAuthVO handleOAuthCallback(String code);

    void createMenu();

    void sendTemplateMessage(String openId, String templateId, Map<String, String> data, String url);

    void sendSubscribeMessage(String openId, String templateId, Map<String, String> data, String page);

    String getQrcodeTicket(String scene, int expireSeconds);

    String getQrcodeTicket(String scene);

    WxMpUser getUserInfo(String openId);

    boolean checkSignature(String signature, String timestamp, String nonce);

    String getAccessToken();

    void refreshAccessToken();
}
