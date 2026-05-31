package com.hkuh.complaint.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hkuh.complaint.entity.Complainant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ComplainantMapper extends BaseMapper<Complainant> {

    Complainant selectByPhone(@Param("phone") String phone);

    Complainant selectByIdCard(@Param("idCard") String idCard);

    int insertComplainant(Complainant complainant);

    int updateComplainant(Complainant complainant);
}
