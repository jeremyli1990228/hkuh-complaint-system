package com.hkuh.complaint.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("T_HANDLE_RECORD")
public class HandleRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @TableField("FEEDBACK_ID")
    private Long feedbackId;

    @TableField("HANDLER_ID")
    private Long handlerId;

    @TableField("ACTION")
    private String action;

    @TableField("CONTENT")
    private String content;

    @TableField("ATTACHMENT_IDS")
    private String attachmentIds;

    @TableField("RESULT")
    private String result;

    @TableField("IS_VISIBLE")
    private Integer isVisible;

    @TableField("CREATE_TIME")
    private Date createTime;

    @TableField(exist = false)
    private String handlerName;
}
