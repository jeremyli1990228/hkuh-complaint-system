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
public class SatisfactionSubmitDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long surveyId;

    private Long feedbackId;

    private String respondentName;

    private String respondentPhone;

    private String respondentDept;

    private BigDecimal overallScore;

    private List<SatisfactionItemDTO> items;
}
