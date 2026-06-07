package com.mallcloud.mallpay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mallcloud.mallpay.domain.PayRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付记录 Mapper
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Mapper
public interface PayRecordMapper extends BaseMapper<PayRecord> {
}
