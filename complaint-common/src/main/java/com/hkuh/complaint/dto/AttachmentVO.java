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
public class AttachmentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String fileName;

    private String filePath;

    private Long fileSize;

    private String fileType;

    private Date createTime;
}
