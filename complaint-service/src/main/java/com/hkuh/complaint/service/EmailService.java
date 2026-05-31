package com.hkuh.complaint.service;

import java.io.File;
import java.util.List;

public interface EmailService {

    boolean sendEmail(String to, String subject, String content, boolean isHtml);

    boolean sendEmailWithCc(String to, String cc, String subject, String content, boolean isHtml);

    boolean sendEmailWithAttachment(String to, String subject, String content, boolean isHtml, List<File> attachments);

    boolean sendEmailToMultiple(String[] tos, String subject, String content, boolean isHtml);

    void sendSlaUrgingNotice(String to, String feedbackNo, String title, String handlerName, String deadline);

    void sendOverdueNotice(String to, String feedbackNo, String title, String handlerName, String overdueDays);

    void sendDailyReport(String to, String date, Object reportData);

    void sendWeeklyReport(String to, String weekStart, String weekEnd, Object reportData);

    void recordEmailSend(String from, String to, String subject, boolean success, String errorMsg);
}
