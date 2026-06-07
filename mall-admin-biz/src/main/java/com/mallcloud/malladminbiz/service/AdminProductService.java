package com.mallcloud.malladminbiz.service;

import com.mallcloud.malladminbiz.api.dto.AdminProductQueryDTO;
import com.mallcloud.malladminbiz.api.vo.AdminProductVO;
import com.mallcloud.mallcommon.response.PageData;

/**
 * 后台商品服务
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
public interface AdminProductService {

    /**
     * 查询后台商品列表
     */
    PageData<AdminProductVO> listProducts(AdminProductQueryDTO query);
}
