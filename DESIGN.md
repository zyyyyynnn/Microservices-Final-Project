# Product Design

> 项目：MallCloud 微商城
> 前端技术栈：Vue 3 + Vite + TypeScript + Element Plus + Axios + Pinia
> 设计基线：DESIGN.md
> 设计语言：低饱和雾霾蓝极简 + 轻电商质感
> 最后更新：2026-06-12

---

## 1. Product Goal

MallCloud 是面向微服务课程期末大作业的演示与测试系统。产品设计目标是把当前后端微服务能力组织成一条可演示、可测试、可展示的用户体验链路：

```text
登录 / 注册
  → 商品浏览 / 分类检索
  → 商品搜索与详情查看 (选择 SKU)
  → 购物车管理 (添加、勾选、删除、改数)
  → 确认结算与订单创建 (库存锁定)
  → 模拟支付发起 (创建支付流水)
  → 支付消息分发与处理 (订单状态与库存更新)
  → 查询订单与支付记录 (演示闭环)
```

设计服务于以下核心目标：
- 保证用户前台购物与订单流转链路的流畅连贯，前台不暴露任何后端开发及服务联调状态字眼；
- 保证商家及管理员能够方便、直观地在后台浏览关键统计指标（订单数、商品数、销售额）并执行订单发货及商品基础维护；
- 保证 Gateway 鉴权、服务隔离、消息队列（RocketMQ）、分布式事务（Seata）、Sentinel 限流和秒杀并发等后端核心技术在前端有明确的承载和入口；
- 保持文档与代码同步，所有未通过最终网页端人工复验和视觉验收的页面与状态一律标记为“待网页端复审，待视觉验收”，不得写成已通过。

---

## 2. Target Users and Roles

| 角色 | 来源依据 | 产品目标 | 权限范围 | 状态 |
|---|---|---|---|---|
| 游客 | `docs/process/01-需求规格说明书.md`、Gateway 白名单 | 浏览公共商品信息、分类展示与检索搜索 | 公共商品详情、分类树、搜索结果、热门搜索词、账户注册与登录 | 能力已定义，待网页端复审 |
| 普通用户（`USER`） | `docs/process/09-本地部署与联调手册.md` | 体验完整的购物流程及核心订单交易链路 | 个人信息修改、地址簿管理、购物车管理、订单创建、模拟支付、秒杀参与 | 能力已定义，待网页端复审 |
| 商家（`MERCHANT`） | `docs/process/01-需求规格说明书.md`、`docs/process/09-本地部署与联调手册.md` | 课程演示所需的商品和订单基础维护 | 后台订单列表、发货、商品列表浏览及商品上下架 | 权限边界待确认，后台登录待确认 |
| 管理员（`ADMIN`） | `docs/process/01-需求规格说明书.md` | 后台全部指标浏览与核心后台演示操作 | 后台看板、订单及商品全局管理；独立管理员登录流程 | 权限边界待确认，后台登录待确认 |

演示账号统一密码为 `123456`，仅限本地测试使用。

---

## 3. Information Architecture

当前微商城前端（`mall-frontend`）通过路由划分前台商城与后台管理功能区，整体信息架构定义如下：

```text
MallCloud 前端应用
├── 公共服务区
│   ├── 注册页 (RegisterView)
│   ├── 登录页 (LoginView)
│   ├── 首页 / 商品推荐 (HomeView)
│   ├── 商品详情 (ProductDetailView)
│   ├── 商品搜索 / 热词 (SearchView)
│   └── 404 错误页 (NotFoundView)
├── 用户中心 (受限访问)
│   ├── 账户资料与头像 (AccountView)
│   └── 收货地址管理 (AccountView)
├── 购物车系统 (受限访问)
│   └── 购物车明细与结算入口 (CartView)
├── 订单与结算流 (受限访问)
│   ├── 订单结算确认 (CheckoutView)
│   ├── 订单详情与物流状态 (OrderDetailView)
│   ├── 模拟支付收银台 (PayView)
│   └── 秒杀活动大厅 (SeckillView)
└── 后台管理系统 (受限访问，仅限管理员/商家)
    ├── 数据指标看板 (AdminView)
    ├── 后台订单列表与发货 (AdminView)
    └── 后台商品列表与维护 (AdminView)
```

---

## 4. Core User Flows

### 4.1 认证与登录上下文
1. 游客在 `/login` 提交表单 -> 经 Gateway 转发至 `mall-auth` 进行校验。
2. 登录成功后，前端 Pinia Store 存储 `accessToken`、`refreshToken` 和 `userInfo`。
3. 后续所有受限请求通过 Axios 拦截器在 Header 携带 `Authorization: Bearer <token>`。
4. 退出登录时，调用 `/api/v1/auth/logout` 接口，并清空本地存储，重定向回登录页。

### 4.2 购物车交易流
1. 用户在商品详情页选择具体 SKU 属性和购买数量。
2. 点击“加入购物车”触发请求，后台将商品加入 `mall-cart`。
3. 购物车页面（`/cart`）拉取列表，并允许用户修改数量、单项勾选、一键删除等，最终汇总计算总额。

### 4.3 结算与下单
1. 用户从购物车或商品详情点击结算，进入 `/checkout`。
2. 页面拉取收货地址列表，用户确认收货地址并提交订单。
3. `mall-order` 接收请求，调用商品服务验证价格，调用 `mall-inventory` 锁定库存，创建成功后返回 `orderNo` 并跳转至订单详情页。

### 4.4 模拟支付与回调
1. 用户从订单详情进入 `/pay/:orderNo`。
2. 点击“确认支付”触发模拟收银台创建支付单（`mall-pay`）。
3. 页面展示“确认模拟支付回调”的操作按钮，点击后模拟发送支付成功消息（`PAY_RESULT`）。
4. 消息队列（RocketMQ）分发消息：`mall-order` 更新订单状态为已付款，`mall-inventory` 正式扣减库存，用户可查询到最新的订单详情。

### 4.5 秒杀高并发交易
1. 用户进入 `/seckill` 活动大厅，获取当前的秒杀活动列表。
2. 活动开始后，点击“立即抢购”发起秒杀异步请求，后台写入 Redis 并返回 `requestId`。
3. 前端获取 `requestId` 后进入轮询等待状态，直到获取到成功或失败的最终结果。

---

## 5. Pages and Routes

| 页面名称 | 路由路径 | 目标角色 | 核心依赖 API | 当前状态 |
|---|---|---|---|---|
| 登录页 | `/login` | 游客 | `POST /api/v1/auth/login` | 已存在，待网页端复审，待视觉验收 |
| 注册页 | `/register` | 游客 | `POST /api/v1/users/register` | 已存在，待网页端复审，待视觉验收 |
| 首页 | `/` | 游客/用户 | `GET /api/v1/categories/tree` | 已存在，最终截图验收中 |
| 商品详情页 | `/products/:id` | 游客/用户 | `GET /api/v1/products/{id}` | 已存在，最终截图验收中 |
| 商品搜索页 | `/search` | 游客/用户 | `GET /api/v1/search/products` | 已存在，最终截图验收中 |
| 购物车页 | `/cart` | 用户 | `GET /api/v1/carts` | 已存在，最终截图验收中 |
| 个人账户页 | `/account` | 用户 | `GET /api/v1/users/me`、地址 CRUD | 已存在，待网页端复审，待视觉验收 |
| 确认结算页 | `/checkout` | 用户 | `POST /api/v1/orders` | 已存在，待网页端复审，待视觉验收 |
| 订单详情页 | `/orders/:orderNo` | 用户 | `GET /api/v1/orders/{orderNo}` | 已存在，最终截图验收中 |
| 模拟支付页 | `/pay/:orderNo` | 用户 | `POST /api/v1/pay/create` | 已存在，最终截图验收中 |
| 秒杀活动页 | `/seckill` | 用户 | 秒杀列表、下单及结果轮询接口 | 已存在，最终截图验收中 |
| 后台管理页 | `/admin` | 商家/管理员 | 看板指标、订单发货、商品维护接口 | 已存在，待网页端复审，待视觉验收 |
| 404 页面 | `/:pathMatch(.*)*` | 游客/用户 | 无 | 已存在，待网页端复审，待视觉验收 |

---

## 6. Page-level Interaction Requirements

### 6.1 前台商城页面
- **首页 (HomeView)**:
  - 首屏为极简排版布局，展示清晰的“交易链路（演示流程）”步骤指引卡片，方便评审与快速导航；
  - 核心频道（如数码家电、美妆护肤等）支持点击一键切换搜索检索；
  - 推荐商品列表及限时秒杀展示区采用规范网格排列，不可被拉伸或挤压。
- **商品详情 (ProductDetailView)**:
  - 必须完整展示主图、商品名、SPU/SKU 选择器、价格、库存以及描述；
  - 用户切换 SKU 时，页面中的价格、库存和购买数量限制应当瞬时对应刷新；
  - 库存为 0 或下架的 SKU 对应按钮须为禁用状态，无法加入购物车或购买。
- **搜索页 (SearchView)**:
  - 搜索结果网格展示，当无匹配结果时展示统一的 Empty 空态，并展示“推荐热词”引导用户重试。
- **购物车页 (CartView)**:
  - 勾选行、改变数量、单项删除均需响应式联动底部结算悬浮条；
  - 修改数量时，该行按钮与输入框应进入提交锁定状态，直到网络响应返回。
- **账户页 (AccountView)**:
  - 整合用户资料卡与收件地址卡；地址列表提供新增/编辑/删除表单，布局极简，无挤压。

### 6.2 后台管理页面
- **后台管理 (AdminView)**:
  - 看板指标区以清晰的三格栅格呈现：订单数、商品数、销售额。数据加载失败或未返回时，对应指标显示 `—` 兜底；
  - 订单表格及商品表格仅用于管理演示，订单号、用户等缺失时必须兜底显示 `—`；
  - 商品列重构为“商品图文卡片”，单列整合缩略图、商品名称与 SPU 编号；
  - 发货区域使用整洁的横向排版，输入框无占位符，响应式适配移动端。

---

## 7. UI States

### 7.1 Loading 状态
- **页面级 Loading**: 首次进入页面时展示骨架屏或低饱和度的静态“正在加载数据”占位，禁止使用旋转的加载动画，防止页面首屏高度发生剧烈跳动；
- **操作锁定 Loading**: 登录、下单、支付、秒杀、发货按钮被点击后，按钮状态立即变为 `:loading="true"` 或 `:disabled="true"`，并保持文字“处理中”，网络请求返回前禁止二次点击。

### 7.2 Empty 状态
- 数据为空时采用统一的 `<el-empty>` 结构展示；
- 订单列表空态描述统一为：“当前暂无订单记录。”；
- 商品列表空态描述统一为：“当前暂无商品记录。”；
- 秒杀活动空态描述统一为：“当前暂无秒杀活动。”。

### 7.3 Error 状态
- 接口请求失败或后端服务未启动时，必须使用统一的 `PageState` 或 `el-alert` 提示错误信息，并提供“重试”按钮；
- 用户可见错误文案不得出现技术错误码、异常名和微服务名称（例如禁止在前台直接抛出 `500`、`Network Error`、`NullPointerException` 以及具体服务名），替换为“数据暂时无法加载，请稍后重试”。开发文档、日志、测试报告不受此限制。

### 7.4 Disabled 状态
- Disabled 状态必须通过 token 化的背景、边框、文本色、cursor 和控件 disabled 属性共同表达，确保禁用按钮看起来不可点击且对比度充足。
- **严禁仅通过 `opacity: 0.5` 弱化透明度作为 Disabled 控件的主要表达方式**。Element Plus 内部 opacity 若存在，必须通过主题覆盖统一处理。

### 7.5 Success 状态
- 交易或操作成功后弹出 Element Plus Message，操作按钮恢复为可用状态，成功文案需客观明确（如“发货请求已提交”、“订单已成功创建”），避免浮夸。

---

## 8. Roles and Permissions

| 功能页面 | 游客 | 普通用户 (USER) | 商家 (MERCHANT) | 管理员 (ADMIN) |
|---|---|---|---|---|
| 公共浏览 (首页/搜索/详情) | 允许 | 允许 | 允许 | 允许 |
| 购物车 (CartView) | 拦截 (引导登录) | 允许 | 权限边界待确认 | 权限边界待确认 |
| 结算与下单 (Checkout) | 拦截 (引导登录) | 允许 | 权限边界待确认 | 权限边界待确认 |
| 支付收银台 (PayView) | 拦截 (引导登录) | 允许 | 权限边界待确认 | 权限边界待确认 |
| 个人账户资料与地址 | 拦截 (引导登录) | 允许 | 权限边界待确认 | 权限边界待确认 |
| 秒杀活动大厅 | 拦截 (引导登录) | 允许 | 权限边界待确认 | 权限边界待确认 |
| 后台管理 (看板/发货/维护) | 拦截 (跳转首页) | 拦截 (跳转首页) | 允许 | 允许 |

---

## 9. Responsive Behavior

系统应支持主流的屏幕尺寸，避免布局重叠和组件挤压溢出，各断点对应的核心响应式行为如下：

- **桌面端大屏 (>= 1440px)**:
  - 采用页面最大宽度 `1440px` 并水平居中，页面两侧留白 gutter 为 `32px`。
- **小屏笔记本 (1024px ~ 1366px)**:
  - 主布局容器自适应缩放，gutter 降为 `24px`；
  - 导航栏及 Header 保持完整展开，推荐商品网格按每行 4 或 5 列自适应排列。
- **平板端 (768px ~ 1023px)**:
  - 首页 Hero 区域必须折叠为单栏（不再并排展示交易链路卡片）；
  - 头部 Header 在 `1024px` 以下触发折叠，重排导航结构；
  - 搜索框在窄屏下自适应占满或折行；
  - 购物车结算悬浮栏应悬浮固定在底部，不能遮挡表单输入和关键操作按钮。
- **移动端窄屏 (< 768px，最小适配至 390px)**:
  - 页面两侧留白 gutter 降为 `16px`；
  - 表格必须由外层 `.table-scroll` 容器包裹实现横向滚动，或在移动端换用流式卡片替代；
  - 标题、大字号文本统一使用 `clamp` 属性或根据断点降低字号，以防在 `390px` 视口中产生溢出；
  - 所有 Dialog 和 Drawer 宽度应自动适配，不得超出视口。

---

## 10. Design System

设计系统的核心设计风格为：**低饱和雾霾蓝极简 + 轻电商质感**。首页采用首屏高级极简与商品区轻电商的混合设计方向，前后台共享基础 token，但允许前台与后台采用不同的信息密度。

### 10.1 Design Principles
- **高级极简与轻电商质感**: 首页首屏与背景采用极简的白灰蓝为主调，利用极轻微的阴影建立层级，避免大面积强渐变。前台商城卡片更有呼吸感，更重视主 CTA；后台管理则专注于表格扫描效率与信息密度。
- **克制的动效**: 允许体系化的微弱过渡效果（如颜色、背景、边框、阴影平滑过渡及极微弱的 hover 缩放），任何多余的弹跳与旋转动画均属于禁止范畴。
- **数据兜底安全**: 字段为 null、undefined、NaN、空字符串或接口未返回时，展示为 `—`。合法 0 值（例如 0 元价格、0 件商品、购物车数量为 0）必须按业务语义展示为 0、0 件或 ¥0.00。

### 10.2 Color Tokens
所有颜色必须全部 token 化，禁止裸写 hex/rgb/hsl，严禁在业务组件中直接硬编码十六进制颜色值。

```css
:root {
  /* 品牌色 (低饱和雾霾蓝品牌主色) */
  --color-brand: #4b7099;
  --color-brand-hover: #3b5a7d;
  --color-brand-soft: #f0f4f8;

  /* 基础文本色 */
  --color-text-primary: #1e293b;   /* 深石板灰 */
  --color-text-secondary: #475569; /* 中石板灰 */
  --color-text-tertiary: #94a3b8;  /* 弱辅助文本 */
  --color-text-inverse: #ffffff;

  /* 画布与表面色 */
  --color-bg-page: #f8fafc;        /* 极浅灰蓝画布 */
  --color-bg-surface: #ffffff;     /* 卡片、容器白背景 */
  --color-bg-subtle: #f1f5f9;      /* 次级容器及悬停背景 */

  /* 边框色 */
  --color-border: #e2e8f0;         /* 基础网格边框 */
  --color-border-strong: #4b7099;  /* 聚焦及高亮边框 */

  /* 价格体系 (低饱和红点缀) */
  --color-price: #cc4d4d;
  --color-price-muted: #94a3b8;
  --color-promo: #d97706;          /* 低饱和橘黄 */

  /* 语义反馈色 (低饱和度表达) */
  --color-success: #15803d;        /* 成功绿 */
  --color-success-soft: #f0fdf4;

  --color-warning: #b45309;        /* 警告黄 */
  --color-warning-soft: #fffbeb;

  --color-danger: #b91c1c;         /* 危险/错误红 */
  --color-danger-soft: #fef2f2;
}
```

### 10.3 Typography
字号优先 token 化。组件特定尺寸允许使用局部常量，但必须集中、可解释、不可重复散落。
- **通用字体族**: `Inter, "PingFang SC", "Microsoft YaHei", system-ui, -apple-system, BlinkMacSystemFont, sans-serif`；
- **等宽字体族 (数值、订单号、价格)**: `"JetBrains Mono", Consolas, monospace`；
- **字号体系**:
  - `--font-xs: 12px;` (标签、次要 SKU 编号)
  - `--font-sm: 14px;` (常规正文、输入框、常规导航)
  - `--font-base: 16px;` (商品卡片标题、中等按钮)
  - `--font-lg: 18px;` (模块小标题、订单总价、购物车小计)
  - `--font-xl: 20px;` (商品详情价、小页面标题)
  - `--font-2xl: 24px;` (大页面标题、看板指标数)
- **字重**:
  - `--weight-normal: 400;`
  - `--weight-medium: 500;`
  - `--weight-bold: 700;`

### 10.4 Spacing
间距优先 token 化。
- `--spacing-xs: 4px;`
- `--spacing-sm: 8px;`
- `--spacing-md: 16px;`
- `--spacing-lg: 24px;`
- `--spacing-xl: 32px;`

### 10.5 Radius
圆角优先 token 化。
- `--radius-sm: 2px;` (小标签)
- `--radius-md: 4px;` (输入框、常规按钮)
- `--radius-lg: 8px;` (商品卡片、大面板、对话框)
- `--radius-xl: 16px;` (特色气泡、大图圆角)

### 10.6 Shadow
允许引入极其克制的浅阴影来建立视觉层级关系。
- `--shadow-sm: 0 1px 2px rgba(15, 23, 42, 0.06);` (卡片、次要区块)
- `--shadow-md: 0 8px 24px rgba(15, 23, 42, 0.08);` (Dropdown, Dialog, Drawer, 悬浮条, 重点面板)
- **禁止**: 厚重阴影、弥散光、大面积阴影装饰、彩色光晕。

### 10.7 Transition
允许使用体系化极简过渡来实现平滑的操作反馈。
- `--transition-fast: 120ms ease-out;` (按钮、输入框高亮切换)
- `--transition-base: 160ms ease-out;` (卡片 Hover 阴影与 transform 过渡)
- **允许的属性**: `color`、`background-color`、`border-color`、`box-shadow`。
- **有条件允许**: `opacity`（仅允许用于非 disabled 的淡入淡出辅助状态，不能作为 disabled 主要表达）；`transform`（仅允许极轻微 hover 提示）。
- **禁止**: `@keyframes`、复杂/弹跳/旋转/离场/页面进入等影响性能与稳定性的动画。

### 10.8 Layout Container
统一桌面端最大宽度与页面留白标准：
- `--layout-max-width: 1440px;`
- `--layout-gutter-desktop: 32px;`
- `--layout-gutter-tablet: 24px;`
- `--layout-gutter-mobile: 16px;`

### 10.9 Element Plus Theme Mapping
所有 Element Plus 全局变量必须在 `src/styles/element-theme.css` 中映射到 Design System Token，确保整体 UI 的一致性：

```css
:root {
  --el-color-primary: var(--color-brand);
  --el-color-primary-light-3: var(--color-brand-hover);
  --el-color-primary-light-9: var(--color-brand-soft);
  --el-color-danger: var(--color-danger);
  --el-color-success: var(--color-success);
  --el-color-warning: var(--color-warning);
  --el-color-info: var(--color-text-secondary);

  --el-text-color-primary: var(--color-text-primary);
  --el-text-color-regular: var(--color-text-secondary);
  --el-text-color-secondary: var(--color-text-tertiary);
  --el-text-color-placeholder: var(--color-text-tertiary);

  --el-bg-color: var(--color-bg-page);
  --el-bg-color-page: var(--color-bg-page);
  --el-fill-color-blank: var(--color-bg-surface);
  --el-fill-color-light: var(--color-bg-subtle);

  --el-border-color: var(--color-border);
  --el-border-radius-base: var(--radius-md);

  --el-transition-duration: 0.16s;
  --el-transition-duration-fast: 0.12s;
}
```

### 10.10 Frontend Commerce Components
- **ProductCard (商品卡片)**:
  - 鼠标悬停（hover）时应用 `--transition-base` 平滑触发微弱位移 `transform: translateY(-2px)` 并显现 `--shadow-sm`，卡片边框色变更为 `--color-brand-light`。
- **ProductImage (商品图)**:
  - 优先使用真实商品图或可信公开商品素材，禁止使用 AI 图伪装真实商品。
  - 商品图缺失时使用统一占位图，且占位图必须明确表达“图片缺失 / 暂无图片”。ProductImage 组件必须支持 fallback。
- **SKUSelector (SKU 选择器)**:
  - 默认状态：白色背景，`--color-border` 边框，鼠标悬浮为手型；
  - 选中状态：`--color-brand` 边框，字体加粗，无多余的彩色发光或弥散底色；
  - 禁用状态：`--color-bg-subtle` 背景，灰淡文本，`cursor: not-allowed`，可点划线标识。
- **Price (价格组件)**:
  - 商品主价格必须醒目，原价使用 muted 文本和删除线。订单金额汇总和商品价格使用同一价格语义体系（促销价使用低饱和红）。
- **Dialog / Drawer / Dropdown**:
  - 尺寸和间距适配视口，允许应用浅阴影与极简渐变，禁用厚重投影和弹跳动画。
- **Button / Input / Select**:
  - 规范高度与内边距，Disabled 状态严禁只靠 opacity 弱化，必须有明确的背景色、边框色、置灰文本和置灰光标，焦点变化瞬时生效。

### 10.11 Admin Components
- **AdminTable (后台表格)**:
  - 追求扫描效率和紧凑的信息密度。商品列单列整合缩略图、商品名称与 SPU 编号。字段缺失均展示为 `—`。
- **ShipForm (发货单)**:
  - 采用上下分层的极简 `ship-query` 并行布局结构，去除 placeholder，输入框与发货按钮横向对齐并支持窄屏折行。

### 10.12 State Components
- **PageState (状态管理)**:
  - 错误态下展示“重试”按钮；正在加载数据展示骨架（Skeleton）或低饱和文本。

### 10.13 Red Lines
1. **严禁在组件 style scoped 或内联样式中直接写入任何未经 Token 映射的十六进制/RGB颜色值**（如 `#ff0000`、`rgba(0,0,255,1)`）。
2. **严禁使用复杂的动画缓动及大范围旋转**；`@keyframes` 和第三方动效库默认禁止。
3. **严禁以 `opacity` 弱化透明度作为 Disabled 控件的唯一表达方式**。
4. **严禁在页面无数据时显示 `¥0.00`**；凡是空额必须做兜底渲染为 `—`。
5. **严禁在任何地方使用 AI 伪造的图片或插图作为商品展示主图**。

---

## 11. Accessibility Requirements

- **色彩对比度**: 所有的文本、按钮和关键信息与其背景的色彩对比度必须满足正常人群在低亮度屏幕下的无障碍可读性，禁止使用太浅的蓝灰色或浅红色展示核心文案。
- **键盘操作支持**: 所有的下拉菜单、按钮、对话框的关闭按钮（X）、地址选项卡和结算栏必须能够使用键盘聚焦，且 `:focus-visible` 时高亮显现 `--color-border-strong`。
- **辅助文本 (Aria)**:
  - 购物车商品图片及主图必须标注 `alt` 属性；
  - 无直观中文文字的纯图标按钮（如购物车加减号、删除图标）必须声明 `aria-label`。

---

## 12. Service and API Dependencies

| 依赖服务 | 提供能力 | 消费场景 |
|---|---|---|
| `mall-auth` | 登录生成 token、注销失效 token | 全局登录/登出拦截器 |
| `mall-user` | 资料拉取、个人收货地址增删改查 | 账户资料卡与个人地址簿 |
| `mall-product` | 全分类类目树、具体商品 spu/sku 详情 | 商品大类导航、搜索与详情页 |
| `mall-search` | 商品全文检索与热门搜索词展示 | 搜索中心、热词推荐栏 |
| `mall-cart` | 加入购物车、拉取列表、改数、单项删除 | 购物车与结算悬浮条 |
| `mall-order` | 订单确认核算、订单创建、当前用户订单详情 | 订单结算页、订单状态跟踪 |
| `mall-inventory` | 库存状态锁定、扣减与回滚状态更新 | 商品详情、购物车、后台管理 |
| `mall-pay` | 模拟支付单流水生成、模拟发起支付回调 | 收银台支付页 |
| `mall-seckill` | 秒杀活动列表加载、发起秒杀、并发结果轮询 | 秒杀活动大厅 |
| `mall-admin-biz` | 看板统计信息、管理员商品与订单列表及发货 | 后台看板、维护页面 |

---

## 13. Acceptance Criteria

任何前端 UI 重构和页面完善，必须经过以下验收标准的严格审查：

### 13.1 一致性与落地审计
- 所有的页面视觉设计及布局必须与本设计系统的标准一致，所有路由对应的页面已存在。
- 颜色必须全部 token 化，禁止裸写 hex/rgb/hsl。字号、间距、圆角优先 token 化。组件特定尺寸允许使用局部常量，但必须集中、可解释、不可重复散落。
- 禁用状态必须满足视觉对比度，严禁只靠 opacity 表达。

### 13.2 金额与状态兜底审计
- 字段为 null、undefined、NaN、空字符串或接口未返回时，展示为 `—`。
- 合法 0 值必须按业务语义展示为 0、0 件或 ¥0.00。

### 13.3 规范与兼容审计
- `npm run build` 命令必须无错误地编译完成；现有构建警告需记录且不得新增未评估警告；`git diff --check` 执行结果返回为 0。
- 多尺寸视口（1440px / 1366px / 1024px / 768px / 390px）下，页面无任何文本溢出截断或异常的横向滚动条。

### 13.4 交互流程与真实性审计
- 普通用户与后台管理的交互均必须提供真实的前端代码实现与对应的浏览器测试截图，禁止伪造成功。

### 13.5 Frontend Page Completion Matrix

| 页面名称 | 路由路径 | 必须具备 | 当前状态 |
|---|---|---|---|
| 首页 / 商品推荐 | `/` | 类目入口、商品卡片、商品详情入口、搜索入口 | 已存在，已纳入最终截图验收 |
| 商品详情 | `/products/:id` | 商品标题、价格、SKU、库存状态、数量、加入购物车、错误状态 | 已存在，已纳入最终截图验收 |
| 搜索页 | `/search` | 关键字、热词、结果列表、分页、空结果、错误状态 | 已存在，已纳入最终截图验收 |
| 登录页 | `/login` | 登录表单、错误提示、跳转来源 | 已存在，待网页端复审，待视觉验收 |
| 注册页 | `/register` | 独立注册表单、字段校验、成功后登录引导 | 已存在，待网页端复审，待视觉验收 |
| 个人账户页 | `/account` | 用户资料、地址列表、新增入口、空状态 | 已存在，待网页端复审，待视觉验收 |
| 购物车页 | `/cart` | 商品行、价格、小计、勾选、数量、删除、结算、空状态 | 已存在，已纳入最终截图验收 |
| 确认结算页 | `/checkout` | 地址选择、订单项确认、金额汇总、提交状态 | 已存在，待网页端复审，待视觉验收 |
| 订单详情页 | `/orders/:orderNo` | 订单状态、商品明细、金额、支付入口、错误状态 | 已存在，已纳入最终截图验收 |
| 模拟支付页 | `/pay/:orderNo` | 支付记录、模拟支付说明、通知结果、查询订单入口 | 已存在，已纳入最终截图验收 |
| 秒杀活动页 | `/seckill` | 活动列表、活动详情、状态文案、发起请求、结果轮询 | 已存在，已纳入最终截图验收 |
| 后台管理页 | `/admin` | 看板统计、订单表格、商品表格、发货操作 | 已存在，三个真实区块已纳入最终截图验收 |
| 404 页面 | `/:pathMatch(.*)*` | 友好提示、返回首页入口 | 已存在，待网页端复审，待视觉验收 |

---

## 14. Controlled Exceptions

当且仅当发生以下情况时，允许通过例外流程进行库引入或后端接口改动，其必须在重构前单独申请批准：

1. **引入新 UI 依赖**: 现有的 Element Plus 确实无法以合理的开发成本实现某些特有的复杂微商城组件交互（如某些图表看板）；
2. **接口与架构调整**: 为了完成真正的订单或商品流程，必须在 Controller 中新增列表接口或调整 OpenFeign 传递参数；
3. **业务流程变动**: 配合秒杀机制的队列调整修改前端的轮询逻辑。

- **例外流程执行标准**:
  - 新 UI 库、后端接口、业务流程调整不得混入普通 UI 重构 Sprint。必须单独提出方案、影响范围、回滚方案，并经用户批准后单独阶段执行。
  - 必须单独作为一个 Git Commit 或分支进行验证，禁止与普通 UI 视觉重构代码混在一处。

---

## 15. Open Questions

- **待确认**: 后端 `ProductController` 尚未确认 `/api/v1/products` 外部列表接口，目前首页及搜索商品列表依然通过特定硬编码 ID 进行单个拉取，有待后期确定。
- **待确认**: 管理员与商家的权限边界是否统一，后台路由 `/admin` 的鉴权白名单在 Gateway 中依然包含两种角色，待微服务部署架构最终确定。
- **待确认**: 购物车删除、商品彻底删除、订单支付回滚等属于高危破坏性操作，在当前前端中是否需要弹出统一的 Element Plus MessageBox 二次确认框。

---

## 16. Decision Log

| 日期 | 决策事件 | 决策内容与设计依据 |
|---|---|---|
| 2026-06-06 | v1.0 全局样式基线确立 | 确立使用白蓝线条极简风、Element Plus 主题映射以及禁动效。 |
| 2026-06-08 | 拆分多路由与独立页面 | 不使用任何 mock 假数据，对支付、详情、秒杀多路由实施工程拆分与规范命名。 |
| 2026-06-11 | 后台管理页阶段性整改 | 清理所有带有 `AdminController` 开发联调色彩的警示条，商品列重构为图文卡片。 |
| 2026-06-11 | **设计基线升级** | 确立**低饱和雾霾蓝极简 + 轻电商质感**；Hero 背景图解锁允许重构，阴影和极简过渡有条件解禁；对金额和状态做强兜底以显示为 `—`。 |
| 2026-06-12 | **视觉重构设计系统复审** | 修正未验证的完成结论与绝对规则；限定合法 0 值显示规则；微调组件特定尺寸及 token 验收细则，强化 Controlled Exceptions 方案审批标准。 |
