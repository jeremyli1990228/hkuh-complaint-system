package com.hkuh.complaint.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandleRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long feedbackId;

    private Long handlerId;

    private String handlerName;

    private String action;

    private String content;

    private String result;

    private Integer isVisible;

    private Date createTime;

    private List<AttachmentVO> attachments;
}
