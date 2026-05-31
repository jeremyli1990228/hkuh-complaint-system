package com.hkuh.complaint.service.impl;

import cn.hutool.core.util.IdUtil;
import com.hkuh.complaint.service.WechatMpService;
import com.hkuh.complaint.vo.WechatOAuthVO;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpMenuService;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.WxMpTemplateMsgService;
import me.chanjar.weixin.mp.bean.menu.WxMpMenu;
import me.chanjar.weixin.mp.bean.menu.WxMpMenuButton;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessTokenResult;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WechatMpServiceImpl implements WechatMpService {

    @Autowired
    private WxMpService wxMpService;

    @Value("${wechat.mp.h5-base-url:https://m.hkuh.hk}")
    private String h5BaseUrl;

    @Value("${wechat.mp.template-id.complaint-notice:TM00001}")
    private String complaintNoticeTemplateId;

    @Value("${wechat.mp.template-id.process-notice:TM00002}")
    private String processNoticeTemplateId;

    @Value("${wechat.mp.template-id.complete-notice:TM00003}")
    private String completeNoticeTemplateId;

    @Value("${wechat.mp.template-id.survey-invite:TM00004}")
    private String surveyInviteTemplateId;

    @Override
    public String generateOAuthUrl(String redirectUri, String state, String scope) {
        try {
            String url = wxMpService.getOAuth2Service().buildAuthorizationUrl(redirectUri, scope, state);
            log.info("生成OAuth授权URL: {}", url);
            return url;
        } catch (WxErrorException e) {
            log.error("生成OAuth授权URL失败: {}", e.getMessage());
            throw new RuntimeException("生成授权链接失败", e);
        }
    }

    @Override
    public String generateOAuthUrl(String redirectUri, String state) {
        return generateOAuthUrl(redirectUri, state, "snsapi_base");
    }

    @Override
    public WechatOAuthVO handleOAuthCallback(String code) {
        try {
            WxMpOAuth2AccessTokenResult accessTokenResult = wxMpService.getOAuth2Service().getAccessToken(code);

            String openId = accessTokenResult.getOpenid();
            String accessToken = accessTokenResult.getAccessToken();
            Long expiresIn = accessTokenResult.getExpiresIn();

            WechatOAuthVO.WechatOAuthVOBuilder builder = WechatOAuthVO.builder()
                    .openId(openId)
                    .accessToken(accessToken)
                    .expiresIn(expiresIn)
                    .sessionToken(IdUtil.simpleUUID());

            try {
                WxMpUser userInfo = wxMpService.getUserService().getUserInfo(openId);
                if (userInfo != null) {
                    builder.unionId(userInfo.getUnionId())
                            .nickname(userInfo.getNickname())
                            .avatar(userInfo.getHeadImgUrl())
                            .subscribe(userInfo.getSubscribe())
                            .subscribeTime(userInfo.getSubscribeTime());
                }
            } catch (WxErrorException e) {
                log.warn("获取用户信息失败，可能是静默授权模式: {}", e.getMessage());
            }

            log.info("OAuth回调处理成功, openId: {}", openId);
            return builder.build();

        } catch (WxErrorException e) {
            log.error("处理OAuth回调失败: {}", e.getMessage());
            throw new RuntimeException("处理授权回调失败", e);
        }
    }

    @Override
    public void createMenu() {
        try {
            WxMpMenuService menuService = wxMpService.getMenuService();

            List<WxMpMenuButton> buttons = new ArrayList<>();

            WxMpMenuButton complaint = new WxMpMenuButton();
            complaint.setName("投诉建议");
            List<WxMpMenuButton> complaintSubButtons = new ArrayList<>();

            WxMpMenuButton onlineComplaint = new WxMpMenuButton();
            onlineComplaint.setType("view");
            onlineComplaint.setName("在线投诉");
            onlineComplaint.setUrl(h5BaseUrl + "/submit");
            complaintSubButtons.add(onlineComplaint);

            WxMpMenuButton myComplaint = new WxMpMenuButton();
            myComplaint.setType("view");
            myComplaint.setName("我的投诉");
            myComplaint.setUrl(h5BaseUrl + "/my-list");
            complaintSubButtons.add(myComplaint);

            WxMpMenuButton faq = new WxMpMenuButton();
            faq.setType("view");
            faq.setName("常见问题");
            faq.setUrl(h5BaseUrl + "/faq");
            complaintSubButtons.add(faq);

            complaint.setSubButtons(complaintSubButtons);
            buttons.add(complaint);

            WxMpMenuButton survey = new WxMpMenuButton();
            survey.setName("满意度调查");
            List<WxMpMenuButton> surveySubButtons = new ArrayList<>();

            WxMpMenuButton joinSurvey = new WxMpMenuButton();
            joinSurvey.setType("view");
            joinSurvey.setName("参与评价");
            joinSurvey.setUrl(h5BaseUrl + "/survey");
            surveySubButtons.add(joinSurvey);

            WxMpMenuButton viewResult = new WxMpMenuButton();
            viewResult.setType("view");
            viewResult.setName("查看结果");
            viewResult.setUrl(h5BaseUrl + "/survey-result");
            surveySubButtons.add(viewResult);

            survey.setSubButtons(surveySubButtons);
            buttons.add(survey);

            WxMpMenuButton profile = new WxMpMenuButton();
            profile.setName("个人中心");
            List<WxMpMenuButton> profileSubButtons = new ArrayList<>();

            WxMpMenuButton bindAccount = new WxMpMenuButton();
            bindAccount.setType("view");
            bindAccount.setName("绑定账号");
            bindAccount.setUrl(h5BaseUrl + "/bind");
            profileSubButtons.add(bindAccount);

            WxMpMenuButton aboutUs = new WxMpMenuButton();
            aboutUs.setType("view");
            aboutUs.setName("关于我们");
            aboutUs.setUrl(h5BaseUrl + "/about");
            profileSubButtons.add(aboutUs);

            profile.setSubButtons(profileSubButtons);
            buttons.add(profile);

            menuService.menuCreate(buttons);
            log.info("微信公众号菜单创建成功");

        } catch (WxErrorException e) {
            log.error("创建微信公众号菜单失败: {}", e.getMessage());
            throw new RuntimeException("创建菜单失败", e);
        }
    }

    @Override
    public void sendTemplateMessage(String openId, String templateId, Map<String, String> data, String url) {
        try {
            WxMpTemplateMsgService templateMsgService = wxMpService.getTemplateMsgService();

            Map<String, String> templateData = new HashMap<>();
            for (Map.Entry<String, String> entry : data.entrySet()) {
                templateData.put(entry.getKey(), entry.getValue());
            }

            templateMsgService.sendTemplateMsg(
                    me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage.builder()
                            .toUser(openId)
                            .templateId(templateId)
                            .url(url)
                            .data(templateData)
                            .build()
            );

            log.info("发送模板消息成功, openId: {}, templateId: {}", openId, templateId);

        } catch (WxErrorException e) {
            log.error("发送模板消息失败: {}, openId: {}, templateId: {}", e.getMessage(), openId, templateId);
            throw new RuntimeException("发送模板消息失败", e);
        }
    }

    @Override
    public void sendComplaintNotice(String openId, String feedbackNo, String title, String status) {
        Map<String, String> data = new HashMap<>();
        data.put("first", "您好，您提交的投诉已受理");
        data.put("keyword1", feedbackNo);
        data.put("keyword2", title);
        data.put("keyword3", status);
        data.put("remark", "我们将在24小时内处理您的投诉，感谢您的理解与支持！");

        String url = h5BaseUrl + "/detail?feedbackNo=" + feedbackNo;
        sendTemplateMessage(openId, complaintNoticeTemplateId, data, url);
    }

    @Override
    public void sendProcessNotice(String openId, String feedbackNo, String progress, String handler) {
        Map<String, String> data = new HashMap<>();
        data.put("first", "您好，您的投诉有新进展");
        data.put("keyword1", feedbackNo);
        data.put("keyword2", progress);
        data.put("keyword3", handler);
        data.put("remark", "点击查看详情，如有疑问请联系客服。");

        String url = h5BaseUrl + "/detail?feedbackNo=" + feedbackNo;
        sendTemplateMessage(openId, processNoticeTemplateId, data, url);
    }

    @Override
    public void sendCompleteNotice(String openId, String feedbackNo, String result) {
        Map<String, String> data = new HashMap<>();
        data.put("first", "您好，您的投诉已处理完成");
        data.put("keyword1", feedbackNo);
        data.put("keyword2", "已处理完成");
        data.put("keyword3", result);
        data.put("remark", "感谢您的反馈，请对本次服务进行评价。");

        String url = h5BaseUrl + "/detail?feedbackNo=" + feedbackNo;
        sendTemplateMessage(openId, completeNoticeTemplateId, data, url);
    }

    @Override
    public void sendSurveyInvite(String openId, String suggestion) {
        Map<String, String> data = new HashMap<>();
        data.put("first", "感谢您对我们工作的支持");
        data.put("keyword1", "满意度调查");
        data.put("keyword2", "请参与满意度评价");
        data.put("remark", suggestion != null ? suggestion : "您的评价对我们非常重要");

        String url = h5BaseUrl + "/survey?feedbackNo=xxx";
        sendTemplateMessage(openId, surveyInviteTemplateId, data, url);
    }

    @Override
    public void sendSubscribeMessage(String openId, String templateId, Map<String, String> data, String page) {
        try {
            Map<String, Object> subscribeData = new HashMap<>();
            for (Map.Entry<String, String> entry : data.entrySet()) {
                Map<String, Object> item = new HashMap<>();
                item.put("value", entry.getValue());
                subscribeData.put(entry.getKey(), item);
            }

            wxMpService.getSubscribeMsgService().sendSubscribeMsg(
                    me.chanjar.weixin.mp.bean.result.WxMpSubscribeMessage.builder()
                            .templateId(templateId)
                            .scene("1000")
                            .title("投诉进度通知")
                            .content("您有一条新的投诉处理进度")
                            .data(subscribeData)
                            .page(page)
                            .toUser(openId)
                            .build()
            );

            log.info("发送订阅消息成功, openId: {}, templateId: {}", openId, templateId);

        } catch (WxErrorException e) {
            log.error("发送订阅消息失败: {}, openId: {}, templateId: {}", e.getMessage(), openId, templateId);
            throw new RuntimeException("发送订阅消息失败", e);
        }
    }

    @Override
    public String getQrcodeTicket(String scene, int expireSeconds) {
        try {
            WxMpQrCodeTicket ticket = wxMpService.getQrcodeService().qrCodeCreateTmpTicket(scene, expireSeconds);
            log.info("生成临时二维码成功, scene: {}, expireSeconds: {}", scene, expireSeconds);
            return ticket.getTicket();
        } catch (WxErrorException e) {
            log.error("生成二维码失败: {}, scene: {}", e.getMessage(), scene);
            throw new RuntimeException("生成二维码失败", e);
        }
    }

    @Override
    public String getQrcodeTicket(String scene) {
        return getQrcodeTicket(scene, 2592000);
    }

    @Override
    public WxMpUser getUserInfo(String openId) {
        try {
            WxMpUser userInfo = wxMpService.getUserService().getUserInfo(openId);
            log.info("获取用户信息成功, openId: {}", openId);
            return userInfo;
        } catch (WxErrorException e) {
            log.error("获取用户信息失败: {}, openId: {}", e.getMessage(), openId);
            throw new RuntimeException("获取用户信息失败", e);
        }
    }

    @Override
    public boolean checkSignature(String signature, String timestamp, String nonce) {
        try {
            return wxMpService.getCheckSignatureService().check(timestamp, nonce, signature);
        } catch (WxErrorException e) {
            log.error("验证签名失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getAccessToken() {
        try {
            return wxMpService.getAccessToken();
        } catch (WxErrorException e) {
            log.error("获取AccessToken失败: {}", e.getMessage());
            throw new RuntimeException("获取AccessToken失败", e);
        }
    }

    @Override
    public void refreshAccessToken() {
        try {
            wxMpService.getAccessToken(true);
            log.info("刷新AccessToken成功");
        } catch (WxErrorException e) {
            log.error("刷新AccessToken失败: {}", e.getMessage());
            throw new RuntimeException("刷新AccessToken失败", e);
        }
    }
}
