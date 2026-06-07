package com.mallcloud.mallauth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mallcloud.mallauth.domain.SysUserAuth;
import org.apache.ibatis.annotations.Mapper;

/**
 * 认证信息 Mapper
 */
@Mapper
public interface SysUserAuthMapper extends BaseMapper<SysUserAuth> {
}
