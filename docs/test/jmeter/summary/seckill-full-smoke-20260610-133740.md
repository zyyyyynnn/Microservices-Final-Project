# JMeter 秒杀完整链路短冒烟摘要

| 项目 | 内容 |
|---|---|
| 执行时间 | 2026-06-10 13:37 |
| 前置数据 | `pwsh .\scripts\prepare-seckill-jmeter.ps1 -ActivityId 9001 -SkuId 9003 -TotalStock 100 -UserCount 1 -MysqlPassword 123456` |
| 命令 | `pwsh .\scripts\run-jmeter.ps1 -Scenario seckill -Users 1 -RampUp 1 -Loops 1 -ActivityId 9001 -SkuId 9003 -UsernamePrefix jmeter_seckill_` |
| 场景 | `docs/test/jmeter/seckill-stress.jmx` |
| 活动 | `9001`，JMeter 专用活动 |
| SKU | `9003` |
| 用户数 | 1 |
| 样本数 | 10 |
| 失败数 | 0 |
| 请求分布 | 登录 1 次；发起秒杀 1 次；查询结果 7 次；最终结果校验 1 次 |
| 平均 RT | 377ms |
| P95 | 2200ms |
| 吞吐量 | 1.31/s |
| 错误率 | 0% |
| 原始 JTL | `docs/test/jmeter/results/seckill-1-20260610-133740.jtl`，本地生成，未纳入仓库 |
| 脱敏聚合指标 | `docs/test/jmeter/summary/aggregate-20260610.csv` |

最终状态核验：

| 项目 | 结果 |
|---|---|
| `seckill_order` 最终成功数 | 1 |
| `seckill_order` 最终失败数 | 0 |
| `orderNo` 非空记录数 | 1 |
| 去重用户数 | 1 |
| 活动总库存 | 100 |
| Redis 剩余库存 `seckill:stock:9001` | 99 |

该结果仅证明 1 用户秒杀完整链路短冒烟通过，包括请求受理、异步结果轮询、最终 `status=1` 和 `orderNo` 非空校验；不替代 10 用户以上并发验证或 50/100/200/300/500 阶梯压力测试。
