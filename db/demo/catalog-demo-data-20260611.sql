SET NAMES utf8mb4;

USE `mall_product`;

-- 补齐二级/三级类目
INSERT INTO `category` (id, parent_id, name, level, sort) VALUES
(51, 5, '彩妆', 2, 1),
(52, 5, '面部护肤', 2, 2),
(511, 51, '口红', 3, 1),
(521, 52, '精华', 3, 1),
(61, 6, '冲调饮品', 2, 1),
(62, 6, '休闲零食', 2, 2),
(611, 61, '咖啡', 3, 1),
(621, 62, '坚果', 3, 1),
(71, 7, '运动鞋包', 2, 1),
(72, 7, '健身训练', 2, 2),
(711, 71, '跑步鞋', 3, 1),
(721, 72, '瑜伽垫', 3, 1),
(231, 23, '鼠标', 3, 1)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- SPU 扩展到 12 个
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
(1012, '小米智能台灯', '护眼防蓝光', '/products/1012-desk-lamp.svg', 311, '小米', 1006, 1, 1800)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  main_image = VALUES(main_image),
  category_id = VALUES(category_id),
  brand = VALUES(brand),
  merchant_id = VALUES(merchant_id),
  status = VALUES(status),
  sales = VALUES(sales);

-- SKU 扩展到 12 个
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
(9012, 1012, '{"颜色":"白色"}', 149.00, 169.00, '/products/1012-desk-lamp.svg', 1)
ON DUPLICATE KEY UPDATE
  spu_id = VALUES(spu_id),
  spec_json = VALUES(spec_json),
  price = VALUES(price),
  original_price = VALUES(original_price),
  image = VALUES(image),
  status = VALUES(status);

USE `mall_inventory`;

-- Stock 扩展
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
(9012, 500, 0, 500)
ON DUPLICATE KEY UPDATE
  total = VALUES(total),
  locked = VALUES(locked),
  available = VALUES(available);

USE `mall_seckill`;

-- 秒杀活动
INSERT INTO `seckill_activity` (id, name, sku_id, seckill_price, total_stock, limit_per_user, start_time, end_time, status) VALUES
(1, 'iPhone 15 Pro 限时秒杀', 9001, 7999.00, 100, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 1 DAY), 0),
(2, '咖啡礼盒限量秒杀',        9009, 99.00,   50,  1, DATE_ADD(NOW(), INTERVAL 2 HOUR), DATE_ADD(NOW(), INTERVAL 4 HOUR), 0),
(3, '跑步鞋整点抢',           9011, 399.00,  200, 1, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 0)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  sku_id = VALUES(sku_id),
  seckill_price = VALUES(seckill_price),
  total_stock = VALUES(total_stock),
  limit_per_user = VALUES(limit_per_user),
  start_time = VALUES(start_time),
  end_time = VALUES(end_time),
  status = VALUES(status);
