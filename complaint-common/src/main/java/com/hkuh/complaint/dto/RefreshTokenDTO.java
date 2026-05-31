package com.hkuh.complaint.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RefreshTokenDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String refreshToken;
}
