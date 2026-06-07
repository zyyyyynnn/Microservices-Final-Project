package com.mallcloud.mallauth.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 认证信息实体类
 */
@Data
@TableName("sys_user_auth")
public class SysUserAuth {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String identityType;
    
    private String identifier;
    
    private String credential;
    
    private Integer status;
}
