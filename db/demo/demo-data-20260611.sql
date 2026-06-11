-- 仅用于本地业务验收前修正秒杀活动有效时间。
-- 不创建账号、商品、SKU、库存等基础数据。
-- 依赖 db/seed.sql 或既有初始化脚本已完成基础数据导入。
USE `mall_seckill`;

UPDATE `seckill_activity` 
SET start_time = DATE_SUB(NOW(), INTERVAL 1 HOUR), 
    end_time = DATE_ADD(NOW(), INTERVAL 1 DAY),
    status = 0
WHERE id IN (1, 2, 3);
