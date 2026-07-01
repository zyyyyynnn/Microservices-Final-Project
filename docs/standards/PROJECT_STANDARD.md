# MallCloud 项目开发与交付标准

> 文档状态：生效
> 团队规模：5 人
> 默认环境：Windows 11、PowerShell 7+、UTF-8、JDK 21、Maven 3.9+
> 适用范围：需求、设计、代码、配置、测试、部署与答辩材料

---

## 1. 文档目的

本文件是 MallCloud 后续开发的统一基准，用于控制项目规模、约束技术实现、统一文档口径，并直接对应课程评分标准。

项目后续遵循：

```text
先修改标准与需求
  → 再修改设计与接口
  → 再修改代码与配置
  → 最后补测试证据
```

不得只修改代码而不更新相关文档，也不得把规划能力描述为已实现能力。

---

## 2. 技术基线

### 2.1 后端版本

| 技术 | 版本 | 状态 |
|---|---:|---|
| Java | 21 LTS | 项目编译与运行基线 |
| Spring Boot | 3.2.4 | 保持 |
| Spring Cloud | 2023.0.1 | 保持 |
| Spring Cloud Alibaba | 2023.0.1.0 | 保持 |
| Nacos | 2.3.2 | 保持 |
| Sentinel | 1.8.6 | 保持 |
| RocketMQ | 5.1.4 | 保持 |
| Seata Server | 2.0.0 | Docker/K8s 统一版本 |
| Redis | 7 | 保持 |
| Elasticsearch | 8.11 | 保持 |
| MySQL | 8.0 | 保持 |

### 2.2 Java 版本规则

- 项目只支持 JDK 21，不支持用 JDK 17 作为最终构建环境；
- 不使用 Java 21 预览特性；
- 不为了升级 JDK 批量改写 DTO、实体或业务代码；
- 虚拟线程默认关闭，只有完成基线压测后才允许单服务 A/B 验证；
- 不升级到 Java 24；
- 不在本期迁移 Spring Boot 3.5/4.0 或 Spring Cloud Alibaba 2025.x。

### 2.3 Maven 规则

父 POM 只保留：

```xml
<java.version>21</java.version>
```

不重复设置 `maven.compiler.source` 与 `maven.compiler.target`，避免版本漂移。

---

## 3. 项目定位

MallCloud 是面向微服务课程期末大作业的电商系统，重点展示：

- 合理的微服务划分；
- Spring Cloud Alibaba 核心组件的正确使用；
- 一条完整、可验证的交易主链路；
- 服务治理、分布式事务、消息异步和限流降级；
- 可重复执行的接口测试、负载测试、压力测试和异常测试。

项目不以功能数量为目标，不通过新增无关服务、无关页面或无关业务模块扩大规模；前端完整演示系统属于已明确的必做交付范围，不视为无关扩展。前端交付必须是产品化页面和可演示用户流程，不得以接口调试台、raw JSON 展示或基础渲染截图代替。

---

## 4. 范围冻结

### 4.1 核心业务链路

```text
用户登录
  → 浏览/查询商品
  → 加入购物车
  → 创建订单
  → 查询商品信息
  → 锁定库存
  → 支付结果通知
  → 更新订单状态
  → 确认扣减库存
  → 查询订单结果
```

核心服务：

- `mall-gateway`
- `mall-auth`
- `mall-user`
- `mall-product`
- `mall-cart`
- `mall-order`
- `mall-inventory`
- `mall-pay`

### 4.2 技术亮点

以下能力完成最小闭环即可：

- `mall-search`：Elasticsearch 商品搜索；
- `mall-seckill`：Redis 预扣、限购和 Sentinel 限流；
- `mall-message`：RocketMQ 消费与跨服务状态更新。

### 4.3 辅助服务

- `mall-admin-biz`：后台聚合查询和基础管理；
- `mall-job`：超时订单关闭、库存对账等基础任务。

### 4.4 明确不做

- 真实支付渠道；
- 物流轨迹；
- 优惠券、满减和促销引擎；
- 推荐算法；
- 客服 IM；
- 多租户；
- 完整生产级 Kubernetes 高可用集群；
- MySQL 主从切换和 MHA；
- 为全部 Feign Client 机械增加降级类；
- 与评分目标无直接关系的复杂兜底框架。

---

## 5. 能力状态定义

| 状态 | 定义 |
|---|---|
| 已实现并验证 | 代码存在，已运行，并保存可复现证据 |
| 已实现待验证 | 代码存在，但尚未完成运行验证或未保存证据 |
| 部分实现 | 仅完成主流程、骨架或部分场景 |
| 规划项 | 尚未实现，仅作为后续设计 |

没有命令、测试报告、截图或运行记录支撑的内容，不得标记为“已实现并验证”。

---

## 6. 单一真相源

| 内容 | 权威来源 |
|---|---|
| 项目范围与验收目标 | `docs/standards/PROJECT_STANDARD.md`、`docs/process/01-需求规格说明书.md` |
| 服务边界与调用关系 | `docs/process/04-系统架构设计文档.md` |
| HTTP 接口契约 | `docs/process/06-API接口文档.md` 与实际 Controller/DTO |
| 表结构 | `db/init/00-create-databases.sql` |
| 演示数据与账号 | `db/init/seed.sql` |
| 启动部署 | `docs/process/09-本地部署与联调手册.md` 与实际脚本 |
| 编码规范 | `docs/standards/CODING_STYLE.md` |
| 测试方法与资产目录 | `docs/test/README.md` |
| 当前测试结果与答辩证据 | `docs/documents/FINAL_REPORT.md` |

演示账号统一密码为：

```text
123456
```

该密码只用于课程演示环境，正式环境不得沿用。

---

## 7. 架构实现标准

### 7.1 服务边界

- 每个业务服务只访问自己的业务库；
- 跨服务查询或写操作通过 OpenFeign 或 RocketMQ；
- 禁止直接连接其他服务数据库；
- 不新增服务，除非现有边界无法承载且 PRD 明确批准。

### 7.2 Gateway

- 统一入口为 `http://localhost:9100`；
- Gateway 负责路由和 JWT 基础认证；
- 白名单接口必须显式配置；
- 下游通过 `X-User-Id`、`X-User-Roles` 获取用户上下文；
- 未实现方法级权限控制前，不得声明已使用 `@PreAuthorize`。

### 7.3 Nacos

- 用于服务注册和配置中心；
- Nacos 配置必须是合法 YAML；
- YAML 注释只使用 `#`；
- 配置热更新必须通过实际测试；
- DataId、Group、Namespace 必须在部署文档中明确。

### 7.4 OpenFeign

- 核心同步调用使用 OpenFeign；
- 调用方必须检查 `Result` 和数据；
- 仅对核心链路补充有业务意义的 fallback；
- 不要求辅助 Client 创建空泛降级类。

优先治理：

- `order → product`
- `order → inventory`
- `message → order`
- `message → inventory`

### 7.5 Seata

- 服务端统一使用 Seata 2.0.0；
- Server 配置来自 `deploy/docker/seata/conf/application.yml`，通过 Docker 挂载；
- Store Mode 为 DB（`mall_seata` 库），注册中心为 Nacos `dev / SEATA_GROUP`；
- `@GlobalTransactional` 只描述实际参与资源；
- 普通下单重点验证 `mall-order` 与 `mall-inventory` 的一致性；
- 支付服务未参与创建订单调用时，不得画入 Seata 分支；
- 必须通过失败场景验证回滚。

### 7.6 RocketMQ

- 文档区分普通消息与事务消息；
- 未实现 `@RocketMQTransactionListener` 时不得声明事务消息；
- 消费者必须有清晰幂等策略或状态约束；
- 不默认引入复杂本地消息表。

### 7.7 Sentinel

- 只选择少量核心资源进行限流/熔断展示；
- 建议资源：创建订单、秒杀请求；
- 未使用 `@SentinelResource` 时按实际 Web 资源描述；
- 必须提供一次限流和一次下游异常测试证据。

---

## 8. 代码质量标准

### 8.1 必须满足

- 全部 Maven 模块在 JDK 21 下可编译；
- Controller、Service、Mapper 分层清晰；
- 核心写操作具有明确事务边界；
- 统一使用 `Result<T>` 和统一异常处理；
- 核心远程调用检查失败结果；
- 配置文件无语法错误；
- 不提交真实密钥、Token 或个人绝对路径；
- 不保留固定伪 Token、`dummy` 请求体和不可执行命令；
- 关键日志包含订单号、请求 ID 或用户 ID。

### 8.2 不要求

- 不要求所有类写大量注释；
- 不要求所有 Feign Client 都有 fallback；
- 不要求所有 Service 达到同一覆盖率；
- 不为展示设计模式额外抽象；
- 不进行与当前缺陷无关的大范围重构。

---

## 9. 测试交付标准

### 9.1 Postman

- 核心接口不少于 6 个；
- 总请求不少于 20 次；
- 包含正常和异常场景；
- 登录后自动保存 Token；
- 创建订单后自动保存 `orderNo`；
- 请求体使用真实 DTO 字段；
- 至少证明一次 OpenFeign 调用成功；
- 保存 Newman 或 Postman 运行报告。

### 9.2 服务治理

必须验证：

- Nacos 注册、心跳与下线感知；
- Gateway 路由；
- 无 Token、错误 Token、有效 Token；
- 核心服务恢复后的重新注册。

### 9.3 JMeter

至少建立：

| 场景 | 目的 |
|---|---|
| 商品查询负载 | 50 用户与 150 用户对比 |
| 创建订单负载 | 验证 P95、吞吐量和错误率 |
| 秒杀压力 | 逐级增加到 500 用户并验证限流 |

报告记录：

- 并发用户；
- Ramp-up；
- 持续时间；
- 平均响应时间；
- P95；
- 吞吐量；
- 错误率；
- CPU 和内存峰值；
- JDK 版本；
- 代码 Commit。

性能目标为 P95 小于 1 秒；无法达到时如实记录瓶颈。

### 9.4 异常测试

至少完成：

- 一个下游服务宕机或超时；
- 一次 Sentinel 限流或熔断；
- 一次 Nacos 配置热更新；
- 一次库存不足或并发边界；
- 一次 Seata 回滚；
- 一次重复消息幂等验证。

---

## 10. 文档质量标准

每项关键结论至少提供一种证据：

- 代码路径；
- 配置文件路径；
- 执行命令；
- 测试报告；
- 截图；
- 数据库查询结果。

禁止：

- 不存在的脚本或文件；
- 未执行过的“一键启动”；
- 没有报告支撑的精确性能数字；
- 把建议架构写成当前架构；
- 占位团队成员；
- 失效 Token、伪业务数据和个人绝对路径。

---

## 11. 修改流程

1. 明确完成标准；
2. 更新 PRD 或架构/API 契约；
3. 实现最小必要代码；
4. 在 JDK 21 下编译或运行；
5. 增加针对性测试；
6. 更新测试报告和最终报告；
7. 记录未验证项。

---

## 12. 阶段任务与设计规范入口

本文件不维护频繁变化的当前重构进度、单次 Newman/JMeter 结果或本地启动执行结果。

当前前端重构与界面设计系统规范统一维护在：

```text
DESIGN.md
```

当前真实测试结果、截图索引、报告路径、失败项和已知限制统一维护在：

```text
docs/documents/FINAL_REPORT.md
```
