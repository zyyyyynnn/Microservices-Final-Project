# Product Design

> 项目：MallCloud 微商城
> 设计基线：`docs/PROJECT_STANDARD.md`、`docs/PRD.md`、`docs/ARCHITECTURE.md`、`docs/API.md`、实际 Controller
> 前端技术栈：Vue 3 + Vite + TypeScript + Element Plus + Axios + Pinia（`mall-frontend`）
> 设计语言：白蓝线条极简风（White-Blue Lineart Minimalist）
> 核心原则：零动效（no transition / no animation / no easing）
> 最后更新：2026-06-08
> 当前前端状态：`mall-frontend` 已完成基础工程、接口入口和技术演示断点；完整产品化页面仍需整改，不得作为最终前端交付完成。

---

## 1. Product Goal

MallCloud 是面向微服务课程期末大作业的电商系统。产品设计目标不是扩展业务规模，而是把当前后端微服务能力组织成一条可演示、可测试、可讲清楚的用户体验链路：

```text
登录
  → 查询商品 / 搜索商品
  → 查看商品详情
  → 加入购物车
  → 创建订单
  → 发起模拟支付
  → 支付结果消息更新订单和库存
  → 查询订单结果
```

设计必须服务以下交付目标：

- 让前台用户可以围绕商品、购物车、订单和支付完成核心链路演示；
- 让商家和管理员可以完成课程演示所需的基础后台查看与管理；
- 让 Gateway 鉴权、服务调用、MQ、Seata、Sentinel、Nacos、搜索和秒杀等技术点有清晰的用户操作入口或验证入口；
- 不把未验证的业务服务、接口、性能数据或前端页面写成已完成能力。

---

## 2. Target Users and Roles

| 角色 | 来源依据 | 产品目标 | 可确认能力 |
|---|---|---|---|
| 游客 | `docs/PRD.md`、Gateway 白名单 | 浏览公共商品信息和搜索结果 | 类目树、商品详情、搜索、热词、注册、登录 |
| 普通用户（`USER`） | `docs/QUICK_START.md` 演示账号 | 完成购物和订单链路 | 登录、用户资料、地址、购物车、创建订单、查询订单、发起支付、支付记录、秒杀请求和结果查询 |
| 商家（`MERCHANT`） | `docs/PRD.md`、`docs/QUICK_START.md` | 课程演示所需的商品和订单基础管理 | 后台订单列表、发货、商品列表、商品增删改、上下架；权限细节待确认 |
| 管理员（`ADMIN`） | `docs/PRD.md`、`docs/QUICK_START.md` | 课程演示所需的基础后台能力 | 后台看板、后台商品和订单能力；独立后台登录实现待确认 |

演示账号统一密码为 `123456`，只用于本地课程演示环境。

---

## 3. Information Architecture

当前仓库已新增 `mall-frontend` 单应用前端工程。以下信息架构是基于已确认后端接口和项目文档建立的产品基线，前端路由以 `mall-frontend/src/router/index.ts` 为准。

```text
MallCloud
├── 公共浏览
│   ├── 登录 / 注册
│   ├── 类目
│   ├── 商品详情
│   └── 搜索 / 热词
├── 用户中心
│   ├── 当前用户资料
│   └── 收货地址
├── 购物车
│   ├── 加入购物车
│   ├── 查询购物车
│   ├── 修改数量
│   ├── 勾选商品
│   └── 删除商品
├── 订单与支付
│   ├── 创建订单
│   ├── 查询订单
│   ├── 发起模拟支付
│   ├── 支付结果通知
│   └── 查询支付记录
├── 秒杀
│   ├── 活动列表
│   ├── 活动详情
│   ├── 发起秒杀
│   └── 轮询秒杀结果
├── 后台
│   ├── 数据看板
│   ├── 订单列表 / 发货
│   └── 商品列表 / 商品维护
└── 技术演示页
    ├── Gateway 鉴权状态
    ├── 当前用户与角色
    ├── Seata 回滚验证说明
    ├── RocketMQ 支付消息链路说明
    ├── Sentinel 限流入口
    ├── Elasticsearch 搜索入口
    └── Nacos 热更新说明或验证入口
```

---

## 4. Core User Flows

### 4.1 登录和用户上下文

```text
用户提交 zhangsan / 123456
  → Gateway 白名单转发到 mall-auth
  → mall-auth 查询 mall-user
  → 返回 accessToken / refreshToken / userInfo
  → 前端保存 Token
  → 后续受限请求携带 Authorization: Bearer <token>
  → Gateway 校验 JWT 并透传 X-User-Id、X-User-Roles
```

必须覆盖登录成功、登录失败、无 Token、错误 Token 和有效 Token。

### 4.2 商品到购物车

```text
游客或用户查看类目 / 商品详情 / 搜索结果
  → 用户选择 SKU 和数量
  → 已登录用户加入购物车
  → 购物车展示商品信息、数量、选中状态和小计
```

商品列表页前端路由待确认；当前可确认外部商品接口为类目树和商品详情，搜索服务提供商品搜索。

### 4.3 创建订单

```text
用户从购物车或商品详情发起下单
  → 选择或确认收货地址
  → 提交 addressId 和订单项
  → mall-order 查询商品 SKU
  → mall-order 调用 mall-inventory 锁定库存
  → 返回 orderNo 和金额等创建结果
  → 用户进入订单详情或支付入口
```

订单创建不得信任客户端价格，价格以商品服务返回为准。

### 4.4 支付结果

```text
用户发起模拟支付
  → mall-pay 创建支付记录
  → 支付通知产生 PAY_RESULT
  → mall-message 消费消息
  → mall-order 更新订单状态
  → mall-inventory 确认扣减库存
  → 用户查询订单或支付记录
```

真实第三方支付不是本期目标。

### 4.5 秒杀

```text
用户查看秒杀活动
  → 进入活动详情
  → 发起秒杀请求
  → 返回 requestId
  → 前端轮询秒杀结果
  → 展示成功、失败、售罄、未开始、已结束、限购或限流结果
```

秒杀最终状态必须以接口返回和测试结果为准，不用前端本地状态伪造成功。

### 4.6 后台基础管理

```text
商家 / 管理员进入后台
  → 查看数据看板
  → 查看订单列表
  → 执行发货
  → 查看商品列表
  → 维护商品和上下架
```

独立后台登录接口在文档中出现，但当前未在 `mall-admin-biz` Controller 中确认，标记为待确认。

---

## 5. Pages and Routes

前端路由文件已位于 `mall-frontend/src/router/index.ts`；「Gateway / API」列只使用仓库已确认的路径。当前提交的实际路由少于本表定义的产品页面，未实现独立路由或仅以接口面板合并展示的页面，均不得标记为"完整完成"。

| 页面 | 前端路由 | 角色 | Gateway / API 依据 | 状态 |
|---|---|---|---|---|
| 登录页 | `/login` | 游客 | `POST /api/v1/auth/login` | 前端待确认 |
| 注册页 | `/register` | 游客 | `POST /api/v1/users/register` | 前端待确认 |
| 首页 / 类目入口 | `/` | 游客 / 用户 | `GET /api/v1/categories/tree` | 前端待确认 |
| 商品详情页 | `/products/:id` | 游客 / 用户 | `GET /api/v1/products/{id}` | 前端待确认 |
| 商品搜索页 | `/search` | 游客 / 用户 | `GET /api/v1/search/products`、`GET /api/v1/search/hot-words` | 前端待确认 |
| 购物车页 / 抽屉 | `/cart` 或全局 Drawer | 普通用户 | `GET/POST/PUT/PATCH/DELETE /api/v1/carts` | 前端待确认 |
| 用户资料页 | `/account/profile` | 普通用户 | `GET /api/v1/users/me`、`PUT /api/v1/users/me` | 前端待确认 |
| 地址管理页 | `/account/addresses` | 普通用户 | `GET/POST/PUT/DELETE /api/v1/users/me/addresses` | 前端待确认 |
| 订单创建 / 确认页 | `/checkout` | 普通用户 | `POST /api/v1/orders` | 前端待确认 |
| 订单详情页 | `/orders/:orderNo` | 普通用户 | `GET /api/v1/orders/{orderNo}` | 前端待确认 |
| 支付页 | `/pay/:orderNo` | 普通用户 | `POST /api/v1/pay/create`、`GET /api/v1/pay/record/{orderNo}` | 前端待确认 |
| 秒杀活动页 | `/seckill` | 游客 / 用户 | `GET /api/v1/seckill/activities` | 前端待确认 |
| 秒杀详情页 | `/seckill/:activityId` | 游客 / 用户 | `GET /api/v1/seckill/activities/{id}`、`POST /api/v1/seckill/{activityId}` | 前端待确认 |
| 秒杀结果页 / 状态块 | `/seckill/result/:requestId` | 普通用户 | `GET /api/v1/seckill/result/{requestId}` | 前端待确认 |
| 后台看板 | `/admin/dashboard` | 商家 / 管理员 | `GET /api/v1/admin/dashboard` | 前端待确认 |
| 后台订单 | `/admin/orders` | 商家 / 管理员 | `GET /api/v1/admin/orders`、`POST /api/v1/admin/orders/{orderNo}/ship` | 前端待确认 |
| 后台商品 | `/admin/products` | 商家 / 管理员 | `GET /api/v1/admin/products`、`/api/v1/admin/products/**` | 前端待确认 |

### 5.1 当前前端实现差距

截至 `codex/frontend-demo-system` 分支的第五阶段断点，`mall-frontend` 已具备基础工程、公共布局、路由守卫、接口封装和部分操作入口，但实际页面仍偏向接口调试面板。以下差距必须在后续前端整改中消除：

| 设计页面 | 当前实现 | 结论 |
|---|---|---|
| 注册页 `/register` | 合并在 `/login` 表单中 | 未完成独立页面 |
| 商品详情 `/products/:id` | 合并在首页，以 JSON 显示商品响应 | 未完成产品化页面 |
| 搜索页 `/search` | 合并在首页，以 JSON 显示搜索响应 | 未完成产品化页面 |
| 订单确认 `/checkout` | 购物车中直接输入 `addressId` 创建订单 | 未完成 |
| 订单详情 `/orders/:orderNo` | 合并在 `/orders`，以 JSON 显示订单响应 | 未完成产品化页面 |
| 支付页 `/pay/:orderNo` | 合并在 `/orders`，以 JSON 显示支付响应 | 未完成产品化页面 |
| 秒杀详情 `/seckill/:activityId` | 合并在 `/seckill`，手动输入 ID 查询 | 未完成 |
| 秒杀结果 `/seckill/result/:requestId` | 合并在 `/seckill`，未实现自动轮询 | 未完成 |
| 后台看板 `/admin/dashboard` | 合并在 `/admin`，以 JSON 显示响应 | 未完成产品化页面 |
| 后台订单 `/admin/orders` | 合并在 `/admin`，缺少结构化订单管理页面 | 未完成 |
| 后台商品 `/admin/products` | 合并在 `/admin`，缺少结构化商品管理页面 | 未完成 |

允许保留技术演示页中的接口响应调试信息，但业务页面不得以 raw JSON 作为主要内容。

---

### 6.1 登录页

- 输入用户名、密码和可选 `loginType`；
- 提交时按钮进入 disabled，避免重复提交；
- 成功后保存 Token，并按来源跳转回原页面或进入默认首页；
- 失败时展示后端错误，不把错误密码静默重试；
- 退出登录调用 `POST /api/v1/auth/logout`，并清理本地 Token。

### 6.2 公共浏览和搜索

- 类目树加载失败时展示可重试错误状态；
- 商品详情必须展示商品基础信息、SKU、价格和加入购物车入口；
- SKU 不可售或无库存时必须禁用对应选择，不允许继续下单；
- 搜索页支持关键字、页码和页大小；筛选项是否存在待确认；
- 热词为空时展示 empty，不伪造热门词。

### 6.3 购物车

- 支持加入、查询、修改数量、勾选、删除；
- 数量修改时禁用当前行操作，接口返回后刷新小计；
- 删除前是否二次确认待确认；
- 商品远程查询失败时购物车应显示明确错误或不可用状态，不伪造商品名称和价格；
- 未登录访问购物车必须引导登录。

### 6.4 订单

- 创建订单前必须确认地址和订单项；
- 创建订单提交期间禁用提交按钮；
- 库存不足、商品不存在、远程调用失败必须展示明确失败原因；
- 成功后展示 `orderNo`，并允许进入订单详情或支付；
- 查询订单只能展示当前用户有权访问的订单。

### 6.5 支付

- 支付页只展示模拟支付，不出现真实支付渠道承诺；
- 支付创建成功后展示支付记录或下一步模拟通知入口；
- 支付通知成功后引导用户重新查询订单状态；
- 重复通知或状态非法时按后端返回展示，不在前端强制改为成功。

### 6.6 秒杀

- 活动未开始、已结束、售罄、限购和限流必须有独立反馈文案；
- 发起秒杀后展示 `requestId` 对应的处理中状态，并轮询结果；
- 轮询间隔、超时次数待确认；
- 接口返回失败时停止轮询并展示失败原因。

### 6.7 后台

- 看板数据加载中展示骨架或加载状态；
- 订单发货提交前必须确认必要信息；
- 商品创建、编辑、上下架、删除必须展示成功或失败结果；
- 角色权限边界未完成验证前，不把方法级 RBAC 写成已实现。

---

## 7. UI States

### Loading

- 页面级加载：首次进入页面时显示结构稳定的 Loading，不造成布局跳动；
- 区块级加载：列表、详情、看板、购物车和订单区域可独立加载；
- 提交中：登录、加入购物车、创建订单、支付、秒杀、发货、商品维护等写操作必须禁用触发按钮；
- Loading 不能使用动画；只能使用静态文本、静态骨架或瞬时状态切换。

### Empty

- 商品搜索无结果：展示空结果和调整关键字提示；
- 购物车为空：展示继续浏览商品入口；
- 地址为空：展示新增地址入口；
- 订单为空：展示返回商品或购物车入口；
- 秒杀活动为空：展示暂无活动；
- 后台列表为空：展示空列表，不构造假数据。

### Error

- 认证错误：区分未登录、Token 无效、Token 过期；
- 业务错误：展示后端业务码对应的明确原因，如库存不足、商品不存在、订单状态非法；
- 远程调用失败：提示服务暂不可用，并允许重试或返回上一步；
- 表单错误：在字段附近展示校验信息；
- 不隐藏失败请求，不只判断 HTTP 200。

### Disabled

- 未登录用户禁用受限操作，并提供登录入口；
- 库存不足、SKU 不可售、活动未开始/已结束时禁用对应主操作；
- 提交中禁用重复提交；
- Disabled 不使用 `opacity` 表达，必须使用 token 指定的文本、背景和边框状态。

### Success

- 登录成功：进入目标页面并保存用户状态；
- 加入购物车成功：提示成功，并允许查看购物车；
- 创建订单成功：展示 `orderNo` 和下一步支付入口；
- 支付处理成功：展示支付结果，并引导查询订单；
- 后台写操作成功：刷新列表或详情；
- 成功状态必须来自接口返回，不由前端本地假定。

---

## 8. Roles and Permissions

| 功能区 | 游客 | USER | MERCHANT | ADMIN |
|---|---|---|---|---|
| 登录 / 注册 | 可访问 | 可访问 | 可访问 | 可访问 |
| 类目、商品详情、搜索 | 可访问 | 可访问 | 可访问 | 可访问 |
| 用户资料和地址 | 不可访问 | 可访问 | 待确认 | 待确认 |
| 购物车 | 不可访问 | 可访问 | 待确认 | 待确认 |
| 创建和查询本人订单 | 不可访问 | 可访问 | 待确认 | 待确认 |
| 支付和支付记录 | 不可访问 | 可访问 | 待确认 | 待确认 |
| 秒杀发起和结果 | 待确认 | 可访问 | 待确认 | 待确认 |
| 后台看板 | 不可访问 | 不可访问 | 可访问，待验证 | 可访问，待验证 |
| 后台订单和商品管理 | 不可访问 | 不可访问 | 可访问，待验证 | 可访问，待验证 |

当前仓库可确认 Gateway 会校验 JWT 并透传 `X-User-Id`、`X-User-Roles`。方法级权限和后台登录闭环未完成代码级确认前，必须标记为待确认。

---

## 9. Responsive Behavior

| 断点 | 屏幕宽度 | 容器宽度 | 行为 |
|---|---:|---:|---|
| `sm` | `< 640px` | `100%` | 单列布局；购物车、筛选和后台操作优先用 Drawer 或纵向区块 |
| `md` | `640-1024px` | `720px` | 商品和后台列表可使用双列或紧凑表格 |
| `lg` | `1024-1280px` | `960px` | 商品信息、购物车摘要、订单摘要可左右分栏 |
| `xl` | `>= 1280px` | `1200px` | 后台列表和看板使用更高信息密度 |

响应式约束：

- 文字不得溢出按钮、卡片、表格单元格或 Drawer；
- 关键操作在移动端必须可见，不依赖 hover；
- 表格在窄屏应支持横向滚动或折叠为列表；
- 弹窗和抽屉不能超出视口；
- 不使用 viewport 字号缩放制造响应式效果。

---

## 10. Design System Constraints

以下视觉规则来自原 `DESIGN.md`，保留为 MallCloud 前端实现的设计系统基线。当前采用 `mall-frontend` 单应用，不拆 `web-portal` / `web-admin`，必须复用本节 token 和 Element Plus 映射，不另起一套组件库或设计系统。

### 10.1 设计原则

- **极简线框隐喻**：以高纯度白色为基底，电光蓝作为线条与视觉骨架。去除所有阴影、过渡、缓动，仅靠清晰的 1px 边框和网格划分空间。
- **克制的品牌色**：蓝色仅用于交互焦点、核心操作和关键装饰线，绝不大面积高饱和色块。
- **零动效 / 静态切换**：状态变化通过瞬时的颜色、边框、文字色切换实现，不使用 `transition`、`animation`、`transform` 缓动。`transition: all` 永久禁用。
- **Token 驱动**：间距、高度、颜色、圆角全面变量化。业务代码中避免硬编码颜色、间距和圆角。
- **直觉式电商交互**：商品、购物车、结算链路保持最高视觉优先级，弱化非核心装饰。

### 10.2 色彩与 Token

| Token | 值 | 用途 |
|---|---|---|
| `--color-text-primary` | `#070b19` | 基础文本色 |
| `--color-text-secondary` | `#5c678a` | 次级文本色 |
| `--color-text-tertiary` | `#8b96b5` | 第三级辅助文本色 |
| `--color-text-inverse` | `#ffffff` | 反色文本 |
| `--color-brand` | `#002bf0` | 品牌主色 |
| `--color-brand-light` | `color-mix(in srgb, var(--color-brand) 8%, var(--color-surface))` | 品牌暗示高亮 |
| `--color-bg` | `#ffffff` | 页面全局基础底色 |
| `--color-surface` | `#ffffff` | 组件表面色 |
| `--color-surface-hover` | `#f4f7fc` | Hover 态极浅冷灰蓝背景 |
| `--color-surface-muted` | `#ebf0f8` | Active 态、次级按钮底色 |
| `--color-border` | `#d6e0f5` | 标准边框色 |
| `--color-border-strong` | `var(--color-brand)` | 强边框色 |
| `--color-error` | `#e11d48` | 错误 / 告警 |
| `--color-success` | `#16a34a` | 成功 |
| `--color-warning` | `#d97706` | 警告 |
| `--mask-overlay` | `color-mix(in srgb, var(--color-text-primary) 20%, transparent)` | 浅层遮罩 |

| 语义 Token | 映射 | 用途 |
|---|---|---|
| `--color-price` | `var(--color-text-primary)` | 默认价格 |
| `--color-price-action` | `var(--color-brand)` | 促销 / 强调价格 |
| `--color-cart-badge` | `var(--color-brand)` | 购物车角标 |
| `--color-sku-border` | `var(--color-border)` | SKU 默认边框 |
| `--color-sku-border-active` | `var(--color-brand)` | SKU 选中态边框 |

### 10.3 间距、圆角、字体和层级

| Token | 值 | 用途 |
|---|---:|---|
| `--spacing-xs` | `4px` | 极小间距 |
| `--spacing-sm` | `8px` | 小间距 |
| `--spacing-md` | `16px` | 中间距 |
| `--spacing-lg` | `24px` | 大间距 |
| `--spacing-xl` | `32px` | 页面级留白 |

| Token | 值 | 用途 |
|---|---:|---|
| `--radius-sm` | `2px` | 小标签、Checkbox |
| `--radius-md` | `4px` | Button、Input |
| `--radius-lg` | `8px` | 商品卡片、Dialog、Dropdown |

| Class | 像素值 | 用途 |
|---|---:|---|
| `.text-xs` | `12px` | 标签、辅助说明 |
| `.text-sm` | `14px` | 基础正文、表单输入 |
| `.text-base` | `16px` | 商品标题、主按钮、价格 |
| `.text-lg` | `18px` | 模块小标题、订单总价 |
| `.text-xl` | `20px` | 商品详情标题、弹窗标题 |
| `.text-2xl` | `24px` | 页面主标题 |

- 文本字体：`Inter` / `PingFang SC` / `Microsoft YaHei` / sans-serif；
- 数据、订单号、SKU、价格数字：`JetBrains Mono` / `Consolas` / monospace；
- Z 轴：Dialog `101` < Dropdown `105` < Tooltip `110`。

### 10.4 Element Plus 主题映射

Element Plus 内置组件必须与 token 对齐；若未来创建 `web-portal` / `web-admin`，应在各自 `src/styles/element-theme.css` 中统一覆盖：

```css
:root {
  --el-color-primary: var(--color-brand);
  --el-color-danger: var(--color-error);
  --el-color-success: var(--color-success);
  --el-color-warning: var(--color-warning);
  --el-color-info: var(--color-text-secondary);

  --el-text-color-primary: var(--color-text-primary);
  --el-text-color-regular: var(--color-text-secondary);
  --el-text-color-secondary: var(--color-text-tertiary);
  --el-text-color-placeholder: var(--color-text-tertiary);

  --el-bg-color: var(--color-bg);
  --el-bg-color-page: var(--color-bg);
  --el-fill-color-blank: var(--color-surface);
  --el-fill-color-light: var(--color-surface-hover);
  --el-fill-color-lighter: var(--color-surface-muted);

  --el-border-color: var(--color-border);
  --el-border-color-light: var(--color-border);
  --el-border-color-lighter: var(--color-border);
  --el-border-color-extra-light: var(--color-border);

  --el-border-radius-base: var(--radius-md);
  --el-border-radius-small: var(--radius-sm);

  --el-transition-duration: 0s;
  --el-transition-duration-fast: 0s;
  --el-transition-all: none;
  --el-transition-fade: none;
  --el-transition-md-fade: none;
  --el-transition-fade-linear: none;
  --el-transition-border: none;
  --el-transition-box-shadow: none;
  --el-transition-color: none;
}
```

### 10.5 组件规则

- Button：Primary 用品牌蓝背景和品牌蓝边框；Outline 用透明背景和标准边框；Disabled 用 muted 背景和 tertiary 文本，不使用 `opacity`。
- SKU 选择器：默认 1px 边框；选中态品牌蓝边框和角标；禁用态 muted 背景、not-allowed 光标、斜线标识。
- Drawer / Dialog / Dropdown / Tooltip：背景使用 `var(--color-bg)`，用 1px 边框区分层级，禁止阴影和过渡。
- Input / Select / Checkbox：使用 token 边框、圆角和焦点边框；焦点变化瞬时生效。
- Table / Tag / Badge：表头、悬停、标签颜色统一走 Element Plus token 映射。

### 10.6 Red Lines

1. 禁止任何动效、过渡、缓动：不得使用 `transition`、`animation`、`@keyframes`、hover 形变。
2. 禁止弥散光和厚重阴影：层级只能靠 1px 边框和颜色对比。
3. 禁止用 `opacity` 表达 Disabled。
4. 禁止规范外字号和负字距。
5. 禁止业务 CSS 写入全局；全局样式只放 token、reset、Element Plus 主题映射。
6. 禁止引入平行组件库或平行设计系统，除非明确批准。
7. 禁止用前端 mock 把后端未实现能力展示为已完成。

### 10.7 工程集成清单

当前前端全局样式位于 `mall-frontend/src/styles/`，入口文件 `mall-frontend/src/main.ts` 使用同一顺序导入：

```ts
import './styles/reset.css';
import './styles/tokens.css';
import './styles/element-theme.css';
```

| 步骤 | 动作 | 文件 |
|---|---|---|
| 1 | 引入全局重置和禁动效 | `mall-frontend/src/styles/reset.css` |
| 2 | 引入 token 变量 | `mall-frontend/src/styles/tokens.css` |
| 3 | 覆盖 Element Plus 主题 | `mall-frontend/src/styles/element-theme.css` |
| 4 | 在前端入口导入全局样式 | `mall-frontend/src/main.ts` |
| 5 | 业务组件使用局部样式或 scoped 样式 | 具体组件路径待确认 |

---

## 11. Accessibility Requirements

- 所有表单控件必须有可感知 label 或等价的 `aria-label`；
- 错误信息必须与字段或操作区域相邻，不能只依赖颜色；
- 主操作、链接、表单、弹窗关闭、Drawer 关闭必须支持键盘操作；
- Focus 状态必须可见，使用 `--color-border-strong` 或等价 token；
- 禁用状态必须同时通过视觉和控件状态表达；
- 商品图片、图标按钮和状态图标必须有文本替代或隐藏策略；
- 表格和列表必须保留清晰表头或上下文；
- 颜色对比度需满足常规正文可读性，不用浅蓝色承载关键信息；
- Loading、Empty、Error、Success 不依赖动画或颜色单独传达。

---

## 12. Service and API Dependencies

本节只描述页面依赖哪个服务及所需能力，不虚构字段或返回值。

| 页面 / 功能 | 依赖服务 | 所需能力 |
|---|---|---|
| 登录、刷新、退出 | `mall-auth`、`mall-user`、`mall-gateway` | 账号密码校验、Token 生成/刷新/黑名单、用户信息查询、JWT 校验与 Header 透传 |
| 注册、资料、地址 | `mall-user`、`mall-gateway` | 用户注册、当前用户资料、资料更新、地址增删改查 |
| 类目和商品详情 | `mall-product`、`mall-gateway` | 类目树、商品详情、SKU 信息；商品列表接口是否存在待确认 |
| 搜索和热词 | `mall-search`、`mall-gateway`、`mall-product` | 商品搜索、热词、商品同步或索引初始化 |
| 购物车 | `mall-cart`、`mall-product`、`mall-gateway` | 加入、查询、修改数量、勾选、删除；购物车商品信息远程查询 |
| 创建订单和查询订单 | `mall-order`、`mall-product`、`mall-inventory`、`mall-gateway` | 创建订单、查询订单、查询 SKU、锁定库存、用户上下文读取 |
| 支付 | `mall-pay`、`mall-message`、`mall-order`、`mall-inventory`、`mall-gateway` | 创建支付记录、支付通知、PAY_RESULT 消费、订单状态更新、库存确认扣减 |
| 秒杀 | `mall-seckill`、`mall-message`、`mall-order`、`mall-gateway`、`Redis`、`Sentinel` | 活动查询、发起秒杀、结果轮询、限购、库存边界、限流、异步创建秒杀订单 |
| 后台看板 | `mall-admin-biz`、`mall-order`、`mall-product`、`mall-gateway` | 聚合看板、订单统计、商品统计 |
| 后台订单 | `mall-admin-biz`、`mall-order`、`mall-gateway` | 订单列表、发货 |
| 后台商品 | `mall-admin-biz`、`mall-product`、`mall-gateway` | 商品列表、创建、编辑、删除、上下架 |

---

## 13. Acceptance Criteria

### 13.1 文档和设计基线

- `DESIGN.md` 包含 Product Goal、Target Users and Roles、Information Architecture、Core User Flows、Pages and Routes、Page-level Interactions、UI States、Roles and Permissions、Responsive Behavior、Design System Constraints、Accessibility Requirements、Service and API Dependencies、Acceptance Criteria、Open Questions、Decision Log；
- 所有不能从代码、README 或现有文档确认的页面、路由、权限和前端路径均标记为 `待确认`；
- 设计文档中的服务名、角色名和 API 能在 `docs/`、Gateway 配置或 Controller 中找到依据；
- 不把 Docker/K8s 全栈、真实支付、方法级 RBAC、商品列表页、前端源码或未验证核心链路写成已完成。

### 13.2 前端实现验收

- 用户面对的页面、组件、路由、交互变更前必须先核对 `DESIGN.md`；
- 每个写操作至少覆盖 loading、disabled、success、error；
- 每个列表或集合页面至少覆盖 loading、empty、error；
- 受限页面在无 Token 和错误 Token 下不能展示受保护数据；
- 商品、订单、支付、秒杀成功状态必须来自接口返回；
- 浏览器可用时，用户可见变更必须用真实浏览器验证。

### 13.3 核心流程验收

- 登录成功后受限接口携带 Token 可访问；
- 无 Token 和错误 Token 访问订单等受限接口返回未授权；
- 商品详情和搜索不要求登录；
- 加入购物车、创建订单、支付、秒杀请求在未登录时不可直接成功；
- 创建订单成功时可获取订单号；
- 库存不足、商品不存在、订单状态非法等错误有明确展示；
- 后台商品和订单操作不越过既有角色和服务边界。

### 13.4 设计系统验收

- 页面实现复用 Element Plus 和项目 token；
- 不引入第二套组件库或独立主题；
- 不使用动画、过渡、hover 形变、厚重阴影；
- Disabled 不用 `opacity` 表达；
- 文本、按钮、表格、弹窗、Drawer 在移动端和桌面端不重叠、不溢出。

### 13.5 Frontend Page Completion Matrix

| 页面 | 必须具备 | 不合格表现 |
|---|---|---|
| 首页 / 商品浏览 | 类目入口、商品卡片、商品详情入口、搜索入口、登录状态入口 | 只显示接口 JSON |
| 商品详情 | 商品标题、价格、SKU、库存状态、数量、加入购物车、错误状态 | 只输入 SPU/SKU 调接口 |
| 搜索页 | 关键字、热词、结果列表、分页、空结果、错误状态 | 只显示搜索 JSON |
| 登录页 | 登录表单、错误提示、跳转来源、Token 状态 | 只有默认账号或无错误状态 |
| 注册页 | 独立注册表单、字段校验、成功后登录引导 | 混在登录页且无校验 |
| 账户页 | 用户资料、地址列表、新增、编辑、删除入口、空状态 | 只有简单字段和 ID |
| 购物车 | 商品行、价格、小计、勾选、数量、删除、结算、空状态 | 只显示表格且无状态控制 |
| 订单确认 | 地址选择、订单项确认、金额汇总、提交状态 | 用地址 ID 直接下单 |
| 订单详情 | 订单状态、商品明细、金额、支付入口、错误状态 | 只显示订单 JSON |
| 支付页 | 支付记录、模拟支付说明、通知结果、查询订单入口 | 只显示支付 JSON |
| 秒杀页 | 活动列表、活动详情、状态文案、发起请求、结果轮询 | 手动输入 ID 调接口 |
| 后台看板 | 统计卡片、订单入口、商品入口、错误状态 | 只显示 dashboard JSON |
| 后台订单 | 订单表格、状态、发货操作、提交反馈 | 只输入订单号发货 |
| 后台商品 | 商品表格、上下架 / 编辑 / 删除状态说明 | 只显示商品 JSON |
| 技术演示页 | 技术点说明、可验证入口、未验证标记 | 把业务页面做成技术页 |

前端验收时必须逐行填写实现状态、证据路径和未完成原因。未满足本矩阵的页面只能标记为"部分实现"或"未完成"。

---

## 14. Open Questions

- 已确认：当前仓库采用 `mall-frontend` 单应用前端工程，不拆 `web-portal` / `web-admin`。
- 待确认：前端正式路由命名是否采用本文件建议路径，或已有团队约定。
- 待确认：`README.md` 声明的 Vue 3 / Vite / Element Plus / Axios / Pinia 前端栈是否已经存在于未提交或外部目录。
- 待确认：商品列表接口在 `docs/API.md` 中出现，但当前 `ProductController` 未确认 `GET /api/v1/products` 外部列表接口。
- 待确认：独立后台登录 `POST /api/v1/admin/auth/login` 在 `docs/API.md` 中出现，但当前 `mall-admin-biz` Controller 未确认该接口。
- 待确认：`MERCHANT` 与 `ADMIN` 在后台接口上的精确权限边界和是否存在方法级权限控制。
- 待确认：秒杀轮询间隔、轮询超时和失败重试策略。
- 待确认：购物车删除、订单发货、商品删除等破坏性操作是否要求二次确认。
- 待确认：秒杀活动是否允许游客浏览；当前 Gateway 白名单未开放 `/api/v1/seckill/**`。
- 待确认：搜索页筛选、排序、分页展示规则。
- 待确认：支付页是否需要人工触发模拟通知入口，或仅由后端测试工具触发。
- 待确认：前端是否需要分别实现前台和后台两个应用，或单应用按路由分区。

---

## 15. Decision Log

| 日期 | 决策 | 依据 |
|---|---|---|
| 2026-06-06 | 采用白蓝线条极简风、零动效、Token 驱动、Element Plus 主题覆盖 | 原 `DESIGN.md` |
| 2026-06-07 | `DESIGN.md` 升级为产品和 UI 设计基线，不只作为视觉样式规范 | 本次 Product Design 审查 |
| 2026-06-07 | 前端源码、路由、页面和 token 文件未在当前仓库检出，相关路径统一标记为待确认 | 文件系统检查 |
| 2026-06-07 | 页面和 API 依赖只绑定当前文档、Gateway 配置和 Controller 可确认能力 | `docs/API.md`、`deploy/nacos/mall-gateway.yaml`、各服务 Controller |
| 2026-06-07 | 不把商品列表、后台独立登录、方法级 RBAC、核心链路运行结果写成已完成 | 当前代码与项目标准要求 |
| 2026-06-08 | 新增 `mall-frontend` 单应用基础工程、接口入口和技术演示断点 | 第五阶段提交 `483e433 feat(frontend): add mallcloud demo app` |
| 2026-06-08 | 复查后确认当前前端不能作为完整产品化页面交付，需按 Frontend Page Completion Matrix 整改 | 前端页面质量审查 |
