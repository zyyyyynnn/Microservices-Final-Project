package com.mallcloud.malluser.controller;

import com.mallcloud.mallcommon.constant.CommonConstants;
import com.mallcloud.mallcommon.exception.BizException;
import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.malluser.api.dto.AddressDTO;
import com.mallcloud.malluser.api.dto.UserRegisterDTO;
import com.mallcloud.malluser.api.dto.UserUpdateDTO;
import com.mallcloud.malluser.api.vo.AddressVO;
import com.mallcloud.malluser.api.vo.UserVO;
import com.mallcloud.malluser.service.AddressService;
import com.mallcloud.malluser.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    /**
     * 服务间共享 token。dev 默认值仅用于本地开发；生产必须通过 Nacos / 环境变量覆盖。
     * 与 mall-common 的 InternalAuthProperties 配对使用：调用方 Feign 拦截器注入，被调方 controller 校验。
     */
    @Value("${mall.internal.token:dev-internal-token}")
    private String internalToken;

    private final UserService userService;
    private final AddressService addressService;

    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.ok("mall-user pong");
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody UserRegisterDTO dto) {
        userService.register(dto);
        return Result.ok();
    }

    @GetMapping("/me")
    public Result<UserVO> getCurrentUser() {
        return Result.ok(userService.getCurrentUser());
    }

    @GetMapping("/internal/{userId}")
    public Result<UserVO> getInternalUser(@PathVariable("userId") Long userId,
                                          @RequestHeader(value = CommonConstants.HEADER_INTERNAL_TOKEN, required = false) String token) {
        assertInternalToken(token);
        return Result.ok(userService.getByUserId(userId));
    }

    /**
     * 服务内部地址查询（订单服务调用）。
     * 双层防护：
     *  1) Gateway 阻断外部 /api/v1/users/internal/** 直接访问（InternalPathBlockFilter）；
     *  2) 本 controller 校验 X-Internal-Token header 匹配 mall.internal.token 配置。
     * DB 查询仍用 userId + addressId 双键过滤，避免越权。
     */
    @GetMapping("/internal/{userId}/addresses/{addressId}")
    public Result<AddressVO> getInternalAddress(@PathVariable("userId") Long userId,
                                                 @PathVariable("addressId") Long addressId,
                                                 @RequestHeader(value = CommonConstants.HEADER_INTERNAL_TOKEN, required = false) String token) {
        assertInternalToken(token);
        AddressVO vo = addressService.getInternalAddress(userId, addressId);
        if (vo == null) {
            return Result.error(10001, "地址不存在");
        }
        return Result.ok(vo);
    }

    private void assertInternalToken(String token) {
        if (!StringUtils.hasText(token) || !internalToken.equals(token)) {
            // 抛 BizException，让 GlobalExceptionHandler 统一返回统一响应结构；
            // 该路径只服务内部调用，所以不会污染普通用户错误码。
            throw new BizException(401, "服务间鉴权失败：X-Internal-Token 缺失或不匹配");
        }
    }

    @PutMapping("/me")
    public Result<Void> updateCurrentUser(@RequestBody UserUpdateDTO dto) {
        userService.updateCurrentUser(dto);
        return Result.ok();
    }

    @GetMapping("/me/addresses")
    public Result<List<AddressVO>> listAddresses() {
        return Result.ok(addressService.listCurrentUserAddresses());
    }

    @PostMapping("/me/addresses")
    public Result<Void> addAddress(@RequestBody AddressDTO dto) {
        addressService.addAddress(dto);
        return Result.ok();
    }

    @PutMapping("/me/addresses/{id}")
    public Result<Void> updateAddress(@PathVariable("id") Long id, @RequestBody AddressDTO dto) {
        addressService.updateAddress(id, dto);
        return Result.ok();
    }

    @DeleteMapping("/me/addresses/{id}")
    public Result<Void> deleteAddress(@PathVariable("id") Long id) {
        addressService.deleteAddress(id);
        return Result.ok();
    }
}

