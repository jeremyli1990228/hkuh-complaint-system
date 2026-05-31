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
public class DailyReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String reportDate;

    private Integer newComplaints;

    private Integer processedComplaints;

    private Integer pendingComplaints;

    private Integer overdueComplaints;

    private Double slaComplianceRate;

    private Double averageProcessingTime;

    private List<DeptStatistics> deptStats;

    private List<TopComplaintType> topTypes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeptStatistics implements Serializable {
        private String deptName;
        private Integer newCount;
        private Integer processedCount;
        private Integer pendingCount;
        private Integer overdueCount;
        private Double slaRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopComplaintType implements Serializable {
        private String typeName;
        private Integer count;
        private Double percentage;
    }
}
