package com.hkuh.complaint.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hkuh.complaint.entity.HandleRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HandleRecordMapper extends BaseMapper<HandleRecord> {

    List<HandleRecord> selectByFeedbackId(@Param("feedbackId") Long feedbackId);

    int insertHandleRecord(HandleRecord handleRecord);

    int updateHandleRecord(HandleRecord handleRecord);
}
