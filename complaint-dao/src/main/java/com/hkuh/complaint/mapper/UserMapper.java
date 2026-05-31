package com.hkuh.complaint.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hkuh.complaint.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    User selectById(@Param("id") Long id);

    User selectByUsername(@Param("username") String username);
}
