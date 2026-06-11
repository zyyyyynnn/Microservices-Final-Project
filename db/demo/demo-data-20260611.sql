USE `mall_seckill`;

UPDATE `seckill_activity` 
SET start_time = DATE_SUB(NOW(), INTERVAL 1 HOUR), 
    end_time = DATE_ADD(NOW(), INTERVAL 1 DAY),
    status = 0
WHERE id IN (1, 2, 3);
