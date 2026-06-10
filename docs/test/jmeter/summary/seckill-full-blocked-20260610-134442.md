# JMeter 秒杀 10 用户完整链路阻断记录

| 项目 | 内容 |
|---|---|
| 执行时间 | 2026-06-10 13:44 |
| 前置数据 | `pwsh .\scripts\prepare-seckill-jmeter.ps1 -ActivityId 9001 -SkuId 9003 -TotalStock 100 -UserCount 10 -MysqlPassword 123456` |
| 命令 | `pwsh .\scripts\run-jmeter.ps1 -Scenario seckill -Users 10 -RampUp 2 -Loops 1 -ActivityId 9001 -SkuId 9003 -UsernamePrefix jmeter_seckill_` |
| 场景 | `docs/test/jmeter/seckill-stress.jmx` |
| 样本数 | 104 |
| 失败数 | 18 |
| 失败采样器 | `GET /api/v1/seckill/result/{requestId}` 9 次；`Validate final seckill result` 9 次 |
| 登录请求 | 10 次，0 失败 |
| 秒杀受理请求 | 10 次，0 失败 |
| 结果查询请求 | 74 次，9 失败 |
| 最终结果校验 | 10 次，9 失败 |
| 原始 JTL | `docs/test/jmeter/results/seckill-10-20260610-134442.jtl`，本地生成，未纳入仓库 |

最终状态核验：

| 项目 | 结果 |
|---|---|
| `seckill_order` 最终成功数 | 1 |
| `seckill_order` 最终失败数 | 9 |
| `orderNo` 非空记录数 | 1 |
| 去重用户数 | 10 |
| 活动总库存 | 100 |
| Redis 剩余库存 `seckill:stock:9001` | 90 |

结论：10 用户并发下，请求受理成功，但异步创建订单和结果回写未全部成功。该结果不得写成秒杀完整链路通过，也不得进入 50/100/200/300/500 阶梯压力结论。
