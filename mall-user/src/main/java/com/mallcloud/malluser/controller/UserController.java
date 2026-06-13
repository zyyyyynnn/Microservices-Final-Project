package com.mallcloud.malluser.controller;

import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.malluser.api.dto.AddressDTO;
import com.mallcloud.malluser.api.dto.UserRegisterDTO;
import com.mallcloud.malluser.api.dto.UserUpdateDTO;
import com.mallcloud.malluser.api.vo.AddressVO;
import com.mallcloud.malluser.api.vo.UserVO;
import com.mallcloud.malluser.service.AddressService;
import com.mallcloud.malluser.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

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
    public Result<UserVO> getInternalUser(@PathVariable("userId") Long userId) {
        return Result.ok(userService.getByUserId(userId));
    }

    /**
     * 服务内部地址查询（订单服务调用）。
     * 网关路由 /api/v1/users/**，不依赖用户登录态（路径参数 + 调用方信任）。
     */
    @GetMapping("/internal/{userId}/addresses/{addressId}")
    public Result<AddressVO> getInternalAddress(@PathVariable("userId") Long userId,
                                                 @PathVariable("addressId") Long addressId) {
        AddressVO vo = addressService.getInternalAddress(userId, addressId);
        if (vo == null) {
            return Result.error(10001, "地址不存在");
        }
        return Result.ok(vo);
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
