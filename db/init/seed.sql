-- ============================================================
-- MallCloud 种子数据
-- 默认用户密码统一为 P@ssw0rd123（BCrypt cost=10）
-- admin 密码: Admin@123
-- ============================================================

USE `mall_user`;

-- 测试用户
INSERT INTO `user` (id, username, phone, nickname, status) VALUES
(1001, 'zhangsan', '13800138001', '张三', 1),
(1002, 'lisi',     '13800138002', '李四', 1),
(1003, 'wangwu',   '13800138003', '王五', 1),
(1004, 'zhaoliu',  '13800138004', '赵六', 1),
(1005, 'merchant01','13800138005', '苹果旗舰店', 1),
(1006, 'merchant02','13800138006', '小米之家', 1),
(1007, 'admin',    '13800138000', '超级管理员', 1),
(1008, 'user1',    '13800138011', '测试1', 1),
(1009, 'user2',    '13800138012', '测试2', 1),
(1010, 'user3',    '13800138013', '测试3', 1);

-- 用户地址
INSERT INTO `address` (user_id, receiver, phone, province, city, district, detail, is_default) VALUES
(1001, '张三', '13800138001', '北京市', '北京市', '海淀区', '中关村大街1号院1号楼101', 1),
(1001, '张三', '13800138001', '北京市', '北京市', '朝阳区', '国贸中心B座2008', 0),
(1002, '李四', '13800138002', '上海市', '上海市', '浦东新区', '世纪大道100号', 1);

-- ============================================================
USE `mall_auth`;

-- BCrypt cost=10 of "P@ssw0rd123"
SET @BCRYPT_PWD = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';
SET @BCRYPT_ADMIN = '$2a$10$DowJonesIndexP9rQ8w6nZ1uLO.6N9KQAGF7p92ldGxad68LJZdL17lhWy';

INSERT INTO `sys_user_auth` (user_id, identity_type, identifier, credential) VALUES
(1001, 'PASSWORD', 'zhangsan',    @BCRYPT_PWD),
(1002, 'PASSWORD', 'lisi',        @BCRYPT_PWD),
(1003, 'PASSWORD', 'wangwu',      @BCRYPT_PWD),
(1004, 'PASSWORD', 'zhaoliu',     @BCRYPT_PWD),
(1005, 'PASSWORD', 'merchant01',  @BCRYPT_PWD),
(1006, 'PASSWORD', 'merchant02',  @BCRYPT_PWD),
(1007, 'PASSWORD', 'admin',       @BCRYPT_ADMIN),
(1008, 'PASSWORD', 'user1',       @BCRYPT_PWD),
(1009, 'PASSWORD', 'user2',       @BCRYPT_PWD),
(1010, 'PASSWORD', 'user3',       @BCRYPT_PWD),
(1001, 'PHONE',    '13800138001', @BCRYPT_PWD),
(1002, 'PHONE',    '13800138002', @BCRYPT_PWD);

-- ============================================================
USE `mall_product`;

-- 三级类目
INSERT INTO `category` (id, parent_id, name, level, sort) VALUES
(1, 0, '手机数码', 1, 1),
(2, 0, '电脑办公', 1, 2),
(3, 0, '家用电器', 1, 3),
(4, 0, '服饰鞋包', 1, 4),
(5, 0, '美妆个护', 1, 5),
(6, 0, '食品生鲜', 1, 6),
(7, 0, '运动户外', 1, 7),
(8, 0, '图书音像', 1, 8),
(9, 0, '母婴玩具', 1, 9),
(10, 0, '汽车用品', 1, 10),

(11, 1, '手机通讯', 2, 1),
(12, 1, '手机配件', 2, 2),
(13, 1, '摄影摄像', 2, 3),
(14, 1, '数码配件', 2, 4),
(15, 1, '影音娱乐', 2, 5),

(111, 11, '智能手机', 3, 1),
(112, 11, '老人机', 3, 2),
(113, 11, '对讲机', 3, 3),
(121, 12, '手机壳', 3, 1),
(122, 12, '贴膜', 3, 2),
(123, 12, '数据线', 3, 3),
(124, 12, '充电器', 3, 4),
(125, 12, '移动电源', 3, 5),

(21, 2, '电脑整机', 2, 1),
(22, 2, '电脑配件', 2, 2),
(23, 2, '外设产品', 2, 3),
(24, 2, '办公设备', 2, 4),
(211, 21, '笔记本', 3, 1),
(212, 21, '台式机', 3, 2),
(213, 21, '一体机', 3, 3),
(221, 22, 'CPU', 3, 1),
(222, 22, '主板', 3, 2),
(223, 22, '显卡', 3, 3);

-- 演示 SPU（仅插入 5 个示例）
INSERT INTO `spu` (id, name, description, main_image, category_id, brand, merchant_id, status, sales) VALUES
(1001, 'iPhone 15 Pro 256G 钛原色', 'Apple iPhone 15 Pro 256G 钛原色，全新 A17 Pro 芯片', 'https://picsum.photos/seed/iphone15/600/600', 111, 'Apple', 1005, 1, 234),
(1002, 'iPhone 15 128G 粉色', 'Apple iPhone 15 128G 粉色，A16 芯片', 'https://picsum.photos/seed/iphone15pink/600/600', 111, 'Apple', 1005, 1, 567),
(1003, '小米 14 Pro 16+512 黑色', '小米 14 Pro 骁龙8 Gen3，徕卡光学', 'https://picsum.photos/seed/mi14pro/600/600', 111, '小米', 1006, 1, 890),
(1004, '华为 Mate 60 Pro 12+512', '华为 Mate 60 Pro 麒麟9000S', 'https://picsum.photos/seed/mate60/600/600', 111, '华为', 1006, 1, 1500),
(1005, 'MacBook Air 13 M3 8+256', 'Apple MacBook Air 13 M3 8+256G', 'https://picsum.photos/seed/macbookair/600/600', 211, 'Apple', 1005, 1, 345);

-- 演示 SKU
INSERT INTO `sku` (id, spu_id, spec_json, price, original_price, image, status) VALUES
(9001, 1001, JSON_OBJECT('颜色','钛原色','版本','256G'), 8999.00, 9999.00, 'https://picsum.photos/seed/iphone15a/400/400', 1),
(9002, 1001, JSON_OBJECT('颜色','钛原色','版本','512G'), 10999.00, 11999.00, 'https://picsum.photos/seed/iphone15b/400/400', 1),
(9003, 1002, JSON_OBJECT('颜色','粉色','版本','128G'), 5999.00, 6499.00, 'https://picsum.photos/seed/iphone15c/400/400', 1),
(9004, 1003, JSON_OBJECT('颜色','黑色','版本','512G'), 5499.00, 5999.00, 'https://picsum.photos/seed/mi14a/400/400', 1),
(9005, 1003, JSON_OBJECT('颜色','白色','版本','512G'), 5499.00, 5999.00, 'https://picsum.photos/seed/mi14b/400/400', 1),
(9006, 1004, JSON_OBJECT('颜色','雅川青','版本','512G'), 6999.00, 7499.00, 'https://picsum.photos/seed/mate60a/400/400', 1),
(9007, 1005, JSON_OBJECT('颜色','午夜色','版本','256G'), 8999.00, 9499.00, 'https://picsum.photos/seed/macbooka/400/400', 1);

-- SPU 属性
INSERT INTO `spu_attr` (spu_id, attr_name, attr_value) VALUES
(1001, '品牌', 'Apple'),
(1001, '型号', 'iPhone 15 Pro'),
(1001, 'CPU', 'A17 Pro'),
(1003, '品牌', '小米'),
(1003, '型号', '14 Pro'),
(1003, 'CPU', '骁龙8 Gen3');

-- ============================================================
USE `mall_inventory`;

-- 初始化库存
INSERT INTO `stock` (sku_id, total, locked, available) VALUES
(9001, 100, 0, 100),
(9002, 50,  0, 50),
(9003, 200, 0, 200),
(9004, 300, 0, 300),
(9005, 300, 0, 300),
(9006, 500, 0, 500),
(9007, 80,  0, 80);

-- ============================================================
USE `mall_seckill`;

-- 秒杀活动
INSERT INTO `seckill_activity` (id, name, sku_id, seckill_price, total_stock, limit_per_user, start_time, end_time, status) VALUES
(1, 'iPhone 15 限时秒杀 8 折', 9003, 4799.00, 100, 1, '2026-06-08 10:00:00', '2026-06-08 12:00:00', 0),
(2, '小米 14 Pro 限量秒杀',     9004, 4599.00, 50,  1, '2026-06-08 14:00:00', '2026-06-08 16:00:00', 0),
(3, '华为 Mate 60 Pro 整点抢',  9006, 6499.00, 200, 1, '2026-06-09 20:00:00', '2026-06-09 22:00:00', 0);

-- ============================================================
-- 5. 验证：检查数据
-- ============================================================
SELECT 'mall_user.user' AS table_name, COUNT(*) AS cnt FROM `mall_user`.`user`
UNION ALL SELECT 'mall_user.address', COUNT(*) FROM `mall_user`.`address`
UNION ALL SELECT 'mall_auth.sys_user_auth', COUNT(*) FROM `mall_auth`.`sys_user_auth`
UNION ALL SELECT 'mall_product.category', COUNT(*) FROM `mall_product`.`category`
UNION ALL SELECT 'mall_product.spu', COUNT(*) FROM `mall_product`.`spu`
UNION ALL SELECT 'mall_product.sku', COUNT(*) FROM `mall_product`.`sku`
UNION ALL SELECT 'mall_inventory.stock', COUNT(*) FROM `mall_inventory`.`stock`
UNION ALL SELECT 'mall_seckill.seckill_activity', COUNT(*) FROM `mall_seckill`.`seckill_activity`;
