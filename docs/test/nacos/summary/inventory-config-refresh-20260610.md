# Nacos 普通业务配置热更新验证摘要

| 项目 | 内容 |
|---|---|
| 执行时间 | 2026-06-10 |
| 验证目标 | `mall-inventory` 普通业务配置通过 Nacos 热更新生效，无需重启服务 |
| 命名空间 | `dev` |
| Group | `DEFAULT_GROUP` |
| DataId | `mall-inventory.yaml` |
| 配置项 | `mallcloud.inventory.ping-message` |
| 业务入口 | `GET /api/v1/inventory/ping` |
| 实现方式 | `InventoryController` 使用 `@RefreshScope` 和 `${mallcloud.inventory.ping-message:mall-inventory pong}` |
| 修改前结果 | `mall-inventory pong` |
| 热更新配置 | `mall-inventory nacos hot update` |
| 热更新后结果 | `mall-inventory nacos hot update` |
| 是否重启 | 否 |
| 回滚结果 | 已恢复仓库基线 `mall-inventory pong` |
| Gateway 验证 | 回滚后通过 Gateway `GET /api/v1/inventory/ping` 返回 `mall-inventory pong` |

该结果证明 `mall-inventory.yaml` 中的普通业务配置可以通过 Nacos 热更新影响运行中服务。验证结束后已回滚到仓库基线配置。
