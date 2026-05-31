package com.hkuh.complaint.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long total;

    private Long pending;

    private Long processing;

    private Long resolved;

    private Long closed;

    private Map<String, Long> byStatus;

    private Map<String, Long> byType;

    private Map<String, Long> bySource;

    private Map<String, Long> byPriority;

    private Double avgHandleTime;

    private Double slaComplianceRate;

    private List<TrendItem> trend;
}
