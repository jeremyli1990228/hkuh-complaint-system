package com.hkuh.complaint.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("SYS_DEPT")
public class Dept implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @TableField("DEPT_NAME")
    private String deptName;

    @TableField("DEPT_CODE")
    private String deptCode;

    @TableField("PARENT_ID")
    private Long parentId;

    @TableField("MANAGER_ID")
    private Long managerId;

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
