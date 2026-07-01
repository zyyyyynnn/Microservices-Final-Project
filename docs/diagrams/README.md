# 图表交付区

Mermaid 源文件位于 `mmd/`，导出图位于 `svg/` 和 `png/`。图表只描述当前 MallCloud 已实现或已验证的课程项目能力。

| 序号 | 图表 | Mermaid | SVG | PNG | 口径 |
| --- | --- | --- | --- | --- | --- |
| 1 | 系统总体架构图 | [mmd/01-system-architecture.mmd](mmd/01-system-architecture.mmd) | [svg/01-system-architecture.svg](svg/01-system-architecture.svg) | [png/01-system-architecture.png](png/01-system-architecture.png) | Vue 经 Gateway 访问 13 个后端服务和中间件 |
| 2 | 核心交易链路图 | [mmd/02-trade-flow.mmd](mmd/02-trade-flow.mmd) | [svg/02-trade-flow.svg](svg/02-trade-flow.svg) | [png/02-trade-flow.png](png/02-trade-flow.png) | 普通购物、下单、支付消息闭环 |
| 3 | Gateway 鉴权与内部路径防护图 | [mmd/03-gateway-security.mmd](mmd/03-gateway-security.mmd) | [svg/03-gateway-security.svg](svg/03-gateway-security.svg) | [png/03-gateway-security.png](png/03-gateway-security.png) | JWT、internal 阻断、请求头净化 |
| 4 | Seata 订单-库存一致性图 | [mmd/04-seata-order-inventory.mmd](mmd/04-seata-order-inventory.mmd) | [svg/04-seata-order-inventory.svg](svg/04-seata-order-inventory.svg) | [png/04-seata-order-inventory.png](png/04-seata-order-inventory.png) | 订单创建与库存锁定一致性 |
| 5 | RocketMQ 支付结果处理图 | [mmd/05-rocketmq-pay-result.mmd](mmd/05-rocketmq-pay-result.mmd) | [svg/05-rocketmq-pay-result.svg](svg/05-rocketmq-pay-result.svg) | [png/05-rocketmq-pay-result.png](png/05-rocketmq-pay-result.png) | 支付结果异步消费 |
| 6 | 秒杀请求处理与限流图 | [mmd/06-seckill-rate-limit.mmd](mmd/06-seckill-rate-limit.mmd) | [svg/06-seckill-rate-limit.svg](svg/06-seckill-rate-limit.svg) | [png/06-seckill-rate-limit.png](png/06-seckill-rate-limit.png) | 秒杀活动、Redis 状态、限流和结果轮询 |
| 7 | 本地部署拓扑图 | [mmd/07-local-deployment.mmd](mmd/07-local-deployment.mmd) | [svg/07-local-deployment.svg](svg/07-local-deployment.svg) | [png/07-local-deployment.png](png/07-local-deployment.png) | Windows/PowerShell、本地 JAR、Docker 中间件 |

## 导出命令

```powershell
foreach ($f in Get-ChildItem .\docs\diagrams\mmd\*.mmd) {
  mmdc -i $f.FullName -o (Join-Path .\docs\diagrams\svg ($f.BaseName + '.svg'))
  mmdc -i $f.FullName -o (Join-Path .\docs\diagrams\png ($f.BaseName + '.png'))
}
```
