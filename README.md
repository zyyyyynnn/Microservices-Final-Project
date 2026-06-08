# MallCloud 微商城

MallCloud 是一个基于 Spring Cloud Alibaba 的电商微服务课程项目。项目目标不是堆砌功能，而是以合理规模完成一条可运行、可测试、可讲清楚的交易主链路，并展示注册配置中心、网关、服务调用、分布式事务、消息队列、限流熔断等核心能力。

> 团队规模：5 人
> 当前阶段：前端产品化页面整改已完成一轮；当前推进技术亮点专项验证、后端真实联调和测试资产补齐。

---

## 1. 项目目标

核心业务链路：

```text
登录 → 商品查询 → 加入购物车 → 创建订单
→ 查询商品 → 锁定库存 → 支付结果消息
→ 更新订单 → 确认扣减库存 → 查询订单
```

技术展示重点：

- Nacos 服务注册与配置中心；
- Spring Cloud Gateway 路由与 JWT 鉴权；
- OpenFeign 服务调用；
- Seata 分布式事务；
- RocketMQ 异步消息；
- Sentinel 限流与异常降级；
- Redis 缓存与秒杀库存；
- Elasticsearch 商品搜索。

项目范围与后续开发规则以 [`docs/PROJECT_STANDARD.md`](docs/PROJECT_STANDARD.md) 为准。

---

## 2. 当前能力状态

| 能力 | 当前状态 | 说明 |
|---|---|---|
| 13 个微服务模块 | 已实现并验证 | 模块与基础分层代码已建立 |
| Java 21 基线 | 已验证 | 全模块构建通过，13 个测试通过 |
| Gateway 路由与 JWT | 已验证 | 无 Token→401、有效 Token→200、用户上下文透传 |
| Nacos 注册与配置 | 核心已验证 | 注册和配置加载已验证；热更新待验证 |
| OpenFeign 调用 | 已验证 | order→product、order→inventory 核心链路已验证 |
| Seata 2.0.0 | 已验证 | Server 基线和 AT 回滚已验证 |
| RocketMQ | 已验证 | 支付结果链路已验证 |
| Sentinel | 待验证 | 基础接入存在，核心规则和异常测试待完成 |
| Elasticsearch | 待验证 | 搜索与同步链路需形成运行证据 |
| Docker 中间件 | 已验证 | `docker-compose.middleware.yml` 已提供 |
| Docker 全栈 | 规划项 | 构建文件和启动链路尚未完整 |
| Kubernetes 全栈 | 规划项 | 当前仅提供部分示例 manifest |
| Postman 测试 | 已建立，已执行当前后端环境回归 | `run-newman.ps1 -SkipHtml` 已执行 28 个请求、56 个断言，当前 50 通过 / 6 失败；失败集中在 Elasticsearch 未运行导致搜索业务码 10003、库存查询 500、秒杀请求/结果断言 |
| JMeter 测试 | 已建立，工具链已验证 | 已新增搜索、订单、秒杀三套 JMeter 脚本；工具链可执行，负载和压力报告待完整后端环境执行 |
| 技术专项检查 | 已建立，当前环境部分通过 | Nacos、Gateway、搜索热词、搜索商品 HTTP 可达，秒杀活动鉴权状态可达；搜索商品业务码仍受 Elasticsearch 不可达影响，不等同于搜索专项通过 |
| 前端演示系统 | 部分实现，受后端限制 | `mall-frontend` 已完成产品化页面整改，覆盖首页、商品详情、搜索、登录、注册、账户、购物车、结算、订单详情、支付、秒杀和后台；浏览器已验证 502/后端不可用错误状态，真实后端业务联调待补充 |

状态含义见 `docs/PROJECT_STANDARD.md`。

---

## 3. 技术栈

### 后端

- Java 21 LTS
- Spring Boot 3.2.4
- Spring Cloud 2023.0.1
- Spring Cloud Alibaba 2023.0.1.0
- Nacos 2.3.2
- Spring Cloud Gateway
- OpenFeign
- Sentinel 1.8.6
- Seata 2.0.0
- RocketMQ 5.1.4
- Redis 7
- Elasticsearch 8.11
- MyBatis-Plus 3.5.5
- MySQL 8
- JWT

### 前端

- Vue 3
- Vite
- TypeScript
- Element Plus
- Axios
- Pinia

### 测试与部署

- JUnit 5 / Mockito
- Postman / Newman
- JMeter
- Docker Compose
- Kubernetes 示例配置

---

## 4. 服务清单

| 服务 | 端口 | 职责 |
|---|---:|---|
| mall-gateway | 9000 | 统一入口、路由、JWT 鉴权 |
| mall-auth | 9001 | 登录、Token 刷新、退出 |
| mall-user | 9002 | 用户资料和地址 |
| mall-product | 9003 | 类目、SPU、SKU |
| mall-inventory | 9004 | 库存锁定、扣减、释放 |
| mall-cart | 9005 | 购物车 |
| mall-order | 9006 | 订单创建和查询 |
| mall-pay | 9007 | 支付记录和结果通知 |
| mall-search | 9008 | Elasticsearch 搜索 |
| mall-seckill | 9009 | 秒杀活动和请求处理 |
| mall-message | 9010 | RocketMQ 消息消费与分发 |
| mall-admin-biz | 9011 | 后台聚合业务 |
| mall-job | 9012 | 定时任务 |

后续不新增微服务，优先提高现有核心链路质量。

---

## 5. 当前推荐启动方式

默认环境：Windows 11、PowerShell 7+、UTF-8、JDK 21。

### 5.1 启动中间件

```powershell
Set-Location <项目根目录>
.\scripts\start-middleware.ps1
```

### 5.2 初始化数据库

```powershell
.\scripts\init-db.ps1 -Force
```

### 5.3 编译项目

```powershell
java -version
mvn -version
mvn clean package -DskipTests
```

`java -version` 和 Maven 使用的 Java Home 均应指向 JDK 21。

### 5.4 启动后端

当前可使用脚本启动后端，也可继续使用 IDE 启动所需服务。脚本路径：

```powershell
.\scripts\start-all.ps1 -SkipInfrastructure -SkipFrontend
.\scripts\stop-all.ps1
```

默认情况下，启动脚本存在失败服务会返回非 0；如需记录 `mall-job` 等非核心服务失败但继续联调，可显式使用 `-AllowPartial`。2026-06-08 本地验证中，除 `mall-job` 因 9012 被外部 `ArmourySocketServer` 占用外，其余 12 个后端服务均可注册到 Nacos `dev` 命名空间并监听对应端口。首次验证核心链路时至少启动：

```text
mall-gateway
mall-auth
mall-user
mall-product
mall-inventory
mall-cart
mall-order
mall-pay
mall-message
```

Docker 全栈与 Kubernetes 全栈暂不作为正式可用启动方式。

### 5.5 启动前端

```powershell
Set-Location .\mall-frontend
npm install
npm run dev
```

默认地址：

```text
http://localhost:5173
```

开发环境通过 Vite 代理把 `/api/v1/**` 转发到 `http://localhost:9000`，前端仍统一经 Gateway 调用后端。

当前 `mall-frontend` 已完成一轮产品化页面整改；完整交付仍需补充后端真实联调、逐页成功态截图和主流程操作证据。

---

## 6. 演示数据

`db/init/seed.sql` 当前提供：

- 10 个测试用户；
- 30 个类目；
- 5 个 SPU；
- 7 个 SKU；
- 7 条库存；
- 3 场秒杀活动。

测试账号统一使用密码 `123456`：

| 用户名 | 密码 | 角色 |
|---|---|---|
| zhangsan | 123456 | USER |
| merchant01 | 123456 | MERCHANT |
| admin | 123456 | ADMIN |

密码摘要以 `db/init/seed.sql` 为准。正式环境不得沿用该演示密码。

---

## 7. 文档导航

| 文档 | 说明 |
|---|---|
| [PROJECT_STANDARD.md](docs/PROJECT_STANDARD.md) | 项目范围、开发和交付统一标准 |
| [PRD.md](docs/PRD.md) | 需求规格与完成边界 |
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | 当前架构、调用链路和技术决策 |
| [API.md](docs/API.md) | 接口契约与核心测试清单 |
| [DATABASE.md](docs/DATABASE.md) | 数据库设计与演示数据 |
| [DEPLOY.md](docs/DEPLOY.md) | 当前可执行部署方式 |
| [CODING_STYLE.md](docs/CODING_STYLE.md) | 代码与配置规范 |
| [QUICK_START.md](docs/QUICK_START.md) | 最短启动路径 |
| [FINAL_REPORT.md](docs/FINAL_REPORT.md) | 最终测试与答辩报告模板 |

---

## 8. 评分项对照

| 评分项 | 交付重点 |
|---|---|
| 功能与完整性 | 完整交易主链路，不继续堆砌功能 |
| 技术规范与架构 | 服务边界合理，组件使用与代码一致 |
| 代码测试 | 真实 Postman、JMeter、治理和异常报告 |
| 代码质量 | 核心代码清晰、异常明确、配置可用 |
| 文档报告 | 所有声明有代码、命令或报告支撑 |
| 演示答辩 | 采用已验证路径，不演示未完成能力 |

---

## 9. 开发原则

- 先修复，再完善；
- 先核心链路，再技术亮点；
- 不新增无评分价值的功能；
- 不为所有场景增加重复兜底；
- 没有测试证据的性能数据不得写入报告；
- 任何代码变更必须同步相关文档。
