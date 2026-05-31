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
public class FeedbackAssignDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long handlerId;

    private Long deptId;

    private String remark;
}
