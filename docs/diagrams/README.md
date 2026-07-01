# 图表交付区

Mermaid 源文件位于 `mmd/`，导出图位于 `svg/` 和 `png/`。图表只描述当前 MallCloud 已实现或已验证的课程项目能力。

本轮已重画 7 张图表，并统一使用 `mermaid-config.json` 与 `theme.css`。PNG 均为高清导出，宽度全部大于 2400px；SVG/PNG 已通过尺寸检查、链接检查和人工可读性复核。人工复核重点包括：无 Mermaid 默认紫色样式、无明显线条交叉、标题底部无裁切、PNG 不模糊。

| 序号 | 图表 | Mermaid | SVG | PNG | PNG 尺寸 | 口径 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 系统总体架构图 | [mmd/01-system-architecture.mmd](mmd/01-system-architecture.mmd) | [svg/01-system-architecture.svg](svg/01-system-architecture.svg) | [png/01-system-architecture.png](png/01-system-architecture.png) | 2979×2946 | Vue 经 Gateway 访问 13 个后端服务和中间件 |
| 2 | 核心交易链路图 | [mmd/02-trade-flow.mmd](mmd/02-trade-flow.mmd) | [svg/02-trade-flow.svg](svg/02-trade-flow.svg) | [png/02-trade-flow.png](png/02-trade-flow.png) | 4011×1542 | 普通购物、下单、支付消息闭环 |
| 3 | Gateway 鉴权与内部路径防护图 | [mmd/03-gateway-security.mmd](mmd/03-gateway-security.mmd) | [svg/03-gateway-security.svg](svg/03-gateway-security.svg) | [png/03-gateway-security.png](png/03-gateway-security.png) | 3250×1010 | JWT、internal 阻断、请求头净化 |
| 4 | Seata 订单-库存一致性图 | [mmd/04-seata-order-inventory.mmd](mmd/04-seata-order-inventory.mmd) | [svg/04-seata-order-inventory.svg](svg/04-seata-order-inventory.svg) | [png/04-seata-order-inventory.png](png/04-seata-order-inventory.png) | 2702×2026 | 订单创建与库存锁定一致性 |
| 5 | RocketMQ 支付结果处理图 | [mmd/05-rocketmq-pay-result.mmd](mmd/05-rocketmq-pay-result.mmd) | [svg/05-rocketmq-pay-result.svg](svg/05-rocketmq-pay-result.svg) | [png/05-rocketmq-pay-result.png](png/05-rocketmq-pay-result.png) | 3756×2040 | 支付结果异步消费 |
| 6 | 秒杀请求处理与限流图 | [mmd/06-seckill-rate-limit.mmd](mmd/06-seckill-rate-limit.mmd) | [svg/06-seckill-rate-limit.svg](svg/06-seckill-rate-limit.svg) | [png/06-seckill-rate-limit.png](png/06-seckill-rate-limit.png) | 3252×1550 | 秒杀活动、Redis 状态、限流和结果轮询 |
| 7 | 本地部署拓扑图 | [mmd/07-local-deployment.mmd](mmd/07-local-deployment.mmd) | [svg/07-local-deployment.svg](svg/07-local-deployment.svg) | [png/07-local-deployment.png](png/07-local-deployment.png) | 3156×1904 | Windows/PowerShell、本地 JAR、Docker 中间件 |

## 视觉规范

- 背景：浅蓝灰 `#f8fafc`。
- 主色：低饱和雾霾蓝 `#4b7099`。
- 辅色：低饱和青绿 `#4f7a74`。
- 文本：深蓝灰 `#1e293b` / `#334155`。
- 字体：`Microsoft YaHei`、`Noto Sans CJK SC`、`PingFang SC`、`system-ui`。
- 分区标题固定为 17px / 1.22 行高，避免 Mermaid `foreignObject` 标题高度不足导致尾部裁切。

## 导出命令

```powershell
$config = Resolve-Path .\docs\diagrams\mermaid-config.json
$css = Resolve-Path .\docs\diagrams\theme.css
$scales = @{
  '01-system-architecture' = 3
  '02-trade-flow' = 3
  '03-gateway-security' = 2
  '04-seata-order-inventory' = 2
  '05-rocketmq-pay-result' = 3
  '06-seckill-rate-limit' = 2
  '07-local-deployment' = 2
}

foreach ($f in Get-ChildItem .\docs\diagrams\mmd\*.mmd) {
  $svg = Join-Path .\docs\diagrams\svg ($f.BaseName + '.svg')
  $png = Join-Path .\docs\diagrams\png ($f.BaseName + '.png')
  mmdc -i $f.FullName -o $svg -c $config -C $css -b '#f8fafc' -w 1800 -H 1200 -q
  mmdc -i $f.FullName -o $png -c $config -C $css -b '#f8fafc' -w 1800 -H 1200 -s $scales[$f.BaseName] -q
}
```

## 质量验收

```powershell
pwsh .\docs\diagrams\verify-diagrams.ps1
```

验收结果：

| 项目 | 结果 |
| --- | --- |
| Mermaid 源文件 | 7/7 |
| SVG 导出 | 7/7 |
| PNG 导出 | 7/7 |
| PNG 宽度 | 全部 ≥ 2400px |
| PNG 文件大小 | 全部合理 |
| README 本地链接 | 无缺失 |
| 标题裁切防护 | PASS |
| 人工可读性复核 | PASS，含 01 层级标题统一、层间距统一、标题底部无裁切 |
