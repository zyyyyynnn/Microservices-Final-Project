package com.mallcloud.mallsearch.service;

import com.mallcloud.mallsearch.api.dto.ProductSearchDTO;
import com.mallcloud.mallsearch.api.vo.ProductSearchVO;

import java.util.List;

/**
 * 搜索服务
 *
 * @author lisi
 * @since 2026-03-01
 */
public interface SearchService {

    /**
     * 搜索商品
     *
     * @param dto 搜索参数
     * @return 搜索结果
     */
    ProductSearchVO searchProducts(ProductSearchDTO dto);

    /**
     * 查询搜索热词
     *
     * @return 热词列表
     */
    List<String> getHotWords();

    /**
     * 同步商品到 ES
     *
     * @param spuId SPU ID
     * @param status 商品状态
     */
    void syncProduct(Long spuId, Integer status);
}
