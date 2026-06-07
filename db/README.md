# MallCloud Database Scripts

## 目录说明

| 路径 | 用途 | 适用场景 |
|---|---|---|
| `db/init/00-create-databases.sql` | 创建数据库和表结构 | 新 MySQL 容器首次初始化 |
| `db/init/seed.sql` | 初始化演示数据 | 新 MySQL 容器首次初始化 |
| `db/migration/20260607-upgrade-seata-server-2.0.sql` | 将已有环境的 Seata Server 表结构升级到 2.0.0 官方表 | 已初始化过的开发库 |
| `db/migration/20260607-add-auth-role.sql` | 为已有环境的认证表补充可信角色字段 | 已初始化过的开发库 |

## 执行规则

- 新环境只依赖 `db/init/`；
- 已有开发环境按需要执行 `db/migration/`；
- 不要重复执行 `seed.sql`；
- 执行迁移前必须确认目标库和数据备份；
- 不得删除带保护逻辑的迁移脚本，除非所有文档引用已同步移除且团队确认不再支持旧环境升级。
