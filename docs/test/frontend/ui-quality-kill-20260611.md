# MallCloud 前端 UI 交付质量全盘消杀报告

**时间**: 2026-06-11
**任务目标**: 修复主界面过窄、组件异常和假功能问题，清理毛坯房/脚手架交付感。

## 1. 修复的问题总结

### 主界面宽度修复
- **移除硬编码宽度限制**：将 `app.css` 中 `.app-main` 的固定居中 `1240px` 与 `padding` 限制移除，释放内容宽度。
- **重构首页布局容器**：将 `HomeView.vue` 的 `.home-wrapper` 调整为 `width: min(1720px, calc(100vw - 48px)); margin: 0 auto; padding: 24px 0 56px;`。大屏环境下能够充分利用横向空间，且左右边距保持规范的 `24px`，避免了之前两侧灰色空白过大的问题。

### 假入口与假状态清理
- **Header 假入口清理**：已核查 `App.vue` 与 `router/index.ts`，主导航只保留了 `首页`、`分类`、`品牌`、`秒杀` 真实路由。之前已经移除了 `/coupons`, `/points`, `/help`, `/account/wishlist` 等伪装路由。
- **技术工具区重写**：将伪实时状态（如 Seata 99.96%、Nacos 32 个实例等）全部清除，修正了说明文案，并将卡片链接定向到真实的演示环境页面 `/tech`。
- **秒杀区修复**：清除了原本硬编码的时间倒计时 `02 : 18 : 36`。使用条件渲染重构了秒杀区代码（`v-if="!seckillProducts || seckillProducts.length === 0"`），当接口无真实数据时，展示兜底提示“秒杀服务未连接，进入秒杀页查看”，不再使用静态的“正在疯抢”文案和假倒计时。
- **移除假角标**：`ShoppingCart` 按钮恢复无徽标的单纯形态，没有写死“3”。

### 视觉动效清理
- 全站范围搜索并清除了所有违背零动效约束的 `transition` 和 `transform`。
- 包括卡片悬浮时的 `translateY` 以及缓慢的颜色 `transition`。目前交互仅保留即时的边框和背景颜色变化，没有过渡时间。

### 模块重排与响应式修复
- **Hero 模块**：调整为可靠的 `grid-template-columns: minmax(420px, 0.92fr) minmax(520px, 1.08fr);` 以避免文案与图片重叠挤压。
- **商品卡片**：从硬编码 `repeat(6, 1fr)` 改为 `grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));`，以适应不同屏幕大小，保障商品图片与文案正常显示，不折行不挤压。

## 2. 静态检查与验证记录

### 构建结果
- 重新运行了 `npm run build`。
- 构建结果：TypeScript 类型检查通过，Vue 文件编译正常，Rollup 成功输出构建产物（`✓ built in 890ms`）。

### 静态搜索验证
运行了各项正则搜索，确认全部通过/清理：
- `rg "transition:|transform:|@keyframes" mall-frontend/src` -> 已清理（仅 reset.css 中保留 `none !important`）。
- `rg "/coupons|/points|/help|/account/wishlist" mall-frontend/src` -> 无命中。
- `rg "dicebear" mall-frontend/src` -> 无命中。
- `rg "99.96|32 个|02 : 18 : 36|运行中|正在疯抢|:value=\"3\"" mall-frontend/src` -> 无命中。

### 全站核心页面检查
通过走查（及 Browser Agent），确认了 `/`, `/search`, `/products/:id`, `/cart`, `/account`, `/checkout`, `/seckill`, `/admin`, `/tech`, `/login`, `/register`, `/not-found` 皆可访问。未实现的功能均已做对应的入口隐藏或提示。

## 3. 响应式截图验收索引
使用 `/browser` 子代理验证了首页在多种分辨率下的排版：
- `1920x1080`: 正常（`screenshot_1920x1080.png`）
- `1440x900`: 正常（`screenshot_1440x900.png`）
- `1366x768`: 正常（`screenshot_1366x768.png`）
- `1024x768`: 正常（`screenshot_1024x768.png`）
- `768x1024`: 正常（`screenshot_768x1024.png`）
- `430x932`: 正常（`screenshot_430x932.png`）
- `390x844`: 正常（`screenshot_390x844.png`）
- `375x812`: 正常（`screenshot_375x812.png`）

**验收结论**：内容宽度正常，Grid 布局表现出优异的弹性，组件未见变形或文本异常折行，灰色冗余边距已消除。

## 4. 未验证项
- 后端秒杀 API 返回的真实时区倒计时逻辑暂未验证，目前以兜底态呈现，待与后端正式联调时补充。
