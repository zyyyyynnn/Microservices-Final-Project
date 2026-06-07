# MallCloud Agent 工作协议与操作规范

> 本文件是**执行 Agent 的最高约束文档**。在动任何代码、任何配置、任何文档前，必须先把本文件通读一遍。
> 项目代号：**MallCloud**（电商微商城）
> 团队规模：4 人
> 技术栈：Spring Cloud Alibaba 全家桶 + Vue 3

---

## 0. 项目一句话 & 文档地图

MallCloud 是一个 13 个微服务、7 库分库的高并发电商系统，覆盖**商品/订单/支付/库存/搜索/秒杀**完整链路。亮点是 **Seata 分布式事务 + Sentinel 限流熔断 + ES 全文搜索 + RocketMQ 异步削峰 + Gateway JWT 鉴权**。

### 0.1 必读文档（在动代码前按顺序读完）

| # | 文档 | 何时读 | 重点章节 |
|---|---|---|---|
| 1 | `docs/PRD.md` | 开工前 | §5 功能需求、§6 微服务架构、§11 关键技术点 |
| 2 | `docs/ARCHITECTURE.md` | 写第一个服务前 | §4 网关、§6 Sentinel、§7 Seata、§8 RocketMQ |
| 3 | `docs/API.md` | 写 Controller 前 | §0 通用约定、§9 秒杀、§10 后台 |
| 4 | `docs/DATABASE.md` | 写 Mapper/Entity 前 | §3 表结构、§4 索引原则 |
| 5 | `docs/CODING_STYLE.md` | 写任何代码前 | §1 命名、§3 RESTful、§8 Git |
| 6 | `docs/DEPLOY.md` | 第一次跑通前 | §3 本地开发、§8 常见问题 |
| 7 | `docs/QUICK_START.md` | 拉起环境前 | 全篇 |
| 8 | `db/init/00-create-databases.sql` | 初始化 DB 前 | 全篇（含 25 张表） |

### 0.2 单一真相源

- 业务逻辑 → `docs/PRD.md`；
- 架构决策 → `docs/ARCHITECTURE.md`；
- 接口契约 → `docs/API.md`（Postman 集合以 `API.md` §12 为准）；
- 表结构 → `docs/DATABASE.md` + `db/init/*.sql`；
- 部署方式 → `docs/DEPLOY.md` + `deploy/**`；
- 编码风格 → `docs/CODING_STYLE.md`。

**冲突时优先级**：API.md > ARCHITECTURE.md > PRD.md。若改动需求，先改 PRD，再改 ARCHITECTURE/API，最后改代码，**绝不允许只改代码不改文档**。

---

## 1. 默认基准与交互原则

- **环境基准**：Windows 11、PowerShell 7+、UTF-8（`chcp 65001`）。
- **路径基准**：工作根 `E:\微服务开发\Microservices-Final-Project`（含中文，PowerShell 路径须双引号）。
- **沟通基准**：结论先行；回复简洁、直接、可执行；不解释"我在遵守哪些规则"。
- **诚实底线**：不编造命令、版本、API、配置；不确定就明说"未验证/不确定"。
- **免解释原则**：不输出元解释，不复述用户问题。
- **交互效率**：需求明确就执行，不反复确认；只问真正影响结果的问题；不重复问已提供的信息。

---

## 2. 决策与思考规范

- 不擅自假设，不隐藏歧义。
- 仅当不同解读会**实质影响**实现/输出/风险时，才列出分歧。
- 关键信息不足且影响结果 → 先指出缺失；否则做最稳妥假设并**在结论中显式列出**。
- 基于假设继续时：方案最小、可回退；结尾指出**需要验证的关键假设**。
- 微服务领域里的常见歧义默认处理：
  - "新增一个表" → 默认在**对应业务库**建表（见 `DATABASE.md` §1），不新建库；
  - "加个接口" → 默认在**对应服务**下加 Controller（见 `PRD.md` §6.2 服务端口对照），不新建服务；
  - "调下游 X 服务" → 默认走 **OpenFeign + Sentinel 降级**（见 `ARCHITECTURE.md` §5）；
  - "改配置" → 默认改 **Nacos 配置中心**（DataId 见 `deploy/nacos/`），不直接改 `application.yaml`；
  - "跨服务改数据" → **禁止**，必须通过 OpenFeign 或 MQ 异步。

---

## 3. 目标驱动与验证体系

### 3.1 完成标准先行

接到任何任务前，先用一句话回答：
> "完成的标志是什么？我用什么命令验证？"

举例：
- 任务："加一个商品下架接口"
- 完成标准：`POST /api/v1/admin/products/{id}/off` 调用成功后，商品状态变为 0，Nacos 中服务已注册；
- 验证命令：Postman 调用 + 查 `SELECT status FROM spu WHERE id=?` + `curl http://localhost:8848/nacos`。

### 3.2 验证优先级

按以下顺序，**上一级能跑就跑上一级**：

1. **运行/复现**：`mvn spring-boot:run` / `docker compose up` 实际启动；
2. **自动化测试**：`mvn test` / JUnit / Testcontainers；
3. **静态检查**：`mvn compile` / `mvn verify` / `checkstyle:check`；
4. **最小手动验证**：`curl` / Postman 一次。

降级机制：无法跑上一层时按顺序降级并**明确说明**原因；任何"无法验证"必须显式标注。

### 3.3 项目特定验证清单（必做项，匹配评分标准 25 分）

| 验证项 | 命令/工具 | 通过标准 |
|---|---|---|
| 编译全部模块 | `mvn clean install -DskipTests` | BUILD SUCCESS |
| 单元测试 | `mvn test -pl mall-order,mall-seckill` | tests passed |
| 服务注册 | `curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=mall-order` | 至少 1 健康实例 |
| 网关鉴权 | `curl -i http://localhost:9000/api/v1/orders` | 401 |
| 网关鉴权带 Token | `curl -i -H "Authorization: Bearer xxx" ...` | 200 |
| Feign 远程调用 | 下单后看 `mall-order` 日志 "调用库存服务 lock 成功" | 出现 |
| Seata 回滚 | 库存=1 并发 5 个下单 | 1 成功 4 失败且库存不超卖 |
| Sentinel 限流 | JMeter 1000 并发 /seckill | 部分 429 |
| Nacos 热更新 | 改 Nacos 配置 | 不重启生效 |
| ES 同步 | 上架商品 → `curl http://localhost:9200/mall_product/_search?q=iPhone` | 有数据 |

---

## 4. 架构与代码修改原则

### 4.1 始终简洁优先

- 用最少的代码解决问题。
- 不做没被要求的功能（不要顺手做"优化"）。
- 不为一次性需求过度抽象。
- 优先稳定、清晰、易维护；不炫技。

### 4.2 始终精准修改

- 只改必须改的部分。
- **不**顺手重构 / 不做无关优化 / 不扩大改动范围。
- 保持现有风格、命名和项目习惯（见 `CODING_STYLE.md`）。
- 每一处改动必须直接对应请求。

### 4.3 微服务边界红线（项目特有，绝对禁止）

| 红线 | 后果 |
|---|---|
| 直接调用其他服务的数据库 | 数据一致性崩溃，必须走 Feign/MQ |
| 跨服务改 Nacos 配置给所有服务用 | 污染公共命名空间，必须用 `extension-configs` |
| 引入循环 Feign 依赖（A→B→A） | 服务启动失败，必须引入第三个服务解耦 |
| 业务服务直接操作 Redis 不走 `mall-common-redis` | 序列化不一致 |
| 业务代码 catch 吞掉异常 | 排查灾难，禁止 `catch(Exception e){}` |
| 关闭 Sentinel 熔断来"临时解决问题" | 雪崩，**禁止** |
| 在 Controller 里写业务逻辑 | 违反分层，必须下沉到 Service |
| 业务库里手工 ALTER 表 | 破坏版本管理，必须走 `db/init/00-create-databases.sql` 修改 + 补迁移脚本 |
| 把 JWT 密钥写进代码 | 安全事故，必须从 Nacos 读 |
| 用 `@Transactional` 跨服务调用 | Seata 失效，必须用 `@GlobalTransactional` |

### 4.4 前端纪律

（若任务涉及前端 `web-portal` / `web-admin`，详见 `DESIGN.md`）
- **设计语言**：白蓝线条极简风；克制使用品牌色、扁平线条划分空间。技术栈 = Vue 3 + Vite + Element Plus。
- **零动效原则**：本项目**不做任何动画 / 过渡 / 缓动**。`transition` / `animation` / `@keyframes` 全局禁用；`transition: all` 永久禁用。状态切换走**瞬时**颜色 / 边框 / 文字色变化。
- **色彩 Token 化**：禁止硬编码 `#hex`、`rgba()`、`white` / `black`；必须 `var(--color-xxx)`。透明度强制 `color-mix(in srgb, var(--color-xxx) X%, transparent)`。
- **无阴影美学**：禁止 `box-shadow` 营造浮起；层级划分**只**靠 1px 边框 + 颜色对比。Element Plus 默认阴影必须通过 `--el-transition-*` / `box-shadow: none !important` 覆盖。
- **Z 轴层级**：Dialog `--z-dialog: 101` < Dropdown `--z-dropdown: 105` < Tooltip `--z-tooltip: 110`，禁止同 Z 轴背景色重叠。
- **Element Plus 主题绑定**：通过 `src/styles/element-theme.css` 将 `--el-color-primary` 等变量与 DESIGN Token 映射（详见 `DESIGN.md §2.5`）。
- **样式作用域**：业务 CSS 必须 `<style scoped>`；`src/styles/` 只允许 `tokens.css` / `reset.css` / `element-theme.css`。
- **API 路径**：必须经 Gateway `:9000`，不能直连微服务端口。

---

## 5. 产出与输出规范

### 5.1 方案选择

- 默认提供最稳妥、兼容性好的方案。
- **不引入新依赖**（除非被明确要求，且必须先在 PRD/ARCHITECTURE 记录）。
- **不增加新框架**，不大改结构。
- 技术栈固定如下，不允许新增同类替代品：
  - 注册/配置：Nacos（不允许换 Eureka/Consul/Apollo）
  - 熔断限流：Sentinel（不允许换 Hystrix/Resilience4j）
  - 分布式事务：Seata（不允许换自动补偿/TCC 模式替代）
  - 网关：Spring Cloud Gateway（不允许换 Zuul）
  - 消息：RocketMQ（不允许换 Kafka/RabbitMQ）
  - 缓存：Redis（不允许换 Memcached）
  - 搜索：Elasticsearch（不允许换 Solr）
  - ORM：MyBatis-Plus（不允许换 JPA/MyBatis 原生）
  - 前端：Vue 3 + Vite + Element Plus

### 5.2 代码输出

- 默认输出**最小必要的 diff / 补丁 / 片段**。
- 仅在以下情况才输出完整文件：
  - 用户明确要求；
  - 文件不超过 50 行；
  - 局部输出不足以安全应用。
- Java 源码超过 50 行的，**只输出新增/修改部分**，不重写整个文件。

### 5.3 脚本与编码

- 命令行默认 **PowerShell 7+**。
- 文本读写默认显式 **UTF-8**：`Get-Content -Encoding utf8` / `Set-Content -Encoding utf8`。
- 跨平台脚本同时提供 `.sh`（Linux/macOS）和 `.ps1`（Windows）。
- 文件路径含中文必须双引号包裹。

### 5.4 环境依赖预警

若命令涉及以下场景，**必须**在结论中说明：
- PowerShell 5.1（已不兼容，请用户升级 PS7）；
- Windows Server 2016 等无 Docker Desktop 环境；
- 中文字符集（GBK）终端；
- minikube 驱动选择（`docker`/`hyperv`/`kvm2`）。

---

## 6. 多步骤任务处理

- 默认**一次性完成**所有直接相关且信息充足的步骤。
- **边界停顿**仅在：
  1. 后续依赖人为选择（如"用 Redis 还是本地内存缓存"）；
  2. 依赖外部执行结果（如"先跑测试再说"）；
  3. 属于高风险操作（如 `rm -rf`、`docker system prune`、drop database、生产环境部署）。
- 停顿时**必须**说明：当前在哪一步、卡在哪、下一步需要什么。

---

## 7. 工具集成：CodeGraph MCP

Agent 应优先使用 CodeGraph 工具理解项目上下文，避免盲目 `Read` / `Grep` 浪费 token。

| 工具 | 用途 |
|---|---|
| `codegraph_search` | 按名称搜符号（类/方法/字段） |
| `codegraph_context` | 围绕一个任务构建相关上下文 |
| `codegraph_trace` | 追踪两个符号间的调用路径 |
| `codegraph_callers` | 谁调用了我 |
| `codegraph_callees` | 我调用了谁 |
| `codegraph_impact` | 改我影响哪些代码（重构前必跑） |
| `codegraph_node` | 取一个符号的详细信息（含源码） |
| `codegraph_explore` | 批量看多个相关符号源码 |
| `codegraph_files` | 看项目文件结构 |
| `codegraph_status` | 索引健康度 |

**维护**：
- 日常修改后增量同步：`codegraph sync`（秒级）
- 首次或索引损坏：全量重建 `codegraph index`

**使用纪律**：
- 改 `mall-order` 前必先 `codegraph_impact` 看下游；
- 改 `mall-common` 前必先 `codegraph_callers` 看所有依赖；
- 看到一个不熟的类先 `codegraph_explore` 一次拿全部相关符号。

---

## 8. 项目工作流（项目特有）

### 8.1 启动环境流程

```powershell
# 1. 拉起中间件
cd "E:\微服务开发\Microservices-Final-Project"
.\scripts\start-middleware.ps1

# 2. 初始化数据库
.\scripts\init-db.ps1

# 3. 编译公共模块
mvn clean install -DskipTests -pl mall-common -am

# 4. 启动业务服务（按端口顺序从 9000 开始，详见 PRD §6.2）

# 5. 启动前端
cd web-portal; npm install; npm run dev
cd web-admin; npm install; npm run dev
```

### 8.2 任务执行流程（写新接口/新服务）

```
1. codegraph_status          # 确认索引健康
2. codegraph_explore 相关服务 # 看现有结构
3. codegraph_impact 改动点   # 评估影响
4. 写代码（参考 mall-order 骨架 + CODING_STYLE）
5. mvn compile -pl <module>  # 编译
6. mvn test -pl <module>     # 测试
7. 启动该服务，Postman 验证
8. 跑 codegraph sync
9. 更新 API.md（如有接口变化）
10. git add . && git commit -m "feat(<scope>): ..."
```

### 8.3 任务执行流程（修 Bug）

```
1. 重现 Bug（Postman / 日志）
2. codegraph_explore 涉及的代码
3. codegraph_callers / codegraph_callees 找到根因
4. 最小修改（不重构、不优化）
5. 验证：先运行，再单元测试，再 Postman
6. codegraph sync
7. commit: "fix(<scope>): <subject>"
```

### 8.4 文档维护纪律

- **任何**需求变化 → 先改 `PRD.md` → 改 `ARCHITECTURE.md` → 改 `API.md` → 改 `DATABASE.md` → 改 `DEPLOY.md` → 改代码。
- **任何**接口变化 → 改 `API.md` → 改 `Postman` 集合。
- **任何**表结构变化 → 改 `db/init/00-create-databases.sql` → 补 `db/migration/V2__xxx.sql` 迁移脚本（给已有库用）→ 同步到 `DATABASE.md`。
- **任何**配置变化 → 改 `deploy/nacos/*.yaml` → 不直接改 `application.yaml`。

### 8.5 提交规范（参见 `CODING_STYLE.md` §8.2）

格式：`<type>(<scope>): <subject>`

允许的 type：`feat` / `fix` / `docs` / `refactor` / `test` / `chore` / `perf` / `style`

scope 举例：`order` / `pay` / `gateway` / `seckill` / `common` / `doc` / `deploy`

示例：
- `feat(order): add cancel order api`
- `fix(pay): fix alipay notify signature`
- `docs(api): add seckill polling endpoint`
- `refactor(common): extract BizNoUtil`
- `test(seckill): add concurrent test`
- `chore(deps): upgrade spring boot to 3.2.5`
- `deploy(k8s): add hpa for mall-seckill`

提交频率：每天 ≥ 1 次；单次 commit < 400 行；提交前 `mvn test`。

---

## 9. 文档速查（执行 Agent 救命索引）

### 9.1 关键服务端口

| 服务 | 端口 | 数据库 |
|---|---|---|
| mall-gateway | 9000 | - |
| mall-auth | 9001 | mall_auth |
| mall-user | 9002 | mall_user |
| mall-product | 9003 | mall_product |
| mall-inventory | 9004 | mall_inventory |
| mall-cart | 9005 | Redis |
| mall-order | 9006 | mall_order |
| mall-pay | 9007 | mall_pay |
| mall-search | 9008 | ES |
| mall-seckill | 9009 | mall_seckill + Redis |
| mall-message | 9010 | - |
| mall-admin-biz | 9011 | - |
| mall-job | 9012 | - |

### 9.2 Nacos 配置中心 DataId

```
common-mysql.yaml
common-redis.yaml
common-rocketmq.yaml
common-seata.yaml
common-sentinel.yaml
mall-{service}.yaml            # 私有配置
mall-{service}-{profile}.yaml  # 环境覆盖
```

### 9.3 关键路径与默认账号

| 用途 | URL | 账号 |
|---|---|---|
| API 网关 | http://localhost:9000 | - |
| 用户前台 | http://localhost:5173 | - |
| 商家后台 | http://localhost:5174 | - |
| Nacos | http://localhost:8848/nacos | nacos/nacos |
| Sentinel Dashboard | http://localhost:8080 | sentinel/sentinel |
| Zipkin | http://localhost:9411 | - |
| Kibana | http://localhost:5601 | - |
| RocketMQ Console | http://localhost:8180 | - |
| Seata 控制台 | http://localhost:7091 | - |

### 9.4 测试账号（来自 `db/init/seed.sql`）

| 用户名 | 密码 | 角色 |
|---|---|---|
| zhangsan | P@ssw0rd123 | USER |
| lisi | P@ssw0rd123 | USER |
| merchant01 | P@ssw0rd123 | MERCHANT |
| admin | Admin@123 | ADMIN |

### 9.5 MQ Topic 速查

```
ORDER_CREATED        order → pay
PAY_RESULT           pay → order
SECKILL_REQUEST      seckill → order
STOCK_ROLLBACK       order → inventory
ES_SYNC              product → search
NOTIFY_MERCHANT      order → admin-biz（延时）
```

### 9.6 Sentinel 限流阈值

```
mall-seckill:execute    QPS 500
mall-order:createOrder  QPS 200
mall-inventory:lock     熔断：RT>1s 50% / 异常 50%
```

### 9.7 业务错误码

| 码 | 含义 |
|---|---|
| 200 | 成功 |
| 10001 | 参数校验失败 |
| 10002 | 远程调用失败 |
| 20100 | 未登录 |
| 20101 | Token 过期 |
| 20103 | 无权限 |
| 30100 | 商品不存在 |
| 40100 | 库存不足 |
| 40200 | 订单不存在 |
| 40202 | 分布式事务失败 |
| 40400 | 秒杀已售罄 |
| 50001 | 服务降级中 |
| 50002 | 请求过于频繁 |

完整列表见 `API.md` §0.3。

---

## 10. 评分对齐（最高优先级）

每个提交都应反问：**这个改动对应哪条评分项？** 没有评分项对应 → 砍掉。

| 评分项 | 分值 | 关键实现 |
|---|---|---|
| 项目功能与完整性 | 20 | 13 微服务，商品/订单/支付/搜索/秒杀全链路（PRD §5） |
| 技术规范与架构设计 | 20 | Spring Cloud Alibaba 体系完整（ARCHITECTURE §1）+ 4 大分布式难题解法（ARCHITECTURE §7/8/9/10） |
| 接口功能测试 (Postman) | 25 | ≥6 接口 ≥20 请求（API.md §12） |
| 服务治理测试 | 25 | Nacos 心跳感知 + Gateway Token 鉴权（DEPLOY §8） |
| 负载测试 (JMeter) | 25 | 50→150 用户，P95<800ms（DEPLOY §13.3） |
| 压力测试 | 25 | 阶梯加压至 500QPS，Sentinel 限流（DEPLOY §13.4） |
| 异常场景 | 25 | 杀 service → 熔断；Nacos 改配置 → 热更新（DEPLOY §13.5） |
| 代码质量与注释 | 15 | CODING_STYLE 全套 + JavaDoc + Alibaba 规范 |
| 文档与报告 | 10 | 7 个 .md + 图文并茂 + ER/时序/架构图 |
| 演示与答辩 | 10 | Docker 一键起 + minikube 演示（DEPLOY §12 答辩剧本） |

---

## 11. 禁止行为清单

- ❌ 删改 `db/init/00-create-databases.sql`（只在末尾追加新表/新字段）
- ❌ 把密钥、Token、密码 commit 到仓库
- ❌ 在 `application.yaml` 硬编码配置（必须走 Nacos + `${ENV}`）
- ❌ 在 Service 层直接返回 `Result<T>`（只在 Controller 层包装）
- ❌ 用 `@Autowired` 字段注入（必须构造器注入 `@RequiredArgsConstructor`）
- ❌ `System.out.println`（必须 `log.info`）
- ❌ `try {...} catch (Exception e) {}` 空 catch
- ❌ `SELECT *` / 大事务 / 循环单条 INSERT
- ❌ 不写注释就 commit public 方法
- ❌ 在 `main` 分支直接 push（必须走 `develop` 或 feature 分支）

---

## 12. 紧急联系 & 求助

当遇到以下情况，**停下并报告**：

1. 改 `mall-common` 会影响 ≥5 个下游服务（先讨论）；
2. 改 Seata 事务边界（全局事务 / 局部事务切换）；
3. 改数据库 schema（必须先评审）；
4. 改 Gateway 鉴权逻辑（影响所有接口）；
5. 改 Sentinel 默认限流阈值（影响全部流量）；
6. 删除任何文件 / 表 / Topic。

**求助信息模板**：
```
【求助】任务：XXX
【问题】：YYY
【已尝试】：ZZZ
【阻塞点】：找不到 XXX / 编译失败 / 测试不通过
【建议方案】：AAA / BBB
【请决策】：CCC
```

---

**—— 协议结束 ——**

> 最后一句：MallCloud 是 4 个人的活，**Agent 是第 5 个成员**。遵守这份协议，Agent 的产出就和其他 4 个人的代码无缝衔接。
