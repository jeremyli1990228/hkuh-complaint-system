package com.hkuh.complaint.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SatisfactionDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String surveyNo;

    private Long feedbackId;

    private String feedbackNo;

    private String title;

    private String description;

    private Date startTime;

    private Date endTime;

    private String status;

    private BigDecimal avgScore;

    private Long responseCount;

    private Long promoters;

    private Long passives;

    private Long detractors;

    private BigDecimal nps;

    private Date createTime;

    private String createBy;

    private List<SatisfactionItemDTO> items;

    private List<CategoryScoreVO> categoryScores;
}
