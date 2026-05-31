package com.hkuh.complaint.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("T_FEEDBACK_TYPE")
public class FeedbackType implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @TableField("TYPE_NAME")
    private String typeName;

    @TableField("TYPE_CODE")
    private String typeCode;

    @TableField("SLA_DAYS")
    private Integer slaDays;

    @TableField("SLA_HOURS")
    private Integer slaHours;

    @TableField("DESCRIPTION")
    private String description;

    @TableField("SORT")
    private Integer sort;

    @TableField("STATUS")
    private String status;

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
}
