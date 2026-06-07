package com.mallcloud.mallpay.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.internal.util.AlipaySignature;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mallcloud.mallcommon.enums.ErrorCode;
import com.mallcloud.mallcommon.exception.BizException;
import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallcommon.util.BizNoUtil;
import com.mallcloud.mallpay.api.dto.PayCreateDTO;
import com.mallcloud.mallpay.api.vo.PayCreateVO;
import com.mallcloud.mallpay.api.vo.PayRecordVO;
import com.mallcloud.mallpay.client.OrderClient;
import com.mallcloud.mallpay.client.dto.OrderDTO;
import com.mallcloud.mallpay.config.AlipayProperties;
import com.mallcloud.mallpay.domain.PayRecord;
import com.mallcloud.mallpay.mapper.PayRecordMapper;
import com.mallcloud.mallpay.service.PayService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付服务实现
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayServiceImpl implements PayService {

    private static final String ALIPAY = "ALIPAY";
    private static final String WECHAT = "WECHAT";
    private static final String PAY_RESULT_TOPIC = "PAY_RESULT";
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_SUCCESS = 1;
    private static final int STATUS_FAIL = 2;

    private final PayRecordMapper payRecordMapper;
    private final OrderClient orderClient;
    private final AlipayProperties alipayProperties;
    private final RocketMQTemplate rocketMQTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayCreateVO createPay(Long userId, PayCreateDTO dto) {
        String payChannel = dto.getPayChannel().trim().toUpperCase();
        if (!ALIPAY.equals(payChannel) && !WECHAT.equals(payChannel)) {
            throw new BizException(ErrorCode.PARAM_ERROR.getCode(), "支付渠道不支持");
        }

        Result<OrderDTO> orderResult = orderClient.getOrder(dto.getOrderNo());
        if (!orderResult.isSuccess() || orderResult.getData() == null) {
            throw new BizException(ErrorCode.REMOTE_CALL_ERROR.getCode(), "查询订单失败");
        }
        OrderDTO order = orderResult.getData();
        if (order.getStatus() == null || order.getStatus() != 0) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID);
        }

        PayRecord payRecord = payRecordMapper.selectOne(new LambdaQueryWrapper<PayRecord>()
                .eq(PayRecord::getOrderNo, dto.getOrderNo()));
        if (payRecord == null) {
            payRecord = new PayRecord();
            payRecord.setPayNo(BizNoUtil.generatePayNo());
            payRecord.setOrderNo(dto.getOrderNo());
            payRecord.setUserId(userId);
            payRecord.setPayChannel(payChannel);
            payRecord.setPayAmount(order.getPayAmount());
            payRecord.setStatus(STATUS_PENDING);
            payRecordMapper.insert(payRecord);
        } else if (!userId.equals(payRecord.getUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        } else if (payRecord.getStatus() == STATUS_SUCCESS) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID.getCode(), "订单已支付");
        }

        String payFormHtml = ALIPAY.equals(payChannel) ? buildAlipayForm(payRecord) : "";
        String payUrl = buildPayUrl(payRecord);
        return PayCreateVO.builder()
                .payNo(payRecord.getPayNo())
                .payUrl(payUrl)
                .payFormHtml(payFormHtml)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleNotify(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_ERROR.getCode(), "回调参数不能为空");
        }
        if (!verifyAlipayNotify(params)) {
            throw new BizException(ErrorCode.PAY_FAIL.getCode(), "支付宝回调验签失败");
        }

        String orderNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");
        if (!StringUtils.hasText(orderNo)) {
            throw new BizException(ErrorCode.PARAM_ERROR.getCode(), "订单号不能为空");
        }

        int targetStatus = ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus))
                ? STATUS_SUCCESS : STATUS_FAIL;
        boolean updated = payRecordMapper.update(null, new LambdaUpdateWrapper<PayRecord>()
                .eq(PayRecord::getOrderNo, orderNo)
                .ne(PayRecord::getStatus, STATUS_SUCCESS)
                .set(PayRecord::getStatus, targetStatus)
                .set(PayRecord::getTradeNo, tradeNo)
                .set(PayRecord::getNotifyTime, LocalDateTime.now())) > 0;
        if (updated && targetStatus == STATUS_SUCCESS) {
            sendPayResult(orderNo, tradeNo);
        }
        return true;
    }

    @Override
    public PayRecordVO getRecord(String orderNo, Long userId) {
        PayRecord record = payRecordMapper.selectOne(new LambdaQueryWrapper<PayRecord>()
                .eq(PayRecord::getOrderNo, orderNo)
                .eq(PayRecord::getUserId, userId));
        if (record == null) {
            throw new BizException(ErrorCode.PAY_FAIL.getCode(), "支付记录不存在");
        }
        PayRecordVO vo = new PayRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }

    private String buildAlipayForm(PayRecord payRecord) {
        if (!hasAlipayConfig()) {
            return "";
        }
        AlipayClient alipayClient = new DefaultAlipayClient(
                alipayProperties.getGatewayUrl(),
                alipayProperties.getAppId(),
                alipayProperties.getPrivateKey(),
                alipayProperties.getFormat(),
                alipayProperties.getCharset(),
                alipayProperties.getAlipayPublicKey(),
                alipayProperties.getSignType());
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayProperties.getNotifyUrl());
        request.setReturnUrl(alipayProperties.getReturnUrl());
        request.setBizContent("{"
                + "\"out_trade_no\":\"" + payRecord.getOrderNo() + "\","
                + "\"total_amount\":\"" + payRecord.getPayAmount() + "\","
                + "\"subject\":\"MallCloud订单" + payRecord.getOrderNo() + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\""
                + "}");
        try {
            return alipayClient.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            log.error("生成支付宝支付表单失败 orderNo={}", payRecord.getOrderNo(), e);
            throw new BizException(ErrorCode.PAY_FAIL.getCode(), "生成支付表单失败");
        }
    }

    private String buildPayUrl(PayRecord payRecord) {
        if (ALIPAY.equals(payRecord.getPayChannel())) {
            return alipayProperties.getGatewayUrl() + "?out_trade_no=" + payRecord.getOrderNo();
        }
        return "weixin://wxpay/bizpayurl?pr=" + payRecord.getPayNo();
    }

    private boolean hasAlipayConfig() {
        return StringUtils.hasText(alipayProperties.getAppId())
                && StringUtils.hasText(alipayProperties.getPrivateKey())
                && StringUtils.hasText(alipayProperties.getAlipayPublicKey());
    }

    private boolean verifyAlipayNotify(Map<String, String> params) {
        if (!hasAlipayConfig()) {
            log.warn("支付宝配置未完整，跳过沙箱回调验签，仅用于本地开发");
            return true;
        }
        try {
            return AlipaySignature.rsaCheckV1(
                    params,
                    alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getCharset(),
                    alipayProperties.getSignType());
        } catch (AlipayApiException e) {
            log.warn("支付宝回调验签异常", e);
            return false;
        }
    }

    private void sendPayResult(String orderNo, String tradeNo) {
        Map<String, String> message = new HashMap<>();
        message.put("orderNo", orderNo);
        message.put("tradeNo", tradeNo);
        message.put("status", "SUCCESS");
        try {
            rocketMQTemplate.convertAndSend(PAY_RESULT_TOPIC, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.SYSTEM_ERROR.getCode(), "支付结果消息序列化失败");
        }
    }
}
