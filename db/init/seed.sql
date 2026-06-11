-- ============================================================
-- MallCloud 种子数据
-- 所有测试账号密码统一为 123456（BCrypt cost=10）
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

-- BCrypt cost=10 of "123456"
SET @BCRYPT_PWD = '$2y$10$k8Z56rLWfoKE6XNip7PenuX5tiKdD.QB93WSNZLHH4Y2fOg7.16Ku';

INSERT INTO `sys_user_auth`
    (user_id, identity_type, identifier, credential, role)
VALUES
(1001, 'PASSWORD', 'zhangsan',    @BCRYPT_PWD, 'USER'),
(1002, 'PASSWORD', 'lisi',        @BCRYPT_PWD, 'USER'),
(1003, 'PASSWORD', 'wangwu',      @BCRYPT_PWD, 'USER'),
(1004, 'PASSWORD', 'zhaoliu',     @BCRYPT_PWD, 'USER'),
(1005, 'PASSWORD', 'merchant01',  @BCRYPT_PWD, 'MERCHANT'),
(1006, 'PASSWORD', 'merchant02',  @BCRYPT_PWD, 'MERCHANT'),
(1007, 'PASSWORD', 'admin',       @BCRYPT_PWD, 'ADMIN'),
(1008, 'PASSWORD', 'user1',       @BCRYPT_PWD, 'USER'),
(1009, 'PASSWORD', 'user2',       @BCRYPT_PWD, 'USER'),
(1010, 'PASSWORD', 'user3',       @BCRYPT_PWD, 'USER'),
(1001, 'PHONE',    '13800138001', @BCRYPT_PWD, 'USER'),
(1002, 'PHONE',    '13800138002', @BCRYPT_PWD, 'USER');

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
-- ============================================================
-- MallCloud 种子数据
-- 所有测试账号密码统一为 123456（BCrypt cost=10）
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

-- BCrypt cost=10 of "123456"
SET @BCRYPT_PWD = '$2y$10$k8Z56rLWfoKE6XNip7PenuX5tiKdD.QB93WSNZLHH4Y2fOg7.16Ku';

INSERT INTO `sys_user_auth`
    (user_id, identity_type, identifier, credential, role)
VALUES
(1001, 'PASSWORD', 'zhangsan',    @BCRYPT_PWD, 'USER'),
(1002, 'PASSWORD', 'lisi',        @BCRYPT_PWD, 'USER'),
(1003, 'PASSWORD', 'wangwu',      @BCRYPT_PWD, 'USER'),
(1004, 'PASSWORD', 'zhaoliu',     @BCRYPT_PWD, 'USER'),
(1005, 'PASSWORD', 'merchant01',  @BCRYPT_PWD, 'MERCHANT'),
(1006, 'PASSWORD', 'merchant02',  @BCRYPT_PWD, 'MERCHANT'),
(1007, 'PASSWORD', 'admin',       @BCRYPT_PWD, 'ADMIN'),
(1008, 'PASSWORD', 'user1',       @BCRYPT_PWD, 'USER'),
(1009, 'PASSWORD', 'user2',       @BCRYPT_PWD, 'USER'),
(1010, 'PASSWORD', 'user3',       @BCRYPT_PWD, 'USER'),
(1001, 'PHONE',    '13800138001', @BCRYPT_PWD, 'USER'),
(1002, 'PHONE',    '13800138002', @BCRYPT_PWD, 'USER');

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
-- 演示 SPU
INSERT INTO `spu` (id, name, description, main_image, category_id, brand, merchant_id, status, sales) VALUES
(1001, 'iPhone 15 Pro 256G 钛原色', 'Apple iPhone 15 Pro 256G 钛原色', '/products/1001-iphone-15-pro.svg', 111, 'Apple', 1005, 1, 234),
(1002, 'iPhone 15 128G 粉色', 'Apple iPhone 15 128G 粉色', '/products/1002-iphone-15-pink.svg', 111, 'Apple', 1005, 1, 567),
(1003, '小米 14 Pro 16+512 黑色', '小米 14 Pro 骁龙8 Gen3', '/products/1003-mi-14-pro.svg', 111, '小米', 1006, 1, 890),
(1004, '华为 Mate 60 Pro 12+512', '华为 Mate 60 Pro 麒麟9000S', '/products/1004-huawei-mate-60.svg', 111, '华为', 1006, 1, 1500),
(1005, 'MacBook Air 13 M3 8+256', 'Apple MacBook Air 13 M3', '/products/1005-macbook-air.svg', 211, 'Apple', 1005, 1, 345),
(1006, '罗技 MX Master 3S 鼠标', '罗技办公旗舰鼠标', '/products/1006-logitech-mouse.svg', 231, '罗技', 1006, 1, 450),
(1007, 'YSL 小金条口红 1966', 'YSL 1966 无法复刻的红棕', '/products/1007-ysl-lipstick.svg', 511, 'YSL', 1005, 1, 800),
(1008, '欧莱雅玻色因保湿精华', '抗老淡纹保湿', '/products/1008-loreal-serum.svg', 521, '欧莱雅', 1005, 1, 620),
(1009, '蓝山挂耳咖啡礼盒', '香醇阿拉比卡豆', '/products/1009-coffee-gift-box.svg', 611, '蓝山', 1006, 1, 1200),
(1010, '每日坚果礼盒 30 包', '健康营养补充', '/products/1010-nuts-gift-box.svg', 621, '三只松鼠', 1006, 1, 3000),
(1011, 'Nike Pegasus 跑步鞋', '减震透气', '/products/1011-nike-running-shoes.svg', 711, 'Nike', 1005, 1, 950),
(1012, '迪卡侬加厚瑜伽垫', '环保防滑', '/products/1012-yoga-mat.svg', 721, '迪卡侬', 1006, 1, 1800);

-- 演示 SKU
INSERT INTO `sku` (id, spu_id, spec_json, price, original_price, image, status) VALUES
(9001, 1001, '{"颜色":"钛原色","版本":"256G"}', 8999.00, 9999.00, '/products/1001-iphone-15-pro.svg', 1),
(9002, 1002, '{"颜色":"粉色","版本":"128G"}', 5999.00, 6499.00, '/products/1002-iphone-15-pink.svg', 1),
(9003, 1003, '{"颜色":"黑色","版本":"512G"}', 5499.00, 5999.00, '/products/1003-mi-14-pro.svg', 1),
(9004, 1004, '{"颜色":"雅川青","版本":"512G"}', 6999.00, 7499.00, '/products/1004-huawei-mate-60.svg', 1),
(9005, 1005, '{"颜色":"午夜色","版本":"256G"}', 8999.00, 9499.00, '/products/1005-macbook-air.svg', 1),
(9006, 1006, '{"颜色":"黑色"}', 899.00, 999.00, '/products/1006-logitech-mouse.svg', 1),
(9007, 1007, '{"色号":"1966"}', 350.00, 380.00, '/products/1007-ysl-lipstick.svg', 1),
(9008, 1008, '{"容量":"50ml"}', 450.00, 500.00, '/products/1008-loreal-serum.svg', 1),
(9009, 1009, '{"口味":"经典蓝山"}', 199.00, 259.00, '/products/1009-coffee-gift-box.svg', 1),
(9010, 1010, '{"规格":"30包/箱"}', 129.00, 169.00, '/products/1010-nuts-gift-box.svg', 1),
(9011, 1011, '{"尺码":"42"}', 699.00, 899.00, '/products/1011-nike-running-shoes.svg', 1),
(9012, 1012, '{"厚度":"10mm"}', 89.00, 129.00, '/products/1012-yoga-mat.svg', 1);

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
(9007, 800, 0, 800),
(9008, 600, 0, 600),
(9009, 400, 0, 400),
(9010, 1000, 0, 1000),
(9011, 200, 0, 200),
(9012, 500, 0, 500);

-- ============================================================
USE `mall_seckill`;

-- 秒杀活动
INSERT INTO `seckill_activity` (id, name, sku_id, seckill_price, total_stock, limit_per_user, start_time, end_time, status) VALUES
(1, 'iPhone 15 Pro 限时秒杀', 9001, 7999.00, 100, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 1 DAY), 0),
(2, '咖啡礼盒限量秒杀',        9009, 99.00,   50,  1, DATE_ADD(NOW(), INTERVAL 2 HOUR), DATE_ADD(NOW(), INTERVAL 4 HOUR), 0),
(3, '跑步鞋整点抢',           9011, 399.00,  200, 1, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 0);

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
