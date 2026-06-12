# Sprint 3.2 截图索引

> 收集批次：Sprint 3.2 full profile 启动 + /pay + /admin 验收
> 提交 Commit（最近一次提交）：640f1a8eadf3ba65a5d86cb78eef910cfef21b3d
> 全部截图均通过 Gateway `http://localhost:9100/api/v1/**` 或 `http://localhost:5173` 前端 dev 真实访问生成；未伪造、未补图。
> 自动化工具：browser_navigate + browser_vision 用于 1440x900；Playwright Chromium 1.x (headless) 用于 390x844。

| 文件 | 页面 | 路由 | 视口 | 账号 | 状态 | 说明 |
|---|---|---|---|---|---|---|
| 01-pay-order-detail-before-1440x900.png | 支付前订单详情 | `/orders/SO1781249605871` | 1440x900 | zhangsan | 通过 | 本轮新建订单：iPhone 15 128G 粉色 (sku 9002) × 1，¥5999.00，状态"待支付"，去支付按钮可用 |
| 02-pay-page-1440x900.png | 支付收银台（未创建支付单） | `/pay/SO1781249605871` | 1440x900 | zhangsan | 通过 | 待支付金额 ¥5999.00，订单号 SO1781249605871，支付渠道 ALIPAY，当前状态"待支付"，"创建支付记录"按钮可见 |
| 03-pay-order-detail-after-1440x900.png | 支付后订单详情 | `/orders/SO1781249605871` | 1440x900 | zhangsan | 通过 | 同一订单，状态变为"已支付"（API 验证 status 0→1）；其余字段保持 |
| 04-pay-page-success-390x844.png | 支付成功态（mobile） | `/pay/SO1781249605871` | 390x844 | zhangsan | 通过 | 订单已支付，pay status===1 触发成功态：绿勾 + "支付成功" + "感谢您的购买，订单已支付完成" + 查看订单明细/返回首页按钮；本截图反映已支付订单在 pay 路由的最终渲染（非错误态，是按 `field(payRecord, ['status']) === 1` 的成功分支） |
| 05-admin-dashboard-1440x900.png | 后台看板 | `/admin` | 1440x900 | admin | 通过 | 3 张指标卡（订单数 159 / 商品数 12 / 销售额"—"）+ 后台订单表 11 行（含本轮新建 SO1781249605871 ¥5999.00 已支付 + SO1781249435421 ¥8999.00 已支付 + Sprint 2 历史 SO1781241179114 待支付）+ 后台商品表 6 行（小米台灯 / iPhone 15 Pro / iPhone 15 / 小米 14 Pro / 华为 Mate 60 / 罗技 MX Master） |
| 06-admin-dashboard-390x844.png | 后台看板（mobile） | `/admin` | 390x844 | admin | 通过（首屏） | header + 订单数 159 + 商品数 12，第三张指标卡（销售额）位于折叠线下方；总文档宽度 390px 无横向溢出 |

## 关键链路证据

- **本轮新建订单**：`SO1781249605871`（sku 9002 iPhone 15 128G 粉色，¥5999.00，remark="sprint3.2-screenshot"）
- **支付链路完整**：创建支付单（`payNo=PAY2026061220653374`，`payUrl=https://openapi-sandbox.dl.alipaydev.com/...`）→ `/pay/notify` 模拟通知 → 订单状态 0→1 真实变化
- **admin 登录**：`userId=1007, roles=["ADMIN"]`（`accessToken` 已注入 localStorage）
- **admin 看板**：`/api/v1/admin/dashboard` 返回 `code=200`，含 `todayOrders=3, todaySales=17998.00, totalProducts=12, pendingOrders=17, salesTrend, topProducts`

## 截图生成方法

- **1440x900**：使用 `browser_navigate` + `browser_vision`（与 Sprint 2 Closeout 一致）
- **390x844**：使用 Python 3.13 + Playwright 1.x + Chromium headless，device_scale_factor=1，viewport 严格 390x844，通过 `add_init_script` 注入 admin/zhangsan 凭据到 `localStorage`
- 鉴权凭据文件位于 `.runtime/sprint3.2-tokens.txt`（gitignored，临时产物，提交时不存在）

## 已知缺陷（不修，归类 Sprint 3.3 候选）

1. **admin 看板 "销售额" 显示"—"**：`/api/v1/admin/dashboard` 返回 `todaySales=17998.00`，但 AdminView.vue 的 `moneyText(field(data, ['totalSales', 'todaySales'], 0))` 取 `totalSales`（未返回），`todaySales` 字段被忽略，因此指标卡显示"—"。属于后端响应字段命名与前端期望不一致，前端兜底逻辑工作正常。
2. **订单详情页"收货信息"显示"—"**：与 Sprint 2/3.1 一致，后端 `mall-order` 写入 `addressJson={"addressId":1}` 不含完整地址对象。
3. **mall-order `/actuator/health` 500**：`GlobalExceptionHandler` 把 `NoResourceFoundException` 视为 SystemError；业务接口 200。这是 Sprint 2 已记录的后端 actuator 缺失问题。
