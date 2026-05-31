package com.hkuh.complaint.vo;

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
public class WeeklyReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String weekStart;

    private String weekEnd;

    private Integer totalComplaints;

    private Integer processedComplaints;

    private Double processingRate;

    private Integer avgDailyComplaints;

    private Integer avgDailyProcessed;

    private Double slaComplianceRate;

    private Double avgProcessingTime;

    private Integer totalOverdue;

    private List<DailyTrend> dailyTrends;

    private List<DeptRanking> deptRankings;

    private List<TypeDistribution> typeDistributions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTrend implements Serializable {
        private String date;
        private Integer newCount;
        private Integer processedCount;
        private Integer pendingCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeptRanking implements Serializable {
        private Integer rank;
        private String deptName;
        private Integer processedCount;
        private Double slaRate;
        private Integer avgHours;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeDistribution implements Serializable {
        private String typeName;
        private Integer count;
        private Double percentage;
        private Double satisfactionRate;
    }
}
