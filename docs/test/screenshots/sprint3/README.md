# Sprint 3.2 / 3.3 截图索引

> 收集批次：Sprint 3.2 full profile 启动 + /pay + /admin 验收；Sprint 3.3 mall-order actuator / pay notify 统一响应 / AdminView 销售额字段修复
> 提交 Commit（最近一次提交）：本轮 Sprint 3.3 待提交（前置 9.5 提交 c1c18d6）
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
| 07-admin-dashboard-sales-fixed-1440x900.png | 后台看板销售额修复 | `/admin` | 1440x900 | admin | 通过 | Sprint 3.3 修复 AdminView 销售额字段回退后：3 张指标卡（订单数 160 / 商品数 12 / 销售额 **¥8999.00** —— 取自后端 `todaySales` 字段，含本轮新建订单 SO1781330545972 ¥8999.00 已支付）+ 后台订单表首行 SO1781330545972 + 历史订单若干；admin 登录 `userId=1007, roles=["ADMIN"]` |
| 08-order-address-snapshot-1440x900.png | 订单详情地址快照 | `/orders/SO1781333111756` | 1440x900 | zhangsan | 通过 | 新订单 addressJson 含 `receiver/phone/province/city/district/detail`；前端展示 `张三 / 13800138001 / 北京市北京市海淀区中关村大街1号院1号楼101` |
| 09-search-iphone-1440x900.png | 搜索成功态 | `/search?keyword=iPhone` | 1440x900 | 游客（搜索在 Gateway 白名单） | 通过 | iPhone 关键词返回 2 个真实商品（1001 iPhone 15 Pro 256G 钛原色 ¥8999 / 1002 iPhone 15 128G 粉色 ¥5999），含热词 chip |
| 10-search-empty-1440x900.png | 搜索空态 | `/search?keyword=zzzzz_nonexistent_xyz` | 1440x900 | 游客 | 通过 | `total=0`，展示空态 `暂无搜索结果 / 当前暂未找到可展示内容，请稍后重试或返回首页浏览。` + 翻页器禁用 |
| 11-seckill-list-1440x900.png | 秒杀活动列表 | `/seckill` | 1440x900 | zhangsan | 通过 | 4 个活动真实加载（id=9001/1/2/3），均显示"已结束"状态；详情面板含价格/库存/时间 |
| 12-seckill-action-1440x900.png | 秒杀动作结果 | `/seckill` | 1440x900 | zhangsan | 有条件通过 | 点击 iPhone 15 Pro 限时秒杀 + 发起秒杀，**真实业务码 code=40402 "秒杀已结束"**；后端 `validateActivity` 第 216 行因 endTime 已过抛 `BizException(40402)`；前端默认用 ElMessage toast 提示 3 秒后消失，本截图通过临时 banner 保留完整响应体 |
| 13-admin-dashboard-browser-verified-1440x900.png | 后台看板浏览器验收（**历史 full-page**） | `/admin` | **1254×9840 full-page**（文件名沿用视口命名约定） | admin | 🟡 历史命名误导 | Sprint 3.7 第一轮接力（§9.10 初版）真实浏览器访问 `/admin` 整页截图；`/api/v1/admin/dashboard` 返回 `code=200` 含 `todayOrders/totalProducts/todaySales/pendingOrders`；3 张指标卡读数与 API 一致；**视口宽度 1254** 是 Hermes 浏览器工具（1280 outerWidth）下 dev tools + 滚动条挤压（**非造假**）；文件名命名沿用 sprint3/ 视口命名约定但**实际尺寸不符**。本轮 Sprint 3.7 证据修正新增 14/15 号严格视口截图替代其作为验收主证据，13 号**保留为历史证据不删除** |
| **14-admin-dashboard-viewport-1440x900.png** | 后台看板严格视口（**本轮新增主证据**） | `/admin` | **1440×900 严格视口** | admin | ✅ 通过 | Sprint 3.7 第二轮接力（§9.10.5 证据修正）使用 Python 3.13 + Playwright 1.60 + Chromium headless，`viewport={"width":1440,"height":900}` + `full_page=False` 强制 viewport 截图；`page.wait_for_selector("text=今日订单数")` 等指标卡渲染后才截；`add_init_script` 注入 `mallcloud_access_token`（admin JWT）到 localStorage；`PIL.Image.open()` 验证实际尺寸 = **1440×900**（无 full-page）；3 张指标卡读数与 `/api/v1/admin/dashboard` API 一致（10 / 12 / ¥8999.00） |
| **15-admin-dashboard-mobile-390x844.png** | 后台看板 mobile 视口（**本轮新增主证据**） | `/admin` | **390×844 mobile 视口** | admin | ✅ 通过 | 同上，Playwright `is_mobile=True, has_touch=True, device_scale_factor=1` + `viewport={"width":390,"height":844}`；`PIL.Image.open()` 验证实际尺寸 = **390×844**（无 full-page）；mobile 单列布局；指标卡读数与 API 一致 |
| **18-orders-viewport-1440x900.png** | 订单详情 desktop 视口（**Sprint 3.8 新增**） | `/orders/SO1781616320768` | **1440×900 严格视口** | zhangsan | ✅ 通过 | Sprint 3.8 任务 A：Playwright `viewport=1440x900` + `full_page=False`；`add_init_script` 注入 zhangsan JWT（orderNo=SO1781616320768 是 Sprint 3.7 7 号回归创建的待支付订单）；PIL 核验 = 1440×900（49078 bytes） |
| **19-orders-mobile-390x844.png** | 订单详情 mobile 视口（**Sprint 3.8 新增**） | `/orders/SO1781616320768` | **390×844 mobile 视口** | zhangsan | ✅ 通过 | 同上，mobile 视口 + has_touch=True；PIL 核验 = 390×844（28763 bytes） |
| **20-pay-viewport-1440x900.png** | 支付页 desktop 视口（**Sprint 3.8 新增**） | `/pay/SO1781616320768` | **1440×900 严格视口** | zhangsan | ✅ 通过 | 同上，路径 /pay；PIL 核验 = 1440×900（37878 bytes） |
| **21-pay-mobile-390x844.png** | 支付页 mobile 视口（**Sprint 3.8 新增**） | `/pay/SO1781616320768` | **390×844 mobile 视口** | zhangsan | ✅ 通过 | 同上，mobile 视口；PIL 核验 = 390×844（23398 bytes） |
| **22-seckill-viewport-1440x900.png** | 秒杀列表 desktop 视口（**Sprint 3.8 新增**） | `/seckill` | **1440×900 严格视口** | zhangsan | ✅ 通过 | 同上，路径 /seckill；PIL 核验 = 1440×900（62397 bytes） |
| **23-seckill-mobile-390x844.png** | 秒杀列表 mobile 视口（**Sprint 3.8 新增**） | `/seckill` | **390×844 mobile 视口** | zhangsan | ✅ 通过 | 同上，mobile 视口；PIL 核验 = 390×844（25941 bytes） |
| **24-search-viewport-1440x900.png** | 搜索页 desktop 视口（**Sprint 3.8 新增**） | `/search?keyword=iPhone` | **1440×900 严格视口** | 游客（搜索在 Gateway 白名单） | ✅ 通过 | 同上，路径 /search?keyword=iPhone（公共路由无需 auth）；PIL 核验 = 1440×900（45360 bytes） |
| **25-search-mobile-390x844.png** | 搜索页 mobile 视口（**Sprint 3.8 新增**） | `/search?keyword=iPhone` | **390×844 mobile 视口** | 游客 | ✅ 通过 | 同上，mobile 视口；PIL 核验 = 390×844（24780 bytes） |
| **26-cart-viewport-1440x900.png** | 购物车 desktop 视口（**Sprint 3.8 新增**） | `/cart` | **1440×900 严格视口** | zhangsan | ✅ 通过 | 同上，路径 /cart；PIL 核验 = 1440×900（47953 bytes） |
| **27-cart-mobile-390x844.png** | 购物车 mobile 视口（**Sprint 3.8 新增**） | `/cart` | **390×844 mobile 视口** | zhangsan | ✅ 通过 | 同上，mobile 视口；PIL 核验 = 390×844（29172 bytes） |
| **28-admin-orders-viewport-1440x900.png** | 订单详情 admin 视角 desktop 视口（**Sprint 3.9 新增**） | `/orders/SO1781616320768` | **1440×900 严格视口** | admin | ✅ 通过 | Sprint 3.9 任务 C：Playwright `viewport=1440x900` + `full_page=False`；`add_init_script` 注入 admin JWT（userId=1007, roles=ADMIN）；`file` 命令核验 = 1440×900 |
| **29-admin-orders-mobile-390x844.png** | 订单详情 admin 视角 mobile 视口（**Sprint 3.9 新增**） | `/orders/SO1781616320768` | **390×844 mobile 视口** | admin | ✅ 通过 | 同上，mobile 视口 + has_touch=True；`file` 核验 = 390×844 |
| **30-admin-pay-viewport-1440x900.png** | 支付页 admin 视角 desktop 视口（**Sprint 3.9 新增**） | `/pay/SO1781616320768` | **1440×900 严格视口** | admin | ✅ 通过 | 同上，路径 /pay；`file` 核验 = 1440×900 |
| **31-admin-pay-mobile-390x844.png** | 支付页 admin 视角 mobile 视口（**Sprint 3.9 新增**） | `/pay/SO1781616320768` | **390×844 mobile 视口** | admin | ✅ 通过 | 同上，mobile 视口；`file` 核验 = 390×844 |
| **32-admin-seckill-viewport-1440x900.png** | 秒杀列表 admin 视角 desktop 视口（**Sprint 3.9 新增**） | `/seckill` | **1440×900 严格视口** | admin | ✅ 通过 | 同上，路径 /seckill；`file` 核验 = 1440×900 |
| **33-admin-seckill-mobile-390x844.png** | 秒杀列表 admin 视角 mobile 视口（**Sprint 3.9 新增**） | `/seckill` | **390×844 mobile 视口** | admin | ✅ 通过 | 同上，mobile 视口；`file` 核验 = 390×844 |
| **34-admin-cart-viewport-1440x900.png** | 购物车 admin 视角 desktop 视口（**Sprint 3.9 新增**） | `/cart` | **1440×900 严格视口** | admin | ✅ 通过 | 同上，路径 /cart；`file` 核验 = 1440×900 |
| **35-admin-cart-mobile-390x844.png** | 购物车 admin 视角 mobile 视口（**Sprint 3.9 新增**） | `/cart` | **390×844 mobile 视口** | admin | ✅ 通过 | 同上，mobile 视口；`file` 核验 = 390×844 |

## 关键链路证据

- **Sprint 3.2 关键订单**：`SO1781249605871`（sku 9002 iPhone 15 128G 粉色，¥5999.00，remark="sprint3.2-screenshot"）
- **Sprint 3.3 关键订单**：`SO1781330545972`（sku 9001 iPhone 15 Pro 256G 钛原色，¥8999.00，remark="sprint3.3-screenshot"）
- **Sprint 3.3 支付链路完整**：`payNo=PAY2026061320656761` → `POST /api/v1/pay/notify` 返回统一响应 `{"code":200,"message":"ok","data":"success",...}` → 订单状态 0→1 真实变化
- **admin 登录**：`userId=1007, roles=["ADMIN"]`（`accessToken` 已注入 localStorage）
- **admin 看板**：`/api/v1/admin/dashboard` 返回 `code=200`，含 `todayOrders=1, todaySales=8999.00, totalProducts=12, pendingOrders=19, salesTrend[5], topProducts[5]`
- **Sprint 3.7 admin 看板（本轮独立复跑）**：`/api/v1/admin/dashboard` 返回 `code=200`，含 `todayOrders=10, todaySales=8999.00, totalProducts=12, pendingOrders=22, salesTrend[4], topProducts[≥5]`（实际指标随时间累加，10/12/¥8999.00 是本轮 14/15 号截图生成时的真实值）

## 截图生成方法

- **1440x900**：使用 `browser_navigate` + `browser_vision`（与 Sprint 2 Closeout 一致）
- **390x844**：使用 Python 3.13 + Playwright 1.x + Chromium headless，device_scale_factor=1，viewport 严格 390x844，通过 `add_init_script` 注入 admin/zhangsan 凭据到 `localStorage`
- 鉴权凭据文件位于 `.runtime/sprint3.2-tokens.txt`（gitignored，临时产物，提交时不存在）

## 已知缺陷（不修，归类 Sprint 3.4+ 候选）

1. **订单详情页"收货信息"显示"—"**：与 Sprint 2/3.1/3.2 一致，后端 `mall-order` 写入 `addressJson={"addressId":1}` 不含完整地址对象。属后端 Service 层补齐，前端兜底"—"工作正常。
2. **商品详情 mobile 文档 1617px / 图片在 y=539**：mobile UX 留白过大，属 UI 调优。
3. **首页商品卡 SPU 级"库存 0"**：`mall-product` 接口返回的 SPU `skus[0].stock` 静态 0；实时库存靠 `inventoryStock` 在详情页刷新。属 UI 调优。
4. **`app.css` 4 处预存在硬编码颜色**（rgba / #fdfdfd / #fff）：与既有 token 功能等价，未替换为 `var(--color-bg-surface)` / `var(--shadow-...)`。

## Sprint 3.3 已修复（不列入已知缺陷）

- ✅ mall-order `/actuator/health` 500 → 200：补 `spring-boot-starter-actuator` 依赖；`GlobalExceptionHandler` 新增 `NoResourceFoundException` 处理器，资源不存在时返回 404 + Result 而非 500。
- ✅ `/api/v1/pay/notify` 响应未统一 → 统一：`PayController.notify()` 返回 `Result<String>`，data 为 `"success"`；PayView 的 `notifyResult === 'success'` 兼容（http 拦截器自动 unwrap `data` 字段）。
- ✅ AdminView 销售额显示"—" → ¥8999.00：AdminView.vue 第 130 行 `field()` 字段回退列表加入 `todaySales` 首位；接口实际字段保持 `todaySales`。
