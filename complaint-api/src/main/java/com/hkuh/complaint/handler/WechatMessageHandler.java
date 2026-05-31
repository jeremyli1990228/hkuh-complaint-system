package com.hkuh.complaint.handler;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutTextMessage;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class WechatMessageHandler implements WxMpMessageHandler {

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                     Map<String, Object> context,
                                     WxMpService wxMpService,
                                     WxSessionManager sessionManager) {

        String msgType = wxMessage.getMsgType();
        String openId = wxMessage.getFromUser();

        log.info("收到微信消息 - 类型: {}, OpenId: {}, Content: {}",
                msgType, openId, wxMessage.getContent());

        switch (msgType) {
            case "text":
                return handleTextMessage(wxMessage, wxMpService);
            case "image":
                return handleImageMessage(wxMessage, wxMpService);
            case "voice":
                return handleVoiceMessage(wxMessage, wxMpService);
            case "video":
                return handleVideoMessage(wxMessage, wxMpService);
            case "shortvideo":
                return handleShortVideoMessage(wxMessage, wxMpService);
            case "location":
                return handleLocationMessage(wxMessage, wxMpService);
            case "link":
                return handleLinkMessage(wxMessage, wxMpService);
            case "event":
                return handleEventMessage(wxMessage, wxMpService);
            default:
                return handleUnknownMessage(wxMessage, wxMpService);
        }
    }

    private WxMpXmlOutMessage handleTextMessage(WxMpXmlMessage wxMessage, WxMpService wxMpService) {
        String content = wxMessage.getContent().trim();

        if (content.startsWith("投诉") || content.contains("投诉")) {
            return createTextMessage(wxMessage,
                    "您好，欢迎使用港大医院投诉服务！\n\n" +
                    "您可以通过以下方式提交投诉：\n" +
                    "1. 点击菜单【投诉建议】-【在线投诉】\n" +
                    "2. 访问我们的投诉网站\n\n" +
                    "如有紧急问题，请拨打投诉热线：0755-86913333");
        }

        if (content.startsWith("调查") || content.contains("满意度")) {
            return createTextMessage(wxMessage,
                    "您好，感谢您对港大医院的支持！\n\n" +
                    "点击菜单【满意度调查】-【参与评价】\n" +
                    "即可对本次就医体验进行评价。\n\n" +
                    "您的反馈将帮助我们做得更好！");
        }

        if (content.startsWith("电话") || content.contains("热线")) {
            return createTextMessage(wxMessage,
                    "港大医院投诉热线：\n" +
                    "📞 投诉热线：0755-86913333\n" +
                    "📞 服务监督：0755-86913366\n\n" +
                    "工作时间：周一至周五 8:00-17:30");
        }

        if (content.startsWith("地址") || content.startsWith("在哪")) {
            return createTextMessage(wxMessage,
                    "🏥 港大医院（深圳）\n\n" +
                    "📍 地址：广东省深圳市福田区海园一路1号\n\n" +
                    "🚇 地铁：1号线（罗宝线）侨城东站\n\n" +
                    "🚌 公交：45路、48路等");
        }

        if (content.equals("帮助") || content.equals("help")) {
            return createTextMessage(wxMessage, getHelpMessage());
        }

        if (content.equals("人工") || content.equals("客服")) {
            return createTextMessage(wxMessage,
                    "您好，人工客服在线时间：\n" +
                    "周一至周五 8:00-17:30\n\n" +
                    "请描述您的问题，我们会尽快回复。\n" +
                    "如有紧急情况，请拨打热线：0755-86913333");
        }

        return createTextMessage(wxMessage,
                "感谢您的留言！\n\n" +
                "您可以：\n" +
                "• 回复【投诉】提交投诉\n" +
                "• 回复【调查】参与满意度调查\n" +
                "• 回复【电话】查看联系方式\n" +
                "• 回复【地址】查看医院地址\n" +
                "• 回复【帮助】查看更多功能\n\n" +
                "或点击下方菜单获取更多服务。");
    }

    private WxMpXmlOutMessage handleImageMessage(WxMpXmlMessage wxMessage, WxMpService wxMpService) {
        log.info("收到图片消息, PicUrl: {}", wxMessage.getPicUrl());
        return createTextMessage(wxMessage,
                "感谢您的图片！\n\n" +
                "如果您想提交图片证据，请通过在线投诉表单上传。\n" +
                "点击菜单【投诉建议】-【在线投诉】即可。");
    }

    private WxMpXmlOutMessage handleVoiceMessage(WxMpXmlMessage wxMessage, WxMpService wxMpService) {
        log.info("收到语音消息, Recognition: {}", wxMessage.getRecognition());
        return createTextMessage(wxMessage,
                "感谢您的语音消息！\n\n" +
                "请通过文字描述您的问题，我们会尽快处理。\n" +
                "如有紧急情况，请拨打热线：0755-86913333");
    }

    private WxMpXmlOutMessage handleVideoMessage(WxMpXmlMessage wxMessage, WxMpService wxMpService) {
        log.info("收到视频消息");
        return createTextMessage(wxMessage,
                "感谢您的视频！\n\n" +
                "如果您想提交视频证据，请通过在线投诉表单上传。\n" +
                "点击菜单【投诉建议】-【在线投诉】即可。");
    }

    private WxMpXmlOutMessage handleShortVideoMessage(WxMpXmlMessage wxMessage, WxMpService wxMpService) {
        return handleVideoMessage(wxMessage, wxMpService);
    }

    private WxMpXmlOutMessage handleLocationMessage(WxMpXmlMessage wxMessage, WxMpService wxMpService) {
        log.info("收到位置消息 - Latitude: {}, Longitude: {}, Label: {}",
                wxMessage.getLatitude(), wxMessage.getLongitude(), wxMessage.getLabel());
        return createTextMessage(wxMessage,
                "感谢您分享位置！\n\n" +
                "港大医院（深圳）位于：\n" +
                "📍 广东省深圳市福田区海园一路1号\n\n" +
                "如需到院，请参考导航路线。");
    }

    private WxMpXmlOutMessage handleLinkMessage(WxMpXmlMessage wxMessage, WxMpService wxMpService) {
        log.info("收到链接消息 - Title: {}, Url: {}", wxMessage.getTitle(), wxMessage.getUrl());
        return createTextMessage(wxMessage,
                "感谢您的链接分享！\n\n" +
                "我们已收到您的信息。\n" +
                "如有需要，请通过在线投诉表单提交具体内容。");
    }

    private WxMpXmlOutMessage handleEventMessage(WxMpXmlMessage wxMessage, WxMpService wxMpService) {
        String event = wxMessage.getEvent();

        log.info("收到事件消息 - Event: {}", event);

        switch (event) {
            case "subscribe":
                return handleSubscribeEvent(wxMessage, wxMpService);
            case "unsubscribe":
                return handleUnsubscribeEvent(wxMessage, wxMpService);
            case "SCAN":
                return handleScanEvent(wxMessage, wxMpService);
            case "LOCATION":
                return handleLocationEvent(wxMessage, wxMpService);
            case "CLICK":
                return handleClickEvent(wxMessage, wxMpService);
            case "VIEW":
                return handleViewEvent(wxMessage, wxMpService);
            default:
                return null;
        }
    }

    private WxMpXmlOutMessage handleSubscribeEvent(WxMpXmlMessage wxMessage, WxMpService wxMpService) {
        log.info("用户关注事件 - OpenId: {}", wxMessage.getFromUser());
        return createTextMessage(wxMessage,
                "🎉 感谢关注港大医院！\n\n" +
                "欢迎使用港大医院投诉建议服务。\n\n" +
                "我们可以为您提供：\n" +
                "• 在线提交投诉建议\n" +
                "• 跟踪投诉处理进度\n" +
                "• 参与满意度调查\n\n" +
                getHelpMessage());
    }

    private WxMpXmlOutMessage handleUnsubscribeEvent(WxMpXmlMessage wxMessage, WxMpService wxMpService) {
        log.info("用户取消关注 - OpenId: {}", wxMessage.getFromUser());
        return null;
    }

    private WxMpXmlOutMessage handleScanEvent(WxMpXmlMessage wxMessage, WxMpService wxMpService) {
        String scene = wxMessage.getEventKey();
        log.info("用户扫码事件 - Scene: {}", scene);

        if (scene != null && scene.startsWith("feedback_")) {
            String feedbackNo = scene.substring("feedback_".length());
            return createTextMessage(wxMessage,
                    "您好！\n\n" +
                    "您扫描的是投诉工单二维码\n" +
                    "工单号：" + feedbackNo + "\n\n" +
                    "点击菜单【我的投诉】查看详情。");
        }

        return createTextMessage(wxMessage,
                "您好！\n\n" +
                "感谢您扫描我们的二维码！\n" +
                "请描述您的问题，我们会尽快处理。");
    }

    private WxMpXmlOutMessage handleLocationEvent(WxMpXmlMessage wxMessage, WxMpService wxMpService) {
        log.debug("自动上报位置 - Latitude: {}, Longitude: {}",
                wxMessage.getLatitude(), wxMessage.getLongitude());
        return null;
    }

    private WxMpXmlOutMessage handleClickEvent(WxMpXmlMessage wxMessage, WxMpService wxMpService) {
        String eventKey = wxMessage.getEventKey();
        log.info("自定义菜单点击事件 - Key: {}", eventKey);

        switch (eventKey) {
            case "V1001_GOOD":
                return createTextMessage(wxMessage, "感谢您的支持！");
            case "V1002_COMPLAINT":
                return createTextMessage(wxMessage,
                        "您好！\n\n" +
                        "点击菜单【投诉建议】-【在线投诉】\n" +
                        "即可提交您的投诉或建议。");
            case "V1003_SURVEY":
                return createTextMessage(wxMessage,
                        "您好！\n\n" +
                        "点击菜单【满意度调查】\n" +
                        "即可对本次就医体验进行评价。\n\n" +
                        "您的反馈将帮助我们做得更好！");
            case "V1004_HOTLINE":
                return createTextMessage(wxMessage,
                        "📞 投诉热线：0755-86913333\n" +
                        "📞 服务监督：0755-86913366\n\n" +
                        "工作时间：周一至周五 8:00-17:30");
            default:
                return null;
        }
    }

    private WxMpXmlOutMessage handleViewEvent(WxMpXmlMessage wxMessage, WxMpService wxMpService) {
        String url = wxMessage.getEventKey();
        log.info("菜单跳转事件 - Url: {}", url);
        return null;
    }

    private WxMpXmlOutMessage handleUnknownMessage(WxMpXmlMessage wxMessage, WxMpService wxMpService) {
        return createTextMessage(wxMessage,
                "感谢您的消息！\n\n" +
                "如有疑问，请拨打热线：0755-86913333\n" +
                "或点击菜单获取更多帮助。");
    }

    private WxMpXmlOutMessage createTextMessage(WxMpXmlMessage wxMessage, String content) {
        return WxMpXmlOutMessage.TEXT()
                .fromUser(wxMessage.getToUser())
                .toUser(wxMessage.getFromUser())
                .content(content)
                .build();
    }

    private String getHelpMessage() {
        return "📋 功能说明：\n\n" +
                "【投诉建议】\n" +
                "• 在线投诉 - 提交您的投诉或建议\n" +
                "• 我的投诉 - 查看投诉处理进度\n" +
                "• 常见问题 - 快速找到答案\n\n" +
                "【满意度调查】\n" +
                "• 参与评价 - 对就医体验评分\n" +
                "• 查看结果 - 了解整体满意度\n\n" +
                "【个人中心】\n" +
                "• 绑定账号 - 绑定手机号\n" +
                "• 关于我们 - 了解更多\n\n" +
                "📞 投诉热线：0755-86913333";
    }
}
