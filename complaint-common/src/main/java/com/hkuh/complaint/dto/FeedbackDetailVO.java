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
public class FeedbackDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String feedbackNo;

    private String title;

    private String content;

    private Long feedbackTypeId;

    private String feedbackTypeName;

    private String source;

    private String priority;

    private String status;

    private Long deptId;

    private String deptName;

    private Long handlerId;

    private String handlerName;

    private Date slaDeadline;

    private Date slaWarningTime;

    private Integer isAnonymous;

    private String contactPhone;

    private String contactEmail;

    private Integer rating;

    private String ratingComment;

    private Date createTime;

    private String createBy;

    private Date updateTime;

    private String remark;

    private ComplainantDTO complainant;

    private List<HandleRecordVO> handleRecords;

    private List<AttachmentVO> attachments;
}
