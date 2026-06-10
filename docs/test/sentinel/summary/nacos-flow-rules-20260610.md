# Sentinel Nacos 持久化流控规则验证摘要

| 项目 | 内容 |
|---|---|
| 执行时间 | 2026-06-10 |
| 验证目标 | `mall-seckill` 从 Nacos `SENTINEL_GROUP / mall-seckill-flow-rules.json` 加载流控规则 |
| 配置修正 | `mall-seckill-flow-rules.json` 不再作为 Spring Config import 加载，仅作为 Sentinel datasource；datasource 指定 `namespace=dev`、`data-type=json` |
| Nacos 配置发布 | `common-sentinel.yaml`、`mall-seckill.yaml`、`mall-seckill-flow-rules.json` 通过 Nacos config API 发布到 `dev` 命名空间 |
| 启动验证 | 重启 `mall-seckill` 后，`getRules?type=flow` 返回 `/api/v1/seckill/{activityId}`，`count=500` |
| 热更新验证 | 临时将同一 DataId 发布为 `/api/v1/seckill/activities`，`count=1` |
| 规则加载结果 | `getRules?type=flow` 从 `count=500` 更新为 `count=1`，再恢复为 `count=500` |
| 请求方式 | 通过 Gateway 并发请求 `GET /api/v1/seckill/activities` |
| 请求数量 | 80 |
| 限流结果 | HTTP 200：1 次；HTTP 429：79 次 |
| Sentinel 返回 | `Blocked by Sentinel (flow limiting)` |
| Sentinel 统计 | `oneMinuteBlock=79`，`blockQps=79`，`passQps=1` |
| 回滚结果 | 已恢复仓库规则；最终 `getRules?type=flow` 返回 `/api/v1/seckill/{activityId}`，`count=500` |

该结果证明 `mall-seckill` 的 Sentinel Nacos datasource 能从 `dev / SENTINEL_GROUP / mall-seckill-flow-rules.json` 加载规则，且 Nacos 配置热更新和回滚能影响运行时流控规则。该验证仍不等同于熔断专项。
