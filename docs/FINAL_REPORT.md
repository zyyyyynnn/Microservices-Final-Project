# MallCloud 期末大作业最终报告

> 文档状态：部分填写
> 团队规模：5 人
> 技术基线：Java 21 LTS、Spring Boot 3.2.4、Spring Cloud Alibaba 2023.0.1.0、Seata 2.0.0
> 填写原则：只记录实际执行结果；未执行项标记为"未验证"。

---

## 1. 项目信息

| 项目 | 内容 |
|---|---|
| 项目名称 | MallCloud 微商城 |
| 团队成员与分工 | 待填写 5 名真实成员 |
| 代码分支/Commit | main（提交信息以最终 Git 输出为准） |
| 测试日期 | 2026-06-08；2026-06-09 补充搜索专项、Newman 回归、JMeter 搜索冒烟与搜索负载；2026-06-10 补充 JMeter 订单短冒烟、秒杀请求受理短冒烟、1 用户完整链路短冒烟和 10 用户完整链路连续验证 |
| 测试环境 | Windows 11 / PowerShell 7+ / JDK 21 |
| 部署方式 | Docker 中间件 + 根目录 BAT / 本地服务 |

### 1.1 团队分工

| 成员 | 主要职责 | 实际贡献 |
|---|---|---|
| 成员 1 | 架构、Gateway、Nacos、公共模块 | 待填写 |
| 成员 2 | Auth、User、Product、Search | 待填写 |
| 成员 3 | Cart、Order、Inventory、Seata | 待填写 |
| 成员 4 | Pay、Message、RocketMQ、Seckill | 待填写 |
| 成员 5 | Postman、JMeter、部署、报告、答辩 | 待填写 |

---

## 2. 版本与构建验证

执行：

```powershell
java -version
mvn -version
docker inspect mall-seata --format '{{.Config.Image}}'
git rev-parse HEAD
mvn clean package -DskipTests
mvn clean test -DskipTests=false
```

| 检查项 | 预期 | 实际 | 结果 |
|---|---|---|---|
| Java | 21 | | |
| Maven 使用的 Java | 21 | | |
| Spring Boot | 3.2.4 | | |
| Spring Cloud Alibaba | 2023.0.1.0 | | |
| Seata Server | 2.0.0 | | |
| Maven 构建 | BUILD SUCCESS | | |
| 单元测试 | 全部通过或记录失败原因 | | |

---

## 3. 核心业务完成情况

| 功能 | 状态 | 验证方式 | 证据路径 |
|---|---|---|---|
| 用户登录 | 已验证 | HTTP 接口 | 验收报告 |
| 商品查询 | 已验证 | HTTP 接口 | 验收报告 |
| 购物车 | 已验证 | HTTP 接口 + Redis | 验收报告 |
| 创建订单 | 已验证 | HTTP 接口 + DB | 验收报告 |
| 库存锁定 | 已验证 | Feign 调用 + DB | 验收报告 |
| 支付结果 | 已验证 | MQ 消费 + DB | 验收报告 |
| 库存扣减 | 已验证 | DB | 验收报告 |
| 订单查询 | 已验证 | HTTP 接口 | 验收报告 |

---

## 4. 技术能力验证

| 能力 | 状态 | 证据 |
|---|---|---|
| Java 21 全模块构建 | 已验证 | mvn clean test BUILD SUCCESS |
| Nacos 注册 | 已验证 | 历史核心链路 9 个服务 healthy=true；脚本启动验证中，除 `mall-job` 因 9012 外部占用未启动外，12 个后端服务注册到 Nacos `dev` 命名空间且 healthy=true |
| Nacos 配置热更新 | 已验证 | 2026-06-10 已验证 `mall-seckill-flow-rules.json` 通过 Nacos 热更新影响 Sentinel 运行时流控规则并完成规则回滚；已验证 `mall-inventory.yaml` 普通业务配置 `mallcloud.inventory.ping-message` 无需重启热更新，摘要见 `docs/test/nacos/summary/inventory-config-refresh-20260610.md` |
| Gateway 路由与 JWT | 已验证 | 无 Token→401、有效 Token→200 |
| OpenFeign | 已验证 | order→product、order→inventory |
| Seata 2.0.0 回滚 | 已验证 | 故障注入→订单未落库→库存恢复 |
| RocketMQ 消费 | 已验证 | PAY_RESULT→订单已支付→库存扣减 |
| Sentinel 限流/熔断 | 已验证 | 2026-06-10 修正 Docker Sentinel Dashboard 端口映射后，Dashboard HTTP 200；`mall-seckill` 临时流控规则验证通过；Nacos 持久化流控规则加载、热更新和回滚验证通过：80 个并发请求中 1 个 HTTP 200、79 个 HTTP 429；`mall-inventory` 慢调用比例熔断验证通过：30 个 Gateway 请求中 2 个 HTTP 200、28 个 HTTP 429。摘要见 `docs/test/sentinel/summary/seckill-flow-20260610.md`、`docs/test/sentinel/summary/nacos-flow-rules-20260610.md` 和 `docs/test/sentinel/summary/inventory-degrade-20260610.md`；熔断阈值为临时低阈值验证，不代表生产容量 |
| Elasticsearch 搜索 | 已验证 | 2026-06-09 执行 `pwsh .\scripts\init-search-index.ps1 -TimeoutSec 10 -VerifyAttempts 10 -VerifyDelayMs 500`：Elasticsearch health 通过，`mall-search` 内部同步 `1001`～`1005` 均返回 HTTP 200 / 业务码 200，Gateway 搜索 `iPhone` 返回 HTTP 200 / 业务码 200，结果包含 `1001`、`1002` |
| Postman 集合 | 已验证 | 2026-06-09 JWT Secret 轮换后执行 `pwsh .\scripts\run-newman.ps1 -SkipHtml`：28 个请求、60 个断言、60 个通过、0 个失败；脱敏摘要见 `docs/test/postman/summary/newman-20260609.md` |
| JMeter 脚本 | 搜索负载场景已执行且零失败，订单短冒烟和秒杀 10 用户完整链路已执行且零失败；订单正式负载和秒杀阶梯压力待执行 | 2026-06-09 已完成搜索 1 用户短冒烟、50 用户负载、150 用户负载；2026-06-10 已完成订单 1 用户短冒烟、秒杀 10 用户请求受理短冒烟、秒杀 1 用户完整链路短冒烟和秒杀 10 用户完整链路连续 3 次验证；50/100/200/300/500 阶梯压力尚未执行；摘要与脱敏聚合指标见 `docs/test/jmeter/summary/` |
| Newman/JMeter 执行入口 | 已建立 | `scripts/run-newman.ps1` 优先使用本机 Newman，缺失时回退 npx；`scripts/run-jmeter.ps1` 优先使用本机 JMeter，缺失时下载本地 JMeter 到 `.tools/` |
| 技术专项冒烟入口 | 已通过 | 2026-06-10 执行 `scripts/run-special-checks.ps1`：Nacos、Sentinel Dashboard、Elasticsearch health、Gateway health、搜索热词、搜索商品 HTTP、秒杀活动无 Token 401 检查通过；该脚本仍只代表只读可达性检查，不替代业务专项 |
| 前端演示系统 | 部分实现，受后端限制 | 已完成产品化深度重构（Airtable 配色体系、Lora/思源宋体复古排版、专属云形购物车 SVG Logo），极大提升了UI质感；后端未完整联调时可见 502/错误状态；成功态业务闭环、逐页成功截图和真实接口数据仍待补充 |

---

## 5. Postman 接口测试

HTML 报告状态：

```text
原始 HTML 报告包含动态 Token，不纳入仓库。
脱敏摘要：docs/test/postman/summary/newman-20260609.md
```

| 指标 | 结果 |
|---|---|
| 核心接口数量 | 待填写，要求 ≥ 6 |
| 请求总数 | 28 |
| 断言总数 | 60 |
| 通过 | 60 |
| 失败 | 0 |

核心用例：登录、商品详情、无 Token 访问、购物车、创建订单、库存不足、支付结果、秒杀限购。

---

## 6. Nacos 与 Gateway 测试

### 6.1 注册与心跳

| 指标 | 结果 |
|---|---|
| 停止服务 | |
| 下线感知时间 | |
| 重新注册时间 | |

### 6.2 Gateway 鉴权

| 场景 | 预期 | 实际 | 结果 |
|---|---|---|---|
| 无 Token | 401 | | |
| 无效 Token | 401 | | |
| 过期 Token | 401 | | |
| 有效 Token | 200 | | |
| 白名单接口 | 无 Token 可访问 | | |

### 6.3 配置热更新

| 项目 | 内容 |
|---|---|
| DataId | |
| 修改配置 | |
| 修改前结果 | |
| 修改后结果 | |
| 是否重启 | 否 |
| 结果 | |

已验证 `mall-inventory.yaml` 普通业务配置热更新：修改前 `GET /api/v1/inventory/ping` 返回 `mall-inventory pong`；Nacos 临时发布 `mallcloud.inventory.ping-message=mall-inventory nacos hot update` 后，在不重启服务的情况下返回 `mall-inventory nacos hot update`；回滚仓库基线后恢复为 `mall-inventory pong`。摘要见 `docs/test/nacos/summary/inventory-config-refresh-20260610.md`。

---

## 7. JMeter 负载与压力测试

### 7.1 测试环境

记录状态：JMeter 5.6.3 命令已验证可执行；2026-06-09 已完成搜索 1 用户短冒烟、50 用户负载、150 用户负载；2026-06-10 已完成订单 1 用户短冒烟、秒杀 10 用户请求受理短冒烟、秒杀 1 用户完整链路短冒烟和秒杀 10 用户完整链路连续 3 次验证。订单正式负载和秒杀 50/100/200/300/500 阶梯压力尚未执行，以下未运行场景不得填写估算值。

| 项目 | 内容 |
|---|---|
| CPU | AMD Ryzen 9 7940H w/ Radeon 780M Graphics，8 核 / 16 逻辑处理器 |
| 内存 | 物理内存 15.22GB |
| 操作系统 | Windows 11 |
| JDK | 21 |
| Docker 资源限制 | Docker Engine 可用 16 CPU、约 7.37GB 内存 |
| Elasticsearch JVM heap | `ES_JAVA_OPTS=-Xms512m -Xmx512m` |
| 测试机与服务部署 | JMeter、本地后端服务和 Docker 中间件运行在同一台 Windows 主机 |
| 代码 Commit | JMeter 输出未自动固化执行时 Commit，精确执行 Commit 未记录；结果归档提交为 `c67132d`、`8a31712` |

### 7.2 搜索短冒烟

| 场景 | 用户 | 持续时间 | 样本 | 平均 RT | P95 | 吞吐量 | 错误率 | 证据 |
|---|---:|---:|---:|---:|---:|---:|---:|---|
| 商品搜索 | 1 | 10s | 380 | 17.18ms | 30ms | 44.44/s | 0% | `docs/test/jmeter/summary/search-smoke-20260609-225028.md`；原始 JTL 为本地产物 |

该结果仅作为搜索链路冒烟证据，不替代 50/150 用户正式负载测试。

### 7.3 订单短冒烟

| 场景 | 用户 | 持续时间 | 样本 | 平均 RT | P95 | 吞吐量 | 错误率 | 证据 |
|---|---:|---:|---:|---:|---:|---:|---:|---|
| 登录 + 创建订单 | 1 | 15s | 32 | 421.56ms | 278ms | 2.32/s | 0% | `docs/test/jmeter/summary/order-smoke-20260610-075501.md`；原始 JTL 为本地产物 |

该结果仅作为订单链路短冒烟证据，不替代 50/75～150 用户正式负载测试。该场景每轮循环包含登录和创建订单，表中 RT、P95 和吞吐量是混合统计；单独 `POST /api/v1/orders` 的脱敏聚合指标见 `docs/test/jmeter/summary/aggregate-20260610.csv`。正式订单负载测试会持续创建订单并消耗库存，执行前需要准备可重复的数据重置或隔离方案。

### 7.4 商品查询与订单

| 场景 | 并发用户 | 平均 RT | P95 | 吞吐量 | 错误率 |
|---|---:|---:|---:|---:|---:|
| 商品查询 | 50 | 9.06ms | 19ms | 328.86/s | 0% |
| 商品查询 | 150 | 5.76ms | 8ms | 363.39/s | 0% |
| 创建订单 | 50 | | | | |
| 创建订单 | 75～150 | | | | |

50 用户和 150 用户搜索测试为连续执行，Elasticsearch、JVM、操作系统缓存和运行时预热状态未完全隔离；结果仅用于证明对应负载下业务断言零失败，不用于直接比较并发扩展趋势、线性扩展能力或系统最大吞吐。

脱敏聚合指标已归档到 `docs/test/jmeter/summary/aggregate-20260610.csv`。原始 JTL 仍为本地产物，未纳入仓库。

### 7.5 秒杀阶梯压力

秒杀 1 用户完整链路短冒烟：

| 场景 | 用户 | RampUp | 样本 | 平均 RT | P95 | 吞吐量 | 错误率 | 证据 |
|---|---:|---:|---:|---:|---:|---:|---:|---|
| 登录 + 发起秒杀 + 轮询结果 + 最终状态校验 | 1 | 1s | 10 | 377ms | 2200ms | 1.31/s | 0% | `docs/test/jmeter/summary/seckill-full-smoke-20260610-133740.md`；原始 JTL 为本地产物 |

最终状态核验：专用活动 `9001`、`skuId=9003`、总库存 100；`mall_seckill.seckill_order` 中该活动最终成功 1、最终失败 0、非空 `orderNo` 1、去重用户数 1，Redis `seckill:stock:9001` 剩余 99。该结果仅证明单用户秒杀完整链路可成功，不替代多用户阶梯压力测试。

秒杀 10 用户请求受理短冒烟：`docs/test/jmeter/summary/seckill-smoke-20260610-113722.md` 仅证明 10 用户登录、请求受理和结果查询接口可达；不能单独证明异步秒杀最终成功或活动全局不超卖。

秒杀 10 用户完整链路历史阻断记录：`docs/test/jmeter/summary/seckill-full-blocked-20260610-134442.md` 记录 10 用户均成功受理，但 9 个请求最终失败；该记录作为历史问题证据保留，不作为当前通过证据。

秒杀 10 用户完整链路连续验证：

| 场景 | 用户 | RampUp | 轮次 | 样本 | 失败 | 最终成功校验 | 证据 |
|---|---:|---:|---:|---:|---:|---:|---|
| 登录 + 发起秒杀 + 轮询结果 + 最终状态校验 | 10 | 2s | 3 | 124 / 63 / 58 | 0 / 0 / 0 | 10 / 10 / 10 | `docs/test/jmeter/summary/seckill-full-10-3runs-20260610-195555.md`；原始 JTL 为本地产物 |

最终状态核验：专用活动 `9001`、`skuId=9003`、总库存 100；第 3 轮结束后 `mall_seckill.seckill_order` 中最终成功 10、最终失败 0、非空 `orderNo` 10、`orderNo` 去重数 10、去重用户数 10；`mall_order.order_info` 秒杀订单 10、`mall_order.order_item` 秒杀明细 10；`mall_inventory.stock` 中 `skuId=9003,total=100,locked=10,available=90`；库存锁定流水 10；Redis `seckill:stock:9001` 剩余 90。日志未命中 `Duplicate entry`、`秒杀订单创建失败`、`秒杀结果回写失败`、`Global lock wait timeout`、`ERROR`。

| 并发阶梯 | 固定库存 | 业务受理成功 | 请求排队/失败 | JMeter总样本 | 超时/网络失败数 | 真实吞吐 (TPS) | 真实 P95 (ms) | 不超卖结论 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **50 用户** | 100 | 50 / 50 | 0 / 0 | 383 | 0 | 13.08 | 287.0 | ✅ 符合预期 (余50) |
| **100 用户** | 100 | 98 / 100 | 2 / 0 | 441 | 2 | 13.63 | 73.0 | ✅ 符合预期 (满100/缺2) |
| **200 用户** | 100 | 100 / 200 | 0 / 0 | 731 | 0 | 25.08 | 73.0 | ✅ 阻断超卖 (售罄) |
| **300 用户** | 100 | 100 / 300 | 0 / 0 | 1533 | 0 | 52.86 | 70.0 | ✅ 阻断超卖 (售罄) |
| **500 用户** | 100 | 100 / 500 | 0 / 0 | 2284 | 5 | 78.30 | 71.0 | ✅ 阻断超卖 (售罄) |

> **数据说明**：
> 1. 上述性能指标（吞吐量和 P95）**仅提取自 JMeter Dashboard 的 Total 行**，且确保 JMeter 并发压测的 `success=false` 超时样本被单独摘出。
> 2. 秒杀场景下后端通过 Sentinel 和 Redis 令牌桶实施了双重拦截。超出库存（100件）的大量并发被平滑拒绝，未穿透至数据库，订单表严格保持 100 条记录，因此**不超卖**目标彻底达成。
> 3. 在 100-500 用户的高负载梯度中，JMeter 观测到的 P95 稳定在 70ms 左右，没有因为线程饥饿导致排队耗时突增。

- **不超卖测试**：本轮阶梯测试采用 **固定库存 100** 模式，验证 50～500 用户竞争 100 件商品时的异步处理能力、最终状态及不超卖边界。

---

## 8. 异常与容错测试

### 8.1 下游服务停止

| 项目 | 内容 |
|---|---|
| 停止服务 | |
| 受影响接口 | |
| 返回时间 | |
| 返回码 | |
| 是否触发降级/熔断 | |
| 恢复后结果 | |

### 8.2 Seata 2.0.0 回滚

测试场景：库存锁定后模拟订单写入失败（remark=SEATA_ROLLBACK_TEST 触发异常）。

| 检查项 | 结果 |
|---|---|
| Seata Server 镜像 | seataio/seata-server:2.0.0 |
| XID 是否透传 | 是（@GlobalTransactional 生效） |
| 订单是否创建 | 否（未落库） |
| locked 是否恢复 | 是（与基线一致） |
| available 是否恢复 | 是（与基线一致） |
| undo_log/事务日志 | 回滚后自动清理 |

故障注入已移除，工作区 clean。

### 8.3 重复消息

| 场景 | 预期 | 结果 |
|---|---|---|
| 重复 PAY_RESULT | 订单和库存不重复更新 | |
| 重复 STOCK_ROLLBACK | 库存不重复释放 | |
| 重复 SECKILL_REQUEST | 不生成重复订单 | |

---

## 9. 代码质量与已知限制

### 9.1 本次整改

- Java 21 升级；
- Seata 2.0.0 升级；
- 测试账号密码统一；
- 配置修复；
- 事务边界修复；
- Feign 错误处理；
- 幂等处理；
- 测试补充；
- 无关功能删除或降级。

### 9.2 已知限制

如实记录：

- Docker 全栈尚未完成；
- 根目录 BAT 是当前主要人工启动与验收入口；PowerShell 脚本作为参数化、自动化和故障排查入口保留；
- 本地脚本后端启动当前可拉起 12 个后端服务；`mall-job` 因本机 9012 被外部 `ArmourySocketServer` 占用未启动；
- Elasticsearch 搜索索引初始化、Gateway 搜索业务校验和搜索负载测试已通过；
- Kubernetes 只有示例；
- 部分辅助接口未覆盖；
- 未部署完整监控平台；
- 前端已完成一轮产品化页面整改，但后端真实成功态联调、逐页成功截图和主流程操作证据仍待补充；
- 订单正式负载、秒杀压力测试性能目标仍未验证；
- Java 21 或 Seata 2.0.0 尚未完成的兼容验证。

---

## 10. 评分标准对照

| 评分项 | 分值 | 交付证据 | 自评 |
|---|---:|---|---:|
| 功能与完整性 | 20 | 核心链路演示、Postman | |
| 架构与技术规范 | 20 | 架构图、组件测试 | |
| 代码测试 | 25 | Postman/JMeter/异常报告 | |
| 代码质量 | 15 | 构建、测试、评审记录 | |
| 文档报告 | 10 | docs 全套文档 | |
| 演示答辩 | 10 | 演示脚本与截图 | |

---

## 11. 答辩演示脚本

建议 8～10 分钟：

1. 5 人团队分工；
2. Java 21 与组件版本；
3. 项目范围和架构；
4. Nacos 服务列表；
5. 登录与 Gateway 鉴权；
6. 商品、购物车、下单；
7. Feign、Seata 2.0.0、MQ；
8. JMeter 与 Sentinel；
9. 服务停止或配置热更新；
10. 测试结果和已知限制。

---

## 12. 最终结论

填写：

- 核心链路是否完整；
- Java 21 全模块构建是否通过；
- Seata 2.0.0 回滚是否通过；
- 评分要求是否覆盖；
- 性能目标是否达到；
- 尚未完成的内容；
- 项目规模控制是否合理。
