package com.mallcloud.mallorder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mallcloud.mallorder.domain.OrderInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {
}
