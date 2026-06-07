package com.mallcloud.mallauth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mallcloud.mallauth.api.dto.LoginDTO;
import com.mallcloud.mallauth.api.dto.RefreshTokenDTO;
import com.mallcloud.mallauth.api.dto.UserInternalDTO;
import com.mallcloud.mallauth.api.vo.LoginVO;
import com.mallcloud.mallauth.api.vo.UserInfoVO;
import com.mallcloud.mallauth.client.UserClient;
import com.mallcloud.mallauth.config.JwtUtil;
import com.mallcloud.mallauth.domain.SysUserAuth;
import com.mallcloud.mallauth.mapper.SysUserAuthMapper;
import com.mallcloud.mallauth.service.AuthService;
import com.mallcloud.mallcommon.constant.CommonConstants;
import com.mallcloud.mallcommon.enums.ErrorCode;
import com.mallcloud.mallcommon.exception.BizException;
import com.mallcloud.mallcommon.response.Result;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserAuthMapper sysUserAuthMapper;
    private final UserClient userClient;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        String loginType = loginDTO.getLoginType();
        if (loginType == null || loginType.isBlank()) {
            loginType = "PASSWORD";
        }

        // 查询用户认证信息
        SysUserAuth auth = sysUserAuthMapper.selectOne(new LambdaQueryWrapper<SysUserAuth>()
                .eq(SysUserAuth::getIdentifier, loginDTO.getUsername())
                .eq(SysUserAuth::getIdentityType, loginType));
        
        if (auth == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
        }
        
        if (auth.getStatus() == 0) {
            throw new BizException(ErrorCode.UNAUTHORIZED.getCode(), "账号已被禁用");
        }
        
        // 校验密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), auth.getCredential())) {
            throw new BizException(ErrorCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
        }
        
        return buildLoginVO(auth);
    }

    @Override
    public LoginVO refresh(RefreshTokenDTO refreshDTO) {
        try {
            Claims claims = jwtUtil.parseToken(refreshDTO.getRefreshToken());
            String tokenType = claims.get(CommonConstants.JWT_CLAIM_TOKEN_TYPE, String.class);
            if (!CommonConstants.JWT_TOKEN_TYPE_REFRESH.equals(tokenType)) {
                throw new BizException(
                        ErrorCode.TOKEN_INVALID.getCode(),
                        "Token 类型错误"
                );
            }
            String jti = claims.getId();
            
            // 检查是否在黑名单中
            Boolean isBlacklisted = stringRedisTemplate.hasKey(CommonConstants.JWT_BLACKLIST_PREFIX + jti);
            if (Boolean.TRUE.equals(isBlacklisted)) {
                throw new BizException(ErrorCode.UNAUTHORIZED.getCode(), "无效的 Refresh Token");
            }
            
            Long userId = claims.get("uid", Long.class);
            
            // 查询用户信息
            SysUserAuth auth = sysUserAuthMapper.selectOne(new LambdaQueryWrapper<SysUserAuth>()
                    .eq(SysUserAuth::getUserId, userId)
                    .last("LIMIT 1"));
            if (auth == null || auth.getStatus() == 0) {
                throw new BizException(ErrorCode.UNAUTHORIZED.getCode(), "用户状态异常");
            }
            
            // 旧 Refresh Token 加入黑名单
            long expireTime = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (expireTime > 0) {
                stringRedisTemplate.opsForValue().set(CommonConstants.JWT_BLACKLIST_PREFIX + jti, "1", expireTime, TimeUnit.MILLISECONDS);
            }
            
            return buildLoginVO(auth);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("解析 Refresh Token 失败", e);
            throw new BizException(ErrorCode.UNAUTHORIZED.getCode(), "无效或过期的 Refresh Token");
        }
    }

    @Override
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                Claims claims = jwtUtil.parseToken(token);
                String tokenType = claims.get(CommonConstants.JWT_CLAIM_TOKEN_TYPE, String.class);
                if (!CommonConstants.JWT_TOKEN_TYPE_ACCESS.equals(tokenType)) {
                    return;
                }
                String jti = claims.getId();
                long expireTime = claims.getExpiration().getTime() - System.currentTimeMillis();
                if (expireTime > 0) {
                    stringRedisTemplate.opsForValue().set(CommonConstants.JWT_BLACKLIST_PREFIX + jti, "1", expireTime, TimeUnit.MILLISECONDS);
                }
            } catch (Exception e) {
                log.warn("登出时解析 Token 失败或 Token 已过期", e);
            }
        }
    }

    private LoginVO buildLoginVO(SysUserAuth auth) {
        Long userId = auth.getUserId();
        
        // 读取角色
        String role = auth.getRole();
        if (!"USER".equals(role)
                && !"MERCHANT".equals(role)
                && !"ADMIN".equals(role)) {
            throw new BizException(
                    ErrorCode.FORBIDDEN.getCode(),
                    "用户角色无效"
            );
        }
        List<String> roles = Collections.singletonList(role);
        
        // 调用 mall-user 获取用户信息
        Result<UserInternalDTO> userResult = userClient.getUserById(userId);
        if (userResult == null
                || !userResult.isSuccess()
                || userResult.getData() == null) {
            log.warn("调用 mall-user 获取用户信息失败, userId: {}", userId);
            throw new BizException(
                    ErrorCode.REMOTE_CALL_ERROR.getCode(),
                    ErrorCode.REMOTE_CALL_ERROR.getMessage()
            );
        }
        UserInternalDTO userDto = userResult.getData();
        
        UserInfoVO userInfoVO = UserInfoVO.builder()
                .id(userId)
                .nickname(userDto.getNickname())
                .avatar(userDto.getAvatar())
                .roles(roles)
                .build();
                
        String accessToken = jwtUtil.generateAccessToken(userId, roles);
        String refreshToken = jwtUtil.generateRefreshToken(userId);
        
        return LoginVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(accessTokenExpiration)
                .userInfo(userInfoVO)
                .build();
    }
}
