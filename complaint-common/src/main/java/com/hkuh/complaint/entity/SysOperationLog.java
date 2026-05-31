package com.hkuh.complaint.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_operation_log")
public class SysOperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String module;

    private String operation;

    private String method;

    private String requestUrl;

    private String requestMethod;

    private String requestParams;

    private String responseData;

    private Long operatorId;

    private String operatorName;

    private String operatorIp;

    private Long costTime;

    private Integer status;

    private String errorMsg;

    private String description;

    private LocalDateTime createTime;

    public static final Integer STATUS_SUCCESS = 1;
    public static final Integer STATUS_FAILURE = 0;
}
