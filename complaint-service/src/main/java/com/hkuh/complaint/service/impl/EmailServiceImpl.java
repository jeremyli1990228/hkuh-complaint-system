package com.hkuh.complaint.service.impl;

import com.hkuh.complaint.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.util.List;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username:noreply@hkuh.edu.cn}")
    private String fromEmail;

    @Value("${mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${app.base-url:https://admin.hkuh.hk}")
    private String baseUrl;

    @Override
    public boolean sendEmail(String to, String subject, String content, boolean isHtml) {
        if (!mailEnabled) {
            log.info("邮件发送已禁用 - to: {}, subject: {}", to, subject);
            return true;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, isHtml);

            mailSender.send(message);

            recordEmailSend(fromEmail, to, subject, true, null);
            log.info("邮件发送成功 - to: {}, subject: {}", to, subject);
            return true;

        } catch (Exception e) {
            log.error("邮件发送失败 - to: {}, subject: {}, error: {}", to, subject, e.getMessage());
            recordEmailSend(fromEmail, to, subject, false, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendEmailWithCc(String to, String cc, String subject, String content, boolean isHtml) {
        if (!mailEnabled) {
            log.info("邮件发送已禁用 - to: {}, cc: {}, subject: {}", to, cc, subject);
            return true;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            if (cc != null && !cc.isEmpty()) {
                helper.setCc(cc.split(","));
            }
            helper.setSubject(subject);
            helper.setText(content, isHtml);

            mailSender.send(message);

            log.info("邮件发送成功 - to: {}, cc: {}, subject: {}", to, cc, subject);
            return true;

        } catch (Exception e) {
            log.error("邮件发送失败 - to: {}, cc: {}, subject: {}, error: {}", to, cc, subject, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendEmailWithAttachment(String to, String subject, String content, boolean isHtml, List<File> attachments) {
        if (!mailEnabled) {
            log.info("邮件发送已禁用 - to: {}, subject: {}", to, subject);
            return true;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, isHtml);

            if (attachments != null && !attachments.isEmpty()) {
                for (File file : attachments) {
                    if (file.exists()) {
                        FileSystemResource fileSystemResource = new FileSystemResource(file);
                        helper.addAttachment(file.getName(), fileSystemResource);
                    }
                }
            }

            mailSender.send(message);

            log.info("带附件邮件发送成功 - to: {}, subject: {}, attachments: {}", to, subject, attachments.size());
            return true;

        } catch (Exception e) {
            log.error("带附件邮件发送失败 - to: {}, subject: {}, error: {}", to, subject, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendEmailToMultiple(String[] tos, String subject, String content, boolean isHtml) {
        if (!mailEnabled) {
            log.info("邮件发送已禁用 - tos: {}, subject: {}", tos.length, subject);
            return true;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(tos);
            helper.setSubject(subject);
            helper.setText(content, isHtml);

            mailSender.send(message);

            log.info("群发邮件发送成功 - tos: {}, subject: {}", tos.length, subject);
            return true;

        } catch (Exception e) {
            log.error("群发邮件发送失败 - tos: {}, subject: {}, error: {}", tos.length, subject, e.getMessage());
            return false;
        }
    }

    @Override
    public void sendSlaUrgingNotice(String to, String feedbackNo, String title, String handlerName, String deadline) {
        try {
            Context context = new Context();
            context.setVariable("feedbackNo", feedbackNo);
            context.setVariable("title", title);
            context.setVariable("handlerName", handlerName);
            context.setVariable("deadline", deadline);
            context.setVariable("detailUrl", baseUrl + "/feedback/detail/" + feedbackNo);

            String content = templateEngine.process("email/sla-warning", context);

            String subject = "【SLA预警】投诉工单即将超期提醒 - " + feedbackNo;

            sendEmail(to, subject, content, true);

            log.info("SLA催促提醒邮件已发送 - to: {}, feedbackNo: {}", to, feedbackNo);

        } catch (Exception e) {
            log.error("发送SLA催促提醒邮件失败 - to: {}, feedbackNo: {}, error: {}", to, feedbackNo, e.getMessage());
        }
    }

    @Override
    public void sendOverdueNotice(String to, String feedbackNo, String title, String handlerName, String overdueDays) {
        try {
            Context context = new Context();
            context.setVariable("feedbackNo", feedbackNo);
            context.setVariable("title", title);
            context.setVariable("handlerName", handlerName);
            context.setVariable("overdueDays", overdueDays);
            context.setVariable("detailUrl", baseUrl + "/feedback/detail/" + feedbackNo);

            String content = templateEngine.process("email/overdue-notice", context);

            String subject = "【超期通知】投诉工单已超过SLA时限 - " + feedbackNo;

            sendEmail(to, subject, content, true);

            log.info("超期通知邮件已发送 - to: {}, feedbackNo: {}", to, feedbackNo);

        } catch (Exception e) {
            log.error("发送超期通知邮件失败 - to: {}, feedbackNo: {}, error: {}", to, feedbackNo, e.getMessage());
        }
    }

    @Override
    public void sendDailyReport(String to, String date, Object reportData) {
        try {
            Context context = new Context();
            context.setVariable("reportDate", date);
            context.setVariable("reportData", reportData);
            context.setVariable("detailUrl", baseUrl + "/report/daily");

            String content = templateEngine.process("email/daily-report", context);

            String subject = "【日报】投诉处理情况汇总 - " + date;

            sendEmail(to, subject, content, true);

            log.info("每日投诉处理报告已发送 - to: {}, date: {}", to, date);

        } catch (Exception e) {
            log.error("发送每日报告邮件失败 - to: {}, date: {}, error: {}", to, date, e.getMessage());
        }
    }

    @Override
    public void sendWeeklyReport(String to, String weekStart, String weekEnd, Object reportData) {
        try {
            Context context = new Context();
            context.setVariable("weekStart", weekStart);
            context.setVariable("weekEnd", weekEnd);
            context.setVariable("reportData", reportData);
            context.setVariable("detailUrl", baseUrl + "/report/weekly");

            String content = templateEngine.process("email/weekly-report", context);

            String subject = "【周报】投诉处理情况汇总 - " + weekStart + " 至 " + weekEnd;

            sendEmail(to, subject, content, true);

            log.info("每周投诉处理报告已发送 - to: {}, week: {} - {}", to, weekStart, weekEnd);

        } catch (Exception e) {
            log.error("发送每周报告邮件失败 - to: {}, week: {} - {}, error: {}", to, weekStart, weekEnd, e.getMessage());
        }
    }

    @Override
    public void recordEmailSend(String from, String to, String subject, boolean success, String errorMsg) {
        if (success) {
            log.info("邮件发送记录 - from: {}, to: {}, subject: {}, 成功", from, to, subject);
        } else {
            log.error("邮件发送失败记录 - from: {}, to: {}, subject: {}, 错误: {}", from, to, subject, errorMsg);
        }
    }
}
