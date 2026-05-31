package com.hkuh.complaint.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplainantDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private String phone;

    private String email;

    private String gender;

    private Integer age;

    private String address;

    private String patientId;

    private String idCard;

    private String occupation;
}
