# Sentinel 秒杀活动限流验证摘要

| 项目 | 内容 |
|---|---|
| 执行时间 | 2026-06-10 |
| Dashboard | `http://localhost:8080/` 返回 HTTP 200 |
| 应用接入 | Dashboard `/app/briefinfos.json` 可见 `mall-gateway`、`mall-inventory`、`mall-message`、`mall-order`、`mall-seckill`，状态 healthy |
| 验证服务 | `mall-seckill` |
| 验证资源 | `/api/v1/seckill/activities` |
| 规则来源 | Sentinel 客户端 command center 临时运行时规则 |
| 临时规则 | flow rule，QPS 阈值 1，`controlBehavior=0` |
| 请求方式 | 通过 Gateway 并发请求 `GET /api/v1/seckill/activities` |
| 请求数量 | 12 |
| 结果 | HTTP 200：1 次；HTTP 429：11 次 |
| Sentinel 返回 | `Blocked by Sentinel (flow limiting)` |
| Sentinel 统计 | `oneMinuteBlock=11`，`oneMinutePass=2`，`blockQps=11` |
| 清理结果 | 已调用 `setRules?type=flow&data=[]` 清空临时运行时规则；`getRules?type=flow` 返回 `[]` |

该结果证明 `mall-seckill` Web 资源可以被 Sentinel 流控规则拦截，并且 Gateway 入口可观察到 HTTP 429。该验证不等同于 Sentinel 熔断验证，也不证明 Nacos 持久化规则已加载；熔断和 Nacos 规则热加载仍需单独验证。
