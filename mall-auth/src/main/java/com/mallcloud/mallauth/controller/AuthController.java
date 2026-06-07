package com.mallcloud.mallauth.controller;

import com.mallcloud.mallauth.api.dto.LoginDTO;
import com.mallcloud.mallauth.api.dto.RefreshTokenDTO;
import com.mallcloud.mallauth.api.vo.LoginVO;
import com.mallcloud.mallauth.service.AuthService;
import com.mallcloud.mallcommon.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证服务接口
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        return Result.ok(authService.login(loginDTO));
    }

    @PostMapping("/refresh")
    public Result<LoginVO> refresh(@RequestBody RefreshTokenDTO refreshDTO) {
        return Result.ok(authService.refresh(refreshDTO));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        authService.logout(token);
        return Result.ok();
    }
}
