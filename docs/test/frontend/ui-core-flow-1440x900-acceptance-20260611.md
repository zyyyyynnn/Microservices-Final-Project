# MallCloud 核心购物链路 UI 截图验收报告

## 1. 验收基线
- Commit: 440a546 (补充验证 6a19962)
- Frontend: 5173
- Gateway: 9100
- 用户账号: zhangsan
- 管理员账号: admin
- 数据证据: docs/test/business/db-state-20260611.txt
- Newman 证据: docs/test/business/newman/newman-20260611.txt

## 2. 截图清单
| 序号 | 页面 | 文件 | 状态 | 备注 |
|---:|---|---|---|---|
| 1 | 登录 | 01-login-success-1440x900.png | 通过 | 正常登录 |
| 2 | 首页商品 | 02-home-products-1440x900.png | 通过 | 数据正常加载 |
| 3 | 搜索 | 03-search-iphone-1440x900.png | 通过 | 成功搜出商品 |
| 4 | 商品详情 | 04-product-1001-1440x900.png | 通过 | 信息展示正常 |
| 5 | 加入购物车 | 05-cart-with-item-1440x900.png | 通过 | 商品成功加入购物车并展示 |
| 6 | 结算 | 06-checkout-1440x900.png | 通过 | 正确展示地址、商品金额 |
| 7 | 支付单 | 07-pay-order-1440x900.png | 通过 | 成功发起支付 |
| 8 | 订单详情 | 08-order-detail-1440x900.png | 通过 | 订单详情展示正常 |
| 9 | 账户订单 | 09-account-orders-1440x900.png | 通过 | 用户订单列表非空且正确加载 |
| 11 | 后台看板 | 11-admin-dashboard-1440x900.png | 通过 | 数据正常 |
| 12 | 后台列表 | 12-admin-orders-products-1440x900.png | 通过 | 订单和商品数据展示正常 |

## 3. 普通用户链路
| 步骤 | 页面 | 截图 | 结果 | 备注 |
|---:|---|---|---|---|
| 1 | 登录页 | [01-login-success-1440x900.png](screenshots/20260611-core-flow-1440x900/01-login-success-1440x900.png) | 通过 | |
| 2 | 首页商品态 | [02-home-products-1440x900.png](screenshots/20260611-core-flow-1440x900/02-home-products-1440x900.png) | 通过 | |
| 3 | 搜索 iPhone | [03-search-iphone-1440x900.png](screenshots/20260611-core-flow-1440x900/03-search-iphone-1440x900.png) | 通过 | |
| 4 | 商品详情 1001 | [04-product-1001-1440x900.png](screenshots/20260611-core-flow-1440x900/04-product-1001-1440x900.png) | 通过 | |
| 5 | 购物车 | [05-cart-with-item-1440x900.png](screenshots/20260611-core-flow-1440x900/05-cart-with-item-1440x900.png) | 通过 | |
| 6 | 结算页 | [06-checkout-1440x900.png](screenshots/20260611-core-flow-1440x900/06-checkout-1440x900.png) | 通过 | |
| 7 | 支付单 | [07-pay-order-1440x900.png](screenshots/20260611-core-flow-1440x900/07-pay-order-1440x900.png) | 通过 | |
| 8 | 订单详情 | [08-order-detail-1440x900.png](screenshots/20260611-core-flow-1440x900/08-order-detail-1440x900.png) | 通过 | |
| 9 | 账户订单 | [09-account-orders-1440x900.png](screenshots/20260611-core-flow-1440x900/09-account-orders-1440x900.png) | 通过 | |

**中断记录：**
库存字段断层已在 6a19962 修复，本轮完成补拍。

## 4. 管理员链路
| 步骤 | 页面 | 截图 | 结果 | 备注 |
|---:|---|---|---|---|
| 11 | 登录 admin 后进入后台 | [11-admin-dashboard-1440x900.png](screenshots/20260611-core-flow-1440x900/11-admin-dashboard-1440x900.png) | 通过 | |
| 12 | 后台订单或商品列表 | [12-admin-orders-products-1440x900.png](screenshots/20260611-core-flow-1440x900/12-admin-orders-products-1440x900.png) | 通过 | 页面展示了存在的订单和商品 |

## 5. 未截图范围
- 秒杀成功态：因 zhangsan 已触发重复限购，本轮不截图。
- 外部控制台：不属于核心购物链路截图范围。

## 6. 结论
核心购物链路 UI 截图有条件通过：登录、首页、搜索、商品详情、购物车、结算、支付/订单、账户订单、后台页面均已覆盖；秒杀成功态不纳入本轮截图范围。
