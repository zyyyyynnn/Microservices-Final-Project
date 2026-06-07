package com.mallcloud.mallcart.service;

import com.mallcloud.mallcart.api.dto.CartItemDTO;
import com.mallcloud.mallcart.api.dto.CartSelectDTO;
import com.mallcloud.mallcart.api.dto.CartUpdateDTO;
import com.mallcloud.mallcart.api.vo.CartVO;

public interface CartService {
    void addCart(CartItemDTO dto);
    CartVO getCart();
    void updateQuantity(Long skuId, CartUpdateDTO dto);
    void updateSelect(Long skuId, CartSelectDTO dto);
    void deleteCartItem(Long skuId);
    void clearCart();
}
