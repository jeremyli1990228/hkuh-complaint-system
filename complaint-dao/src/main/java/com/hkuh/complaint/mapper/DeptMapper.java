package com.hkuh.complaint.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hkuh.complaint.entity.Dept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DeptMapper extends BaseMapper<Dept> {

    Dept selectById(@Param("id") Long id);

    Dept selectByCode(@Param("deptCode") String deptCode);
}
