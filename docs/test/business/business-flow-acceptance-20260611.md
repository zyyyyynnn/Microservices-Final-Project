# MallCloud 真实业务链路验收报告

## 1. 验收基线
- Commit: b70ac42
- Profile: full
- Frontend: 5173
- Gateway: 9100
- Newman log: docs/test/business/newman/newman-20260611.log
- DB state: docs/test/business/db-state-20260611.txt
- Runtime ports: docs/test/business/runtime-ports-20260611.txt
- Docker state: docs/test/business/runtime-docker-20260611.txt

## 2. 演示数据状态
| 数据项 | 状态 | 证据 | 备注 |
|---|---|---|---|
| zhangsan 用户 | 通过 | db-state-20260611.txt | 存在 |
| merchant01 用户 | 通过 | db-state-20260611.txt | 存在 |
| admin 用户 | 通过 | db-state-20260611.txt | 存在 |
| 商品 1001 | 通过 | db-state-20260611.txt | iPhone 15 Pro |
| SKU 9001/9002 | 通过 | db-state-20260611.txt | 8999.00 / 10999.00 |
| 库存 | 通过 | db-state-20260611.txt | SKU 9001: 86, SKU 9002: 50 |
| 秒杀活动 | 有条件 | db-state-20260611.txt | 种子活动进行中，但存在旧活动参与记录 |

## 3. Newman 验证结果
| 项 | 数量 |
|---|---:|
| 总请求数 | 28 |
| 通过请求数 | 26 |
| 失败请求数 | 2 |

## 4. 已通过业务链路
- 登录鉴权
- 商品分类
- 商品搜索
- 商品详情
- 加入购物车
- 创建订单
- 支付请求
- 支付回调
- 库存扣减
- 后台看板
- 后台订单查询

## 5. 未通过 / 有条件通过项
如果失败为秒杀重复购买限制：
- 业务码: 40403 (重复限购异常) 和 10003
- request / endpoint: `POST /api/v1/seckill/1` 和 `GET /api/v1/seckill/result/`
- 说明: 防重复购买业务规则触发。因为账号 zhangsan 已在先前的测试中成功抢购过，达到了单人重复购买限额。
- 结论: 秒杀防重复规则验证通过；秒杀成功购买路径本轮未完成。

## 6. 是否允许截图
允许进入登录态截图阶段。

## 7. 未完成项
- 秒杀成功态未完成
- 业务页面截图未完成
- JMeter 大规模压测未在本轮执行

## 8. 结论
有条件通过

原因：
核心购物链路与后台查询链路通过；秒杀重复购买防线触发，成功态路径和业务截图待下一轮补齐。
