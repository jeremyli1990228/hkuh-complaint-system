package com.hkuh.complaint.constant;

public enum DesensitizeType {

    NAME("姓名"),
    PHONE("手机号"),
    ID_CARD("身份证"),
    EMAIL("邮箱"),
    ADDRESS("地址"),
    BANK_CARD("银行卡"),
    CUSTOM("自定义");

    private final String description;

    DesensitizeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
