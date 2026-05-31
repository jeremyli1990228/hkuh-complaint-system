package com.hkuh.complaint.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("SYS_USER")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @TableField("USERNAME")
    private String username;

    @TableField("REAL_NAME")
    private String realName;

    @TableField("PASSWORD")
    private String password;

    @TableField("DEPT_ID")
    private Long deptId;

    @TableField("PHONE")
    private String phone;

    @TableField("EMAIL")
    private String email;

    @TableField("AVATAR")
    private String avatar;

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

    @TableField(exist = false)
    private String deptName;
}
