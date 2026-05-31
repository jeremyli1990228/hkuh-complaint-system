package com.hkuh.complaint.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("T_COMPLAINANT")
public class Complainant implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @TableField("NAME")
    private String name;

    @TableField("ID_CARD")
    private String idCard;

    @TableField("PHONE")
    private String phone;

    @TableField("EMAIL")
    private String email;

    @TableField("GENDER")
    private String gender;

    @TableField("AGE")
    private Integer age;

    @TableField("ADDRESS")
    private String address;

    @TableField("OCCUPATION")
    private String occupation;

    @TableField("PATIENT_ID")
    private String patientId;

    @TableField("CREATE_TIME")
    private Date createTime;

    @TableField("UPDATE_TIME")
    private Date updateTime;

    @TableField("DEL_FLAG")
    private Integer delFlag;
}
