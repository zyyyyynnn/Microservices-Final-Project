-- ============================================================
-- MallCloud Seata Server 2.0.0 升级迁移脚本
-- ============================================================
-- 仅用于本地开发/课程演示环境。
-- 执行前必须停止 Seata Server。
-- 本脚本包含自动安全检查：若 Server 事务表存在数据，将中止执行。
--
-- 使用方式（PowerShell）：
--   Get-Content .\db\migration\20260607-upgrade-seata-server-2.0.sql `
--     -Raw -Encoding UTF8 |
--     docker exec -i mall-mysql mysql -uroot -proot
--
--   if ($LASTEXITCODE -ne 0) {
--       throw 'Seata Server Schema 迁移失败，数据库未通过安全检查。'
--   }
-- ============================================================

USE `mall_seata`;

-- ============================================================
-- 步骤 1：自动安全检查 — 事务表必须为空
-- ============================================================

DELIMITER $$

DROP PROCEDURE IF EXISTS `assert_seata_server_tables_empty`$$

CREATE PROCEDURE `assert_seata_server_tables_empty`()
BEGIN
  DECLARE transaction_count BIGINT DEFAULT 0;

  SELECT
      (SELECT COUNT(*) FROM `global_table`)
    + (SELECT COUNT(*) FROM `branch_table`)
    + (SELECT COUNT(*) FROM `lock_table`)
  INTO transaction_count;

  IF transaction_count > 0 THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT =
        'Migration aborted: Seata Server transaction tables are not empty';
  END IF;
END$$

DELIMITER ;

CALL `assert_seata_server_tables_empty`();
DROP PROCEDURE IF EXISTS `assert_seata_server_tables_empty`;

-- ============================================================
-- 步骤 2：删除旧表（含 vgroup_table，Seata 2.0.0 不需要）
-- ============================================================

DROP TABLE IF EXISTS `vgroup_table`;
DROP TABLE IF EXISTS `distributed_lock`;
DROP TABLE IF EXISTS `lock_table`;
DROP TABLE IF EXISTS `branch_table`;
DROP TABLE IF EXISTS `global_table`;

-- ============================================================
-- 步骤 3：创建 Seata 2.0.0 官方四张 Server 表
-- ============================================================

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

-- ============================================================
-- 步骤 4：验证结果
-- ============================================================
SHOW TABLES;
SELECT `lock_key`, `lock_value`, `expire`
FROM `distributed_lock`
ORDER BY `lock_key`;
