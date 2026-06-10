package com.mallcloud.mallmessage.client.vo;

import lombok.Data;

/**
 * 秒杀请求结果
 */
@Data
public class SeckillResultVO {

    private Integer status;
    private String orderNo;
    private String message;
}
