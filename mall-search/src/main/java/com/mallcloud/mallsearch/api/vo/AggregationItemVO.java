package com.mallcloud.mallsearch.api.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聚合项
 *
 * @author lisi
 * @since 2026-03-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AggregationItemVO {

    private String key;
    private Long id;
    private String name;
    private Long count;
}
