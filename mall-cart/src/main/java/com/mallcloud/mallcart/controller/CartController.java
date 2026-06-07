package com.mallcloud.mallcart.controller;

import com.mallcloud.mallcart.api.dto.CartItemDTO;
import com.mallcloud.mallcart.api.dto.CartSelectDTO;
import com.mallcloud.mallcart.api.dto.CartUpdateDTO;
import com.mallcloud.mallcart.api.vo.CartVO;
import com.mallcloud.mallcart.service.CartService;
import com.mallcloud.mallcommon.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public Result<Void> addCart(@Validated @RequestBody CartItemDTO dto) {
        cartService.addCart(dto);
        return Result.ok();
    }

    @GetMapping
    public Result<CartVO> getCart() {
        return Result.ok(cartService.getCart());
    }

    @PutMapping("/{skuId}")
    public Result<Void> updateQuantity(@PathVariable("skuId") Long skuId, @Validated @RequestBody CartUpdateDTO dto) {
        cartService.updateQuantity(skuId, dto);
        return Result.ok();
    }

    @PatchMapping("/{skuId}")
    public Result<Void> updateSelect(@PathVariable("skuId") Long skuId, @Validated @RequestBody CartSelectDTO dto) {
        cartService.updateSelect(skuId, dto);
        return Result.ok();
    }

    @DeleteMapping("/{skuId}")
    public Result<Void> deleteCartItem(@PathVariable("skuId") Long skuId) {
        cartService.deleteCartItem(skuId);
        return Result.ok();
    }
}
