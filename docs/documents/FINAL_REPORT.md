# MallCloud 期末大作业最终验收报告

> 团队规模：5 人
> 技术基线：Java 21 LTS、Spring Boot 3.2.4、Spring Cloud Alibaba 2023.0.1.0、Seata 2.0.0
> 报告口径：只记录当前仓库内已执行、可追溯或明确归档的结果；未实现和未验证内容单独列入边界。

## 1. 最终结论

| 项目 | 结论 | 证据 |
| --- | --- | --- |
| 综合交付 | 有条件通过 | 微商城主链路、Gateway 测试、运行时回归、页面截图和交付文档已收口；Admin 用户管理页面缺失 |
| 后端构建 | 通过 | `mvn -DskipTests package`，全模块 BUILD SUCCESS |
| Gateway 测试 | 通过 | Gateway 42/42 PASS，其中 RANDOM_PORT 9/9 PASS |
| 运行时回归 | 通过 | 14/14 PASS |
| 启动状态日志 | 通过 | 13/13 后端服务记录 PID 和 health=UP |
| 前端构建 | 通过 | `npm run build` PASS |
| 页面截图 | 通过/受限 | 最终图库 20/20 尺寸匹配、MD5 唯一；Admin 只覆盖真实 `/admin` Dashboard、订单、商品区块 |

本报告不扩大当前验证范围，不对生产安全成熟度、完整 Admin 覆盖、启动绝对可靠性或零遗留作结论。

## 2. 项目概况

| 项目 | 内容 |
| --- | --- |
| 项目名称 | MallCloud 微商城 |
| 项目定位 | Spring Cloud Alibaba 微服务课程项目 |
| 主要入口 | Gateway `http://localhost:9100`；前端开发地址 `http://localhost:5173` |
| 部署方式 | Docker 中间件 + 本地 JAR + PowerShell/BAT 脚本 |
| 演示账号 | `zhangsan / 123456`、`merchant01 / 123456`、`admin / 123456` |
| 最终首页 | 根 [README.md](../../README.md) |
| 文档总入口 | [docs/README.md](../README.md) |

## 3. 核心业务完成情况

| 功能 | 状态 | 验证口径 |
| --- | --- | --- |
| 用户登录 | 已验证 | `zhangsan` 与 `admin` 登录在运行时回归中通过 |
| 商品查询与搜索 | 已验证 | Gateway 搜索、Elasticsearch 初始化和页面截图证据 |
| 购物车 | 已验证 | 前端页面与运行时主链路证据 |
| 创建普通订单 | 已验证 | 运行时回归创建订单，`addressJson` 完整 |
| 库存锁定 | 已验证 | 订单链路调用库存服务，库存不足返回 `40100` |
| 支付结果处理 | 已验证 | 模拟支付 + RocketMQ 消息驱动订单/库存状态 |
| 订单查询 | 已验证 | 订单详情页面和运行时回归 |
| 秒杀 | 已验证/受限 | 已结束 `40402`、库存不足 `40100`；不伪造成功态 |
| Admin Dashboard | 已验证 | `/api/v1/admin/dashboard` 正常，页面截图完成 |
| Admin 后台订单 | 已验证 | `/admin` 同页区块截图完成 |
| Admin 后台商品 | 已验证 | `/admin` 同页区块截图完成 |
| Admin 用户管理 | 未实现 | 当前无 `/admin/users` 路由和用户管理组件 |

## 4. 技术能力验证

| 能力 | 结果 | 证据 |
| --- | --- | --- |
| Nacos 注册与配置 | 已验证 | 历史专项证据与启动 profile 记录 |
| Gateway 路由与 JWT | 已验证 | Gateway 42/42；RANDOM_PORT 9/9 |
| internal 路径防护 | 已验证 | `/api/v1/**/internal/**` 外部访问 404 |
| `X-Internal-*` 净化 | 已验证 | RANDOM_PORT 下游 mock 未收到 internal 请求头 |
| OpenFeign | 已验证 | 订单调用商品和库存服务 |
| Seata 2.0.0 | 已验证 | 订单-库存一致性与回滚专项记录 |
| RocketMQ | 已验证 | 支付结果链路与消息消费记录 |
| Sentinel | 已验证 | 秒杀流控、库存慢调用熔断专项记录 |
| Redis | 已验证 | 购物车、秒杀库存/请求状态 |
| Elasticsearch | 已验证 | 商品搜索索引与 Gateway 搜索结果 |
| 启动脚本 | 已验证 | 13 个后端服务 PID 和 health=UP 落盘 |

## 5. 构建与测试结果

| 检查项 | 结果 | 说明 |
| --- | --- | --- |
| `mvn -pl mall-gateway -am test` | PASS | Gateway 42/42 |
| `RealGatewayRandomPortTest` | PASS | 9/9，真实 RANDOM_PORT running server + mock downstream |
| 多模块后端测试 | PASS | `mall-common,mall-gateway,mall-auth,mall-user,mall-product,mall-order,mall-job,mall-admin-biz` |
| `mvn -DskipTests package` | PASS | 全模块 package 通过 |
| `npm run build` | PASS | 前端类型检查与 Vite 构建通过；保留现有 chunk 体积警告 |
| 运行时回归 | PASS | 14/14 |
| 启动日志 | PASS | start-all.log 记录 13/13 health=UP |
| `git diff --check` | PASS | 提交前静态空白检查通过 |

## 6. RANDOM_PORT Gateway 测试归纳

测试类：`mall-gateway/src/test/java/com/mallcloud/mallgateway/filter/RealGatewayRandomPortTest.java`

| 覆盖点 | 结果 |
| --- | --- |
| `GET /api/v1/users/internal/1001/addresses/1` | 404 |
| `GET /api/v1/products/internal/products/skus/9001` | 404 |
| `GET /api/v1/orders` | 到达 mock downstream |
| `GET /api/v1/search/products?keyword=iPhone` | 到达 mock downstream |
| `GET /api/v1/test-echo` + `X-Internal-*` | 下游收不到 `X-Internal-*` |
| internal 路径 + Authorization + `X-Internal-Token` | 仍为 404 |
| filter order 常量 | internal filter 早于 JWT |

说明：测试关闭真实 Nacos/服务发现/外部 Redis/MySQL 依赖，使用测试 RouteLocator 和 mock downstream 验证真实 Web Server 下的 Gateway 过滤链行为；Redis 黑名单查询使用 mock。

## 7. 页面截图与最终资产

最终图库入口：[docs/page-screenshots/README.md](../page-screenshots/README.md)

| 页面类别 | 状态 |
| --- | --- |
| 首页、搜索、商品详情、购物车、订单详情、支付、秒杀 | desktop/mobile 共 14 张，PIL 尺寸与 MD5 已归档 |
| Admin Dashboard | 已纳入最终图库 |
| Admin 后台订单 | 已纳入最终图库，实际为 `/admin` 同页区块 |
| Admin 后台商品 | 已纳入最终图库，实际为 `/admin` 同页区块 |
| Admin 用户管理 | 未实现，不纳入最终图库 |

最终图库共 20 张：desktop 10 张均为 1440×900，mobile 10 张均为 390×844；PIL 核验 20/20 尺寸匹配，MD5 20/20 唯一。首页 mobile 于 2026-07-01 使用 Playwright CLI 真实访问补采，未使用 full-page。

过程截图索引仍保留在 [docs/test/screenshots/sprint3/README.md](../test/screenshots/sprint3/README.md)。

## 8. 图表与答辩材料

| 材料 | 入口 | 状态 |
| --- | --- | --- |
| 系统与流程图 | [docs/diagrams/README.md](../diagrams/README.md) | 图表交付通过：7/7 SVG、7/7 PNG，PNG 宽度均 ≥ 2400px |
| 视频录制脚本 | [docs/presentation/01-视频录制脚本.md](../presentation/01-视频录制脚本.md) | 已建立 |
| PPT 大纲 | [docs/presentation/02-PPT大纲.md](../presentation/02-PPT大纲.md) | 已建立 |
| PPT 答辩稿 | [docs/presentation/03-PPT答辩稿.md](../presentation/03-PPT答辩稿.md) | 已建立 |
| 关键代码导读 | [docs/presentation/04-关键代码导读与答辩准备.md](../presentation/04-关键代码导读与答辩准备.md) | 已建立 |
| 综合报告材料 | [docs/documents/README.md](README.md) | 已建立 |

图表验收表：

| 验收项 | 结果 | 证据 |
| --- | --- | --- |
| Mermaid 源文件 | 7/7 | [docs/diagrams/mmd/](../diagrams/mmd/) |
| SVG 导出 | 7/7 | [docs/diagrams/svg/](../diagrams/svg/) |
| PNG 导出 | 7/7 | [docs/diagrams/png/](../diagrams/png/) |
| PNG 宽度 | 2702px–4011px，全部 ≥ 2400px | `pwsh .\docs\diagrams\verify-diagrams.ps1` |
| 统一视觉主题 | PASS | `theme.css` + `mermaid-config.json` |
| 标题裁切复核 | PASS | 01 层级标题、02 下单阶段、05 消息发布、06 请求准入、07 Windows 主机均已人工复核 |
| 人工可读性 | PASS | 2026-07-01 生成 contact sheet 并逐图复核，无默认紫色样式、无明显交叉、无模糊低清图 |

## 9. 边界与未解决项

| 项目 | 当前口径 | 后续计划 |
| --- | --- | --- |
| Admin 用户管理页面 | 未实现；无路由、无组件、无截图 | 如课程要求，新增真实页面和后端接口后再验收 |
| full profile 外部基础设施 | 按本地真实复跑记录描述，不扩大为高可用部署 | 后续补容器化全链路稳定性验证 |
| 生产安全 | 当前只覆盖课程项目内 Gateway 防护和鉴权测试 | 后续可补 mTLS、密钥轮换、服务网格和审计 |
| 订单正式负载 | 历史 JMeter 覆盖搜索、秒杀和订单短冒烟；不写未执行精确指标 | 后续准备隔离数据后执行正式订单负载 |
| 秒杀成功态页面 | 当前不造数伪造成功态 | 后续准备可重复活动数据后补成功态截图 |

## 10. 专项证据索引

| 证据 | 路径 |
| --- | --- |
| 测试方法 | [docs/test/README.md](../test/README.md) |
| Sprint 3 截图过程证据 | [docs/test/screenshots/sprint3/README.md](../test/screenshots/sprint3/README.md) |
| Newman 摘要 | [docs/test/postman/summary/](../test/postman/summary/) |
| JMeter 摘要 | [docs/test/jmeter/summary/](../test/jmeter/summary/) |
| Nacos 配置热更新 | [docs/test/nacos/summary/](../test/nacos/summary/) |
| Sentinel 专项 | [docs/test/sentinel/summary/](../test/sentinel/summary/) |
| 前端验收记录 | [docs/test/frontend/](../test/frontend/) |
| 业务联调记录 | [docs/test/business/](../test/business/) |

## 11. 提交信息

最终提交 SHA、远端 SHA 与工作区状态以本轮 Git 输出为准。
