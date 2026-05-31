package com.hkuh.complaint.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sms_send_record")
public class SmsSendRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;

    private String templateCode;

    private String templateParams;

    private String messageId;

    private Integer status;

    private String errorMsg;

    private Long costTime;

    private String ipAddress;

    private String userAgent;

    private LocalDateTime createTime;

    public static final Integer STATUS_SUCCESS = 1;
    public static final Integer STATUS_FAILURE = 0;

    public static final String TEMPLATE_COMPLAINT_ACCEPT = "complaint-accept";
    public static final String TEMPLATE_COMPLAINT_PROGRESS = "complaint-progress";
    public static final String TEMPLATE_COMPLAINT_COMPLETE = "complaint-complete";
    public static final String TEMPLATE_SATISFACTION_INVITE = "satisfaction-invite";
    public static final String TEMPLATE_VERIFICATION_CODE = "verification-code";
}
