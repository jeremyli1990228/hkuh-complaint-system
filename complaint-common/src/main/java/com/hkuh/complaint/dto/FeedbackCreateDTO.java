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
public class FeedbackCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String title;

    private String content;

    private Long feedbackTypeId;

    private String source;

    private String priority;

    private ComplainantDTO complainant;

    private Boolean isAnonymous;

    private String contactPhone;

    private String contactEmail;

    private List<Long> attachmentIds;
}
