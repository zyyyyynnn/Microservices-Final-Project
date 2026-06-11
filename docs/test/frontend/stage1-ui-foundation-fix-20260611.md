# 阶段一 UI 基建修复报告

## 1. 修复范围
- 假 fallback 清理
- 商品卡 grid 宽度修复
- 商品图 resolver 统一
- SKU spec JSON 解析
- 首页推荐 tab 真实响应
- 首页秒杀接口接入
- 搜索/品牌/分类语义清理
- 后台权限隔离

## 2. 具体改动

| 问题 | 文件 | 修复方式 | 结果 |
|---|---|---|---|
| 假导航：分类、品牌实际套壳 /search | `App.vue` | navItems 语义修正："分类"→"全部商品"，"品牌"→"Apple 专区"，路由用 `brand=Apple` query | ✅ 通过 |
| 假筛选：首页推荐分类 tab 只切 active，不改变商品数据 | `HomeView.vue` | categoryTabs 改为对象数组含 keyword，点击跳转 `/search?keyword=xxx` | ✅ 通过 |
| 假兜底：商品卡片缺少 ID 时强制跳转 /products/1001 | `ProductCard.vue` | 增加 `hasValidSpuId` computed，无 ID 时禁用按钮显示"商品信息不完整" | ✅ 通过 |
| 假状态：首页秒杀模块未调用接口，却显示"秒杀服务未连接" | `HomeView.vue` | `loadHome()` 并行调用 `mallApi.seckillActivities()`，空数据显示"暂无秒杀活动"，失败显示"秒杀活动暂不可用" | ✅ 通过 |
| 权限越权：普通用户登录后可见"后台管理"，/admin 仅校验登录态 | `auth.ts` `router/index.ts` `App.vue` | 新增 `normalizeRoles`、`roles`、`isAdmin`、`isMerchant`、`canAccessAdmin`；路由 meta 添加 roles；守卫拦截无角色用户；下拉菜单 `v-if="auth.canAccessAdmin"` | ✅ 通过 |
| 商品卡布局毒瘤：product-grid 使用 auto-fit + 1fr，少量商品时卡片和图片被无限拉伸 | `HomeView.vue` `app.css` | 改为 `repeat(auto-fill, minmax(220px, 240px))` + `justify-content: start` | ✅ 通过 |
| 图片策略不统一：商品图片 fallback 容易用错 id | `productAssets.ts` | 导出 `resolveProductImage` 统一 resolver，优先用 mainImage/image/skuImage，picsum.photos 时按 spuId 映射本地图，禁止用 skuId 替代 spuId | ✅ 通过 |
| SKU spec 未解析：JSON 字符串直接暴露给用户 | `format.ts` `ProductDetailView.vue` | 新增 `formatSkuSpec`，解析 JSON 输出"版本：256G / 颜色：钛原色"格式 | ✅ 通过 |

## 3. 未处理项（进入阶段二）
以下进入阶段二：
- 购物车 Switch 改 Checkbox
- 购物车表格布局重排
- 结算页金额摘要排版
- 订单详情 addressJson 解析
- 支付金额 0
- 账户地址表单省市区
- 秒杀页 requestId 排版
- CartView、AccountView、OrderDetailView、SeckillView 中残留的"待联调"占位文案（这些页面不在阶段一修复范围）

## 4. 验证

| 页面 | 结果 | 备注 |
|---|---|---|
| 构建检查 `npm run build` | ✅ 通过 | vue-tsc + vite build 无错误 |
| `git diff --check` | ✅ 通过 | 仅 CRLF 警告，无尾随空格 |
| 代码搜索 `|| 1001` | ✅ 通过 | 已清理 |
| 代码搜索 `秒杀服务未连接` | ✅ 通过 | 已清理 |
| 代码搜索 `后台管理` 仅出现在 `auth.canAccessAdmin` 条件下或 AdminView 内 | ✅ 通过 | 已验证 |
| 代码搜索 `mock|fake|dummy|javascript:void(0)` | ✅ 通过 | 无新增 |

### 人工复验页面（需人工验证）
- `/` 首页：推荐分类 tab 点击跳转搜索页、秒杀模块显示真实状态、商品卡尺寸受控
- `/search` 搜索页：显示全部商品、语义化标题（全部商品/品牌专区/分类/关键词）
- `/search?keyword=iPhone`：搜索结果正常
- `/search?brand=Apple`：页面语义为"Apple 专区"，非伪装品牌页
- `/products/1001`：商品详情 SKU spec 显示格式化文本、图片与标题匹配、库存来自 inventory 接口
- `/seckill`：秒杀页正常加载活动列表
- 普通用户 zhangsan 登录后：用户菜单不显示后台管理
- 普通用户直接访问 `/admin`：被拦截重定向首页
- admin 登录后：可看到后台管理并进入 `/admin`

## 5. 结论
**阶段一有条件通过**

条件：需人工完成上述 8 个页面的浏览器真实验收，确认无回归。

---

**修改文件清单：**
- `mall-frontend/src/components/ProductCard.vue`
- `mall-frontend/src/utils/format.ts`
- `mall-frontend/src/catalog/productAssets.ts`
- `mall-frontend/src/views/HomeView.vue`
- `mall-frontend/src/views/SearchView.vue`
- `mall-frontend/src/views/ProductDetailView.vue`
- `mall-frontend/src/router/index.ts`
- `mall-frontend/src/stores/auth.ts`
- `mall-frontend/src/App.vue`
- `mall-frontend/src/styles/app.css`

**清理的假 fallback：** ProductCard.vue 移除 `|| 1001`，改为无 ID 时禁用按钮
**商品卡尺寸修复：** `repeat(auto-fill, minmax(220px, 240px))` + `justify-content: start`
**商品图 resolver：** `productAssets.ts` 导出 `resolveProductImage` 统一入口
**SKU 解析：** `format.ts` 新增 `formatSkuSpec`，ProductDetailView.vue 使用
**首页推荐 tab：** categoryTabs 改为带 keyword 对象，点击跳转 `/search?keyword=xxx`
**首页秒杀模块：** 接入 `mallApi.seckillActivities()`，过滤测试活动，空/失败态文案规范
**搜索/品牌/分类路由：** navItems 语义修正，SearchView 支持 brand/categoryId query
**权限隔离：** auth store 角色归一化 + canAccessAdmin，路由守卫 roles 校验，下拉菜单条件渲染
**build 结果：** ✅ 通过
**diff check：** ✅ 通过
**代码搜索结果：** ✅ 禁止模式均已清理
**报告文件：** docs/test/frontend/stage1-ui-foundation-fix-20260611.md