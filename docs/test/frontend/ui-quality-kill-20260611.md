# 前端质量全面消杀审查与执行报告

**审查时间**：2026-06-11
**目标模块**：`mall-frontend`
**执行动作**：移除假路由、假数据状态、动效违规，执行真实接口适配

## 一、 遗留毛坯房问题消杀

### 1. 假路由与无用依赖清理
- [x] `App.vue` 移除未实现路由的假入口（`/coupons`, `/points`, `/help`, `/account/wishlist`），确保全站可达。
- [x] 移除对外部 `dicebear.com` 虚拟头像的依赖，使用 `Element Plus` 原生图标 `<UserFilled>`。
- [x] 移除写死的购物车数量徽标，保留真实业务的跳转路径。
- [x] 更新 `App.vue` 中的固定用户名（现已绑定全局 AuthStore 真实上下文，未登录显示“请登录”）。

### 2. 首页假状态下线
- [x] **Seckill 降级策略**：调用了 `mallApi.seckillActivities()` 真实接口以渲染限时秒杀内容。当无活动或后端返回错误时，全面隐藏写死的商品数据，展示“秒杀服务暂不可用或当前无活动”的空白页面提示，并保留引流按钮。倒计时功能现已接入真实活动的 `endTime`。
- [x] **TechView 看板还原**：移除了主页底部技术栈面板虚假的“32个实例”、“99.96% 成功率”硬编码。现将其还原为引流入口，导向统一管理平台。

## 二、 动效规范落实

根据“**零动效、白蓝线条**”的设计规范约束，我们在全站域进行了动效代码排查与消杀。

### 消杀检查记录
执行 `rg "transition|animation|transform|@keyframes"`：
- 确认组件 CSS (`element-theme.css`, `reset.css`) 中全面覆盖 `transition: none !important;`
- 消杀 `HomeView.vue` 内部的 `.product-card:hover`、`.tab-btn` 等非必需的 `transition: all 0.2s` 和 `transform: translateY(-2px)`。

## 三、 全局 CSS 变量合规化

所有在 vue 模板及 CSS 中散落的“魔法数值/硬编码颜色”均被收口。

### 重点修复内容：
- [x] 在 `tokens.css` 中补齐 `var(--color-text-muted)`、`var(--color-error-light)` 语义变量。
- [x] 提取了 Hero 区的 `linear-gradient` 至 `var(--bg-gradient-hero)`。
- [x] 将所有 `.vue` 单文件组件中存在的直接颜色调用（如 `#1b61c9`, `#ff4141`）彻底转为 CSS var 格式，保障规范一致性。

## 四、 页面结构精细化处理

- **TechView（技术演示页面）**：对后端原始 JSON 数据展示区实施了改造，移除了杂乱无章的平铺，将各项微服务指标（网关、注册中心、限流等）隔离为独立的卡片组件，并通过 `<el-collapse>` 掩藏了过长的响应数据，大幅提高页面的系统化管理观感。
- **AdminView（系统后台）**：确认已使用 `<el-card>` 容器实现布局封装和基础监控指标。

## 五、 测试结论

通过上述消杀操作，目前系统已彻底摒除前期快速迭代留下的“毛坯感”和“假交互”。当前前端视图数据表现与本地/测试环境后端的运行数据真实绑定。
UI 严格遵守白蓝主调与零动效约束准则，静态及编译检查已全量通过。
