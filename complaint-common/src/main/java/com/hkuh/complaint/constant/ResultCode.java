package com.hkuh.complaint.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),

    PARAM_ERROR(400, "参数错误"),

    UNAUTHORIZED(401, "未授权"),

    FORBIDDEN(403, "禁止访问"),

    NOT_FOUND(404, "资源未找到"),

    SERVER_ERROR(500, "服务器错误"),

    TOKEN_EXPIRED(4011, "Token已过期"),

    TOKEN_INVALID(4012, "Token无效"),

    ACCOUNT_LOCKED(4023, "账户已被锁定"),

    ACCOUNT_DISABLED(4024, "账户已被禁用"),

    SLA_TIMEOUT(5001, "SLA处理超时"),

    DATA_NOT_FOUND(4041, "数据不存在"),

    DATA_DUPLICATE(4001, "数据重复"),

    PERMISSION_DENIED(4031, "权限不足"),

    OPERATION_FAILED(5001, "操作失败"),

    VALIDATION_FAILED(4002, "数据校验失败"),

    FEEDBACK_NOT_FOUND(4042, "反馈记录不存在"),

    USER_NOT_FOUND(4043, "用户不存在"),

    ROLE_NOT_FOUND(4044, "角色不存在"),

    DEPT_NOT_FOUND(4045, "科室不存在"),

    UPLOAD_FAILED(5002, "文件上传失败"),

    DOWNLOAD_FAILED(5003, "文件下载失败");

    private final int code;
    private final String message;
}
