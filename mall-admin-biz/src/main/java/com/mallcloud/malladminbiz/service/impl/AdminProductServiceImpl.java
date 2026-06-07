package com.mallcloud.malladminbiz.service.impl;

import com.mallcloud.malladminbiz.api.dto.AdminProductQueryDTO;
import com.mallcloud.malladminbiz.api.vo.AdminProductVO;
import com.mallcloud.malladminbiz.client.ProductAdminClient;
import com.mallcloud.malladminbiz.service.AdminProductService;
import com.mallcloud.mallcommon.enums.ErrorCode;
import com.mallcloud.mallcommon.exception.BizException;
import com.mallcloud.mallcommon.response.PageData;
import com.mallcloud.mallcommon.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 后台商品服务实现
 *
 * @author zhaoliu
 * @since 2026-03-01
 */
@Service
@RequiredArgsConstructor
public class AdminProductServiceImpl implements AdminProductService {

    private final ProductAdminClient productAdminClient;

    @Override
    public PageData<AdminProductVO> listProducts(AdminProductQueryDTO query) {
        Result<PageData<AdminProductVO>> result = productAdminClient.listProducts(query);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            throw new BizException(ErrorCode.REMOTE_CALL_ERROR.getCode(), "后台商品查询失败");
        }
        return result.getData();
    }
}
