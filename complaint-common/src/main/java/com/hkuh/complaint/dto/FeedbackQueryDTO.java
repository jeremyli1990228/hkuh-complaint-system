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
public class FeedbackQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer pageNum;

    private Integer pageSize;

    private String feedbackNo;

    private String title;

    private Long feedbackTypeId;

    private String source;

    private String priority;

    private String status;

    private Long deptId;

    private Long handlerId;

    private String startDate;

    private String endDate;

    private String keyword;
}
