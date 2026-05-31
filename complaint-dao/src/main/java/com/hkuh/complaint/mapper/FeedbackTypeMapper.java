package com.hkuh.complaint.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hkuh.complaint.entity.FeedbackType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FeedbackTypeMapper extends BaseMapper<FeedbackType> {

    FeedbackType selectById(@Param("id") Long id);

    FeedbackType selectByCode(@Param("typeCode") String typeCode);
}
