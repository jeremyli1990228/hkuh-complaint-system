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
public class FeedbackHandleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String action;

    private String content;

    private String result;

    private Long transferTo;

    private Long transferDept;

    private List<Long> attachmentIds;

    private Boolean isVisible;
}
