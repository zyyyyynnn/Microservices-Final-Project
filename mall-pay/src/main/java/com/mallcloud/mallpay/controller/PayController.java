package com.mallcloud.mallpay.controller;

import com.mallcloud.mallcommon.response.Result;
import com.mallcloud.mallcommon.util.UserContext;
import com.mallcloud.mallpay.api.dto.PayCreateDTO;
import com.mallcloud.mallpay.api.vo.PayCreateVO;
import com.mallcloud.mallpay.api.vo.PayRecordVO;
import com.mallcloud.mallpay.service.PayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 支付服务接口（骨架）
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Tag(name = "支付接口")
@RestController
@RequestMapping("/api/v1/pay")
@RequiredArgsConstructor
public class PayController {

    private final PayService payService;

    @Operation(summary = "健康检查")
    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.ok("mall-pay pong");
    }

    @Operation(summary = "发起支付")
    @PostMapping("/create")
    public Result<PayCreateVO> createPay(@Valid @RequestBody PayCreateDTO dto) {
        return Result.ok(payService.createPay(UserContext.requireUserId(), dto));
    }

    @Operation(summary = "支付回调")
    @PostMapping("/notify")
    public Result<String> notify(@RequestParam Map<String, String> params) {
        boolean handled = payService.handleNotify(params);
        if (!handled) {
            return Result.error(40300, "支付回调处理失败");
        }
        return Result.ok("success");
    }

    @Operation(summary = "支付记录查询")
    @GetMapping("/record/{orderNo}")
    public Result<PayRecordVO> getRecord(@PathVariable("orderNo") String orderNo) {
        return Result.ok(payService.getRecord(orderNo, UserContext.requireUserId()));
    }
}
