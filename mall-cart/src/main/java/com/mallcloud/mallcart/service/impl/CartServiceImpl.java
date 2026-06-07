package com.mallcloud.mallcart.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mallcloud.mallcart.api.dto.CartItemDTO;
import com.mallcloud.mallcart.api.dto.CartSelectDTO;
import com.mallcloud.mallcart.api.dto.CartUpdateDTO;
import com.mallcloud.mallcart.api.vo.CartItemVO;
import com.mallcloud.mallcart.api.vo.CartVO;
import com.mallcloud.mallcart.client.ProductClient;
import com.mallcloud.mallcart.client.dto.SkuDTO;
import com.mallcloud.mallcart.service.CartService;
import com.mallcloud.mallcommon.enums.ErrorCode;
import com.mallcloud.mallcommon.exception.BizException;
import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallcommon.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ProductClient productClient;
    private final ObjectMapper objectMapper;

    private static final String CART_PREFIX = "mall:cart:";

    private String getCartKey() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return CART_PREFIX + userId;
    }

    @Override
    public void addCart(CartItemDTO dto) {
        String cartKey = getCartKey();
        HashOperations<String, String, String> hashOps = stringRedisTemplate.opsForHash();
        String skuIdStr = String.valueOf(dto.getSkuId());
        
        try {
            CartItemVO cartItem;
            if (hashOps.hasKey(cartKey, skuIdStr)) {
                String json = hashOps.get(cartKey, skuIdStr);
                cartItem = objectMapper.readValue(json, CartItemVO.class);
                cartItem.setQuantity(cartItem.getQuantity() + dto.getQuantity());
            } else {
                Result<SkuDTO> skuResult = productClient.getSku(dto.getSkuId());
                if (!skuResult.isSuccess() || skuResult.getData() == null) {
                    throw new BizException(ErrorCode.PRODUCT_NOT_FOUND.getCode(), "商品不存在");
                }
                SkuDTO sku = skuResult.getData();
                
                cartItem = new CartItemVO();
                cartItem.setSkuId(sku.getSkuId());
                cartItem.setSkuName(sku.getSpec());
                cartItem.setSkuImage(sku.getImage());
                cartItem.setPrice(sku.getPrice());
                cartItem.setQuantity(dto.getQuantity());
                cartItem.setSelected(true);
            }
            
            hashOps.put(cartKey, skuIdStr, objectMapper.writeValueAsString(cartItem));
        } catch (JsonProcessingException e) {
            log.error("购物车序列化异常", e);
            throw new BizException(ErrorCode.SYSTEM_ERROR.getCode(), "系统异常");
        }
    }

    @Override
    public CartVO getCart() {
        String cartKey = getCartKey();
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(cartKey);
        
        List<CartItemVO> items = new ArrayList<>();
        int totalQuantity = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (Object value : entries.values()) {
            try {
                CartItemVO item = objectMapper.readValue((String) value, CartItemVO.class);
                item.setSubtotal(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
                items.add(item);
                
                if (Boolean.TRUE.equals(item.getSelected())) {
                    totalQuantity += item.getQuantity();
                    totalAmount = totalAmount.add(item.getSubtotal());
                }
            } catch (JsonProcessingException e) {
                log.error("购物车反序列化异常", e);
            }
        }
        
        CartVO cartVO = new CartVO();
        cartVO.setItems(items);
        cartVO.setTotalQuantity(totalQuantity);
        cartVO.setTotalAmount(totalAmount);
        return cartVO;
    }

    @Override
    public void updateQuantity(Long skuId, CartUpdateDTO dto) {
        String cartKey = getCartKey();
        HashOperations<String, String, String> hashOps = stringRedisTemplate.opsForHash();
        String skuIdStr = String.valueOf(skuId);
        
        if (!hashOps.hasKey(cartKey, skuIdStr)) {
            throw new BizException(ErrorCode.PARAM_ERROR.getCode(), "购物车不存在该商品");
        }
        
        try {
            String json = hashOps.get(cartKey, skuIdStr);
            CartItemVO cartItem = objectMapper.readValue(json, CartItemVO.class);
            cartItem.setQuantity(dto.getQuantity());
            hashOps.put(cartKey, skuIdStr, objectMapper.writeValueAsString(cartItem));
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.SYSTEM_ERROR.getCode(), "系统异常");
        }
    }

    @Override
    public void updateSelect(Long skuId, CartSelectDTO dto) {
        String cartKey = getCartKey();
        HashOperations<String, String, String> hashOps = stringRedisTemplate.opsForHash();
        String skuIdStr = String.valueOf(skuId);
        
        if (!hashOps.hasKey(cartKey, skuIdStr)) {
            throw new BizException(ErrorCode.PARAM_ERROR.getCode(), "购物车不存在该商品");
        }
        
        try {
            String json = hashOps.get(cartKey, skuIdStr);
            CartItemVO cartItem = objectMapper.readValue(json, CartItemVO.class);
            cartItem.setSelected(dto.getSelected());
            hashOps.put(cartKey, skuIdStr, objectMapper.writeValueAsString(cartItem));
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.SYSTEM_ERROR.getCode(), "系统异常");
        }
    }

    @Override
    public void deleteCartItem(Long skuId) {
        String cartKey = getCartKey();
        stringRedisTemplate.opsForHash().delete(cartKey, String.valueOf(skuId));
    }

    @Override
    public void clearCart() {
        String cartKey = getCartKey();
        stringRedisTemplate.delete(cartKey);
    }
}
