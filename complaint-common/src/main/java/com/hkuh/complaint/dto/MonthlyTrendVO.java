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
public class MonthlyTrendVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String month;

    private BigDecimal avgScore;

    private Long responseCount;

    private BigDecimal satisfactionRate;

    private BigDecimal nps;
}
