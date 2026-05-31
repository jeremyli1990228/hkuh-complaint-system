package com.hkuh.complaint.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SatisfactionStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long totalResponses;

    private BigDecimal overallAvgScore;

    private BigDecimal overallSatisfactionRate;

    private BigDecimal nps;

    private List<CategoryScoreVO> categoryScores;

    private Map<Integer, Long> scoreDistribution;

    private List<DeptScoreVO> deptRanking;

    private List<MonthlyTrendVO> monthlyTrend;

    private Long promoters;

    private Long passives;

    private Long detractors;
}
