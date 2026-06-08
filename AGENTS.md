# MallCloud Agent 工作协议

> 团队规模：5 人
> 最高标准：`docs/PROJECT_STANDARD.md`
> 默认环境：Windows 11、PowerShell 7+、UTF-8、JDK 21、Maven 3.9+

---

## 1. 必读顺序

执行任何代码、配置或文档修改前，按顺序阅读：

1. `docs/PROJECT_STANDARD.md`
2. `docs/PRD.md`
3. `docs/ARCHITECTURE.md`
4. `docs/API.md`
5. `docs/DATABASE.md`
6. `docs/CODING_STYLE.md`
7. `docs/DEPLOY.md`
8. `docs/QUICK_START.md`

测试任务还需阅读：

- `docs/test/README.md`
- `docs/FINAL_REPORT.md`

产品、前端、页面、路由、组件、用户交互或可见文案相关任务还需阅读：

- `DESIGN.md`

---

## 2. Product and UI Design

- `DESIGN.md` 是产品流程、用户交互、页面状态、角色权限和设计约束的设计基线。
- 修改任何用户可见行为、页面、组件、路由、交互、样式、文案或前端状态前，必须先阅读 `DESIGN.md`。
- 不得静默改变产品需求；实现与 `DESIGN.md` 冲突时，先报告冲突，再修改行为。
- 不得把 `DESIGN.md` 中标记为 `待确认` 的内容当作已批准需求。
- 复用项目既有组件、设计 token、图标、路由约定、状态管理和数据请求模式。
- 不得引入平行组件库或平行设计系统，除非明确要求。
- 适用场景必须覆盖 loading、empty、error、disabled、success 状态。
- 保留既有角色和权限边界，不越权展示或操作数据。
- 不得发明后端 API、字段、服务能力，也不得用 mock 把未完成能力展示成已完成。
- 浏览器工具可用时，用户可见变更必须在真实浏览器中验证。
- 只有在明确要求或已批准设计决策需要记录时，才更新 `DESIGN.md`。
- UI 修改必须聚焦当前任务，不做无关重设计或重构。

### 2.1 Frontend Delivery Quality Gate

- 前端完整演示系统不得实现为接口调试台。
- 不得使用 raw JSON、`<pre>`、`JSON.stringify` 作为用户页面的主要展示方式；仅允许在技术演示页或调试折叠区中辅助显示。
- `DESIGN.md` 中列出的每个页面必须逐项对照实现状态：已完成、部分完成、未完成、受后端限制。
- 未实现独立路由时，不得声称该页面已完成；合并页面必须说明原因并获得明确批准。
- 商品、购物车、订单、支付、秒杀、后台页面必须以真实用户任务为中心设计，不得只暴露接口参数输入框。
- 商品详情页必须展示商品标题、价格、SKU、库存状态、数量选择和加入购物车入口。
- 搜索页必须展示关键字、热词、结果列表、分页、空结果和错误状态。
- 订单确认页必须展示地址选择、订单项确认、金额汇总和提交状态，不得只输入 `addressId`。
- 订单详情页和支付页必须展示结构化业务信息，不得只展示接口响应 JSON。
- 秒杀页必须展示活动状态、请求处理中状态、结果轮询和失败原因。
- 后台页面必须展示结构化看板、订单表格、商品表格和操作反馈；后端不支持的写操作必须明确标记为受限，不得伪造。
- 每个核心页面必须具备正常、loading、empty、error、disabled、success 状态中的适用状态。
- 每个核心页面必须完成桌面端和移动端基础可用性检查。
- 提交前必须输出 `DESIGN.md` 页面验收矩阵，逐项说明实现证据和未完成项。
- 浏览器验证不能只验证"可渲染"，必须验证主流程、状态反馈、移动端布局和页面信息完整性。
- 文档中不得把"接口入口已实现"写成"页面已完成"。

---

## 3. 当前技术基线

| 技术 | 版本 |
|---|---:|
| Java | 21 LTS |
| Spring Boot | 3.2.4 |
| Spring Cloud | 2023.0.1 |
| Spring Cloud Alibaba | 2023.0.1.0 |
| Nacos | 2.3.2 |
| Sentinel | 1.8.6 |
| RocketMQ | 5.1.4 |
| Seata Server | 2.0.0 |
| Redis | 7 |
| Elasticsearch | 8.11 |
| MySQL | 8.0 |

规则：

- 所有构建、IDE 和测试使用 JDK 21；
- 不使用 Java 21 预览特性；
- 不迁移 Java 24；
- 不借 JDK 升级批量重写现有代码；
- 虚拟线程默认关闭，只有完成基线压测后才允许单服务对比测试。

---

## 4. 当前项目状态

### 已完成基线

- Java 21 全模块构建通过；
- 现有 13 个测试全部通过（0 失败）；
- 父 POM 已取消默认 `<skipTests>true</skipTests>`；
- MySQL 8.0、Redis 7、Nacos 2.3.2 已完成基础运行验证；
- Nacos YAML 中非法 `--` 注释已修复；
- Seata Server 2.0.0 DB Store 已验证（session/lock store mode: db）；
- Seata Nacos 注册已验证（dev / SEATA_GROUP / healthy=true）；
- Seata Server Schema 已统一为官方四表；

### 已验收通过

- User/Auth/Gateway 认证闭环（登录、Token 类型隔离、黑名单、用户上下文透传）；
- mall-user 停止/恢复故障场景；
- Cart（无 Token 拒绝、有 Token 访问、加入购物车）；
- Order 主链路（商品查询、库存锁定、订单创建、DB 验证）；
- Seata AT 回滚（故障注入触发、订单未落库、库存恢复、undo_log 清理）；
- Pay Notify 边界（白名单通过、无效通知不改变状态）；
- RocketMQ / Message 支付结果链路（通知→消息→订单已支付→库存扣减、重复通知幂等）。

### 仍需验证或完成

- Elasticsearch 搜索完整验收；
- 秒杀完整链路验收；
- Sentinel 规则限流/熔断实测；
- Nacos 热更新实测；
- 正式 Postman 集合；
- JMeter 脚本和报告；
- 最终答辩材料。

### 基本约束

- 微服务结构和主要代码已建立；
- Docker/K8s 全栈不是正式交付路径；
- 演示账号统一密码为 `123456`。

---

## 5. 执行顺序

```text
[已完成] 运行基线（Java 21 / Maven / MySQL / Redis / Nacos / Seata）
  → [已完成] 认证与网关运行闭环（mall-user / mall-auth / mall-gateway）
  → [已完成] 交易主链路（Product / Inventory / Cart / Order / Seata AT）
  → [已完成] 支付与 MQ
  → [当前] 前端完整演示系统
  → 技术亮点与专项验证
  → 测试资产
  → 最终报告与答辩材料
```

禁止：

- 先新增功能再修核心缺陷；
- 为扩大规模新增微服务；
- 为所有 Feign Client 机械增加 fallback；
- 引入与评分无直接关系的新框架；
- 把规划能力写成已实现；
- 使用不可执行命令、固定伪 Token 或伪性能结果。

---

## 6. 完成标准

每个任务开始前确定：

1. 要解决的问题；
2. 允许修改的文件；
3. 完成后的可观察结果；
4. 验证命令或测试；
5. 需要同步的文档。

任务完成必须满足：

- 修改范围与需求一致；
- 在 JDK 21 下编译通过，或明确说明失败原因；
- 核心场景通过测试；
- 相关文档同步；
- 未验证项明确记录；
- 未引入无关重构和额外功能。

---

## 7. 工具集成：CodeGraph MCP

项目已接入 CodeGraph MCP，Agent 应利用以下工具功能提升上下文理解与修改精准度：

| 工具名 | 功能说明 |
| :--- | :--- |
| `codegraph_search` | 按名称搜索符号 |
| `codegraph_context` | 构建任务相关的代码上下文 |
| `codegraph_trace` | 追踪两个符号之间的调用路径 |
| `codegraph_callers` | 查找谁调用了某个函数 |
| `codegraph_callees` | 查找某个函数调用了谁 |
| `codegraph_impact` | 分析修改某个符号会影响哪些代码 |
| `codegraph_node` | 获取某个符号的详细信息（含源码） |
| `codegraph_explore` | 批量返回多个相关符号的源码 |
| `codegraph_files` | 获取索引的文件结构 |
| `codegraph_status` | 检查索引健康状态和统计 |

- **维护更新**：
  - 日常代码修改后，执行增量同步（仅处理变更文件，秒级完成）：
    ```powershell
    codegraph sync
    ```
  - 首次初始化或索引损坏时，执行全量重建：
    ```powershell
    codegraph index
    ```

---

## 8. 当前优先级

### 已完成基线

- Java 21 全模块构建通过；
- 现有 13 个测试通过；
- Maven 默认测试跳过已移除；
- MySQL、Redis、Nacos 已验证；
- Nacos YAML 语法已修复；
- Seata Server 2.0.0 DB Store 和 Nacos 注册已验证；
- 官方四表 Schema 已完成。

### P0：认证与网关运行闭环（已完成）

1. 启动 `mall-user`；
2. 启动 `mall-auth`；
3. 启动 `mall-gateway`；
4. 验证 Nacos 注册；
5. 验证远程配置加载；
6. 验证登录（`zhangsan / 123456`）；
7. 验证 Gateway JWT；
8. 验证用户上下文透传。

### P1：商品和交易主链路（已完成）

1. Product / Inventory / Cart / Order 启动和注册；
2. 商品查询和购物车；
3. 创建订单；
4. 商品和库存 Feign；
5. Seata AT 真实回滚；
6. 支付消息；
7. 订单和库存状态更新。

### P2：前端完整演示系统（当前任务）

1. 新建 Vue 3 + Vite + TypeScript 前端工程；
2. 使用 Element Plus、Axios、Pinia；
3. 覆盖公共端、用户端、秒杀端、商家/管理员端和技术演示页；
4. 通过 Gateway 调用后端接口；
5. 浏览器真实验证通过。

### P3：技术亮点与专项验证

1. Sentinel 限流/熔断；
2. 秒杀限购和库存边界；
3. Elasticsearch 搜索；
4. Nacos 热更新。

### P4：测试和答辩

1. 重建真实 Postman 集合；
2. 建立三套 JMeter 脚本；
3. 保存结果、截图和环境信息；
4. 填写 `docs/FINAL_REPORT.md`；
5. 整理 5 人分工和演示脚本。

### 前端规则

- 前端完整演示系统是必做阶段，不得降级为可选；
- 当前仓库尚无前端源码，搭建前必须以 DESIGN.md 和实际 API 为依据；
- 前端不得使用 mock 伪装后端未完成能力；
- 前端请求必须经 Gateway，不直接调用内部服务端口；
- 用户可见变更必须通过真实浏览器验证。

---

## 9. 代码修改规则

### 9.1 精准修改

- 只改完成任务必须修改的内容；
- 不顺手统一全项目命名；
- 不大范围重构现有模块；
- 不改变与任务无关的接口或数据库结构；
- 保持现有 Java 包和模块风格。

### 9.2 微服务边界

- 业务服务只访问自己的数据库；
- 跨服务同步调用使用 OpenFeign；
- 异步状态使用 RocketMQ；
- 不新增服务；
- 不形成循环 Feign 调用；
- 聚合服务不复制核心业务写逻辑。

### 9.3 失败处理

- 核心失败必须返回失败；
- 不通过 fallback 返回伪成功；
- 不 catch 后吞掉事务异常；
- 不使用固定业务数据掩盖下游不可用；
- 重试仅用于明确幂等操作。

### 9.4 事务

- 本地多表写使用本地事务；
- 实际跨服务一致性使用 Seata；
- Seata Server 基线为 2.0.0；
- 文档事务参与者必须与代码调用一致；
- MQ 最终一致性流程不强行改成全局事务。

---

## 10. 配置规则

- YAML 注释使用 `#`；
- 禁止使用 `--`；
- 不提交真实密钥或 Token；
- 不提交个人绝对路径；
- Auth 与 Gateway 的 JWT 密钥一致；
- 修改 Nacos 配置后验证加载和热更新；
- 不假定根目录 `.env` 自动注入 Spring Boot；
- 所有 IDE Run Configuration 使用 JDK 21。

---

## 11. 测试规则

### 11.1 环境验证

```powershell
java -version
mvn -version
docker inspect mall-seata --format '{{.Config.Image}}'
```

预期：

- Java 21；
- Maven 使用 Java 21；
- Seata 镜像 `seataio/seata-server:2.0.0`。

### 11.2 必测场景

- 登录成功和失败，测试密码为 `123456`；
- Gateway 无 Token、错误 Token、有效 Token；
- 商品查询；
- 创建订单；
- 库存不足；
- 订单和库存分布式回滚；
- 支付结果重复消费；
- 秒杀重复请求；
- 下游服务停止；
- Nacos 热更新。

### 11.3 禁止方式

- 生成大量重复请求充数；
- 使用 `dummy` 请求体；
- 使用固定伪 Token；
- 只断言 HTTP 200；
- 填写未运行的 P95 或吞吐量；
- 隐藏失败请求。

---

## 12. Git 与提交

```text
<type>(<scope>): <subject>
```

示例：

```text
build: upgrade project to Java 21
chore(deploy): upgrade Seata server to 2.0.0
chore(seed): unify test account password
test(order): verify Seata rollback
docs(report): record Java 21 load-test results
```

每个提交保持单一主题。

---

## 13. 输出要求

完成任务后说明：

- 修改了什么；
- 为什么修改；
- 如何验证；
- 实际验证结果；
- 哪些内容未验证；
- 影响了哪些文档。

不得使用“应该可以”作为验证结果。
