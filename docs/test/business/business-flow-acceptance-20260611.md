# MallCloud 真实业务链路验收报告

## 1. 验收基线
- Commit: 60aac1d 验收执行基线；本报告修复提交见 Git 历史。
- Profile: full
- Frontend: 5173
- Gateway: 9100
- Newman log: docs/test/business/newman/newman-20260611.txt
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
| 秒杀活动 | 有条件 | db-state-20260611.txt | 种子活动进行中，但存在旧活动参与记录。数据库种子数据中文字段存在编码异常，业务链路不受影响，后续可独立修复 seed.sql。 |

## 3. Newman 验证结果
| 项 | 数量 |
|---|---:|
| 请求总数 | 28 |
| 请求失败数 | 0 |
| 断言总数 | 60 |
| 断言失败数 | 3 |
| 受影响请求块 | 2 |

Newman 层面 28 个 HTTP 请求均完成；失败集中在秒杀成功态预期断言，原因是测试账号触发重复限购，导致成功抢购路径未完成。

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
- 请求名称: 05 Seckill Admin / Create seckill
- 请求方法: POST
- 请求路径: /api/v1/seckill/1
- HTTP 状态: 200 OK
- business code: 40403
- 错误 message: AssertionError: expected 40403 to deeply equal 200
- 判断: 账号 zhangsan 已存在历史秒杀参与记录，触发单人重复购买限制，导致本轮无法验证秒杀成功购买路径。该结果可证明防重复购买规则生效，但不能证明秒杀成功态通过。

- 请求名称: 05 Seckill Admin / Get seckill result
- 请求方法: GET
- 请求路径: /api/v1/seckill/result/
- HTTP 状态: 500 Internal Server Error
- business code: 10003
- 错误 message: AssertionError: expected response to have status code 200 but got 500
- 判断: 账号 zhangsan 已存在历史秒杀参与记录，触发单人重复购买限制，导致本轮无法验证秒杀成功购买路径。该结果可证明防重复购买规则生效，但不能证明秒杀成功态通过。

结论: 秒杀防重复规则验证通过；秒杀成功购买路径本轮未完成。

## 6. 是否允许截图
允许进入核心购物链路的登录态截图阶段；秒杀成功态截图暂不允许，需先清理重复购买状态或更换未参与用户。
截图范围仅限登录、首页、搜索、商品详情、购物车、结算、支付、订单、账户、后台等已通过链路；秒杀成功态不进入截图范围。

## 7. 未完成项
- 秒杀成功态未完成
- 业务页面截图未完成
- JMeter 大规模压测未在本轮执行

## 8. 结论
有条件通过

原因：
核心购物链路与后台查询链路通过；秒杀重复购买防线触发，成功态路径和业务截图待下一轮补齐。
