package com.mallcloud.malladminbiz.client.dto;

import com.mallcloud.malladminbiz.api.vo.TopProductVO;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 商品统计 DTO
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@Data
public class ProductStatsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long totalProducts;
    private List<TopProductVO> topProducts = Collections.emptyList();
}
