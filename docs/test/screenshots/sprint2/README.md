# Sprint 2 截图索引

> 收集批次：Sprint 2 Retry + Sprint 2 Closeout
> 提交 Commit（最近一次提交）：ed7e05d94f4696fd567b7ceee4c5ef8971a0e709
> 全部截图均通过 Gateway `http://localhost:9100/api/v1/**` 或 `http://localhost:5173` 前端 dev 真实访问生成；未伪造、未补图。
> 自动化工具：Playwright Chromium 1.x (headless) 用于 390x844；browser_navigate + browser_vision 用于 1440x900。

| 文件 | 页面 | 路由 | 视口 | 账号 | 状态 | 说明 |
|---|---|---|---|---|---|---|
| 01-home-after-login-1440x900.png | 首页（已登录态） | `/` | 1440x900 | zhangsan | 通过 | 登录后跳转首页，header 显示"你好,"；商品列表 12 项可见 |
| 02-product-detail-1001-1440x900.png | 商品详情 | `/products/1001` | 1440x900 | zhangsan | 通过 | iPhone 15 Pro 256G 钛原色，价格 ¥8999.00，实时库存 96，加入购物车/立即结算按钮 |
| 03-cart-1440x900.png | 购物车 | `/cart` | 1440x900 | zhangsan | 通过 | 1 行 × 5 件 iPhone 15 Pro，单价 8999，小计 44995，去结算按钮可用 |
| 04-checkout-1440x900.png | 结算页 | `/checkout` | 1440x900 | zhangsan | 通过 | 2 条地址（默认 1 已选）、1 行订单项、备注输入框、提交订单按钮可用 |
| 05-order-detail-1440x900.png | 订单详情 | `/orders/SO1781241179114` | 1440x900 | zhangsan | 通过 | 订单号 SO1781241179114，状态"待支付"，实付金额 ¥8999.00，创建时间 2026-06-12T05:13:03，1 行 iPhone 15 Pro 256G × 1，金额小计 ¥8999.00；收货信息显示"—"（order.addressJson 仅保存 addressId，无完整地址对象，前端兜底为"—"） |
| 06-product-detail-390x844.png | 商品详情 | `/products/1001` | 390x844 | zhangsan | 通过（页面加载正确） / 视觉说明：移动端页面总高 1617px，844 视口仅显示 header + 图片起始位置；价格/SKU/按钮在折叠线下方 | URL = `/products/1001` 已验（Playwright 抓取 H1 = "iPhone 15 Pro 256G 钛原色"，含 ¥8999.00、"实时库存：96"、"加入购物车"） |
| 07-cart-390x844.png | 购物车 | `/cart` | 390x844 | zhangsan | 通过 | header + iPhone 15 Pro 行 + 数量 5 + 价格/小计在折叠下方；文档总宽 390px，无横向溢出 |
| 08-checkout-390x844.png | 结算页 | `/checkout` | 390x844 | zhangsan | 通过 | header + 2 条地址（默认已选）+ 订单项在折叠下方；文档总宽 390px，无横向溢出 |
| 09-order-detail-390x844.png | 订单详情 | `/orders/SO1781241179114` | 390x844 | zhangsan | 通过 | header + 订单号 SO178124117911[4] + 状态"待支付" + ¥8999.00 + 创建时间可见；收货信息"—" 同 05 说明 |

## 截图生成方法

- **1440x900**：使用 `browser_navigate` + `browser_vision`（与上一轮 Sprint 2 Retry 一致；本轮仅补 05）。
- **390x844**：使用 Python 3.13 + Playwright 1.x + Chromium headless，device_scale_factor=1，viewport 严格 390x844，截图前通过 `add_init_script` 注入 zhangsan 的 accessToken / refreshToken / user 到 `localStorage`，目标 URL 全部经 `http://localhost:5173/...`（Vite dev 5173 → Gateway 9100）。
- 鉴权凭据文件位于 `.runtime/sprint2-auth.json`（gitignored，临时产物，提交时不存在）。

## 已知缺陷（不修，归类 Sprint 3 候选）

1. `05` / `09` 订单详情页"收货信息"列显示"—"：后端 `mall-order` 写入的 `addressJson` 仅含 `{"addressId":1}`，不含完整地址字段；前端 `formatAddress` 解析不到 receiver/phone/detail 时兜底为"—"。该问题是后端写库字段不足，不属于前端"硬编码错误"。
2. `06` 移动端商品详情：文档高度 1617px，图片定位在 y=539px，导致 390x844 视口可见区域仅 header + 图片起始。这是产品详情页的 mobile UX 问题（image 距顶部留白过大），不阻塞交易，但需 UI 调优。
3. 首页商品卡 SPU 级"库存 0"：`mall-product` 接口返回的 SPU 数据中 `skus[0].stock` 静态为 0，依赖前端 `watch(selectedSkuId)` 调 `mallApi.inventoryStock` 实时刷新 SKU 库存。商品详情页内已实时显示"实时库存：96"，首页卡片的"库存 0"是数据回填深度不够。
