package com.hkuh.complaint.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryScoreVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String category;

    private String categoryName;

    private BigDecimal avgScore;

    private BigDecimal satisfactionRate;

    private Long responseCount;

    private java.util.List<TrendItem> trend;
}
