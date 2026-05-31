package com.hkuh.complaint.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SatisfactionCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long feedbackId;

    private String title;

    private String description;

    private Date startTime;

    private Date endTime;
}
