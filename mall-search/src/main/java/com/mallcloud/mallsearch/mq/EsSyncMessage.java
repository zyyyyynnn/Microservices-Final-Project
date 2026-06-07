package com.mallcloud.mallsearch.mq;

import lombok.Data;

/**
 * 商品 ES 同步消息
 *
 * @author lisi
 * @since 2026-03-01
 */
@Data
public class EsSyncMessage {

    private Long spuId;
    private Integer status;
}
