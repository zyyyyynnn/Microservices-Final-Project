# Sentinel 库存慢调用比例熔断验证摘要

| 项目 | 内容 |
|---|---|
| 执行时间 | 2026-06-10 |
| 验证目标 | `mall-inventory` 从 Nacos `SENTINEL_GROUP / mall-inventory-degrade-rules.json` 加载降级规则，并通过慢调用比例规则触发熔断 |
| 配置修正 | `mall-inventory-degrade-rules.json` 不再作为 Spring Config extension 加载，仅作为 Sentinel datasource；datasource 指定 `namespace=dev`、`data-type=json` |
| Nacos 配置发布 | `mall-inventory-degrade-rules.json` 通过 Nacos config API 发布到 `dev` 命名空间 |
| 启动验证 | 重启 `mall-inventory` 后，日志显示订阅 `mall-inventory-degrade-rules.json` 且 `notify-ok`；`getRules?type=degrade` 返回 `/api/v1/inventory/stock/{skuId}` |
| 正常态验证 | `GET /api/v1/inventory/ping` 返回 HTTP 200；Gateway `GET /api/v1/inventory/stock/9001` 返回 HTTP 200 |
| 热更新验证 | 临时将同一 DataId 发布为慢调用比例规则：`count=1ms`、`minRequestAmount=2`、`slowRatioThreshold=0.5`、`timeWindow=10s` |
| 请求方式 | 登录后通过 Gateway 请求 `GET /api/v1/inventory/stock/9001` |
| 请求数量 | 30 |
| 熔断结果 | HTTP 200：2 次；HTTP 429：28 次 |
| Sentinel 返回 | `Blocked by Sentinel (flow limiting)` |
| Sentinel 统计 | `/api/v1/inventory/stock/{skuId}`：`oneMinuteBlock=28`，`blockQps=21`；`oneMinutePass=4` 包含本次验证前同一分钟内的正常探测请求 |
| 回滚结果 | 已恢复仓库保守规则；最终 `getRules?type=degrade` 返回 `/api/v1/inventory/stock/{skuId}`，`count=1000`；回滚后 Gateway 库存查询返回 HTTP 200 |

该结果证明 `mall-inventory` 的 Sentinel Nacos datasource 能从 `dev / SENTINEL_GROUP / mall-inventory-degrade-rules.json` 加载降级规则，且慢调用比例熔断能通过 Gateway 被观察为 HTTP 429。该验证使用临时低阈值规则触发熔断，不代表正式性能容量或最终生产阈值。
