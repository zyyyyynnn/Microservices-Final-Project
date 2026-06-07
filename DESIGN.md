# UI 设计规范 (E-Commerce 极简线框版)

> 项目阶段：从零到一构建期
> 技术栈：Vue 3 + Vite + Element Plus
> 设计语言：**白蓝线条极简风**（White-Blue Lineart Minimalist）
> 核心原则：**零动效**（no transition / no animation / no easing）
> 最后更新：2026-06-06

---

## 1. 设计原则

- **极简线框隐喻**：以高纯度白色为基底，电光蓝作为线条与视觉骨架。去除所有阴影、过渡、缓动，仅靠清晰的 1px 边框和网格划分空间。
- **克制的品牌色**：蓝色仅用于交互焦点、核心操作（"加入购物车"）和关键装饰线，绝不大面积高饱和色块。
- **零动效 / 静态切换**：状态变化（Hover / Active / Focus / Open）通过**瞬时**的颜色 / 边框 / 文字色切换实现，**不**使用 `transition`、`animation`、`transform` 缓动。`transition: all` 永久禁用。
- **Token 绝对驱动**：间距、高度、颜色、圆角全面变量化。业务代码中禁止硬编码 `px`、`#hex`、`white`、`rgba`。即使是纯白背景，也必须调用 `var(--color-bg)`。
- **直觉式电商交互**：商品列表 → 详情 → 购物车 → 结算链路保持最高视觉优先级，弱化一切非核心装饰。

---

## 2. 色彩与 Token 体系

### 2.1 基础色彩 Token (Base Tokens)

**所有色彩禁止使用 `#hex` 写法和原生 `rgba()` 写法**。透明度叠加强制走 `color-mix(in srgb, var(--color-xxx) X%, transparent)`。

| Token | 值 | 用途 |
|-------|-----|------|
| `--color-text-primary` | `#070b19` | 基础文本色（极深的海军蓝黑，代替死黑） |
| `--color-text-secondary` | `#5c678a` | 次级文本色（商品副标题、规格说明） |
| `--color-text-tertiary` | `#8b96b5` | 第三级辅助文本色（占位符、失效状态） |
| `--color-text-inverse` | `#ffffff` | 反色文本（深色背景上的文字、品牌按钮文字） |
| `--color-brand` | `#002bf0` | 品牌主色（电光蓝，控制使用面积） |
| `--color-brand-light` | `color-mix(in srgb, var(--color-brand) 8%, var(--color-surface))` | 品牌暗示高亮（选中态商品卡片底色） |
| `--color-bg` | `#ffffff` | 页面全局基础底色（纯白） |
| `--color-surface` | `#ffffff` | 组件表面色（与背景同色，靠边框区分层级） |
| `--color-surface-hover` | `#f4f7fc` | Hover 态极浅冷灰蓝背景 |
| `--color-surface-muted` | `#ebf0f8` | Active 态、次级按钮底色 |
| `--color-border` | `#d6e0f5` | 标准边框色（轻盈的浅冷灰蓝） |
| `--color-border-strong` | `var(--color-brand)` | 强边框色（输入框激活、选中态边框） |
| `--color-error` | `#e11d48` | 错误 / 告警 / 降价标签 |
| `--mask-overlay` | `color-mix(in srgb, var(--color-text-primary) 20%, transparent)` | 浅层半透明遮罩 |

### 2.2 业务语义映射

| 语义 Token | 映射 | 用途 |
|---|---|---|
| `--color-price` | `var(--color-text-primary)` | 价格默认展示为极深色 |
| `--color-price-action` | `var(--color-brand)` | 促销 / 强调价格 |
| `--color-cart-badge` | `var(--color-brand)` | 购物车角标 |
| `--color-sku-border` | `var(--color-border)` | SKU 选择框默认 |
| `--color-sku-border-active` | `var(--color-brand)` | SKU 选中态 |

### 2.3 间距 Token (Spacing)

| Token | 值 | 用途 |
|-------|-----|------|
| `--spacing-xs` | `4px` | 极小间距（价格与标签） |
| `--spacing-sm` | `8px` | 小间距（卡片内部、表单上下） |
| `--spacing-md` | `16px` | 中间距（基础留白） |
| `--spacing-lg` | `24px` | 大间距（区块分隔） |
| `--spacing-xl` | `32px` | 页面级留白 |

### 2.4 圆角 Token (Radius)

| Token | 值 | 用途 |
|-------|-----|------|
| `--radius-sm` | `2px` | 微圆角（Checkbox、小标签） |
| `--radius-md` | `4px` | 标准圆角（Button、Input） |
| `--radius-lg` | `8px` | 容器圆角（商品卡片、Dialog、Dropdown） |

### 2.5 Element Plus 主题映射

**前端栈基于 Element Plus**，所有 Element Plus 内置组件的视觉必须与本 Token 体系对齐。在 `web-portal` / `web-admin` 的 `src/styles/element-theme.css` 统一覆盖：

```css
:root {
  /* 品牌色 */
  --el-color-primary: var(--color-brand);
  --el-color-primary-light-3: color-mix(in srgb, var(--color-brand) 70%, var(--color-surface));
  --el-color-primary-light-5: color-mix(in srgb, var(--color-brand) 50%, var(--color-surface));
  --el-color-primary-light-7: color-mix(in srgb, var(--color-brand) 30%, var(--color-surface));
  --el-color-primary-light-8: var(--color-brand-light);
  --el-color-primary-light-9: color-mix(in srgb, var(--color-brand) 10%, var(--color-surface));
  --el-color-primary-dark-2: color-mix(in srgb, var(--color-brand) 80%, var(--color-text-primary));

  /* 语义色 */
  --el-color-danger: var(--color-error);
  --el-color-success: #16a34a;
  --el-color-warning: #d97706;
  --el-color-info: var(--color-text-secondary);

  /* 中性色 */
  --el-text-color-primary: var(--color-text-primary);
  --el-text-color-regular: var(--color-text-secondary);
  --el-text-color-secondary: var(--color-text-tertiary);
  --el-text-color-placeholder: var(--color-text-tertiary);

  /* 背景 */
  --el-bg-color: var(--color-bg);
  --el-bg-color-page: var(--color-bg);
  --el-fill-color-blank: var(--color-surface);
  --el-fill-color-light: var(--color-surface-hover);
  --el-fill-color-lighter: var(--color-surface-muted);

  /* 边框 */
  --el-border-color: var(--color-border);
  --el-border-color-light: var(--color-border);
  --el-border-color-lighter: var(--color-border);
  --el-border-color-extra-light: var(--color-border);

  /* 圆角 */
  --el-border-radius-base: var(--radius-md);
  --el-border-radius-small: var(--radius-sm);

  /* === 关键：禁用 Element Plus 默认过渡 === */
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

### 2.6 CSS 变量声明块（`src/styles/tokens.css`）

```css
:root {
  /* 文本 */
  --color-text-primary: #070b19;
  --color-text-secondary: #5c678a;
  --color-text-tertiary: #8b96b5;
  --color-text-inverse: #ffffff;

  /* 品牌 */
  --color-brand: #002bf0;
  --color-brand-light: color-mix(in srgb, #002bf0 8%, #ffffff);

  /* 背景与表面 */
  --color-bg: #ffffff;
  --color-surface: #ffffff;
  --color-surface-hover: #f4f7fc;
  --color-surface-muted: #ebf0f8;

  /* 边框 */
  --color-border: #d6e0f5;
  --color-border-strong: #002bf0;

  /* 语义 */
  --color-error: #e11d48;
  --color-success: #16a34a;
  --color-warning: #d97706;

  /* 遮罩 */
  --mask-overlay: color-mix(in srgb, #070b19 20%, transparent);

  /* 间距 */
  --spacing-xs: 4px;
  --spacing-sm: 8px;
  --spacing-md: 16px;
  --spacing-lg: 24px;
  --spacing-xl: 32px;

  /* 圆角 */
  --radius-sm: 2px;
  --radius-md: 4px;
  --radius-lg: 8px;

  /* 字体 */
  --font-sans: 'Inter', -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  --font-mono: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;

  /* Z 轴 */
  --z-dialog: 101;
  --z-dropdown: 105;
  --z-tooltip: 110;

  /* 零动效：全局强制 */
  --motion-duration: 0s;
  --motion-easing: step-start;
}
```

### 2.7 全局重置（`src/styles/reset.css`）

```css
*, *::before, *::after {
  box-sizing: border-box;
  /* 关键：全局禁用过渡和动画 */
  transition: none !important;
  animation: none !important;
}

html, body {
  margin: 0;
  padding: 0;
  font-family: var(--font-sans);
  font-size: 14px;
  color: var(--color-text-primary);
  background: var(--color-bg);
  -webkit-font-smoothing: antialiased;
}

button {
  font-family: inherit;
  cursor: pointer;
}

a {
  color: var(--color-brand);
  text-decoration: none;
}
```

---

## 3. 字体层级 (Typography)

| Class (BEM) | 像素值 | 用途 |
|------|--------|------|
| `.text-xs` | 12px | 促销标签 (Badge)、商品副说明、页脚 |
| `.text-sm` | 14px | 基础正文、次级按钮、表单输入、SKU 选项 |
| `.text-base` | 16px | 商品列表标题、主操作按钮、价格主数字 |
| `.text-lg` | 18px | 模块小标题、订单列表总价 |
| `.text-xl` | 20px | 详情页商品大标题、弹窗标题 |
| `.text-2xl` | 24px | 页面主标题 |

**强制字体**：
- 文本：`var(--font-sans)` = `Inter` / `PingFang SC` / `Microsoft YaHei`
- 数据 / 订单号 / SKU 编码 / 价格数字：`var(--font-mono)` = `JetBrains Mono`

**禁止**：规范外的字号（13px / 15px 等）、规范外的字重、字间距硬编码。

---

## 4. 空间与布局

### 4.1 扁平线框架构 (Wireframe Aesthetic)

抛弃阴影与投影，**完全靠 1px 边框**划分电商信息区块：

- **Header 隔离**：`border-bottom: 1px solid var(--color-border);`
- **商品网格**：卡片**无阴影**、**无 transform**，仅靠 `1px` 边框。
- **Hover 切换**（瞬时，**无 transition**）：
  - 边框由 `var(--color-border)` → `var(--color-brand)`
  - 文字色保持
  - **不允许** `translateY` / `scale` / 阴影

### 4.2 侧边栏 / 抽屉 (Drawer / Dialog)

- 购物车面板用 Element Plus `<el-drawer direction="rtl">`；
- 背景使用 `var(--color-bg)`，与主页面用 `border-left: 1px solid var(--color-border)` 分割；
- 背景增加极淡 `--mask-overlay`；
- **关键**：`--el-transition-duration: 0s`（已在 §2.5 注入）→ 抽屉瞬时出现/消失。

### 4.3 响应式断点

| 断点 | 屏幕宽度 | 容器宽度 |
|------|---------|---------|
| `sm` | < 640px | 100% |
| `md` | 640-1024px | 720px |
| `lg` | 1024-1280px | 960px |
| `xl` | ≥ 1280px | 1200px |

---

## 5. 核心组件规范

### 5.1 按钮 (Button)

| 状态 | Primary | Outline | Disabled |
|---|---|---|---|
| Default | `bg: var(--color-brand); color: var(--color-text-inverse); border: 1px solid var(--color-brand)` | `bg: transparent; color: var(--color-text-primary); border: 1px solid var(--color-border)` | `bg: var(--color-surface-muted); color: var(--color-text-tertiary); border: 1px solid var(--color-border)` |
| Hover | 边框 + 文字保持，仅 `bg` → `color-mix(in srgb, var(--color-brand) 90%, var(--color-text-primary))` | `border-color: var(--color-brand); color: var(--color-brand)` | 不响应 |
| Active | `bg: color-mix(in srgb, var(--color-brand) 80%, var(--color-text-primary))` | `bg: var(--color-surface-muted); border-color: var(--color-brand); color: var(--color-brand)` | 不响应 |

**硬性约束**：
- ❌ 禁止用 `opacity` 表达 Disabled
- ❌ 禁止 `box-shadow` / `transform` / `transition`
- ✅ 状态变化瞬时生效

### 5.2 商品 SKU 选择器

```css
.sku-option {
  display: inline-flex;
  align-items: center;
  padding: var(--spacing-xs) var(--spacing-sm);
  border: 1px solid var(--color-sku-border);
  color: var(--color-text-primary);
  border-radius: var(--radius-md);
  cursor: pointer;
}

.sku-option--active {
  border-color: var(--color-sku-border-active);
  color: var(--color-sku-border-active);
  position: relative;
}

.sku-option--active::after {
  content: '';
  position: absolute;
  right: -1px; bottom: -1px;
  width: 0; height: 0;
  border-style: solid;
  border-width: 0 0 6px 6px;
  border-color: transparent transparent var(--color-brand) transparent;
}

.sku-option--disabled {
  position: relative;
  color: var(--color-text-tertiary);
  cursor: not-allowed;
  background: var(--color-surface-muted);
}

.sku-option--disabled::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to top right,
    transparent calc(50% - 1px),
    var(--color-text-tertiary) calc(50% - 1px),
    var(--color-text-tertiary) calc(50% + 1px),
    transparent calc(50% + 1px)
  );
}
```

### 5.3 浮层与弹窗 (Dialog / Dropdown / Tooltip)

**核心：完全不用 `box-shadow`，靠边框 + 1px 实线区分层级**。

```css
.el-dialog, .el-drawer, .el-popper, .el-tooltip__popper {
  background: var(--color-bg) !important;
  border: 1px solid var(--color-border) !important;
  box-shadow: none !important;        /* 关键：禁用阴影 */
  border-radius: var(--radius-lg) !important;
  transition: none !important;        /* 关键：禁用过渡 */
}

.el-overlay {
  background-color: var(--mask-overlay) !important;
  transition: none !important;
}
```

### 5.4 表单组件 (Input / Select / Checkbox)

```css
.el-input__wrapper, .el-select__wrapper {
  background: var(--color-surface) !important;
  box-shadow: 0 0 0 1px var(--color-border) inset !important;  /* 1px 边框，不用 shadow */
  border-radius: var(--radius-md) !important;
  transition: none !important;
}

.el-input__wrapper.is-focus, .el-select__wrapper.is-focused {
  box-shadow: 0 0 0 1px var(--color-border-strong) inset !important;
}

.el-checkbox__inner {
  border: 1px solid var(--color-border) !important;
  box-shadow: none !important;
  border-radius: var(--radius-sm) !important;
}

.el-checkbox__input.is-checked .el-checkbox__inner {
  background-color: var(--color-brand) !important;
  border-color: var(--color-brand) !important;
}
```

### 5.5 数据展示 (Table / Tag / Badge)

```css
.el-table {
  --el-table-border-color: var(--color-border);
  --el-table-header-bg-color: var(--color-surface-muted);
  --el-table-row-hover-bg-color: var(--color-surface-hover);
}

.el-tag {
  border-radius: var(--radius-sm) !important;
  border: 1px solid var(--color-border) !important;
  background: var(--color-surface) !important;
  color: var(--color-text-primary) !important;
}

.el-tag--primary {
  color: var(--color-brand) !important;
  border-color: var(--color-brand) !important;
  background: var(--color-brand-light) !important;
}
```

---

## 6. 绝对禁止项 (Red Lines)

1. **禁止任何动效 / 过渡 / 缓动**
   - 全局 `transition: none !important;`（§2.7）
   - 禁止 `transition: all` / `transition: property Xms` / `@keyframes` / `animation:`
2. **禁止硬编码任何尺寸 / 颜色**
   - CSS / Vue template 出现 `px`（在 token 上下文外）、`#hex`、`rgba()`、`white` / `black` 一律打回。
   - 必须走 `var(--color-*)` / `var(--spacing-*)` / `var(--radius-*)`。
3. **禁止弥散光 / 厚重阴影**
   - 禁止 `box-shadow` 营造"浮起" / "立体感"。
   - 层级划分**只能**靠 1px 边框和颜色对比。
4. **禁止 Hover 形变**
   - 禁止 `transform: translate / scale / rotate` 营造动态。
   - 状态切换只能通过 `color` / `background-color` / `border-color` 瞬时变化。
5. **禁止非标准字号**
   - 禁止 `font-size: 13px` / `15px` 等不在 §3 Scale 内的值。
6. **禁止空间塌陷**
   - 浮层组件 Z-Index 必须严格分层：Dialog `--z-dialog: 101` < Dropdown `--z-dropdown: 105` < Tooltip `--z-tooltip: 110`。
7. **禁止原生 rgba 透明度**
   - 必须 `color-mix(in srgb, var(--color-xxx) X%, transparent)`。
8. **禁止业务 CSS 写到全局**
   - 业务样式必须 `<style scoped>`；`src/styles/` 只允许 `tokens.css` / `reset.css` / `element-theme.css`。
9. **禁止 Element Plus 默认阴影穿透**
   - 必须用 §2.5 主题变量禁用。

---

## 7. 工程集成清单

> 入口：`web-portal/src/main.ts` 与 `web-admin/src/main.ts` 顶部依次 import 三个全局样式文件。

```ts
// web-portal/src/main.ts  (与 web-admin/src/main.ts 同)
import { createApp } from 'vue';
import App from './App.vue';

// 1. 全局重置 + 禁动效（必须最先）
import './styles/reset.css';
// 2. Token 变量
import './styles/tokens.css';
// 3. Element Plus 主题映射
import './styles/element-theme.css';

const app = createApp(App);
// ... Element Plus、router、pinia 注册
app.mount('#app');
```

| 步骤 | 动作 | 文件 |
|---|---|---|
| 1 | 引入 token 变量 | `web-portal/src/styles/tokens.css` |
| 2 | 全局重置 + 禁动效 | `web-portal/src/styles/reset.css` |
| 3 | 覆盖 Element Plus 主题 | `web-portal/src/styles/element-theme.css` |
| 4 | 在 `web-portal/src/main.ts` 依次 import | `import './styles/tokens.css'; import './styles/reset.css'; import './styles/element-theme.css';` |
| 5 | 业务组件使用 BEM 命名 | `.sku-option / .sku-option--active / .sku-option--disabled` |
| 6 | 新组件须先在 DESIGN.md 登记 token | — |

---

**—— 文档结束 ——**
