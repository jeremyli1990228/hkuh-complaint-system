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
public class FeedbackRateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer rating;

    private String comment;
}
