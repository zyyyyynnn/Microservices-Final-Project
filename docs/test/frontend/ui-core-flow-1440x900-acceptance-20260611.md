# MallCloud 核心购物链路 UI 截图验收报告

## 1. 验收基线
- Commit: 440a546
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
| 4 | 商品详情 | 04-product-1001-1440x900.png | 通过 | 信息展示正常，但提示库存不足 |
| 5 | 加入购物车 | - | 阻断 | 前端 SKU 库存判定为 0 导致按钮禁用 |
| 6 | 结算 | - | 未执行 | 依赖前置链路 |
| 7 | 支付单 | - | 未执行 | 依赖前置链路 |
| 8 | 支付成功 | - | 未执行 | 依赖前置链路 |
| 9 | 订单详情 | - | 未执行 | 依赖前置链路 |
| 10 | 账户订单 | - | 未执行 | 依赖前置链路 |
| 11 | 后台看板 | 11-admin-dashboard-1440x900.png | 通过 | 数据正常 |
| 12 | 后台列表 | 12-admin-orders-products-1440x900.png | 通过 | 订单和商品数据展示正常 |

## 3. 普通用户链路
| 步骤 | 页面 | 截图 | 结果 | 备注 |
|---:|---|---|---|---|
| 1 | 登录页 | [01-login-success-1440x900.png](screenshots/20260611-core-flow-1440x900/01-login-success-1440x900.png) | 通过 | |
| 2 | 首页商品态 | [02-home-products-1440x900.png](screenshots/20260611-core-flow-1440x900/02-home-products-1440x900.png) | 通过 | |
| 3 | 搜索 iPhone | [03-search-iphone-1440x900.png](screenshots/20260611-core-flow-1440x900/03-search-iphone-1440x900.png) | 通过 | |
| 4 | 商品详情 1001 | [04-product-1001-1440x900.png](screenshots/20260611-core-flow-1440x900/04-product-1001-1440x900.png) | 通过 | 页面加载完成，但 UI 状态阻断了后续操作 |

**中断记录：**
- 页面：商品详情页 /products/1001
- 原因：前端页面提示 "当前 SKU 库存不足或库存字段未返回，购买操作已禁用。"，加入购物车按钮置灰，无法继续。
- 接口状态：API 本身有库存数据（见 Newman 证据），但前端数据渲染处理中库存字段取值失败（或缺省）。
- 是否登录：是 (zhangsan)
- 是否有数据：有基本商品数据，但库存展示为 0

## 4. 管理员链路
| 步骤 | 页面 | 截图 | 结果 | 备注 |
|---:|---|---|---|---|
| 11 | 登录 admin 后进入后台 | [11-admin-dashboard-1440x900.png](screenshots/20260611-core-flow-1440x900/11-admin-dashboard-1440x900.png) | 通过 | |
| 12 | 后台订单或商品列表 | [12-admin-orders-products-1440x900.png](screenshots/20260611-core-flow-1440x900/12-admin-orders-products-1440x900.png) | 通过 | 页面展示了存在的订单和商品 |

## 5. 未截图范围
- 秒杀成功态：因 zhangsan 已触发重复限购，本轮不截图。
- 外部控制台：不属于核心购物链路截图范围。
- 购物车及后续购物链路：因商品详情页 UI 状态阻断操作（库存字段前端判定异常）未生成相应截图。

## 6. 结论
有条件通过

核心购物链路前端页面 UI（登录、首页、搜索、商品、后台）截图通过；购物车与结算等剩余链路因为前端详情页库存字段取值异常导致被中断，未截取空状态；秒杀成功态截图未纳入本轮范围。
