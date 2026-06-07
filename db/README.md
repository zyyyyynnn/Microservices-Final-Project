# MallCloud Database Scripts

## 当前基线

MallCloud 当前只支持基于 `db/init/` 的新环境初始化，不再维护旧数据库环境升级脚本。

## 目录说明

| 路径 | 用途 | 适用场景 |
|---|---|---|
| `db/init/00-create-databases.sql` | 创建数据库和表结构 | 新 MySQL 容器首次初始化 |
| `db/init/seed.sql` | 初始化演示数据 | 新 MySQL 容器首次初始化 |

## 执行规则

- 新环境使用 `db/init/`；
- MySQL Docker 首次初始化时自动执行 `db/init/` 下脚本；
- 不要在已有数据的数据库中重复执行 `seed.sql`；
- 如需重建演示数据，应备份后重建数据库容器。
