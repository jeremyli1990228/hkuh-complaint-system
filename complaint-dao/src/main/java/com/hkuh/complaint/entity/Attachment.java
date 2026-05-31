package com.hkuh.complaint.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("T_ATTACHMENT")
public class Attachment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @TableField("BUSINESS_ID")
    private Long businessId;

    @TableField("BUSINESS_TYPE")
    private String businessType;

    @TableField("FILE_NAME")
    private String fileName;

    @TableField("FILE_PATH")
    private String filePath;

    @TableField("FILE_SIZE")
    private Long fileSize;

    @TableField("FILE_TYPE")
    private String fileType;

    @TableField("CREATE_BY")
    private String createBy;

    @TableField("CREATE_TIME")
    private Date createTime;

    @TableField("DEL_FLAG")
    private Integer delFlag;
}
