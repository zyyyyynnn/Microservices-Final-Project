package com.mallcloud.mallseckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mallcloud.mallcommon.enums.ErrorCode;
import com.mallcloud.mallcommon.exception.BizException;
import com.mallcloud.mallcommon.util.BizNoUtil;
import com.mallcloud.mallcommon.util.UserContext;
import com.mallcloud.mallseckill.api.dto.SeckillCreateDTO;
import com.mallcloud.mallseckill.api.vo.SeckillActivityVO;
import com.mallcloud.mallseckill.api.vo.SeckillCreateVO;
import com.mallcloud.mallseckill.api.vo.SeckillResultVO;
import com.mallcloud.mallseckill.domain.SeckillActivity;
import com.mallcloud.mallseckill.domain.SeckillOrder;
import com.mallcloud.mallseckill.mapper.SeckillActivityMapper;
import com.mallcloud.mallseckill.mapper.SeckillOrderMapper;
import com.mallcloud.mallseckill.mq.SeckillRequestMessage;
import com.mallcloud.mallseckill.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 秒杀服务实现
 *
 * @author wangwu
 * @since 2026-03-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    private static final String SECKILL_REQUEST_TOPIC = "SECKILL_REQUEST";
    private static final String STOCK_KEY_PREFIX = "seckill:stock:";
    private static final String USER_KEY_PREFIX = "seckill:user:";
    private static final int STATUS_WAITING = 0;
    private static final int STATUS_SUCCESS = 1;
    private static final int STATUS_FAIL = 2;
    private static final int ACTIVITY_CANCELLED = 3;
    private static final Long LUA_SUCCESS = 1L;
    private static final Long LUA_STOCK_EMPTY = -1L;
    private static final Long LUA_LIMIT = -2L;

    private static final DefaultRedisScript<Long> PRE_DEDUCT_SCRIPT = new DefaultRedisScript<>(
            """
            local quantity = tonumber(ARGV[1])
            local limit = tonumber(ARGV[2])
            local ttl = tonumber(ARGV[3])
            local stock = tonumber(redis.call('GET', KEYS[1]) or '0')
            if stock < quantity then
                return -1
            end
            local bought = tonumber(redis.call('GET', KEYS[2]) or '0')
            if bought + quantity > limit then
                return -2
            end
            redis.call('DECRBY', KEYS[1], quantity)
            redis.call('INCRBY', KEYS[2], quantity)
            redis.call('EXPIRE', KEYS[2], ttl)
            return 1
            """,
            Long.class);

    private final SeckillActivityMapper activityMapper;
    private final SeckillOrderMapper orderMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RocketMQTemplate rocketMQTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<SeckillActivityVO> listActivities() {
        return activityMapper.selectList(new LambdaQueryWrapper<SeckillActivity>()
                        .orderByAsc(SeckillActivity::getStartTime))
                .stream()
                .map(this::toActivityVO)
                .toList();
    }

    @Override
    public SeckillActivityVO getActivity(Long activityId) {
        return toActivityVO(requireActivity(activityId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SeckillCreateVO create(Long activityId, SeckillCreateDTO dto) {
        Long userId = UserContext.requireUserId();
        SeckillActivity activity = requireActivity(activityId);
        validateActivity(activity, dto);
        initializeStock(activity);

        Long luaResult = stringRedisTemplate.execute(
                PRE_DEDUCT_SCRIPT,
                List.of(stockKey(activityId), userKey(activityId, userId)),
                String.valueOf(dto.getQuantity()),
                String.valueOf(activity.getLimitPerUser()),
                String.valueOf(userKeyTtl(activity)));
        if (Objects.equals(LUA_STOCK_EMPTY, luaResult)) {
            throw new BizException(ErrorCode.SECKILL_STOCK_EMPTY);
        }
        if (Objects.equals(LUA_LIMIT, luaResult)) {
            throw new BizException(ErrorCode.SECKILL_LIMIT);
        }
        if (!Objects.equals(LUA_SUCCESS, luaResult)) {
            throw new BizException(ErrorCode.SYSTEM_ERROR.getCode(), "秒杀预扣失败");
        }

        String requestId = BizNoUtil.generateRequestId(userId, activityId);
        SeckillOrder order = new SeckillOrder();
        order.setActivityId(activityId);
        order.setUserId(userId);
        order.setSkuId(dto.getSkuId());
        order.setRequestId(requestId);
        order.setStatus(STATUS_WAITING);
        try {
            orderMapper.insert(order);
            sendSeckillRequest(activity, dto, userId, requestId);
        } catch (DuplicateKeyException e) {
            rollbackRedis(activityId, userId, dto.getQuantity());
            throw new BizException(ErrorCode.SECKILL_LIMIT);
        } catch (JsonProcessingException e) {
            rollbackRedis(activityId, userId, dto.getQuantity());
            log.error("秒杀消息序列化失败 requestId={}", requestId, e);
            throw new BizException(ErrorCode.SYSTEM_ERROR.getCode(), "秒杀请求投递失败");
        } catch (RuntimeException e) {
            rollbackRedis(activityId, userId, dto.getQuantity());
            log.error("秒杀请求投递失败 requestId={}", requestId, e);
            throw e;
        }

        return SeckillCreateVO.builder()
                .requestId(requestId)
                .resultUrl("/api/v1/seckill/result/" + requestId)
                .build();
    }

    @Override
    public SeckillResultVO getResult(String requestId) {
        SeckillOrder order = orderMapper.selectOne(new LambdaQueryWrapper<SeckillOrder>()
                .eq(SeckillOrder::getRequestId, requestId));
        if (order == null) {
            throw new BizException(ErrorCode.PARAM_ERROR.getCode(), "秒杀请求不存在");
        }
        SeckillResultVO vo = new SeckillResultVO();
        vo.setStatus(order.getStatus());
        vo.setOrderNo(order.getOrderNo());
        vo.setMessage(buildResultMessage(order.getStatus()));
        return vo;
    }

    @Override
    public void markSuccess(String requestId, String orderNo) {
        int rows = orderMapper.update(null, new LambdaUpdateWrapper<SeckillOrder>()
                .eq(SeckillOrder::getRequestId, requestId)
                .eq(SeckillOrder::getStatus, STATUS_WAITING)
                .set(SeckillOrder::getStatus, STATUS_SUCCESS)
                .set(SeckillOrder::getOrderNo, orderNo));
        if (rows == 0) {
            SeckillOrder order = requireOrder(requestId);
            if (!Objects.equals(order.getStatus(), STATUS_SUCCESS)) {
                throw new BizException(ErrorCode.ORDER_STATUS_INVALID.getCode(), "秒杀结果状态非法");
            }
        }
    }

    @Override
    public void markFailed(String requestId, String reason) {
        int rows = orderMapper.update(null, new LambdaUpdateWrapper<SeckillOrder>()
                .eq(SeckillOrder::getRequestId, requestId)
                .eq(SeckillOrder::getStatus, STATUS_WAITING)
                .set(SeckillOrder::getStatus, STATUS_FAIL));
        if (rows > 0) {
            log.warn("秒杀请求失败 requestId={} reason={}", requestId, reason);
        }
    }

    private SeckillActivity requireActivity(Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null || Objects.equals(ACTIVITY_CANCELLED, activity.getStatus())) {
            throw new BizException(ErrorCode.PARAM_ERROR.getCode(), "秒杀活动不存在");
        }
        return activity;
    }

    private SeckillOrder requireOrder(String requestId) {
        SeckillOrder order = orderMapper.selectOne(new LambdaQueryWrapper<SeckillOrder>()
                .eq(SeckillOrder::getRequestId, requestId));
        if (order == null) {
            throw new BizException(ErrorCode.PARAM_ERROR.getCode(), "秒杀请求不存在");
        }
        return order;
    }

    private void validateActivity(SeckillActivity activity, SeckillCreateDTO dto) {
        if (!activity.getSkuId().equals(dto.getSkuId())) {
            throw new BizException(ErrorCode.PARAM_ERROR.getCode(), "活动商品不匹配");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime())) {
            throw new BizException(ErrorCode.SECKILL_NOT_START);
        }
        if (!now.isBefore(activity.getEndTime())) {
            throw new BizException(ErrorCode.SECKILL_END);
        }
        if (dto.getQuantity() > activity.getLimitPerUser()) {
            throw new BizException(ErrorCode.SECKILL_LIMIT);
        }
    }

    private void initializeStock(SeckillActivity activity) {
        String stockKey = stockKey(activity.getId());
        Boolean initialized = stringRedisTemplate.opsForValue()
                .setIfAbsent(stockKey, String.valueOf(activity.getTotalStock()));
        if (Boolean.TRUE.equals(initialized)) {
            Duration ttl = Duration.between(LocalDateTime.now(), activity.getEndTime()).plusMinutes(10);
            if (!ttl.isNegative() && !ttl.isZero()) {
                stringRedisTemplate.expire(stockKey, ttl);
            }
        }
    }

    private void sendSeckillRequest(SeckillActivity activity, SeckillCreateDTO dto, Long userId, String requestId)
            throws JsonProcessingException {
        SeckillRequestMessage message = SeckillRequestMessage.builder()
                .requestId(requestId)
                .activityId(activity.getId())
                .userId(userId)
                .skuId(dto.getSkuId())
                .quantity(dto.getQuantity())
                .seckillPrice(activity.getSeckillPrice())
                .build();
        rocketMQTemplate.convertAndSend(SECKILL_REQUEST_TOPIC, objectMapper.writeValueAsString(message));
        log.info("秒杀请求已投递 requestId={} activityId={} userId={}", requestId, activity.getId(), userId);
    }

    private void rollbackRedis(Long activityId, Long userId, Integer quantity) {
        stringRedisTemplate.opsForValue().increment(stockKey(activityId), quantity);
        stringRedisTemplate.opsForValue().decrement(userKey(activityId, userId), quantity);
    }

    private SeckillActivityVO toActivityVO(SeckillActivity activity) {
        SeckillActivityVO vo = new SeckillActivityVO();
        BeanUtils.copyProperties(activity, vo);
        vo.setActivityId(activity.getId());
        vo.setStatus(resolveActivityStatus(activity));
        return vo;
    }

    private Integer resolveActivityStatus(SeckillActivity activity) {
        if (Objects.equals(ACTIVITY_CANCELLED, activity.getStatus())) {
            return ACTIVITY_CANCELLED;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime())) {
            return 0;
        }
        if (!now.isBefore(activity.getEndTime())) {
            return 2;
        }
        return 1;
    }

    private String buildResultMessage(Integer status) {
        if (Objects.equals(STATUS_SUCCESS, status)) {
            return "秒杀成功";
        }
        if (Objects.equals(STATUS_FAIL, status)) {
            return "秒杀失败";
        }
        return "排队中";
    }

    private long userKeyTtl(SeckillActivity activity) {
        long seconds = Duration.between(LocalDateTime.now(), activity.getEndTime()).plusMinutes(10).getSeconds();
        return Math.max(seconds, 60L);
    }

    private String stockKey(Long activityId) {
        return STOCK_KEY_PREFIX + activityId;
    }

    private String userKey(Long activityId, Long userId) {
        return USER_KEY_PREFIX + activityId + ":" + userId;
    }
}
