package com.mallcloud.malluser.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mallcloud.malluser.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
