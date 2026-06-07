package com.mallcloud.mallpay.service;

import com.mallcloud.mallpay.api.dto.PayCreateDTO;
import com.mallcloud.mallpay.api.vo.PayCreateVO;
import com.mallcloud.mallpay.api.vo.PayRecordVO;

import java.util.Map;

/**
 * 支付服务
 *
 * @author wangwu
 * @since 2026-03-01
 */
public interface PayService {

    /**
     * 发起支付
     *
     * @param userId 用户 ID
     * @param dto 支付请求
     * @return 支付链接和表单
     */
    PayCreateVO createPay(Long userId, PayCreateDTO dto);

    /**
     * 处理支付回调
     *
     * @param params 回调参数
     * @return 是否处理成功
     */
    boolean handleNotify(Map<String, String> params);

    /**
     * 查询支付记录
     *
     * @param orderNo 订单号
     * @param userId 用户 ID
     * @return 支付记录
     */
    PayRecordVO getRecord(String orderNo, Long userId);
}
