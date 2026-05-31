package com.hkuh.complaint.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WechatOAuthVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String openId;

    private String unionId;

    private String nickname;

    private String avatar;

    private String sessionToken;

    private String accessToken;

    private Long expiresIn;

    private Integer subscribe;

    private Integer subscribeTime;
}
