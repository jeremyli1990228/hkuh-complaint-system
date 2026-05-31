package com.hkuh.complaint.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String username;

    private String realName;

    private String deptName;

    private List<String> roles;

    private List<String> permissions;

    private String avatar;

    private String phone;

    private String email;
}
