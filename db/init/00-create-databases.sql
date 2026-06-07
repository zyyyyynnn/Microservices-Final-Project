-- ============================================================
-- MallCloud 初始化数据库脚本
-- 适用：MySQL 8.0+
-- 字符集：utf8mb4
-- ============================================================

-- 1. 创建所有业务库
CREATE DATABASE IF NOT EXISTS `mall_auth`       DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `mall_user`       DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `mall_product`    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `mall_inventory`  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `mall_order`      DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `mall_pay`        DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `mall_seckill`    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `mall_seata`      DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ============================================================
-- 2. mall_auth
-- ============================================================
USE `mall_auth`;

DROP TABLE IF EXISTS `sys_user_auth`;
CREATE TABLE `sys_user_auth` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`       BIGINT       NOT NULL                COMMENT '关联 mall_user.user.id',
  `identity_type` VARCHAR(16)  NOT NULL DEFAULT 'PASSWORD' COMMENT 'PASSWORD/PHONE/WECHAT',
  `identifier`    VARCHAR(64)  NOT NULL                COMMENT '用户名/手机号/openId',
  `credential`    VARCHAR(255) NOT NULL                COMMENT 'BCrypt 加密',
  `status`        TINYINT      NOT NULL DEFAULT 1     COMMENT '1=正常 0=禁用',
  `gmt_create`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_identity` (`identity_type`, `identifier`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户认证表';

-- Seata undo_log
DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log` (
  `id`            BIGINT       AUTO_INCREMENT PRIMARY KEY,
  `branch_id`     BIGINT       NOT NULL,
  `xid`           VARCHAR(100) NOT NULL,
  `context`       VARCHAR(128) NOT NULL,
  `rollback_info` LONGBLOB     NOT NULL,
  `log_status`    INT          NOT NULL,
  `log_created`   DATETIME     NOT NULL,
  `log_modified`  DATETIME     NOT NULL,
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 3. mall_user
-- ============================================================
USE `mall_user`;

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `username`      VARCHAR(64)  NOT NULL,
  `phone`         VARCHAR(20)  DEFAULT NULL,
  `nickname`      VARCHAR(64)  DEFAULT NULL,
  `avatar`        VARCHAR(255) DEFAULT NULL,
  `email`         VARCHAR(128) DEFAULT NULL,
  `id_card`       VARCHAR(64)  DEFAULT NULL            COMMENT 'AES 加密',
  `status`        TINYINT      NOT NULL DEFAULT 1,
  `gmt_create`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

DROP TABLE IF EXISTS `address`;
CREATE TABLE `address` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`       BIGINT       NOT NULL,
  `receiver`      VARCHAR(64)  NOT NULL,
  `phone`         VARCHAR(20)  NOT NULL,
  `province`      VARCHAR(32)  NOT NULL,
  `city`          VARCHAR(32)  NOT NULL,
  `district`      VARCHAR(32)  NOT NULL,
  `detail`        VARCHAR(255) NOT NULL,
  `is_default`    TINYINT      NOT NULL DEFAULT 0,
  `gmt_create`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址';

DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `branch_id` BIGINT NOT NULL,
  `xid` VARCHAR(100) NOT NULL,
  `context` VARCHAR(128) NOT NULL,
  `rollback_info` LONGBLOB NOT NULL,
  `log_status` INT NOT NULL,
  `log_created` DATETIME NOT NULL,
  `log_modified` DATETIME NOT NULL,
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 4. mall_product
-- ============================================================
USE `mall_product`;

DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `parent_id`     BIGINT       NOT NULL DEFAULT 0,
  `name`          VARCHAR(64)  NOT NULL,
  `level`         TINYINT      NOT NULL DEFAULT 1,
  `icon`          VARCHAR(255) DEFAULT NULL,
  `sort`          INT          NOT NULL DEFAULT 0,
  `status`        TINYINT      NOT NULL DEFAULT 1,
  `gmt_create`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三级类目';

DROP TABLE IF EXISTS `spu`;
CREATE TABLE `spu` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `name`          VARCHAR(255) NOT NULL,
  `description`   TEXT,
  `main_image`    VARCHAR(255) DEFAULT NULL,
  `category_id`   BIGINT       NOT NULL,
  `brand`         VARCHAR(64)  DEFAULT NULL,
  `merchant_id`   BIGINT       NOT NULL,
  `status`        TINYINT      NOT NULL DEFAULT 0     COMMENT '0=下架 1=上架 2=审核中',
  `sales`         INT          NOT NULL DEFAULT 0,
  `view_count`    INT          NOT NULL DEFAULT 0,
  `gmt_create`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SPU';

DROP TABLE IF EXISTS `sku`;
CREATE TABLE `sku` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT,
  `spu_id`         BIGINT        NOT NULL,
  `spec_json`      JSON          DEFAULT NULL,
  `price`          DECIMAL(10,2) NOT NULL,
  `original_price` DECIMAL(10,2) DEFAULT NULL,
  `image`          VARCHAR(255)  DEFAULT NULL,
  `weight`         INT           DEFAULT 0,
  `barcode`        VARCHAR(64)   DEFAULT NULL,
  `status`         TINYINT       NOT NULL DEFAULT 1,
  `gmt_create`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_spu` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SKU';

DROP TABLE IF EXISTS `spu_attr`;
CREATE TABLE `spu_attr` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT,
  `spu_id`     BIGINT      NOT NULL,
  `attr_name`  VARCHAR(64) NOT NULL,
  `attr_value` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_spu` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SPU 属性';

DROP TABLE IF EXISTS `spu_img`;
CREATE TABLE `spu_img` (
  `id`      BIGINT      NOT NULL AUTO_INCREMENT,
  `spu_id`  BIGINT      NOT NULL,
  `url`     VARCHAR(255) NOT NULL,
  `sort`    INT         NOT NULL DEFAULT 0,
  `is_main` TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_spu` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SPU 图片';

DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `branch_id` BIGINT NOT NULL,
  `xid` VARCHAR(100) NOT NULL,
  `context` VARCHAR(128) NOT NULL,
  `rollback_info` LONGBLOB NOT NULL,
  `log_status` INT NOT NULL,
  `log_created` DATETIME NOT NULL,
  `log_modified` DATETIME NOT NULL,
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 5. mall_inventory
-- ============================================================
USE `mall_inventory`;

DROP TABLE IF EXISTS `stock`;
CREATE TABLE `stock` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT,
  `sku_id`        BIGINT      NOT NULL,
  `total`         INT         NOT NULL DEFAULT 0,
  `locked`        INT         NOT NULL DEFAULT 0,
  `available`     INT         NOT NULL DEFAULT 0     COMMENT 'total - locked',
  `version`       INT         NOT NULL DEFAULT 0,
  `gmt_modified`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sku` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存';

DROP TABLE IF EXISTS `stock_log`;
CREATE TABLE `stock_log` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `sku_id`     BIGINT       NOT NULL,
  `change`     INT          NOT NULL                COMMENT '+入 -出',
  `type`       VARCHAR(16)  NOT NULL                COMMENT 'LOCK/UNLOCK/DEDUCT/ROLLBACK',
  `ref_no`     VARCHAR(64)  DEFAULT NULL,
  `remark`     VARCHAR(255) DEFAULT NULL,
  `gmt_create` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_sku` (`sku_id`),
  KEY `idx_ref` (`ref_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水';

DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `branch_id` BIGINT NOT NULL,
  `xid` VARCHAR(100) NOT NULL,
  `context` VARCHAR(128) NOT NULL,
  `rollback_info` LONGBLOB NOT NULL,
  `log_status` INT NOT NULL,
  `log_created` DATETIME NOT NULL,
  `log_modified` DATETIME NOT NULL,
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 6. mall_order
-- ============================================================
USE `mall_order`;

DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT,
  `order_no`        VARCHAR(32)   NOT NULL            COMMENT '业务流水号',
  `user_id`         BIGINT        NOT NULL,
  `merchant_id`     BIGINT        NOT NULL,
  `total_amount`    DECIMAL(12,2) NOT NULL,
  `pay_amount`      DECIMAL(12,2) NOT NULL,
  `freight_amount`  DECIMAL(12,2) NOT NULL DEFAULT 0,
  `discount_amount` DECIMAL(12,2) NOT NULL DEFAULT 0,
  `status`          TINYINT       NOT NULL DEFAULT 0  COMMENT '0待付 1已付 2已发 3完成 4取消 5退款',
  `address_json`    JSON          DEFAULT NULL,
  `pay_deadline`    DATETIME      DEFAULT NULL,
  `remark`          VARCHAR(255)  DEFAULT NULL,
  `gmt_create`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_pay`         DATETIME      DEFAULT NULL,
  `gmt_modified`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_merchant_status` (`merchant_id`, `status`),
  KEY `idx_create` (`gmt_create`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';

DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id`         BIGINT        NOT NULL AUTO_INCREMENT,
  `order_id`   BIGINT        NOT NULL,
  `order_no`   VARCHAR(32)   NOT NULL,
  `sku_id`     BIGINT        NOT NULL,
  `spu_id`     BIGINT        NOT NULL,
  `sku_image`  VARCHAR(255)  DEFAULT NULL,
  `sku_name`   VARCHAR(255)  NOT NULL,
  `spec_json`  JSON          DEFAULT NULL,
  `price`      DECIMAL(10,2) NOT NULL,
  `quantity`   INT           NOT NULL,
  `subtotal`   DECIMAL(12,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_sku` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细';

DROP TABLE IF EXISTS `order_log`;
CREATE TABLE `order_log` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `order_id`    BIGINT       NOT NULL,
  `from_status` TINYINT      NOT NULL,
  `to_status`   TINYINT      NOT NULL,
  `operator`    VARCHAR(64)  DEFAULT NULL,
  `remark`      VARCHAR(255) DEFAULT NULL,
  `gmt_create`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单状态日志';

DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `branch_id` BIGINT NOT NULL,
  `xid` VARCHAR(100) NOT NULL,
  `context` VARCHAR(128) NOT NULL,
  `rollback_info` LONGBLOB NOT NULL,
  `log_status` INT NOT NULL,
  `log_created` DATETIME NOT NULL,
  `log_modified` DATETIME NOT NULL,
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 7. mall_pay
-- ============================================================
USE `mall_pay`;

DROP TABLE IF EXISTS `pay_record`;
CREATE TABLE `pay_record` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT,
  `pay_no`       VARCHAR(32)   NOT NULL,
  `order_no`     VARCHAR(32)   NOT NULL,
  `user_id`      BIGINT        NOT NULL,
  `pay_channel`  VARCHAR(16)   NOT NULL                COMMENT 'ALIPAY/WECHAT',
  `pay_amount`   DECIMAL(12,2) NOT NULL,
  `status`       TINYINT       NOT NULL DEFAULT 0     COMMENT '0待付 1成功 2失败 3关闭',
  `trade_no`     VARCHAR(64)   DEFAULT NULL            COMMENT '第三方流水号',
  `notify_time`  DATETIME      DEFAULT NULL,
  `gmt_create`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pay_no` (`pay_no`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录';

DROP TABLE IF EXISTS `refund_record`;
CREATE TABLE `refund_record` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT,
  `refund_no`     VARCHAR(32)   NOT NULL,
  `order_no`      VARCHAR(32)   NOT NULL,
  `pay_no`        VARCHAR(32)   NOT NULL,
  `refund_amount` DECIMAL(12,2) NOT NULL,
  `reason`        VARCHAR(255)  DEFAULT NULL,
  `status`        TINYINT       NOT NULL DEFAULT 0     COMMENT '0待审核 1已退款 2拒绝',
  `gmt_create`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_order` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款记录';

DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `branch_id` BIGINT NOT NULL,
  `xid` VARCHAR(100) NOT NULL,
  `context` VARCHAR(128) NOT NULL,
  `rollback_info` LONGBLOB NOT NULL,
  `log_status` INT NOT NULL,
  `log_created` DATETIME NOT NULL,
  `log_modified` DATETIME NOT NULL,
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 8. mall_seckill
-- ============================================================
USE `mall_seckill`;

DROP TABLE IF EXISTS `seckill_activity`;
CREATE TABLE `seckill_activity` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT,
  `name`            VARCHAR(128)  NOT NULL,
  `sku_id`          BIGINT        NOT NULL,
  `seckill_price`   DECIMAL(10,2) NOT NULL,
  `total_stock`     INT           NOT NULL,
  `limit_per_user`  INT           NOT NULL DEFAULT 1,
  `start_time`      DATETIME      NOT NULL,
  `end_time`        DATETIME      NOT NULL,
  `status`          TINYINT       NOT NULL DEFAULT 0  COMMENT '0未开始 1进行中 2已结束 3取消',
  `gmt_create`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀活动';

DROP TABLE IF EXISTS `seckill_order`;
CREATE TABLE `seckill_order` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `activity_id` BIGINT       NOT NULL,
  `user_id`     BIGINT       NOT NULL,
  `sku_id`      BIGINT       NOT NULL,
  `order_no`    VARCHAR(32)  DEFAULT NULL,
  `request_id`  VARCHAR(64)  NOT NULL,
  `status`      TINYINT      NOT NULL DEFAULT 0  COMMENT '0排队 1成功 2失败 3退款',
  `gmt_create`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_request` (`request_id`),
  UNIQUE KEY `uk_act_user` (`activity_id`, `user_id`),
  KEY `idx_order` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀订单';

-- Seata undo_log（秒杀下单可能走全局事务）
DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log` (
  `id`            BIGINT       AUTO_INCREMENT PRIMARY KEY,
  `branch_id`     BIGINT       NOT NULL,
  `xid`           VARCHAR(100) NOT NULL,
  `context`       VARCHAR(128) NOT NULL,
  `rollback_info` LONGBLOB     NOT NULL,
  `log_status`    INT          NOT NULL,
  `log_created`   DATETIME     NOT NULL,
  `log_modified`  DATETIME     NOT NULL,
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 9. Seata Server 2.0.0 库（5 张表）
-- ============================================================
USE `mall_seata`;

DROP TABLE IF EXISTS `global_table`;
CREATE TABLE `global_table` (
  `xid`                       VARCHAR(128) NOT NULL,
  `transaction_id`            BIGINT       DEFAULT NULL,
  `status`                    TINYINT      NOT NULL,
  `application_id`            VARCHAR(32)  DEFAULT NULL,
  `transaction_service_group` VARCHAR(32)  DEFAULT NULL,
  `transaction_name`          VARCHAR(128) DEFAULT NULL,
  `timeout`                   INT          DEFAULT NULL,
  `begin_time`                BIGINT       DEFAULT NULL,
  `application_data`          VARCHAR(2000)DEFAULT NULL,
  `gmt_create`                DATETIME     DEFAULT NULL,
  `gmt_modified`              DATETIME     DEFAULT NULL,
  PRIMARY KEY (`xid`),
  KEY `idx_status_gmt_modified` (`status`, `gmt_modified`),
  KEY `idx_transaction_id` (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `branch_table`;
CREATE TABLE `branch_table` (
  `branch_id`         BIGINT       NOT NULL,
  `xid`               VARCHAR(128) NOT NULL,
  `transaction_id`    BIGINT       DEFAULT NULL,
  `resource_group_id` VARCHAR(32)  DEFAULT NULL,
  `resource_id`       VARCHAR(256) DEFAULT NULL,
  `branch_type`       VARCHAR(8)   DEFAULT NULL,
  `status`            TINYINT      DEFAULT NULL,
  `client_id`         VARCHAR(64)  DEFAULT NULL,
  `application_data`  VARCHAR(2000)DEFAULT NULL,
  `gmt_create`        DATETIME(6)  DEFAULT NULL,
  `gmt_modified`      DATETIME(6)  DEFAULT NULL,
  PRIMARY KEY (`branch_id`),
  KEY `idx_xid` (`xid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `lock_table`;
CREATE TABLE `lock_table` (
  `row_key`        VARCHAR(128) NOT NULL,
  `xid`            VARCHAR(128) DEFAULT NULL,
  `transaction_id` BIGINT       DEFAULT NULL,
  `branch_id`      BIGINT       NOT NULL,
  `resource_id`    VARCHAR(256) DEFAULT NULL,
  `table_name`     VARCHAR(32)  DEFAULT NULL,
  `pk`             VARCHAR(36)  DEFAULT NULL,
  `status`         TINYINT      NOT NULL DEFAULT 0 COMMENT '0:locked, 1:rollbacking',
  `gmt_create`     DATETIME     DEFAULT NULL,
  `gmt_modified`   DATETIME     DEFAULT NULL,
  PRIMARY KEY (`row_key`),
  KEY `idx_status` (`status`),
  KEY `idx_branch_id` (`branch_id`),
  KEY `idx_xid` (`xid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `distributed_lock`;
CREATE TABLE `distributed_lock` (
  `lock_key`   CHAR(20)    NOT NULL,
  `lock_value` VARCHAR(20) NOT NULL,
  `expire`     BIGINT      DEFAULT NULL,
  PRIMARY KEY (`lock_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `distributed_lock` (lock_key, lock_value, expire) VALUES ('AsyncCommitting', ' ', 0);
INSERT INTO `distributed_lock` (lock_key, lock_value, expire) VALUES ('RetryCommitting', ' ', 0);
INSERT INTO `distributed_lock` (lock_key, lock_value, expire) VALUES ('RetryRollbacking', ' ', 0);
INSERT INTO `distributed_lock` (lock_key, lock_value, expire) VALUES ('TxTimeoutCheck', ' ', 0);

DROP TABLE IF EXISTS `vgroup_table`;
CREATE TABLE `vgroup_table` (
  `vGroup`   VARCHAR(255) DEFAULT NULL,
  `namespace` VARCHAR(255) DEFAULT NULL,
  `cluster`  VARCHAR(255) DEFAULT NULL,
  UNIQUE KEY `idx_vgroup_namespace_cluster` (`vGroup`, `namespace`, `cluster`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 10. 演示完成，可继续执行 seed.sql
-- ============================================================
