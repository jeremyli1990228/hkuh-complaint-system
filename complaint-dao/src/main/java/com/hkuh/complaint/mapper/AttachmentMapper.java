package com.hkuh.complaint.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hkuh.complaint.entity.Attachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AttachmentMapper extends BaseMapper<Attachment> {

    List<Attachment> selectByBusinessId(@Param("businessId") Long businessId,
                                        @Param("businessType") String businessType);

    int updateBusinessId(@Param("ids") List<Long> ids,
                         @Param("businessId") Long businessId,
                         @Param("businessType") String businessType);

    int insertAttachment(Attachment attachment);
}
