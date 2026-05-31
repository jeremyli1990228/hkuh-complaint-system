package com.hkuh.complaint.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@TableName("T_FEEDBACK")
public class Feedback implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @TableField("FEEDBACK_NO")
    private String feedbackNo;

    @TableField("TITLE")
    private String title;

    @TableField("CONTENT")
    private String content;

    @TableField("FEEDBACK_TYPE_ID")
    private Long feedbackTypeId;

    @TableField("COMPLAINANT_ID")
    private Long complainantId;

    @TableField("SOURCE")
    private String source;

    @TableField("PRIORITY")
    private String priority;

    @TableField("STATUS")
    private String status;

    @TableField("DEPT_ID")
    private Long deptId;

    @TableField("HANDLER_ID")
    private Long handlerId;

    @TableField("SLA_DEADLINE")
    private Date slaDeadline;

    @TableField("SLA_WARNING_TIME")
    private Date slaWarningTime;

    @TableField("IS_ANONYMOUS")
    private Integer isAnonymous;

    @TableField("CONTACT_PHONE")
    private String contactPhone;

    @TableField("CONTACT_EMAIL")
    private String contactEmail;

    @TableField("RATING")
    private Integer rating;

    @TableField("RATING_COMMENT")
    private String ratingComment;

    @TableField("CREATE_BY")
    private String createBy;

    @TableField("CREATE_TIME")
    private Date createTime;

    @TableField("UPDATE_BY")
    private String updateBy;

    @TableField("UPDATE_TIME")
    private Date updateTime;

    @TableField("DEL_FLAG")
    private Integer delFlag;

    @TableField("REMARK")
    private String remark;

    @TableField(exist = false)
    private String feedbackTypeName;

    @TableField(exist = false)
    private String complainantName;

    @TableField(exist = false)
    private String deptName;

    @TableField(exist = false)
    private String handlerName;

    @TableField(exist = false)
    private List<HandleRecord> handleRecords;

    @TableField(exist = false)
    private List<Attachment> attachments;
}
