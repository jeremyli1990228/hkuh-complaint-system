package com.hkuh.complaint.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;

    private String messageId;

    private String errorMsg;

    private LocalDateTime sendTime;

    private Long costTime;

    public static SendResult success(String messageId) {
        return SendResult.builder()
                .success(true)
                .messageId(messageId)
                .sendTime(LocalDateTime.now())
                .build();
    }

    public static SendResult failure(String errorMsg) {
        return SendResult.builder()
                .success(false)
                .errorMsg(errorMsg)
                .sendTime(LocalDateTime.now())
                .build();
    }

    public static SendResult success(String messageId, long costTime) {
        return SendResult.builder()
                .success(true)
                .messageId(messageId)
                .sendTime(LocalDateTime.now())
                .costTime(costTime)
                .build();
    }
}
