package com.mallcloud.mallseckill.service;

import com.mallcloud.mallseckill.api.dto.SeckillCreateDTO;
import com.mallcloud.mallseckill.api.vo.SeckillActivityVO;
import com.mallcloud.mallseckill.api.vo.SeckillCreateVO;
import com.mallcloud.mallseckill.api.vo.SeckillResultVO;

import java.util.List;

/**
 * 秒杀服务
 *
 * @author wangwu
 * @since 2026-03-01
 */
public interface SeckillService {

    /**
     * 查询秒杀活动列表
     */
    List<SeckillActivityVO> listActivities();

    /**
     * 查询秒杀活动详情
     */
    SeckillActivityVO getActivity(Long activityId);

    /**
     * 发起秒杀请求
     */
    SeckillCreateVO create(Long activityId, SeckillCreateDTO dto);

    /**
     * 查询秒杀请求结果
     */
    SeckillResultVO getResult(String requestId);

    /**
     * 标记秒杀请求成功
     */
    void markSuccess(String requestId, String orderNo);

    /**
     * 标记秒杀请求失败
     */
    void markFailed(String requestId, String reason);
}
