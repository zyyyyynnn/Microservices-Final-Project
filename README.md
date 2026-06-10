# MallCloud 微商城

MallCloud 是一个基于 Spring Cloud Alibaba 的电商微服务课程项目。项目目标不是堆砌功能，而是以合理规模完成一条可运行、可测试、可讲清楚的交易主链路，并展示注册配置中心、网关、服务调用、分布式事务、消息队列、限流熔断、搜索和秒杀等核心能力。

> 团队规模：5 人
> 当前阶段：技术亮点与专项验证

---

## 1. 项目定位

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
- Elasticsearch 商品搜索；
- Vue 3 前端演示系统。

项目范围、能力状态定义和交付标准以 [docs/PROJECT_STANDARD.md](docs/PROJECT_STANDARD.md) 为准。

---

## 2. 当前能力摘要

| 能力 | 当前状态 | 说明 |
|---|---|---|
| 13 个微服务模块 | 已实现并验证核心模块 | 模块与基础分层代码已建立 |
| Java 21 基线 | 已验证 | 全模块构建和既有测试已通过 |
| Gateway 路由与 JWT | 已验证 | 无 Token、有效 Token、用户上下文透传已验证 |
| Nacos 注册与配置 | 核心已验证 | 注册和配置加载已验证；热更新待验证 |
| OpenFeign 调用 | 已验证 | order→product、order→inventory 核心链路已验证 |
| Seata 2.0.0 | 已验证 | Server 基线和 AT 回滚已验证 |
| RocketMQ | 已验证 | 支付结果链路已验证 |
| Sentinel | 已验证 | Dashboard 可达、`mall-seckill` 流控、Nacos 持久化流控规则加载/热更新/回滚、`mall-inventory` 慢调用比例熔断均已验证 |
| Elasticsearch | 已验证 | 搜索索引初始化、Gateway 搜索业务码和结果校验已通过 |
| Postman/Newman | 已验证 | 当前真实结果见 [docs/FINAL_REPORT.md](docs/FINAL_REPORT.md) |
| JMeter | 搜索负载场景已执行且零失败，订单短冒烟已执行且零失败 | 搜索 1/50/150 用户、订单 1 用户短冒烟已完成；订单正式负载和秒杀压力待执行 |
| 前端演示系统 | 部分实现，受后端限制 | 产品化页面已整改一轮，成功态联调和逐页截图待补 |

未验证能力不得在答辩、报告或页面中写成已完成。

---

## 3. 最短启动方式

默认环境：Windows 11、PowerShell 7+、UTF-8、JDK 21、Maven 3.9+。

### 3.1 主要人工启动与验收入口

普通启动、完整启动测试和人工验收优先使用根目录 BAT：

```bat
start-all.bat
stop-all.bat
```

BAT 面向双击运行、答辩演示和完整启动测试，负责统一启动流程和用户可见输出。

### 3.2 高级参数与故障排查入口

需要精确参数控制、自动化调用或故障排查时使用 PowerShell：

```powershell
pwsh .\scripts\start-all.ps1 -SkipInfrastructure -SkipFrontend
pwsh .\scripts\start-all.ps1 -SkipInfrastructure -SkipFrontend -SkipBuild
pwsh .\scripts\start-all.ps1 -SkipInfrastructure -SkipFrontend -AllowPartial
pwsh .\scripts\stop-all.ps1
```

`mall-job` 端口被外部进程占用时必须如实记录为失败或受限，不得写成已启动。

### 3.3 数据库初始化

数据库脚本会重建业务表并写入演示数据，必须显式确认：

```powershell
pwsh .\scripts\init-db.ps1 -Force
```

演示账号统一密码为 `123456`。密码摘要以 `db/init/seed.sql` 为准。

---

## 4. 前端入口

```powershell
Set-Location .\mall-frontend
npm install
npm run dev
```

默认地址：

```text
http://localhost:5173
```

开发环境通过 Vite 代理把 `/api/v1/**` 转发到 `http://localhost:9000`。前端不得直接调用内部微服务端口，不得使用 mock 伪造后端未完成能力。

---

## 5. 已知限制

- Docker 全栈不是当前正式交付路径；
- Kubernetes 仅保留部分示例；
- Nacos 普通业务配置热更新专项待完成；
- JMeter 订单正式负载、秒杀压力测试待执行；
- 前端成功态业务闭环、逐页截图和真实接口数据待补充。

---

## 6. 文档导航

| 内容 | 文档 |
|---|---|
| 项目最高标准 | [docs/PROJECT_STANDARD.md](docs/PROJECT_STANDARD.md) |
| Agent 工作协议 | [AGENTS.md](AGENTS.md) |
| 当前阶段任务 | [DEVELOPMENT_PROMPT.md](DEVELOPMENT_PROMPT.md) |
| 产品与 UI 设计 | [DESIGN.md](DESIGN.md) |
| 业务需求 | [docs/PRD.md](docs/PRD.md) |
| 架构 | [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) |
| API | [docs/API.md](docs/API.md) |
| 数据库 | [docs/DATABASE.md](docs/DATABASE.md) |
| 快速启动 | [docs/QUICK_START.md](docs/QUICK_START.md) |
| 部署细节 | [docs/DEPLOY.md](docs/DEPLOY.md) |
| 脚本说明 | [scripts/README.md](scripts/README.md) |
| 测试方法 | [docs/test/README.md](docs/test/README.md) |
| 当前测试结果 | [docs/FINAL_REPORT.md](docs/FINAL_REPORT.md) |
