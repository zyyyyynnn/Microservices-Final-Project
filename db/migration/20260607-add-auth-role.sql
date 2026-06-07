-- MallCloud existing dev environment migration: persist trusted auth roles.
-- MySQL 8.0+, safe to execute repeatedly.

USE `mall_auth`;

SET @role_column_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sys_user_auth'
    AND COLUMN_NAME = 'role'
);

SET @role_ddl = IF(
  @role_column_exists = 0,
  'ALTER TABLE `sys_user_auth` ADD COLUMN `role` VARCHAR(16) NOT NULL DEFAULT ''USER'' COMMENT ''USER/MERCHANT/ADMIN'' AFTER `status`',
  'SELECT ''sys_user_auth.role already exists'' AS message'
);

PREPARE role_stmt FROM @role_ddl;
EXECUTE role_stmt;
DEALLOCATE PREPARE role_stmt;

UPDATE `sys_user_auth`
SET `role` = 'USER'
WHERE `role` NOT IN ('USER', 'MERCHANT', 'ADMIN');

UPDATE `sys_user_auth`
SET `role` = 'MERCHANT'
WHERE `user_id` IN (1005, 1006);

UPDATE `sys_user_auth`
SET `role` = 'ADMIN'
WHERE `user_id` = 1007;

SELECT `user_id`, `identity_type`, `identifier`, `role`
FROM `sys_user_auth`
ORDER BY `user_id`, `identity_type`;

SELECT `role`, COUNT(*) AS `count`
FROM `sys_user_auth`
GROUP BY `role`
ORDER BY `role`;
