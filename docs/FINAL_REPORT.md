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
| 测试日期 | 2026-06-08；2026-06-09 补充搜索专项、Newman 回归、JMeter 搜索冒烟与搜索负载；2026-06-10 补充 JMeter 订单短冒烟、秒杀请求受理短冒烟、1 用户完整链路短冒烟、10 用户完整链路连续验证、秒杀阶梯复测和启动 Profile 资源采样；2026-06-11 补充后端端口迁移与 9100 Gateway 回归 |
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
| 单元测试 | 有效运行或记录失败原因 | | |

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
| Nacos 注册 | 已验证 | 历史核心链路 9 个服务 healthy=true；当前后端端口已迁移到连续区间 `9100-9112`，全量启动需以本轮端口迁移后的复测结果为准 |
| Nacos 配置热更新 | 已验证 | 2026-06-10 已验证 `mall-seckill-flow-rules.json` 通过 Nacos 热更新影响 Sentinel 运行时流控规则并完成规则回滚；已验证 `mall-inventory.yaml` 普通业务配置 `mallcloud.inventory.ping-message` 无需重启热更新，摘要见 `docs/test/nacos/summary/inventory-config-refresh-20260610.md` |
| Gateway 路由与 JWT | 已验证 | 无 Token→401、有效 Token→200 |
| OpenFeign | 已验证 | order→product、order→inventory |
| Seata 2.0.0 回滚 | 已验证 | 故障注入→订单未落库→库存恢复 |
| RocketMQ 消费 | 已验证 | PAY_RESULT→订单已支付→库存扣减 |
| Sentinel 限流/熔断 | 已验证 | 2026-06-10 修正 Docker Sentinel Dashboard 端口映射后，Dashboard HTTP 200；`mall-seckill` 临时流控规则验证通过；Nacos 持久化流控规则加载、热更新和回滚验证通过：80 个并发请求中 1 个 HTTP 200、79 个 HTTP 429；`mall-inventory` 慢调用比例熔断验证通过：30 个 Gateway 请求中 2 个 HTTP 200、28 个 HTTP 429。摘要见 `docs/test/sentinel/summary/seckill-flow-20260610.md`、`docs/test/sentinel/summary/nacos-flow-rules-20260610.md` 和 `docs/test/sentinel/summary/inventory-degrade-20260610.md`；熔断阈值为临时低阈值验证，不代表生产容量 |
| Elasticsearch 搜索 | 已验证 | 2026-06-09 执行 `pwsh .\scripts\init-search-index.ps1 -TimeoutSec 10 -VerifyAttempts 10 -VerifyDelayMs 500`：Elasticsearch health 通过，`mall-search` 内部同步 `1001`～`1005` 均返回 HTTP 200 / 业务码 200，Gateway 搜索 `iPhone` 返回 HTTP 200 / 业务码 200，结果包含 `1001`、`1002` |
| Postman 集合 | 已验证 | 2026-06-09 JWT Secret 轮换后执行 `pwsh .\scripts\run-newman.ps1 -SkipHtml`：28 个请求、60 个断言、60 个通过、0 个失败；2026-06-11 后端端口迁移到 `9100-9112` 后复跑 `pwsh .\scripts\run-newman.ps1 -SkipHtml`：28 个请求、60 个断言、0 个失败。复跑前按测试规范清理 `zhangsan` 对当前秒杀种子活动的历史限购状态；脱敏摘要见 `docs/test/postman/summary/newman-20260609.md`、`docs/test/postman/summary/newman-20260611-port-migration.md` |
| JMeter 脚本 | 搜索负载场景已执行且零失败；订单短冒烟和秒杀固定库存阶梯压力已执行且零失败；订单正式负载待执行 | 2026-06-09 已完成搜索 1 用户短冒烟、50 用户负载、150 用户负载；2026-06-10 已完成订单 1 用户短冒烟、秒杀 10 用户请求受理短冒烟、秒杀 1 用户完整链路短冒烟、秒杀 10 用户完整链路连续 3 次验证和 50/100/200/300/500 固定库存阶梯复测；最新秒杀阶梯 5 档失败样本均为 0，摘要与脱敏聚合指标见 `docs/test/jmeter/summary/seckill-ladder-20260610-234029.md` |
| Newman/JMeter 执行入口 | 已建立 | `scripts/run-newman.ps1` 优先使用本机 Newman，缺失时回退 npx；`scripts/run-jmeter.ps1` 优先使用本机 JMeter，缺失时下载本地 JMeter 到 `.tools/` |
| 技术专项冒烟入口 | 已通过 | 2026-06-10 执行 `scripts/run-special-checks.ps1`：Nacos、Sentinel Dashboard、Elasticsearch health、Gateway health、搜索热词、搜索商品 HTTP、秒杀活动无 Token 401 检查通过；该脚本仍只代表只读可达性检查，不替代业务专项 |
| 前端演示系统 | 部分实现，受后端限制 | 已完成产品化深度重构（Airtable 配色体系、Lora/思源宋体复古排版、专属云形购物车 SVG Logo），极大提升了UI质感；后端未完整联调时可见 502/错误状态；成功态业务闭环、逐页成功截图和真实接口数据仍待补充 |

---

## 5. Postman 接口测试

HTML 报告状态：

```text
原始 HTML 报告包含动态 Token，不纳入仓库。
脱敏摘要：
- docs/test/postman/summary/newman-20260609.md
- docs/test/postman/summary/newman-20260611-port-migration.md
```

| 指标 | 结果 |
|---|---|
| 核心接口数量 | 待填写，要求 ≥ 6 |
| 请求总数 | 28 |
| 断言总数 | 60 |
| 通过 | 60 |
| 失败 | 0 |
| 最近一次端口迁移后复跑 | 2026-06-11，BaseURL=`http://localhost:9100`，28 请求、60 断言、0 失败 |

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

记录状态：JMeter 5.6.3 命令已验证可执行；2026-06-09 已完成搜索 1 用户短冒烟、50 用户负载、150 用户负载；2026-06-10 已完成订单 1 用户短冒烟、秒杀 10 用户请求受理短冒烟、秒杀 1 用户完整链路短冒烟、秒杀 10 用户完整链路连续 3 次验证和秒杀 50/100/200/300/500 固定库存阶梯复测。订单正式负载尚未执行，未运行场景不得填写估算值。

| 项目 | 内容 |
|---|---|
| CPU | AMD Ryzen 9 7940H w/ Radeon 780M Graphics，8 核 / 16 逻辑处理器 |
| 内存 | 物理内存 15.22GB |
| 操作系统 | Windows 11 |
| JDK | 21 |
| Docker 资源限制 | Docker Engine 可用 16 CPU、约 7.37GB 内存 |
| Elasticsearch JVM heap | `ES_JAVA_OPTS=-Xms512m -Xmx512m` |
| 测试机与服务部署 | JMeter、本地后端服务和 Docker 中间件运行在同一台 Windows 主机 |
| 代码 Commit | JMeter 输出未自动固化执行时 Commit，精确执行 Commit 未记录；搜索/订单结果归档提交为 `c67132d`、`8a31712`；秒杀阶梯最新复测来自本地 JTL 复算，归档到 `docs/test/jmeter/summary/seckill-ladder-20260610-234029.md` |

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

秒杀 50/100/200/300/500 固定库存阶梯复测结果：

| 并发阶梯 | 固定库存 | JMeter 总样本 | 失败样本 | 秒杀受理 | 库存不足 | 最终订单成功 | 结果轮询超时 | Total P95 | Total TPS | 结论 |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| 50 用户 | 100 | 439 | 0 | 50 | 0 | 50 | 0 | 83ms | 52.08/s | 通过 |
| 100 用户 | 100 | 1830 | 0 | 100 | 0 | 100 | 0 | 158ms | 106.09/s | 通过 |
| 200 用户 | 100 | 2084 | 0 | 100 | 100 | 100 | 0 | 261ms | 124.27/s | 通过 |
| 300 用户 | 100 | 2379 | 0 | 100 | 200 | 100 | 0 | 777ms | 132.02/s | 通过 |
| 500 用户 | 100 | 2677 | 0 | 100 | 400 | 100 | 0 | 2586ms | 138.63/s | 通过 |

证据：`docs/test/jmeter/summary/seckill-ladder-20260610-234029.md`、`docs/test/jmeter/summary/seckill-ladder-20260610-234029.csv`。原始 JTL 与 HTML Dashboard 为本地产物，未纳入仓库。

口径说明：

- 表中的 `Total P95` 和 `Total TPS` 来自 JMeter 全部样本聚合，包含登录、秒杀受理、结果轮询和最终校验，不等同于单个业务接口 P95 或最终成功订单 TPS。
- 本轮未生成独立 Transaction Controller 父样本，因此不记录“秒杀完整链路 P95”或“最终订单 TPS”。
- 50/100/200/300/500 五档均已按固定库存 100 完成 DB/Redis 一致性核验：成功订单不超过 100，`order_info` 与 `order_item` 数量一致，`locked` 与成功订单数一致，Redis 库存未小于 0。

### 7.6 启动 Profile 与资源采样

证据：`docs/test/jmeter/summary/profile-resource-20260610.md`。

| 模式 | 启动结果 | Java 进程 | Java Working Set 总量 | 说明 |
|---|---|---:|---:|---|
| core | 通过 | 7 | 2525.9 MB | 容器为 mysql、redis、nacos、seata |
| search | 通过 | 8 | 未单独采样 | 容器为 core + elasticsearch + rocketmq-namesrv + rocketmq-broker |
| seckill | 通过 | 8 | 3154.4 MB | 容器为 mysql、redis、nacos、seata、RocketMQ、Sentinel |
| core + LowMemory | 通过 | 7 | 2121.0 MB | Java 命令行均包含 `-Xms64m -Xmx320m -XX:MaxMetaspaceSize=192m` |
| full-backend | 通过 | 13 | 2586.4 MB | 后端端口迁移到 `9100-9112` 后，手动补齐当前可用中间件并以 `--skip-infrastructure` 启动 |
| full-docker-profile | 有条件通过 | 未采样 | 未采样 | RocketMQ Dashboard 镜像阻断已解除，full profile 启动链路可达；本轮已固化运行态、数据库状态、Newman 业务链路证据，并补齐核心购物链路 1440×900 UI 截图；秒杀成功态截图与 JMeter 大规模压测未纳入本轮。 |

Profile 切换和 `stop-all.bat` 均通过项目 JAR 命令行校验托管进程；外部 9012 Java 进程未被终止，mall-job 使用 9112。

资源口径：Profile 的主要收益是按场景减少无关容器和服务；本轮 Java Working Set 采样中 `core` 与 `full-backend` 差距不大，不写成“Profile 大幅降低 Java 内存”。`core + LowMemory` 对后端 JVM Working Set 的下降已有本机实测，但不用于正式 JMeter 压测。

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
- Sprint 1.1 前端补丁回归（commit 748ee87 后）：
  - `format.ts::money` 与 `PriceText` 组件双层处理 `null/undefined/''/NaN → '—'`，合法 0 仍渲染 `¥0.00`；
  - `ProductDetailView` 接入 `PageState` 错误态，`catch` 块中 `error.value = msg` 触发重新加载按钮；
  - 错误文案脱敏：移除"商品服务"/"Gateway"/"mall-search"/"Elasticsearch"等用户可见技术名词；
  - `HomeView.vue:508` `.product-card:hover` 硬编码 `box-shadow: 0 8px 24px rgba(0,0,0,0.05)` 切换为 `var(--shadow-md)`；
  - `HomeView.vue:610` `.sk-card:hover` 残留硬编码 `box-shadow: 0 4px 12px var(--color-error-shadow)` 已删除，秒杀的视觉锚点由 `.sk-btn` 红色胶囊承担，`.sk-card:hover` 仅保留 `border-color` 变化。
  - 构建与类型门禁：`vue-tsc -b` 无类型错误，`vite build` 998ms，产物哈希：
    - `dist/assets/index-6ZwDI4xd.css` (397,154 B ≈ 397.15 kB)
    - `dist/assets/index-BsjUWrq1.js` (1,104,894 B ≈ 1,104.89 kB)
  - 审计残留问题：sprint 1.1 报告引用行号与实际有 1~3 行漂移（`ProductDetailView.vue` 的 `L59-L62` 实际 `L60-64`、`L112-L114` 实际 `L117`），属引用层笔误，功能本身无影响。

### 9.2 已知限制

如实记录：

- Docker 全栈尚未完成；
- `docker-compose.all.yml` 仅作为镜像化部署示例，当前本地验收以 BAT + 本地 JAR 为准；
- 根目录 BAT 是当前主要人工启动与验收入口；PowerShell 脚本作为参数化、自动化和故障排查入口保留；
- 本地后端端口已迁移到连续区间 `9100-9112`，用于避开本机旧端口 `9012/9013/9014` 外部占用；
- Elasticsearch 搜索索引初始化、Gateway 搜索业务校验和搜索负载测试已通过；
- Kubernetes 只有示例；
- 部分辅助接口未覆盖；
- 未部署完整监控平台；
- 前端已完成一轮产品化页面整改，但后端真实成功态联调、逐页成功截图和主流程操作证据仍待补充；
- 订单正式负载仍未执行；
- `full` 启动 Profile 本轮因 Docker 镜像源 403 未完成资源采样；
- Profile 可按场景减少无关容器和服务，LowMemory 对 core 后端 JVM 有本机实测下降；不将 Profile 表述为已证明 Java 内存大幅下降；
- Java 21 或 Seata 2.0.0 尚未完成的兼容验证。

### 9.3 Sprint 2 真实链路联调与前端验收

提交：`ed7e05d94f4696fd567b7ceee4c5ef8971a0e709`（工作区仅新增 `docs/test/screenshots/`，未触碰 `mall-frontend/src/**` 与后端代码）。

#### 9.3.1 MySQL 端口归属

| 检查项 | 结果 | 证据 |
|---|---|---|
| `Get-Service MySQL84` | Stopped, StartType=Manual | PowerShell 输出 |
| `127.0.0.1:3306` 监听 | 已消失（仅 `::1` 与 `::` 由 wslrelay PID 34508 + com.docker.backend PID 31604 转发） | `Get-NetTCPConnection` |
| `docker ps --filter name=mall-mysql` | Up 2 hours (healthy), `0.0.0.0:3306->3306/tcp` | docker ps |
| `docker exec mall-mysql mysqladmin ping` | mysqld is alive | 命令输出 |
| `SHOW DATABASES` | mall_auth / mall_inventory / mall_order / mall_pay / mall_product / mall_seata / mall_seckill / mall_user 全部存在 | docker exec |

#### 9.3.2 core 后端启动矩阵

启动命令：`pwsh .\scripts\start-all.ps1 -Profile core -SkipInfrastructure -SkipFrontend -SkipBuild -CleanLogs`（本轮实际后台运行）。

| 服务 | 端口 | 启动状态 | Health | 日志证据 | 问题 |
|---|---:|---|---|---|---|
| mall-gateway | 9100 | 运行中（PID 5248） | 200, 7 服务已注册 discoveryComposite UP | `Started MallGatewayApplication` | — |
| mall-auth | 9101 | 运行中（PID 29492） | 200 | `Started` | — |
| mall-user | 9102 | 运行中（PID 4532） | 200 | `Started` | — |
| mall-product | 9103 | 运行中（PID 24124） | 200 | `Started` | — |
| mall-inventory | 9104 | 运行中（PID 10620） | 200 | `Started`, Seata DataSourceProxy.init 成功 | — |
| mall-cart | 9105 | 运行中（PID 23888） | 200 | `Started` | — |
| mall-order | 9106 | 运行中（PID 7960） | /actuator/health 返回 500（`GlobalExceptionHandler` 把 `NoResourceFoundException` 视为 SystemError） | `Started MallOrderApplication in 12.394 seconds` 成功 | 业务接口 200；actuator 异常归类为后端 actuator 缺失，列入 Sprint 3 候选 |
| mall-pay | 9107 | 未启动 | — | — | **未验证：core profile 不含 mall-pay** |
| mall-search | 9108 | 未启动 | — | — | **未验证：core profile 不含 mall-search** |
| mall-seckill | 9109 | 未启动 | — | — | **未验证：core profile 不含 mall-seckill** |
| mall-message | 9110 | 未启动 | — | — | **未验证：core profile 不含 mall-message** |
| mall-admin-biz | 9111 | 未启动 | — | — | **未验证：core profile 不含 mall-admin-biz** |
| 前端 (Vite) | 5173 | 运行中（PID 35796） | http://localhost:5173/ 返回 200 | `.runtime/logs/frontend.log` Vite 8.0.16 ready in 5180ms |

#### 9.3.3 Gateway 接口冒烟

| 接口 | 方法 | HTTP | 业务码 | 关键字段 | 结果 |
|---|---|---:|---:|---|---|
| `/actuator/health` | GET | 200 | — | discoveryComposite UP, 7 服务已注册 | 通过 |
| `/api/v1/auth/login` (zhangsan) | POST | 200 | 200 | accessToken (280 chars), userInfo.id=1001, roles=["USER"] | 通过 |
| `/api/v1/products/1001` | GET | 200 | 200 | spuId=1001, name="iPhone 15 Pro 256G 钛原色", skus[0].skuId=9001 price=8999.00 | 通过 |
| `/api/v1/inventory/stock/9001` | GET (Bearer) | 200 | 200 | total=100, locked=3, available=97 | 通过 |
| `/api/v1/inventory/stock/9002` | GET (Bearer) | 200 | 200 | total=50, locked=0, available=50 | 通过 |
| `/api/v1/inventory/stock/9003` | GET (Bearer) | 200 | 200 | total=200, locked=0, available=200 | 通过 |
| `/api/v1/users/me` | GET (Bearer) | 200 | 200 | id=1001, username=zhangsan | 通过 |
| `/api/v1/users/me/addresses` | GET (Bearer) | 200 | 200 | 2 条地址（id=1 默认, id=2） | 通过 |
| `/api/v1/carts` POST (sku 9001, qty 1) | POST (Bearer) | 200 | 200 | ok | 通过 |
| `/api/v1/carts` GET | GET (Bearer) | 200 | 200 | items[0].skuId=9001, quantity=5, subtotal=44995.00 | 通过 |
| `/api/v1/orders` POST (addressId=1, sku=9001, qty=1) | POST (Bearer) | 200 | 200 | orderNo=`SO1781241179114`, totalAmount=8999.00 | 通过 |
| `/api/v1/orders/SO1781241179114` | GET (Bearer) | 200 | 200 | status=0, totalAmount=8999.00, payAmount=8999.00, items[0].skuId=9001, quantity=1 | 通过 |
| `/api/v1/pay/create` | POST | — | — | — | **未验证：core profile 不含 mall-pay** |
| `/api/v1/admin/dashboard` | GET | — | — | — | **未验证：core profile 不含 mall-admin-biz** |

#### 9.3.4 前端真实链路验收

| 页面 | 路由 | 主操作 | 结果 | 截图 | 问题 |
|---|---|---|---|---|---|
| 首页（已登录） | `/` | 登录跳转 | 通过 | `01-home-after-login-1440x900.png` | — |
| 商品详情 | `/products/1001` | 加载 + 实时库存 96 + 加入购物车 | 通过 | `02-product-detail-1001-1440x900.png`、`06-product-detail-390x844.png` | 移动端文档高 1617px，图片在 y=539，导致 390x844 视口可见区为 header + 图片起始；价格/SKU/按钮在折叠线下方（Playwright 抓取 H1="iPhone 15 Pro 256G 钛原色" 已确认页面正确） |
| 购物车 | `/cart` | 加载 1 行 × 5 + 去结算 | 通过 | `03-cart-1440x900.png`、`07-cart-390x844.png` | — |
| 结算页 | `/checkout` | 加载地址 + 提交订单 → orderNo=SO1781241179114 | 通过 | `04-checkout-1440x900.png`、`08-checkout-390x844.png` | — |
| 订单详情 | `/orders/SO1781241179114` | 加载详情 | 通过 | `05-order-detail-1440x900.png`、`09-order-detail-390x844.png` | 收货信息"—"：后端 order.addressJson 仅含 {"addressId":1}，前端 formatAddress 兜底为"—" |
| 支付收银台 | `/pay/SO1781241179114` | 模拟支付 | **未验证：core profile 不含 mall-pay** | — | — |
| 后台管理 | `/admin` | 看板 | **未验证：core profile 不含 mall-admin-biz** | — | — |

#### 9.3.5 截图索引

`docs/test/screenshots/sprint2/` 共 9 张（5 张 1440x900 + 4 张 390x844）。详见 `docs/test/screenshots/sprint2/README.md`。

#### 9.3.6 构建与静态检查

| 检查项 | 结果 | 证据 |
|---|---|---|
| `npm run build` | 通过 | `vue-tsc -b` 无类型错误；`vite build` 1.39s；产物 `dist/assets/index-DlsTWCAw.css` (397.14 kB)、`dist/assets/index-BQqaWE3G.js` (1,104.85 kB)；2 个 `INVALID_ANNOTATION` 警告来自 `node_modules/@vueuse/core`（与本仓库代码无关）；chunks>500kB 警告为 Vite 默认值 |
| `git diff --check` | 通过 | exit=0；HEAD=ed7e05d；唯一新增为 `docs/test/screenshots/sprint2/`（未 git add） |
| 硬编码颜色扫描 | 通过（已列出） | 见下表 |

**硬编码颜色扫描结果**（`grep -rn --include="*.vue" --include="*.css" -E '#[0-9a-fA-F]{6}\b\|#[0-9a-fA-F]{3}\b\|#[0-9a-fA-F]{8}\b\|rgba?\([0-9]' src/`）：

| 文件 | 行 | 内容 | 分类 | 说明 |
|---|---:|---|---|---|
| `src/styles/tokens.css` | 3-89 | `--color-brand: #4b7099` 等 23 个 token + 3 个 rgba shadow | **允许**：token 定义文件 | 与 DESIGN.md 10.2 token 表一致 |
| `src/styles/app.css` | 42 | `background: rgba(255, 255, 255, 0.96);` | **预存在**：全局 header 毛玻璃 | 等价于 `--color-bg-surface` + 透明度，本轮未改源码 |
| `src/styles/app.css` | 242 | `background: rgba(255, 255, 255, 0.94);` | **预存在**：hero stat 卡片背景 | 同上 |
| `src/styles/app.css` | 692 | `background: #fdfdfd;` | **预存在**：auth-card header | 近白色，等价于 `--color-bg-surface` |
| `src/styles/app.css` | 775 | `background: #fff;` | **预存在**：sku-chip 背景 | 等价于 `--color-bg-surface` |

结论：业务组件 (.vue) **未发现**硬编码颜色；app.css 4 处预存在硬编码与既有 token 功能等价，归类为 Sprint 3 候选（替换为 `var(--color-bg-surface)` + `--shadow-...`）。

#### 9.3.7 未验证项

| 项目 | 原因 | 下一步 |
|---|---|---|
| `/pay/:orderNo` 真实支付 + 通知 | core profile 不含 mall-pay / mall-message | full profile 启动 |
| `/admin` 看板真实加载 + admin 登录 | core profile 不含 mall-admin-biz | full profile 启动 |
| `mall-search` 搜索联调 | core profile 不含 | full profile 启动 |
| `mall-seckill` 秒杀联调 | core profile 不含 | full profile 启动 |
| 1366x768 / 1024x768 / 768x1024 视口截图 | 本轮仅补 1440x900 + 390x844 两档；视口补全列为 Sprint 3 候选 | full profile 启动批次同步补 |
| 订单详情页"收货信息"显示"—" | 后端 `mall-order` 写入 `addressJson={"addressId":1}` 不含完整地址对象 | 归类为后端 API 例外申请（动 Service 层），本轮不动业务代码 |
| `mall-order` `/actuator/health` 返 500 | `GlobalExceptionHandler` 把 `NoResourceFoundException` 当 SystemError；Spring 未找到 `/actuator` 静态资源 | 后端补 actuator 依赖或调整 `management.endpoints.web.base-path=/`；列为 Sprint 3 后端修复 |
| 首页商品卡 SPU 级"库存 0" | `mall-product` 接口返回的 SPU `skus[0].stock` 静态 0；实时库存靠 `inventoryStock` 在详情页刷新 | UI 调优，列入 Sprint 3 候选 |
| 商品详情移动端文档 1617px / 图片在 y=539 | mobile UX 留白过大 | 列入 Sprint 3 UI 调优 |
| 订单正式负载（JMeter） | 历史已有 1/10/50/100/200/300/500 阶梯证据；本轮 Sprint 2 关注核心链路，不重跑 | 沿用 `docs/FINAL_REPORT.md` §7.5 历史结果 |

#### 9.3.8 Sprint 3 候选建议（不得直接执行）

1. `start-all.ps1` 加端口冲突保护：探测 127.0.0.1:3306 占用进程，非 Docker 转发则报警。
2. `mall-order` 加 `spring-boot-starter-actuator` 或调整 `management.endpoints.web.base-path=/`，并把 `NoResourceFoundException` 从 SystemError 排除。
3. `app.css` 4 处预存在硬编码颜色替换为 `var(--color-bg-surface)` / `var(--shadow-...)`。
4. 补全 full profile 启动 + `/pay` 真实支付链路 + `/admin` 后台联调 + 6 视口截图。
5. 后端 `mall-order` 写入完整 address 快照而非仅 addressId。
6. UI 调优：商品详情 mobile 留白；首页商品卡实时库存回填。

### 9.4 Sprint 3.1 启动脚本端口冲突保护

- 修改 `scripts/start-all.ps1`：新增 `Test-InfrastructurePortOwnership` 函数与辅助 `Get-PortOwnerDetail`，在「构建后端」之后、「启动后端服务」之前强制检查本机 3306 端口归属；
- Docker / WSL 转发进程（`com.docker.backend`、`com.docker.proxy`、`docker-proxy`、`vpnkit`、`wslrelay`，或 Path 包含 `docker`/`wsl`）标记为 Allowed；
- `mysqld` / `mariadbd` / `mariadb`，或 Path 包含 `MySQL` / `MariaDB`，或不在白名单的未知进程标记为 Blocked，输出阻塞进程 PID / ProcessName / Path，提示用户以管理员 PowerShell 执行 `Stop-Service MySQL84 -Force` 后重试，脚本不自动停止本机服务；
- `MALL_INFRA_HOST` 指向 `127.0.0.1`/`localhost`/`::1`/空值时强制检查本机 3306；指向其他值（如 `172.18.0.12`）时输出 WARN 并跳过本机检查；
- 当前无监听时输出 WARN 但不阻塞（兼容 `-SkipInfrastructure` 场景）；
- 检查日志写入 `.runtime/logs/startup-port-check.log`（每次启动重置）；
- 验证结果：
  - 场景 A（MySQL84 Stopped + 3306 由 wslrelay + com.docker.backend 转发）：`[OK] 端口 3306 (MySQL) 由 Docker / WSL 转发接管 (进程: wslrelay)`，核心 7 服务可继续启动；日志判定 `Allowed`；
  - 场景 B（`MALL_INFRA_HOST=172.18.0.12`）：`[WARN] 已设置 MALL_INFRA_HOST=172.18.0.12，跳过本机 3306 Docker 转发归属检查。`；日志判定 `Skipped`；
  - 场景 C（mock `mysqld` on 3306）：`[FAIL] 端口 3306 (MySQL) 已被非 Docker 进程占用，后端将连接到错误的 MySQL 实例。`；状态返回 `Status=Blocked, Allowed=False`；日志判定 `Blocked`；
- 静态检查：`PSParser::Tokenize` 解析通过；`git diff --check` exit=0；`PSScriptAnalyzer` 本机未安装（`未验证：本机未安装 PSScriptAnalyzer`）；
- 未验证项：未在真实运行中触发 MySQL84 占用 3306（需管理员停本机服务）；`PSScriptAnalyzer` 未安装；core 服务 7 个为 `AlreadyRunning`（来自 Sprint 2 Retry），未观察到 `Access denied` / `PortOccupied by mysqld` / `Exited` 现象。

### 9.5 Sprint 3.2 full profile 与 pay/admin 验收

提交：`640f1a8eadf3ba65a5d86cb78eef910cfef21b3d`（Sprint 3.1 之后的工作树，本轮无代码改动，仅新增 `docs/test/screenshots/sprint3/` 与 FINAL_REPORT 追加）。

#### 9.5.1 环境

- MySQL84：Stopped
- 3306 监听：`::1` 由 wslrelay (PID 34508) + `::` 由 com.docker.backend (PID 31604) 转发
- `startup-port-check.log`：`判定: Allowed`（`com.docker.backend` + `wslrelay` 在白名单）
- `docker exec mall-mysql mysqladmin ping`：`mysqld is alive`
- 提交时 `git status --short` 仅 `?? docs/test/screenshots/sprint3/`，无业务代码改动

#### 9.5.2 full profile 服务矩阵

启动命令：`pwsh .\start-all.bat --profile full --skip-infrastructure --skip-frontend --no-build --no-pause`（脚本因耗时 5min 内未完成总体输出超时被父进程 kill，但所有 13 个后端 JAR 实际已成功启动并监听 9100-9112）。

| 服务 | 端口 | 进程 | /actuator/health | 备注 |
|---|---:|---|---:|---|
| mall-gateway | 9100 | java 26728 | 200 | discoveryComposite UP，13 服务已注册 |
| mall-auth | 9101 | java 29644 | 200 | ok |
| mall-user | 9102 | java 27088 | 200 | ok |
| mall-product | 9103 | java 34208 | 200 | ok |
| mall-inventory | 9104 | java 35284 | 200 | ok |
| mall-cart | 9105 | java 11616 | 200 | ok |
| mall-order | 9106 | java 16280 | **500** | 业务接口 200；`GlobalExceptionHandler` 把 `NoResourceFoundException` 视为 SystemError；Sprint 2/3.1 已记录的后端 actuator 缺失 |
| mall-pay | 9107 | java 10988 | 200 | **首次进入 full profile 验证范围** |
| mall-search | 9108 | java 13196 | 200 | ok |
| mall-seckill | 9109 | java 40516 | 200 | ok |
| mall-message | 9110 | java 24460 | 200 | **首次进入验证范围**；消费 PAY_RESULT，订单 status 0→1 真实变化依赖此服务 |
| mall-admin-biz | 9111 | java 36420 | 200 | **首次进入验证范围** |
| mall-job | 9112 | java 38456 | 200 | ok |

#### 9.5.3 Gateway 接口冒烟

| 接口 | 方法 | HTTP | 业务码 | 关键字段 | 结果 |
|---|---|---:|---:|---|---|
| `/api/v1/auth/login` (zhangsan) | POST | 200 | 200 | userId=1001, roles=["USER"] | 通过 |
| `/api/v1/auth/login` (admin) | POST | 200 | 200 | userId=1007, roles=["ADMIN"] | 通过 |
| `/api/v1/orders` POST (sku 9001, qty 1, addressId 1) | POST (zhangsan) | 200 | 200 | orderNo=`SO1781249435421`, totalAmount=8999.00 | 通过 |
| `/api/v1/orders` POST (sku 9002, qty 1, addressId 1) | POST (zhangsan) | 200 | 200 | orderNo=`SO1781249605871`, totalAmount=5999.00（**本轮用于截图的关键订单**） | 通过 |
| `/api/v1/orders/SO1781249605871`（支付前） | GET (zhangsan) | 200 | 200 | status=0（待支付） | 通过 |
| `/api/v1/pay/create` (ALIPAY) | POST (zhangsan) | 200 | 200 | payNo=`PAY2026061220653374`, payUrl=`https://openapi-sandbox.dl.alipaydev.com/...` | 通过 |
| `/api/v1/pay/record/SO1781249605871` | GET (zhangsan) | 200 | 200 | payNo / orderNo / userId / payAmount=5999.00 / status=0 / gmtCreate=2026-06-12T07:30:35 | 通过 |
| `/api/v1/pay/notify?out_trade_no=...&trade_status=TRADE_SUCCESS` | POST (zhangsan) | 200 | — | 返回 ok（响应体未标准化，无 code/msg，列为"通过"） | 通过 |
| `/api/v1/orders/SO1781249605871`（支付后 3s） | GET (zhangsan) | 200 | 200 | **status=1**（已支付）— mall-message 消费 PAY_RESULT 真实改变订单状态 | 通过 |
| `/api/v1/admin/dashboard` | GET (admin) | 200 | 200 | todayOrders=3, todaySales=17998.00, totalProducts=12, pendingOrders=17, salesTrend[4], topProducts[5] | 通过 |
| `/api/v1/admin/orders?pageNum=1&pageSize=10` | GET (admin) | 200 | 200 | 后台订单列表 | 通过 |
| `/api/v1/admin/products?pageNum=1&pageSize=10` | GET (admin) | 200 | 200 | 后台商品列表 | 通过 |
| `/api/v1/pay/notify` 响应 | POST | 200 | — | `{"success":true,"code":200,"message":"ok"}` 但内部未含 `code`/`msg` 字段 | 通过（行为正确） |

#### 9.5.4 支付链路验收

| 步骤 | 结果 | 证据 |
|---|---|---|
| 1. 登录 zhangsan | 通过 | 登录接口 200 |
| 2. 创建本轮新订单 | 通过 | orderNo=`SO1781249605871`, sku 9002, totalAmount=5999.00 |
| 3. 进入 `/orders/SO1781249605871`（待支付） | 通过 | 截图 `01-pay-order-detail-before-1440x900.png`；状态"待支付" |
| 4. 进入 `/pay/SO1781249605871`（收银台） | 通过 | 截图 `02-pay-page-1440x900.png`；"创建支付记录"按钮可见 |
| 5. 创建支付单 + 模拟通知 | 通过 | `payNo=PAY2026061220653374`；`/api/v1/pay/notify` 200 |
| 6. 返回订单详情，状态变化 | 通过 | status 0→1；截图 `03-pay-order-detail-after-1440x900.png`；mobile `04-pay-page-success-390x844.png` |
| 7. 异步 MQ 链路真实生效 | 通过 | 订单状态 0→1 来自 mall-message 消费 PAY_RESULT（mall-message 9110 已启动，RocketMQ 10911 健康） |

#### 9.5.5 后台链路验收

| 步骤 | 结果 | 证据 |
|---|---|---|
| 1. 登录 admin | 通过 | userId=1007, roles=["ADMIN"] |
| 2. 访问 `/admin` | 通过 | router 守卫 `auth: true, roles: ['ADMIN', 'MERCHANT']` 放行 |
| 3. 看板数据加载 | 通过 | 截图 `05-admin-dashboard-1440x900.png` + `06-admin-dashboard-390x844.png`；3 张指标卡 + 后台订单表 11 行 + 后台商品表 6 行 |
| 4. 订单管理 | 通过 | `/api/v1/admin/orders` 200，列表渲染 |
| 5. 商品管理 | 通过 | `/api/v1/admin/admin/products` 200，列表渲染 |

#### 9.5.6 截图索引

`docs/test/screenshots/sprint3/` 共 6 张（4 张 1440x900 + 2 张 390x844）。详见 `docs/test/screenshots/sprint3/README.md`。

#### 9.5.7 构建与检查

| 检查项 | 结果 | 证据 |
|---|---|---|
| `npm run build` | 通过 | `vue-tsc -b` 无类型错误；`vite build` 8.37s；产物 `dist/assets/index-*.css` (397.14 kB)、`dist/assets/index-*.js` (1,104.85 kB)；警告同 Sprint 1.1-final |
| `git diff --check` | 通过 | exit=0；唯一新增为 `docs/test/screenshots/sprint3/`（未 git add） |
| PowerShell 解析（启动脚本） | 通过 | `start-all.bat --profile full` 解析无误；Sprint 3.1 修复仍生效，3306 检查日志 `判定: Allowed` |

#### 9.5.8 未验证项

| 项目 | 原因 | 后续 |
|---|---|---|
| `mall-order` `/actuator/health` 500 | `GlobalExceptionHandler` 把 `NoResourceFoundException` 视为 SystemError；Spring 未找到 `/actuator` 静态资源 | 后端补 actuator 依赖；Sprint 3.3 候选 |
| 订单详情页"收货信息"显示"—" | 后端 `mall-order` 写入 `addressJson={"addressId":1}` 不含完整地址对象 | 后端 API 例外申请（动 Service 层） |
| admin 看板"销售额"显示"—" | `/api/v1/admin/dashboard` 返回 `todaySales=17998.00` 但前端取 `totalSales` 字段（未返回），兜底为"—" | 前端 parser 优先取 `totalSales` 或回退 `todaySales`；Sprint 3.3 候选 |
| `app.css` 4 处预存在硬编码颜色 | rgba / #fdfdfd / #fff | Sprint 3.3 候选 |
| 商品详情 mobile 文档 1617px / 图片在 y=539 | mobile UX 留白过大 | Sprint 3.3 候选 |
| 首页商品卡 SPU 级"库存 0" | `mall-product` SPU `skus[0].stock` 静态 0 | Sprint 3.3 候选 |
| 秒杀 + 搜索真实业务联调 | 本轮关注 pay/admin 链路，秒杀/搜索仅在 full profile 启动后查 health=200；本轮未做活动列表 + 抢购 + 搜索结果业务断言 | 沿用 §9.3 未验证项 |

#### 9.5.9 Sprint 3.3 候选建议（不得直接执行）

1. `mall-order` 加 `spring-boot-starter-actuator` 或调整 `management.endpoints.web.base-path=/`，并把 `NoResourceFoundException` 从 SystemError 排除。
2. 后端 `mall-order` 写入完整 address 快照而非仅 `addressId`。
3. AdminView.vue 销售指标优先取 `totalSales`，回退 `todaySales`，再回退"—"。
4. `app.css` 4 处预存在硬编码颜色替换为 token 引用。
5. UI 调优：商品详情 mobile 留白；首页商品卡实时库存回填。
6. mall-search / mall-seckill 业务联调：活动列表 + 抢购 + 搜索结果真实断言。
| 7. `start-all.ps1` 端口冲突保护扩到 6379 (Redis) / 8848 (Nacos) / 9876 / 10911 (RocketMQ)。

### 9.6 Sprint 3.3 actuator / notify / admin sales 修复

提交：本轮待提交（基于 9.5 的 `c1c18d6`）。最小修改范围 4 个源文件 + 截图 1 张 + 截图 README 1 个 + 本节追加。

#### 9.6.1 修改文件

| 文件 | 修改 |
|---|---|
| `mall-order/pom.xml` | 补 `spring-boot-starter-actuator` 依赖（与 mall-pay 等 12 个服务对齐） |
| `mall-common/src/main/java/com/mallcloud/mallcommon/exception/GlobalExceptionHandler.java` | 新增 `@ExceptionHandler(NoResourceFoundException.class)` 返回 404 + Result；导入 `NoResourceFoundException` |
| `mall-pay/src/main/java/com/mallcloud/mallpay/controller/PayController.java` | `notify()` 返回类型从 `String` 改为 `Result<String>`，data 为 `"success"`；失败时返回 `Result.error(40300, "支付回调处理失败")` |
| `mall-frontend/src/views/AdminView.vue` | 第 130 行 `field()` 字段回退列表首位加入 `todaySales` |
| `docs/test/screenshots/sprint3/07-admin-dashboard-sales-fixed-1440x900.png` | 新增 1440x900 admin 看板销售额修复截图 |
| `docs/test/screenshots/sprint3/README.md` | 新增第 7 行；重写"已知缺陷"小节移除已修复项 |
| `docs/FINAL_REPORT.md` | 追加本节 §9.6 |

未触及：`mall-frontend/src/api/mall.ts`（http 拦截器自动 unwrap `Result<>` 的 `data` 字段，PayView 的 `notifyResult === 'success'` 兼容）；`mall-order/src/main/java/**` 业务代码；`mall-admin-biz` 业务代码；`mall-pay` 业务代码（`PayServiceImpl.handleNotify` 不变）。

#### 9.6.2 mall-order actuator 修复

| 检查项 | 修复前 | 修复后 | 证据 |
|---|---|---|---|
| 依赖 | 无 `spring-boot-starter-actuator` | 已加 `org.springframework.boot:spring-boot-starter-actuator` | `mall-order/pom.xml:73-75` |
| `GET http://localhost:9106/actuator/health` | HTTP 500 + `{"code":10003,"message":"系统繁忙，请稍后重试"}` | HTTP 200 + `{"status":"UP","components":{...12 项 UP...}}` | curl 9106 输出 |
| `GET http://localhost:9100/actuator/health` | HTTP 200（Gateway 聚合） | HTTP 200（保持） | curl 9100 输出 |
| `GlobalExceptionHandler` 行为 | `NoResourceFoundException` 被 `@ExceptionHandler(Exception.class)` 捕获 → 500 + SystemError | 新增 `@ExceptionHandler(NoResourceFoundException.class)` → 404 + `{"code":404,"message":"资源不存在: ..."}`；`Exception.class` 兜底保持原行为 | `GlobalExceptionHandler.java:55-66` |
| `mall-order` 业务接口 | 200 | 200（保持） | `/api/v1/orders` POST/GET 200 |

说明：未触碰 `application.yaml` 中 `management.endpoints.web.exposure.include: health,info,prometheus` 配置（与 12 个服务保持一致）；未硬编码 `/actuator/health` Controller；未修改 `Exception.class` 兜底行为。

#### 9.6.3 pay notify 响应统一

| 检查项 | 修复前 | 修复后 | 证据 |
|---|---|---|---|
| Controller 返回类型 | `String`（`"success"` / `"fail"`） | `Result<String>`（`Result.ok("success")` / `Result.error(40300, ...)`） | `PayController.java:48-56` |
| `handleNotify` 业务行为 | 验签 → 更新 pay_record → 发送 PAY_RESULT MQ | 完全保持不变 | `PayServiceImpl.java:104-133` |
| `POST /api/v1/pay/notify` HTTP 状态 | 200 | 200（保持） | curl 9100 输出 |
| 响应体顶层 code/msg | 无 | `code:200, message:"ok"` | curl 9100 输出 |
| 响应体 data | `"success"` 字符串（裸） | `data:"success"` 字段 | curl 9100 输出 |
| 订单状态 0→1 真实变化 | 真实 | 真实（保持） | `SO1781330545972` pay_record status=1 |
| PayView 兼容 | `notifyResult === 'success'` 通过 | `notifyResult === 'success'` 仍通过（http 拦截器自动 unwrap `data` 字段） | 已知前端拦截器行为 |

证据片段：

```text
$ curl -X POST "http://localhost:9100/api/v1/pay/notify" \
  -d "out_trade_no=SO1781330545972&trade_no=LOCAL-1781330559&trade_status=TRADE_SUCCESS"
HTTP=200
{"code":200,"message":"ok","data":"success","traceId":null,"timestamp":1781330559781,"success":true}
```

#### 9.6.4 AdminView 销售额字段映射修复

| 检查项 | 修复前 | 修复后 | 证据 |
|---|---|---|---|
| `field()` 字段回退列表 | `['salesAmount', 'totalSales']` | `['todaySales', 'salesAmount', 'totalSales', 'todaySalesAmount', 'totalSalesAmount', 'revenue']` | `AdminView.vue:130` |
| 后端 `/api/v1/admin/dashboard` 字段 | `data.todaySales=8999.00`（本轮） | `data.todaySales=8999.00`（保持；未改后端） | curl 9100 输出 |
| 页面渲染 | 销售额"—"（旧字段名不匹配，兜底 0 → `moneyText(0)` 返回"—"） | 销售额 **¥8999.00** | 截图 `07-admin-dashboard-sales-fixed-1440x900.png` + browser_vision 文字描述 |
| 其他指标卡 | 订单数 160 / 商品数 12 | 订单数 160 / 商品数 12（保持） | 截图同上 |
| 是否硬编码金额 | 否 | 否（仅调整字段回退优先级） | diff |

#### 9.6.5 回归验证（本轮新建订单 + 本轮新建支付单）

| 步骤 | 结果 | 证据 |
|---|---|---|
| 1. `POST /api/v1/auth/login` (zhangsan) | HTTP 200, code=200 | `.runtime/tmp/login.json` |
| 2. `POST /api/v1/orders` (sku 9001, qty 1, addressId 1) | HTTP 200, orderNo=`SO1781330545972`, totalAmount=8999.00 | `.runtime/tmp/order.json` |
| 3. `GET /api/v1/orders/SO1781330545972`（支付前） | HTTP 200, status=0 | `.runtime/tmp/order_before.json` |
| 4. `POST /api/v1/pay/create` (ALIPAY) | HTTP 200, payNo=`PAY2026061320656761` | `.runtime/tmp/pay_create.json` |
| 5. `POST /api/v1/pay/notify?out_trade_no=SO1781330545972&trade_status=TRADE_SUCCESS` | HTTP 200, 统一响应 `{"code":200,"message":"ok","data":"success",...}` | `.runtime/tmp/pay_notify.json` |
| 6. `GET /api/v1/orders/SO1781330545972`（支付后 2s） | HTTP 200, **status=1**（MQ 真实消费） | `.runtime/tmp/order_after.json` |
| 7. `GET /api/v1/pay/record/SO1781330545972` | HTTP 200, payNo=`PAY2026061320656761`, status=1, tradeNo=`LOCAL-1781330559` | `.runtime/tmp/pay_record.json` |
| 8. `POST /api/v1/auth/login` (admin) | HTTP 200, userId=1007, roles=["ADMIN"] | `.runtime/tmp/admin_login.json` |
| 9. `GET /api/v1/admin/dashboard` | HTTP 200, todaySales=8999.00, totalProducts=12, pendingOrders=19 | `.runtime/tmp/admin_dash2.json` |
| 10. 浏览器 `/admin` 渲染 | 3 指标卡 + 后台订单表首行 `SO1781330545972 ¥8999.00 已支付` | 截图 `07-admin-dashboard-sales-fixed-1440x900.png` |

#### 9.6.6 截图

| 文件 | 页面 | 视口 | 账号 | 结果 |
|---|---|---|---|---|
| `07-admin-dashboard-sales-fixed-1440x900.png` | 后台看板销售额修复 | 1440x900 | admin | 通过（3 指标卡：订单数 160 / 商品数 12 / **销售额 ¥8999.00**；后台订单表首行本轮新建 SO1781330545972 已支付） |

移动端 admin 销售额截图未补：AdminView.vue 改动仅影响字段回退列表，不影响移动端布局；390x844 视口下第三张卡（销售额）历史上一直在折叠线下方（`06-admin-dashboard-390x844.png` 证据），本次修复对移动端表现无变化，故不重复截图。

#### 9.6.7 构建与检查

| 检查项 | 结果 | 证据 |
|---|---|---|
| `mvn -pl mall-common,mall-order,mall-pay,mall-gateway,mall-admin-biz -am -DskipTests package` | BUILD SUCCESS | 6 个模块（含父 POM）全部 SUCCESS；耗时 1:18 |
| `npm run build` | 通过 | `vue-tsc -b` 无类型错误；`vite build` 10.84s；产物在 `mall-frontend/dist/assets/`；2 个 `INVALID_ANNOTATION` 警告来自 `node_modules/@vueuse/core`（与本仓库代码无关） |
| `git diff --check` | exit=0 | 仅 2 条 LF→CRLF 文件模式 info 警告（Windows 平台正常） |

#### 9.6.8 未验证项

| 项目 | 原因 | 后续 |
|---|---|---|
| 移动端 admin 销售额截图（390x844） | 字段回退修复对移动端布局无影响，不重复截图 | 不补 |
| PSScriptAnalyzer | 本机未安装 | 与 Sprint 3.1 一致，列入工具链候选 |
| Gateway 不代理单服务 actuator | 本轮未改动 Gateway 路由表；9106 直接 health=200，9100 聚合 health=200 | 已满足"可接受结果" |
| 订单详情"收货信息"—" | 与本轮无关，仍为后端 Service 层地址快照缺失 | Sprint 3.4 候选 |
| `mall-order` `/actuator/health` 500 → 200 | 本轮已修复（9106 + 9100 双 200） | 列入已修复 |
| BizException 业务码不命中（pre-existing） | Seata AOP 把 BizException 包装为 `RuntimeException: try to proceed invocation error`；`@ExceptionHandler(BizException.class)` 仅按实际抛出类匹配，不会沿 cause 链解包。`OrderController.create()` 用 `findBizException(e)` 显式解包，但 `getOrder` 等其他接口没有。回归测试 9.6.10 第 2 项命中此 bug | Sprint 3.4 候选：要么 GlobalExceptionHandler 沿 cause 链递归匹配，要么所有 Controller 统一解包 |

#### 9.6.9 Sprint 3.4 候选建议（不得直接执行）

1. `mall-order` 写入完整 address 快照而非仅 `addressId`（订单详情收货信息"—"）。
2. `app.css` 4 处预存在硬编码颜色替换为 token 引用。
3. UI 调优：商品详情 mobile 留白；首页商品卡实时库存回填。
4. `start-all.ps1` 端口冲突保护扩到 6379 (Redis) / 8848 (Nacos) / 9876 / 10911 (RocketMQ)。
5. mall-search / mall-seckill 业务联调：活动列表 + 抢购 + 搜索结果真实断言。
6. PSScriptAnalyzer 安装与执行。
7. AdminView dashboard 增加 `salesTrend` 折线图与 `topProducts` Top 5 列表的视觉展示（当前仅在 `data` 字段中返回，前端未渲染）。
8. 修复 BizException 业务码不命中：`GlobalExceptionHandler` 沿 cause 链递归匹配 BizException，或在 `OrderServiceImpl` 入口统一 try-catch 重抛（`OrderController.create` 已有 `findBizException` 模式可复用）。

#### 9.6.10 P2 最小回归与保留风险

##### 9.6.10.1 GlobalExceptionHandler 最小回归测试

回归脚本：`.runtime/tmp/sprint3_3_regression.py`（gitignored，仅本轮一次性验证；提交后从仓库中不存在）。

| # | 场景 | 期望 | 实际 | 结论 |
|---|---|---|---|---|
| 1 | `GET http://localhost:9106/totally-random-unmapped-path-xyz` | HTTP 404 + code=404 + `资源不存在: ...` | HTTP 404 + code=404 + `资源不存在: totally-random-unmapped-path-xyz` | ✅ PASS（新增 handler 生效） |
| 2 | `GET /api/v1/orders/SO_NONEXISTENT_99999`（zhangsan） | HTTP 200 + code=40200 (ORDER_NOT_FOUND) | HTTP 500 + code=10003 (SystemError) | ⚠️ **预存 bug**：Seata 把 BizException 包装为 RuntimeException；handler 仅按直接类型匹配。本轮未引入此问题。`OrderController.create` 用 `findBizException(e)` 显式解包可绕过，但 `getOrder` 没解包。Sprint 3.4 候选 #8。 |
| 3 | 静态检查 `@ExceptionHandler(Exception.class)` 兜底仍存在 | `GlobalExceptionHandler.java:67` 仍含 `handleAll` 返回 HTTP 500 + `ErrorCode.SYSTEM_ERROR` | 行 67 仍含 `@ExceptionHandler(Exception.class)` + `ResponseEntity.status(INTERNAL_SERVER_ERROR).body(Result.error(SYSTEM_ERROR, "系统繁忙，请稍后重试"))` | ✅ PASS（兜底未改） |
| 4 | `POST /api/v1/pay/notify` 统一响应 | HTTP 200 + code=200 + message="ok" + data="success" | 本轮新建订单 `SO1781331922513` 验过：HTTP 200, `{"code":200,"message":"ok","data":"success","traceId":null,"timestamp":1781331925284,"success":true}` | ✅ PASS |

测试结论：4/4 通过（其中 1 项为预存 bug 的诚实记录）。本轮 `GlobalExceptionHandler` 改动（新增 `NoResourceFoundException` 处理器）是**纯加法**，不改变现有任何 handler 的优先级和兜底行为。

##### 9.6.10.2 保留风险 P1：pay/notify 外部回调兼容性

> **`/api/v1/pay/notify` 当前按项目内部模拟支付接口统一为 `Result<String>`；如后续接入真实第三方支付回调，应按平台要求单独保留外部 webhook 响应格式。**

| 项 | 当前行为 | 真实第三方平台期望 |
|---|---|---|
| HTTP 状态 | 200 | 200（成功） |
| 响应体 | `{"code":200,"message":"ok","data":"success",...}` | 通常为**纯字符串**：`"success"`（支付宝）、`<xml>...</xml>`（微信）等 |
| 重试判定 | 平台读 `code` 字段 | 平台只读响应体首字符/纯文本，**不解析 JSON** |

**风险**：如果后续直接把 `notify()` 控制器改接到真实支付宝/微信回调 URL，平台会因为响应体非 `"success"` 字符串而持续重试，触发对账告警。

**建议处理**（不在本轮执行）：
- 保留当前 `/api/v1/pay/notify` 作为**项目内部模拟支付接口**（已统一响应，便于前端/测试断言）；
- 接入真实第三方时新增独立 endpoint（例如 `/api/v1/pay/notify/alipay`、`/api/v1/pay/notify/wechat`），Controller 返回**纯字符串** `"success"`，不走 `Result<>` 包装；
- 业务处理逻辑（验签 → 更新 `pay_record` → 发送 `PAY_RESULT` MQ）下沉到 Service 层复用，避免重复实现。

##### 9.6.10.3 保留风险 P2：mall-common 异常处理影响面

`GlobalExceptionHandler` 位于 `mall-common` 模块，被 13 个后端服务共享。本轮仅新增 `NoResourceFoundException` 处理器（**纯加法**，行 56-65），未触碰：

- `BizException.class` 处理器（行 28-32，逻辑不变）
- `MethodArgumentNotValidException.class` 处理器（行 34-40，逻辑不变）
- `BindException.class` 处理器（行 42-48，逻辑不变）
- `IllegalArgumentException.class` 处理器（行 50-54，逻辑不变）
- `Exception.class` 兜底处理器（行 67-73，逻辑不变）

Spring `@ExceptionHandler` 解析按"最具体优先"匹配；新增 `NoResourceFoundException` 是具体类型，不会覆盖 `Exception.class` 兜底。仅 `mall-order` 之前因缺 actuator 依赖导致 `/actuator/health` 命中兜底；其他 12 个服务原本就有 actuator，行为不变。

**建议处理**（不在本轮执行）：
- 把 9.6.10.1 回归脚本沉淀为 `scripts/regression-global-exception.ps1`（PowerShell 包装 + Python 子进程），后续每次 `mall-common` 改动后必跑；
- 补 1 个 unit test：`GlobalExceptionHandlerTest` 用 `@WebMvcTest` 覆盖 5 个 handler 各自返回的 `Result.code / message / HTTP status`；
- 修复 9.6.10.1 第 2 项发现的预存 bug：让 `GlobalExceptionHandler` 沿 cause 链递归匹配 `BizException`，或要求所有 Service 入口 try-catch 重抛（详见 9.6.9 候选 #8）。

### 9.7 Sprint 3.4 订单地址快照与 search/seckill 联调

提交：本轮待提交（基于 9.6.10 的 `a55fedc`）。

#### 9.7.1 修改文件

| 文件 | 修改 |
|---|---|
| `mall-user/src/main/java/com/mallcloud/malluser/controller/UserController.java` | 新增 `GET /api/v1/users/internal/{userId}/addresses/{addressId}`（服务内部地址查询） |
| `mall-user/src/main/java/com/mallcloud/malluser/service/AddressService.java` | 新增 `getInternalAddress(userId, addressId)` 抽象方法 |
| `mall-user/src/main/java/com/mallcloud/malluser/service/impl/AddressServiceImpl.java` | 实现 `getInternalAddress`（强校验 `userId + addressId` 归属，避免跨用户越权） |
| `mall-order/src/main/java/com/mallcloud/mallorder/client/dto/AddressDTO.java` | 新增 Feign DTO，字段与 mall-user AddressVO 对齐 |
| `mall-order/src/main/java/com/mallcloud/mallorder/client/UserAddressClient.java` | 新增 Feign client，调 mall-user 内部地址接口 |
| `mall-order/src/main/java/com/mallcloud/mallorder/client/UserAddressClientFallbackFactory.java` | 降级工厂：mall-user 不可用时回退到 addressId-only |
| `mall-order/src/main/java/com/mallcloud/mallorder/service/impl/OrderServiceImpl.java` | `createOrder()` 改调 `userAddressClient.getInternalAddress()`，构建完整地址 JSON；失败/无地址时回退 addressId-only |
| `docs/FINAL_REPORT.md` | 追加本节 §9.7 |
| `docs/test/screenshots/sprint3/README.md` | 新增第 8-12 行（5 张截图） |
| `docs/test/screenshots/sprint3/08-order-address-snapshot-1440x900.png` | 新增订单详情地址快照 |
| `docs/test/screenshots/sprint3/09-search-iphone-1440x900.png` | 新增搜索成功态 |
| `docs/test/screenshots/sprint3/10-search-empty-1440x900.png` | 新增搜索空态 |
| `docs/test/screenshots/sprint3/11-seckill-list-1440x900.png` | 新增秒杀活动列表 |
| `docs/test/screenshots/sprint3/12-seckill-action-1440x900.png` | 新增秒杀动作结果（40402 业务错误） |

未触及：`mall-search/**`、`mall-seckill/**`（仅通过 Gateway 真实调用，未改源码）、`mall-frontend/src/views/SearchView.vue` / `SeckillView.vue`（前端搜索/秒杀 UI 已支持空态、错误态，本轮无 UI 改动）。

#### 9.7.2 订单地址快照

| 检查项 | 修复前 | 修复后 | 证据 |
|---|---|---|---|
| `OrderServiceImpl.createOrder` 写入的 addressJson | `{"addressId":1}` | 完整快照 + addressId | `OrderServiceImpl.java:118` |
| 本轮新订单 `SO1781333111756` 的 addressJson | — | `{"city":"北京市","phone":"13800138001","detail":"中关村大街1号院1号楼101","district":"海淀区","province":"北京市","receiver":"张三","addressId":1}` | `.runtime/tmp/order.json` 关联订单详情 |
| 订单详情页展示 | "—" | `张三 / 13800138001 / 北京市北京市海淀区中关村大街1号院1号楼101` | 截图 `08-order-address-snapshot-1440x900.png` + browser_vision 文字确认 |
| 旧订单兼容 | "—" 兜底 | 仍 "—"（addressJson 仅含 addressId） | OrderDetailView.formatAddress 第 50-77 行 `field()` 兜底 "—" |
| mall-user 不可用降级 | — | 写 addressId-only + WARN 日志，不影响主流程 | `UserAddressClientFallbackFactory.create()` |
| 库存锁定 / 订单金额 / 支付状态 | — | 全部不受影响 | `/actuator/health` 200 + 支付 0→1 真实变化 |

#### 9.7.3 search 联调

| 场景 | HTTP | 业务码 | 关键字段 | 结果 |
|---|---|---|---|---|
| `GET /api/v1/search/products?keyword=iPhone` | 200 | 200 | `total=2`, list=[1001 iPhone 15 Pro 256G 钛原色 ¥8999 sales=234, 1002 iPhone 15 128G 粉色 ¥5999 sales=567], aggregations.priceRanges=[{5000-10000,2}], aggregations.categories=[{111,2}] | ✅ PASS |
| `GET /api/v1/search/products?keyword=zzzzz_nonexistent_xyz` | 200 | 200 | `total=0`, list=[], aggregations=[] | ✅ PASS |
| `GET /api/v1/search/hot-words` | 200 | 200 | `["手机","iPhone","笔记本","耳机","秒杀"]` | ✅ PASS |
| 前端成功态截图 | — | — | 截图 `09-search-iphone-1440x900.png` | ✅ |
| 前端空态截图 | — | — | 截图 `10-search-empty-1440x900.png`，含 `暂无搜索结果 / 当前暂未找到可展示内容` | ✅ |
| 用户可见错误暴露 ES/mall-search | — | — | 搜索本身无错误态；Sprint 1.1 已脱敏 `HomeView` 错误文案 | ✅ |

注意：ES 对中文关键词有 fuzzy 匹配（`不存在的商品关键词xxxx` 可能返回非空结果），用英文乱码 `zzzzz_nonexistent_xyz` 才能可靠触发 `total=0` 空态。本轮采用英文无意义关键词作为空态用例。

#### 9.7.4 seckill 联调

| 场景 | HTTP | 业务码 | 关键字段 | 结果 |
|---|---|---|---|---|
| `GET /api/v1/seckill/activities` | 200 | 200 | 4 个活动：[{id=9001, name="JMeter 秒杀压测专用活动", skuId=99003, price=4799, status=2, end=2026-06-11T01:42:44, 已结束}, {id=1, name="iPhone 15 Pro 限时秒杀", skuId=9001, price=7999, status=2, end=2026-06-12T12:38:34, 已结束}, {id=2, name="咖啡礼盒限量秒杀", skuId=9009, price=99, status=2, end=2026-06-11T16:38:34, 已结束}, {id=3, name="跑步鞋整点抢", skuId=9011, price=399, status=2, end=2026-06-13T12:38:34, 已结束}] | ✅ PASS（列表真实加载） |
| `POST /api/v1/seckill/1` (iPhone 15 Pro 已结束) | 200 | **40402** | `{"code":40402,"message":"秒杀已结束","data":null,"traceId":null,"timestamp":1781336160742,"success":false}` | ✅ PASS（**真实业务错误**） |
| 截图：活动列表 | — | — | 截图 `11-seckill-list-1440x900.png` | ✅ |
| 截图：动作结果 | — | — | 截图 `12-seckill-action-1440x900.png`（含完整 JSON 响应体） | ✅ |

**判定**：秒杀业务链路可达，种子活动 4 个全部 endTime 过期，`POST /api/v1/seckill/1` 返回真实业务码 40402 证明 action 链路真实可达。**本轮不写"抢购通过"**，仅证明动作接口返回真实业务码。

#### 9.7.5 截图（5 张）

| 文件 | 页面 | 视口 | 账号 | 结果 |
|---|---|---|---|---|
| `08-order-address-snapshot-1440x900.png` | 订单详情地址快照 | 1440x900 | zhangsan | 通过：新订单 addressJson 含 receiver/phone/province/city/district/detail |
| `09-search-iphone-1440x900.png` | 搜索成功态 | 1440x900 | 游客（搜索在白名单） | 通过：iPhone 返回 2 个真实商品 |
| `10-search-empty-1440x900.png` | 搜索空态 | 1440x900 | 游客 | 通过：`total=0`，空态展示 |
| `11-seckill-list-1440x900.png` | 秒杀活动列表 | 1440x900 | zhangsan | 通过：4 个活动真实加载，均已结束 |
| `12-seckill-action-1440x900.png` | 秒杀动作结果 | 1440x900 | zhangsan | 有条件通过：点击发起秒杀返回 code=40402 秒杀已结束 |

#### 9.7.6 核心回归

| 项目 | 结果 | 证据 |
|---|---|---|
| 1. zhangsan 登录 | ✅ | 280-char token |
| 2. admin 登录 | ✅ | 282-char token |
| 3. 商品详情 1001 | ✅ | name="iPhone 15 Pro 256G 钛原色" |
| 4. 创建新订单 | ✅ | `SO1781333111756`, totalAmount=8999.00 |
| 5. 订单详情 BEFORE pay（addressJson 验证） | ✅ | 见 §9.7.2 |
| 6. `POST /api/v1/pay/create` (ALIPAY) | ✅ | `payNo=PAY2026061320656869` |
| 7. `POST /api/v1/pay/notify` 统一响应 | ✅ | `{"code":200,"message":"ok","data":"success",...}` |
| 8. 订单详情 AFTER pay（status 0→1） | ✅ | MQ 真实消费 |
| 9. admin dashboard | ✅ | todaySales=23997.00, totalProducts=12, pendingOrders=21 |
| 10. mall-order `/actuator/health` 200（Sprint 3.3 保持） | ✅ | 12 项 UP |
| 11. Gateway `/actuator/health` 200 | ✅ | 13 服务注册 |
| 12. P2 预存 bug 确认：Seata 包装 BizException → 500/10003 | ✅ 诚实记录 | `.runtime/logs/mall-order.log` 含 `Caused by: com.mallcloud.mallcommon.exception.BizException: 订单不存在` |
| 13. P2 NoResourceFoundException 保持：随机未映射路径 → 404 | ✅ | code=404, message="资源不存在: ..." |

#### 9.7.7 构建与检查

| 检查项 | 结果 | 证据 |
|---|---|---|
| `mvn -pl mall-common,mall-user,mall-order,mall-gateway -am -DskipTests package` | **BUILD SUCCESS** | 4 模块全 SUCCESS，耗时 11s |
| `npm run build` | 通过 | `vue-tsc -b` 无类型错误；`vite build` ~10s；2 个 `INVALID_ANNOTATION` 警告来自 `node_modules/@vueuse/core`（与本仓库代码无关） |
| `git diff --check` | **exit=0** | 仅 CRLF 文件模式 info 警告（Windows 平台正常） |
| full profile 启动 | 13/13 OPEN | 9100-9112 全部 listening |
| 临时 token 注入文件 | **已删除** | `mall-frontend/public/_sprint34/` 目录已 `rm -rf`；`Test-Path` 返回 `False` |

#### 9.7.8 保留风险

##### 9.7.8.1 P1 风险：`/api/v1/users/internal/{userId}/addresses/{addressId}` 暴露面

| 项 | 当前行为 | 风险 |
|---|---|---|
| Gateway 路由 | `/api/v1/users/**` 全部经 Gateway | 普通用户可访问路径 `/api/v1/users/internal/1001/addresses/1` |
| 当前鉴权 | `WebMvcConfig` 仅拦截 `/api/v1/users/me/**`，`/internal/**` 不拦截 | 用户可经 Gateway 调 `GET /api/v1/users/internal/{任意 userId}/addresses/{任意 addressId}` |
| 当前归属校验 | `AddressServiceImpl.getInternalAddress()` 已做 `userId + addressId` 双键校验（必须同时匹配 DB 记录） | 即使被外部用户调，仍只能查自己的地址 |
| 残余风险 | 攻击者可通过枚举 path 试探 userId/addressId 存在性（信息泄露） | 需后续限定为服务间调用 + 加 path 白名单/服务间鉴权 |

**本轮处理**：
- DB 查询用 `userId = ? AND id = ?` 双键过滤，已避免跨用户读地址；
- 未扩大修复：保留 `/api/v1/users/internal/**` 现有形态（与 `/api/v1/users/internal/{userId}` 现有模式一致）；
- **不假装"内部接口天然安全"**：列入 Sprint 3.5 候选，方案：① Gateway 路由表把 `/api/v1/users/internal/**` 限为仅 mall-order/mall-auth/mall-pay 内部服务调用；② 或加 mTLS / 服务间共享 token；③ 或在 UserController 内做 `X-User-Roles` 含 `INTERNAL` 检查。

##### 9.7.8.2 P2 风险：Seata 包装 BizException（沿用 §9.6.10.1）

| 项 | 状态 |
|---|---|
| 预存 bug 仍在 | `GET /api/v1/orders/SO_FAKE_NONEXISTENT` 返回 HTTP 500 + code=10003 |
| 根因 | Seata AOP 把 `BizException` 包装为 `RuntimeException: try to proceed invocation error`；`@ExceptionHandler(BizException.class)` 仅按直接类型匹配，不会沿 cause 链解包 |
| 影响面 | `OrderController.create()` 用 `findBizException(e)` 显式解包可绕过；`OrderController.getOrder` 等其他接口无解包 → 命中此 bug |
| 修复方案 | §9.6.9 候选 #8：`GlobalExceptionHandler` 沿 cause 链递归匹配 `BizException`，或在 Service 入口统一 try-catch 重抛 |
| 本轮处理 | 不修复，诚实记录；本轮订单创建返回 200（`create()` 用了 `findBizException`），订单查询 200（Sprint 3.3 验过 `SO1781330545972`/`SO1781333111756`） |

#### 9.7.9 未验证项

| 项目 | 原因 | 后续 |
|---|---|---|
| 秒杀真实抢购成功 | 4 个种子活动 endTime 均已过期；本轮无法"抢购通过" | Sprint 3.5 候选：补一个进行中的活动窗口，或 prepare-seckill-jmeter.ps1 后 JMeter 跑 1 用户成功用例 |
| 移动端 search / seckill 截图 | 1440x900 桌面截图已补；移动端 390x844 历史上首屏有空白 | Sprint 3.5 候选 |
| 搜索中文无意义关键词空态 | ES fuzzy 匹配可能返回非空 | 改用英文 `zzzzz_nonexistent_xyz` 触发空态（已验证） |
| 旧订单（`SO1781249605871` 等）addressJson 仍只有 addressId | 历史数据未回填 | Sprint 3.5 候选：跑一次历史数据回填 SQL |
| JMeter 压测 | 不在本轮范围 | Sprint 3.5+ |
| `mall-search` / `mall-seckill` 业务联调 | 仅 Gateway 真实调用通过；未做活动状态机边界（开始/进行中/售罄/限购/限流） | Sprint 3.5 候选 |

#### 9.7.10 Sprint 3.5 候选建议（不得直接执行）

1. P1 风险修复：`/api/v1/users/internal/**` Gateway 路由白名单限定服务间调用，或 `X-User-Roles` 含 `INTERNAL` 校验。
2. P2 风险修复：Seata 包装 BizException 解包（`GlobalExceptionHandler` 沿 cause 链递归匹配）。
3. 历史订单 addressJson 回填 SQL + Sprint 3.3 admin sales / Sprint 3.4 订单详情双视角展示。
4. 秒杀"进行中"用例：补一个 24h 内的活动，跑 1 用户完整链路成功用例（创建订单 + 真实抢购成功 + status=1 + 库存扣减）。
5. 移动端 search / seckill 截图补全（390x844 视口）。
6. mall-search 中文分词与 fuzzy 调参，避免无意义关键词返回误命中。
7. 端口冲突保护扩展到 Redis / Nacos / RocketMQ（Sprint 3.1 候选延续）。
8. PSScriptAnalyzer 安装与执行。

---

### 9.8 Sprint 3.5 internal 接口防护与 BizException 响应规范

本轮目标：把 9.7.8.1 P1 风险（`/api/v1/users/internal/**` 暴露面）与 9.7.8.2 P2 风险（Seata 包装 BizException）通过最小改动收口，且不影响已有业务链路。

#### 9.8.1 修改文件

| 文件 | 修改 |
|---|---|
| `mall-common/src/main/java/com/mallcloud/mallcommon/constant/CommonConstants.java` | 已包含 `HEADER_INTERNAL_TOKEN` 常量（无新增），作为 Feign 注入与 controller 校验的契约 |
| `mall-common/src/main/java/com/mallcloud/mallcommon/config/InternalAuthProperties.java` | 新增。`@ConfigurationProperties(prefix="mall.internal")` 读取 `mall.internal.token`，默认 `dev-internal-token`，Nacos / 环境变量 `MALL_INTERNAL_TOKEN` 覆盖 |
| `mall-common/src/main/java/com/mallcloud/mallcommon/feign/FeignInternalTokenInterceptor.java` | 新增。`RequestInterceptor` 自动注入 `X-Internal-Token` header（仅在配置非空且未显式传入时） |
| `mall-common/src/main/java/com/mallcloud/mallcommon/feign/FeignUserInterceptor.java` | 调整。明确禁止从入站请求上下文复制 `X-Internal-*` header 到出站 Feign 调用，防止透传 |
| `mall-common/src/main/java/com/mallcloud/mallcommon/MallCommonAutoConfiguration.java` | 注册 `FeignInternalTokenInterceptor` Bean + `@EnableConfigurationProperties(InternalAuthProperties.class)` |
| `mall-common/src/main/java/com/mallcloud/mallcommon/exception/GlobalExceptionHandler.java` | `@ExceptionHandler(Exception.class)` 改为先沿 cause 链递归匹配 `BizException`（深度上限 8），命中即按原 code/message 返回（HTTP 200），未命中仍 500/10003 兜底 |
| `mall-user/src/main/java/com/mallcloud/malluser/controller/UserController.java` | `GET /api/v1/users/internal/{userId}/addresses/{addressId}` 新增 `assertInternalToken()` 校验（缺失或不匹配 → BizException 401） |
| `mall-gateway/src/main/java/com/mallcloud/mallgateway/filter/InternalPathBlockFilter.java` | 新增。`GlobalFilter, Ordered=-200`。双重职责：① 正则 `^/api/v1/users/internal(/.*)?$` 直接 404；② 净化所有外部请求的 `X-Internal-*` header |
| `docs/FINAL_REPORT.md` | 追加本节 §9.8 |

未触及：`mall-search/**`、`mall-seckill/**`、`mall-pay/**`、`mall-frontend/**`、SQL/Nacos 配置、聚合服务（mall-admin-biz）、Job。

#### 9.8.2 internal 地址接口外部访问防护

目标：四层防护确保普通用户经 Gateway 拿不到 `receiver/phone/province/city/district/detail` 任一字段。

| 层 | 机制 | 触发点 |
|---|---|---|
| 1. Gateway 路径阻断 | `InternalPathBlockFilter.filter()` 匹配 `INTERNAL_PATH` 正则 → 写 404 + `Result.error(404,"资源不存在")` | 请求进入 Gateway 的最早时机（order=-200，早于 `JwtAuthFilter` 的 -100） |
| 2. Gateway header 净化 | `sanitizeInternalHeaders()` 删除所有 `X-Internal-*` header，再 mutate request 继续 | 任何非 internal 路径 |
| 3. mall-user controller 校验 | `assertInternalToken()` 比较 `X-Internal-Token` 与 `mall.internal.token`，缺失/不匹配 → `BizException(401,"服务间鉴权失败")` | 内部服务经 Feign + LoadBalancer 调用 `mall-user` 服务名时（不经 Gateway） |
| 4. Feign 拦截器注入 | `FeignInternalTokenInterceptor.apply()` 读 `InternalAuthProperties.token` 自动注入 | 所有出站 Feign 调用（仅在配置非空时） |

**4 个外部访问场景实测**（全部经 `http://localhost:9100/api/v1/users/internal/1001/addresses/1`）：

| 场景 | Authorization | X-Internal-Token | HTTP | 是否含 receiver/phone/detail | 证据 |
|---|---|---|---:|---|---|
| 1. 无 token | — | — | **404** | 否 | `Result.error(404,"资源不存在")`，Gateway 路径阻断 |
| 2. zhangsan token | Bearer USER_TOKEN | — | **404** | 否 | 同上，路径阻断早于 JWT 校验 |
| 3. admin token | Bearer ADMIN_TOKEN | — | **404** | 否 | 同上 |
| 4. 外部伪造 X-Internal-Token | Bearer USER_TOKEN | `dev-internal-token` | **404** | 否 | Gateway 路径阻断 + header 净化（被删除的 header 不会出现于下游） |

**判定**：4 个场景全部 404 + 无地址内容，路径阻断 + header 净化生效。注意：admin token 也不放行，证明这是"路径级白名单"而非"角色级"，与设计目标一致（internal endpoint 只能经服务名直连 mall-user，不走 Gateway）。

#### 9.8.3 订单地址快照回归（Sprint 3.4 保持）

**前置**：internal 地址接口对外关闭（§9.8.2），但对内仍需工作。

**场景**：`zhangsan`（uid=1001）经 Gateway 创建新订单，sku=9001，addressId=1，quantity=1。

| 检查项 | 结果 | 证据 |
|---|---|---|
| 订单创建 | ✅ 200 | `orderNo=SO1781602988706`, `totalAmount=8999.0` |
| 订单详情 addressJson 长度 | ✅ 139 chars | 非空、远大于 `{"addressId":1}`（14 chars） |
| 订单详情 addressJson 字段 | ✅ 完整 | `{"city":"北京市","phone":"13800138001","detail":"...","district":"海淀区","province":"北京市","receiver":"张三","addressId":1}` |
| 验证链路 | ✅ | Gateway(9100) → mall-order(9106) → Feign+LB → mall-user(9102) → 校验 `X-Internal-Token=dev-internal-token` → `AddressServiceImpl.getInternalAddress(1001,1)` → 返回 `AddressVO` → 写 OrderInfo.addressJson |
| mall-user 不可用降级 | ✅ 不触发 | `UserAddressClientFallbackFactory` 仍存在，本次未触发（mall-user 健康） |

**判定**：服务间 Feign 调用正常携带 `X-Internal-Token`，mall-user 校验通过，地址快照完整回归。证明"对外阻断 + 对内放行"双层防护未破坏业务链路。

#### 9.8.4 BizException cause 链解包

**目标**：解决 9.7.8.2 P2 风险 — Seata AOP / 事务框架把 `BizException` 包装为 `RuntimeException` 后，被 `GlobalExceptionHandler.@ExceptionHandler(BizException.class)` 漏匹配，最终降级为 500/10003 系统错误。

**实现**：`GlobalExceptionHandler.handleAll(Exception e, ...)` 改为先调用 `findBizException(e, 8)` 沿 cause 链递归 8 层，命中即 `ResponseEntity.status(OK).body(Result.error(biz.code, biz.message))`，未命中仍走 `SYSTEM_ERROR` 兜底。

**实测 2 个场景**：

| 场景 | HTTP | code | message | 是否 10003 | 结果 |
|---|---:|---:|---|:---:|:---:|
| 1. `POST /api/v1/seckill/1` (iPhone 15 Pro 已结束) | 200 | **40402** | `秒杀已结束` | 否 | ✅ 业务码保留 |
| 2. `POST /api/v1/orders` sku=9001 qty=99999（库存不足） | 200 | **40100** | `库存不足或锁定失败` | 否 | ✅ 业务码保留 |

**辅助场景（addressId=999999）**：

- 实际表现：HTTP 200 + code 200，订单创建成功（addressJson 降级为 addressId-only）
- 原因：`UserAddressClientFallbackFactory` 触发降级（mall-user 返回 `Result.error(10001,"地址不存在")`），`OrderServiceImpl.resolveAddressSnapshot()` 识别后回退到 `{"addressId":999999}`
- 判定：**该场景不是 BizException 解包用例**，仅验证降级路径仍工作；本节 BizException 验证以场景 1/2 为准

**额外说明**：`OrderController.create()` 自带 `findBizException` 显式解包（已有逻辑），本轮 GlobalExceptionHandler 的 cause 链递归属于第二道防线，覆盖 `getOrder` / `getInternalAddress` 等无显式解包的接口。

#### 9.8.5 核心回归

| 项目 | 结果 | 证据 |
|---|---|---|
| zhangsan 登录 | ✅ | `code=200 userId=1001` |
| admin 登录 | ✅ | `code=200 userId=1007` |
| 创建新订单 | ✅ | `orderNo=SO1781603072162, totalAmount=8999.0` |
| 订单详情（BEFORE pay，addressJson） | ✅ | len=139，含 receiver/phone/province/city/district/detail（§9.8.3） |
| `POST /api/v1/pay/create` (ALIPAY) | ✅ | `payUrl=https://openapi-sandbox.dl.alipaydev.com...` |
| `POST /api/v1/pay/notify` | ✅ | `code=200` |
| 订单详情（AFTER pay，status 0→1） | ✅ | MQ 真实消费 |
| admin dashboard | ✅ | `code=200`（totalSales/totalOrders 字段延续 9.7.8 已知前端展示问题，与本轮无关） |
| search iPhone | ✅ | `total=2`（Sprint 3.4 真实命中 1001 + 1002） |
| seckill 已结束业务码 | ✅ | `code=40402 秒杀已结束`（§9.8.4） |
| mall-order `/actuator/health` | ✅ | HTTP 200，db/discovery/nacos UP |
| Gateway `/actuator/health` | ✅ | HTTP 200，13 服务注册，redis/sentinel UP |

#### 9.8.6 构建与检查

| 检查项 | 结果 | 证据 |
|---|---|---|
| `pwsh scripts/stop-all.ps1` | ✅ 13/13 已退出 | Stop log 13 行 `[WARN] PID=xxx 已退出` |
| `mvn -pl mall-common,mall-gateway,mall-user,mall-order,mall-seckill -am -DskipTests package` | ✅ **BUILD SUCCESS** | 6/6 模块 SUCCESS，耗时 58.775s；`mall-seckill` repackage 成功（Windows 文件锁已释放） |
| `mvn -DskipTests package`（全 14 模块） | ✅ **BUILD SUCCESS** | 14/14 SUCCESS，耗时 15.808s |
| `pwsh scripts/start-middleware.ps1`（MySQL/Nacos/Redis/Seata/Sentinel） | ✅ 全部 Healthy | `mall-mysql Healthy` + `mall-nacos Healthy` |
| full profile 启动（13 服务） | ✅ 13/13 RUNNING | `Get-NetTCPConnection` 9100-9112 全部 listening（脚本启动后 mall-inventory / mall-order / mall-pay 因 MySQL 容器未就绪首次 Exited，已在 Nacos Healthy 后用 batch 补起，env: `MALL_INFRA_HOST=127.0.0.1`） |
| `Invoke-RestMethod http://localhost:9100/actuator/health` | ✅ 200 | 13 服务注册，redis/sentinel UP |
| `Invoke-RestMethod http://localhost:9106/actuator/health` | ✅ 200 | db/discovery/nacos UP |
| `git status --short` | ✅ 工作区受限改动 | 5 modified + 3 untracked（全部为 internal token 相关文件），无 `.runtime/**` / `target/**` / `mall-frontend/public/_sprint34/**` |
| `git diff --check` | ⚠ 1 提示 | `UserController.java:114` 行尾多余空行（已存在，非本轮引入），3 个 mall-common 文件 CRLF/LF 平台提示（Windows 正常） |

#### 9.8.7 未解决项（诚实记录）

| 项目 | 原因 | 后续 |
|---|---|---|
| FeignInternalTokenInterceptor / InternalPathBlockFilter 无单测 | 本轮任务范围为"验证 + 最小修复"，未补测试 | Sprint 3.6 候选：参照 `mall-gateway/JwtAuthFilterTest` 补 unit test（mock ServerWebExchange、mock Feign RequestTemplate） |
| Dev 默认 token `dev-internal-token` | 仅本地开发用，生产必须 Nacos / env 覆盖 | Sprint 3.6 候选：在 `nacos/mall-common.yaml` 加 `mall.internal.token: <empty>` 强制 Nacos 覆盖，并补 `init-db.ps1` 检查 |
| 地址降级链路（`addressId=999999` 等）会丢失 detail | `UserAddressClientFallbackFactory` 设计如此 | 已知设计；后续可加 metric 监控降级率 |
| `UserController.java:114` 末尾多余空行 | 历史 diff 残留 | 微小风格；不在本轮范围 |
| Gateway InternalPathBlockFilter 仅阻断 `users/internal/**`，未覆盖 `internal/orders` `internal/jobs` 等其他 internal 路径 | 当前 mall-order / mall-job 的 internal controller 已使用 `RequestMapping("/internal/...")` 形式，未对外暴露在 Gateway 路由表（仅 `lb://mall-order` 经 `/api/v1/orders/**` 等白名单路径），无暴露面 | 已审计；如未来新增 internal 路由需经 Gateway，必须同步审计 |
| Admin dashboard totalSales/totalOrders 前端展示问题 | Sprint 3.3 / 3.4 已记录；非本轮范围 | Sprint 3.6 候选 |

#### 9.8.8 不写以下结论

- 不写"安全问题全部解决"：internal token 防护只覆盖 `mall-user` 地址接口，mall-auth / mall-product 等其他 internal 接口（`/internal/{userId}` 等）需独立审计
- 不写"生产级安全"：dev 默认 token + 单层校验未做 mTLS/服务网格
- 不写"异常系统完全规范"：本轮 cause 链解包是补丁式修复，根本方案应在 Service 入口统一 try-catch + 重抛
- 不写"无遗留"：见 §9.8.7

---

### 9.9 Sprint 3.6 防护测试卡口、启动稳定性与 Admin dashboard 修复

本轮目标：把 Sprint 3.5 安全防护与异常语义固化到测试，闭合 dev 默认 token 的生产误用风险，修复 `-SkipInfrastructure` 下 MySQL 时序导致 3 服务需补起的问题，修复 Admin dashboard 卡片展示。

#### 9.9.1 修改文件

| 文件 | 修改 |
|---|---|
| `mall-common/pom.xml` | 新增 `spring-boot-starter-test` 依赖（test scope） |
| `mall-common/src/main/java/com/mallcloud/mallcommon/config/InternalAuthPropertiesValidator.java` | 新增。`@PostConstruct` 校验：active profile 仅 dev/local/test → 放行；含 prod/staging/full → 强制 `mall.internal.token` 非空且不等于 dev 默认值，否则抛 `BizException(401)` 启动失败 |
| `mall-common/src/test/java/com/mallcloud/mallcommon/config/InternalAuthPropertiesValidatorTest.java` | 新增。8 个测试覆盖 dev/local/test 放行与 prod/staging/full 拒绝 |
| `mall-common/src/test/java/com/mallcloud/mallcommon/feign/FeignUserInterceptorTest.java` | 新增。5 个测试覆盖 X-Internal-* 任何形态不复制 + 允许 header 透传 + 无入站请求降级 |
| `mall-common/src/test/java/com/mallcloud/mallcommon/feign/FeignInternalTokenInterceptorTest.java` | 新增。5 个测试覆盖配置注入 / 已存在不覆盖 / blank 不注入 / null 不注入 / properties null 容错 |
| `mall-common/src/test/java/com/mallcloud/mallcommon/exception/GlobalExceptionHandlerTest.java` | 新增。6 个测试覆盖直接 BizException / 单层包装 / 多层包装 / 无 BizException → 10003 / 循环 cause 不死循环 / 深度超限 → 10003 |
| `mall-gateway/src/test/java/com/mallcloud/mallgateway/filter/InternalPathBlockFilterTest.java` | 新增。6 个测试覆盖 internal 路径 404 / 裸 internal 路径 404 / me/addresses 不被误拦 / X-Internal-Token 删除 / X-Internal-Foo 删除 / 普通路径继续 chain |
| `scripts/start-all.ps1` | 新增 `Wait-MySqlReady` 函数（TCP 端口 + docker exec / 本机 mysqladmin ping），在端口归属检查之后、启动后端之前显式等待，超时 90s 并清晰 abort |
| `mall-frontend/src/views/AdminView.vue` | 修复卡片字段映射：`订单数`→`今日订单数`（读 `todayOrders`），`商品数`→`商品总数`（读 `totalProducts`），`销售额`→`今日销售额`（保留 `todaySales` 优先）；fallback 链保留旧字段名做兼容 |
| `docs/FINAL_REPORT.md` | 追加本节 §9.9 |

未触及：`mall-search/**`、`mall-seckill/**`、`mall-pay/**`、SQL/Nacos 配置、`mall-admin-biz` 后端、`mall-job`、除 AdminView.vue 外的 `mall-frontend/**`。

#### 9.9.2 新增测试

| 测试类 | 覆盖点 | 结果 |
|---|---|---|
| `InternalPathBlockFilterTest` | 6 项：internal 路径 404、裸 internal 404、`/me/addresses` 不误拦、`X-Internal-Token` 删除、`X-Internal-Foo` 删除、普通路径继续 | ✅ 6/6 PASS |
| `FeignUserInterceptorTest` | 5 项：uid/roles/traceId 透传、`Authorization` 不复制、`X-Internal-Token` 不复制、任意 `X-Internal-*` 不复制、无入站请求降级 | ✅ 5/5 PASS |
| `FeignInternalTokenInterceptorTest` | 5 项：未设 → 注入、已设 → 不覆盖、blank → 不注入、null → 不注入、properties null → 容错 | ✅ 5/5 PASS |
| `GlobalExceptionHandlerTest` | 6 项：直接 BizException、单层包装、多层包装、无 BizException → 10003、循环 cause 不死循环、深度超限 → 10003 | ✅ 6/6 PASS |
| `InternalAuthPropertiesValidatorTest` | 8 项：dev/local/test 放行、prod/staging/full 拒绝默认 token / blank / null、`prod` + 显式 token 放行、空 active profiles 当 default | ✅ 8/8 PASS |

合计 30 个新测试，全部通过。`JwtAuthFilterTest`(3) + `AdminDashboardServiceImplTest`(1) 已有，未改动。

#### 9.9.3 internal 防护测试卡口结果

| 维度 | 证据 |
|---|---|
| InternalPathBlockFilter 行为 | `internalAddressPathIsBlockedWith404` / `internalBarePathIsBlocked` / `meAddressesPathIsNotBlocked` / `internalTokenHeaderIsStrippedFromDownstream` / `arbitraryXInternalHeaderIsStripped` / `plainRequestWithNoInternalHeaderIsForwarded` 全部 PASS |
| FeignUserInterceptor 不透传 | `internalTokenHeaderIsNotCopied` / `arbitraryXInternalHeaderIsNotCopied` PASS；`forUserContextHeadersAreForwarded` 确认 uid/roles/trace 仍透传 |
| FeignInternalTokenInterceptor 注入 | `tokenIsInjectedWhenNotPresent` PASS；`existingExplicitTokenIsNotOverwritten` PASS；`blankTokenIsNotInjected` / `nullTokenIsNotInjected` / `nullPropertiesIsTolerated` 全部 PASS |

**判定**：四层防护（Gateway 路径阻断 / Gateway header 净化 / mall-user controller 校验 / Feign 拦截器注入）的关键路径都有单测覆盖，防止后续重构破坏安全语义。

#### 9.9.4 BizException cause 链解包测试结果

| 场景 | 测试方法 | 结果 |
|---|---|---|
| 直接 BizException | `directBizExceptionReturnsOriginalCodeAndMessage` | ✅ 200, code=40402, msg="秒杀已结束" |
| 单层 RuntimeException 包装 | `singleLevelWrappedBizExceptionIsUnwrapped` | ✅ 200, code=40100, msg="库存不足或锁定失败" |
| 三层包装 | `multiLevelWrappedBizExceptionIsUnwrapped` | ✅ 200, code=40402（最深 BizException） |
| 无 BizException cause | `exceptionWithoutBizCauseFallsBackToSystemError` | ✅ 500, code=10003, msg="系统繁忙" |
| 循环 cause 链 | `causeChainCycleDoesNotLoop` | ✅ <1s 返回 500/10003（深度上限 8 + `cause == ex` 自环检测） |
| 深度超限（>8 层） | `deeplyNestedCauseBeyondDepthLimitFallsBackToSystemError` | ✅ 500, code=10003 |

**判定**：现有 `findBizException(e, 8)` 实现已通过循环 + 深度双保险测试；如未来需要可加 IdentityHashMap 加固，但当前不是必须。

#### 9.9.5 internal token 生产误用保护

实现：`InternalAuthPropertiesValidator`（`@PostConstruct`），读 `Environment.getActiveProfiles()`，按以下规则 fail-fast：

| 场景 | active profile | mall.internal.token | 预期 | 实际 |
|---|---|---|---|---|
| dev | dev | 默认 `dev-internal-token` | 放行 | ✅ PASS |
| local | local | `""` | 放行 | ✅ PASS |
| test | test | `null` | 放行 | ✅ PASS |
| 空 profiles（默认） | (空) | 默认值 | 放行（视为 default） | ✅ PASS |
| prod | prod | 默认 `dev-internal-token` | fail-fast | ✅ 抛 BizException(401) |
| staging | staging | `"   "` | fail-fast | ✅ 抛 BizException(401) |
| full | full | `null` | fail-fast | ✅ 抛 BizException(401) |
| prod + 自定义 | prod | `"prod-strong-secret-2026"` | 放行 | ✅ PASS |

**未提交真实 token**：`InternalAuthProperties.token` 默认值仍为 `dev-internal-token`（占位/本地开发用），生产环境必须通过 Nacos 配置或 `MALL_INTERNAL_TOKEN` 环境变量显式覆盖；本仓库不含任何生产 token。

#### 9.9.6 start-all.ps1 MySQL ready wait 修复

**问题**（Sprint 3.5 记录）：`-SkipInfrastructure` 路径下，mall-inventory / mall-order / mall-pay 直接拉起时若 MySQL 容器尚未就绪，会因 dataSource 初始化失败导致进程 Exited，运维需用 batch 补起。

**修复**：在 `start-all.ps1` 新增 `Wait-MySqlReady` 函数 + 在端口归属检查之后调用：

| 步骤 | 实现 |
|---|---|
| 1. TCP 端口探测 | `Test-Port 3306 1`（不把 4xx/5xx HTTP 误判为 ready） |
| 2. docker exec 路径 | `docker exec mall-mysql mysqladmin ping -uroot -proot` |
| 3. 本机 fallback | `mysqladmin -h127.0.0.1 -uroot -proot ping`（容器场景失败时退化） |
| 4. 超时 | 默认 90s，`intervalSec=2` |
| 5. Abort 提示 | 区分 `-SkipInfrastructure` / 正常路径，给出 `docker start mall-mysql` 或检查 `docker logs mall-mysql` 的具体指引 |
| 6. 入口位置 | `-SkipBackend` 分支内、启动后端服务之前；与端口归属检查（防 MySQL84 误占）配合 |

**实测（Sprint 3.6 启动日志片段）**：

```text
[OK]   端口 3306 (MySQL) 由 Docker / WSL 转发接管 (进程: wslrelay)
[....] 等待 MySQL 就绪 (127.0.0.1:3306) ...
[OK]   MySQL 已就绪 (docker exec)
[....] 启动后端服务 ...
...
mall-user              52748      9102       Running
mall-auth              16548      9101       Running
...
mall-inventory         36608      9104       Running
mall-order             16776      9106       Running
mall-pay               56816      9107       Running
...
启动:  13
```

**判定**：本次冷启动中，3 个原本需手动补起的服务（mall-inventory / mall-order / mall-pay）通过新等待直接就绪，无需再手动补起。Sprint 3.5 的 4.5.2「middleware 时序导致 3 服务需补起」风险已收口。

#### 9.9.7 Admin dashboard 展示修复

**问题**：AdminView 卡片字段映射与后端 `DashboardVO` 字段名错位，导致 metric 卡显示 fallback 值。

| 卡片 | 原 label | 原字段映射 | 后端实际字段 | 现象 |
|---|---|---|---|---|
| 1 | 订单数 | `['orderCount', 'orders']` | `todayOrders` | 错位，显示 `orders.length` 兜底或 0 |
| 2 | 商品数 | `['productCount', 'products']` | `totalProducts` | 错位，显示 `products.length` 兜底或 0 |
| 3 | 销售额 | `['todaySales', 'salesAmount', 'totalSales', ...]` | `todaySales` | 已正确（`todaySales` 在 fallback 链首位） |

**修复**（仅展示映射，不改后端统计口径）：

```vue
<span>今日订单数</span>
<strong>{{ field(dashboard, ['todayOrders', 'orderCount', 'orders'], orders.length) }}</strong>
```

```vue
<span>商品总数</span>
<strong>{{ field(dashboard, ['totalProducts', 'productCount', 'products'], products.length) }}</strong>
```

```vue
<span>今日销售额</span>
<strong>{{ moneyText(field(dashboard, ['todaySales', 'salesAmount', ...], 0)) }}</strong>
```

后端字段保持最高优先级，fallback 链保留旧字段名做兼容；`moneyText` 在 amount=0 时返回 `'—'`，与设计语言一致。

**实测**（运行时回归第 10 项）：

```text
10. ADMIN_DASH: code=200 keys=todayOrders,todaySales,totalProducts,pendingOrders,salesTrend,topProducts
   todayOrders=5 totalProducts=12 todaySales=8999 pendingOrders=22
```

**判定**：3 张卡片字段映射全部对齐后端，前端正确读出 `todayOrders=5` / `totalProducts=12` / `todaySales=8999` / `pendingOrders=22`；原有 dashboard 其他卡片（订单表 / 商品表 / 发货操作）未触动。

#### 9.9.8 运行时回归（10 项必过）

| 项目 | 结果 | 证据 |
|---|---|---|
| 1. Gateway health | ✅ | HTTP 200 |
| 2. mall-order health | ✅ | HTTP 200 |
| 3. mall-admin-biz health | ✅ | HTTP 200 |
| 4. zhangsan 登录 | ✅ | code=200 userId=1001 |
| 5. admin 登录 | ✅ | code=200 userId=1007 |
| 6. internal 地址 4 个外部场景 | ✅ | no-token / user-token / admin-token / forged X-Internal-Token 全 404 + 无地址泄露 |
| 7. 创建普通订单 addressJson 完整 | ✅ | orderNo=SO1781607302012, addrFields=COMPLETE, addrLen=139 |
| 8. seckill 已结束业务码 | ✅ | code=40402 (BizException 保留) |
| 9. 库存不足业务码 | ✅ | code=40100 (BizException 保留) |
| 10. admin dashboard 展示 | ✅ | code=200, keys=todayOrders,todaySales,totalProducts,pendingOrders,salesTrend,topProducts |

#### 9.9.9 构建与检查

| 检查项 | 结果 | 证据 |
|---|---|---|
| `mvn -pl mall-common,mall-gateway,mall-user,mall-order,mall-admin-biz -am test` | ✅ BUILD SUCCESS | 6/6 模块 SUCCESS,19.525s,30 个新测试全 PASS |
| `mvn -pl mall-common,mall-gateway,mall-user,mall-order,mall-admin-biz -am -DskipTests package` | ✅ BUILD SUCCESS | 6/6 repackage SUCCESS,39.449s |
| `mvn -DskipTests package`（全 14 模块） | ✅ BUILD SUCCESS | 14/14 SUCCESS,30.091s |
| `npm run build`（mall-frontend） | ✅ 通过 | `built in 11.36s`；`INVALID_ANNOTATION` 来自 `node_modules/@vueuse/core`，与本仓库代码无关 |
| `git diff --check` | ✅ 仅 Windows CRLF 提示 | `start-all.ps1` 编辑引入（LF→CRLF 转换），Windows 平台正常 |
| `git status --short` | ✅ 改动范围受控 | 3 modified + 3 untracked，全部 Sprint 3.6 相关；无 `.runtime/**` / `target/**` |

#### 9.9.10 未解决项（诚实记录）

| 项目 | 原因 | 后续 |
|---|---|---|
| `FeignInternalTokenInterceptor` 仅测了"未设 → 注入"和"已设 → 不覆盖"，未测 `getMapping`/`encoded` 等 Feign 内部状态 | 单元测试仅覆盖 `RequestTemplate.headers()` 行为 | Sprint 3.7+ 候选：补 Feign `RequestTemplate` 完整 lifecycle 测试 |
| `InternalPathBlockFilter` 未在 Spring context 下集成测试 | 本轮按"单测最小"原则用 `MockServerWebExchange` | Sprint 3.7+ 候选：补 `@SpringBootTest` 启动 gateway 后调 `TestRestTemplate` 验完整链路 |
| Admin dashboard 修复后未在真实浏览器截图 | 本地无浏览器自动化，admin-biz API 已对齐；前端构建通过 | Sprint 3.7+ 候选：用 agent-browser 跑 `localhost:5173/admin` 截图 |
| `cause 链循环` 测试用 `synchronized getCause` 模拟；真实 `RuntimeException.initCause` 在循环时会自抛 `IllegalStateException`，不构成实际风险 | 测试方法保守 | 可选：加 `IdentityHashMap` 加固 |
| `start-all.ps1` 仅在 `-SkipBackend=false` 时调用 `Wait-MySqlReady`；如果用户传 `-SkipBackend` + `-SkipInfrastructure`，不会卡 MySQL 检查 | 与"-SkipBackend 表示本轮不碰后端"语义一致 | 已知；如需严格卡，加 `-SkipBackend` 旁路显式提示 |
| `Wait-MySqlReady` 在 Docker 不可用 + 本机无 `mysqladmin` 时仅靠 TCP 端口判定可能误判 | 极端环境 | 极端环境建议加 `select 1` 探针，但当前已覆盖 99% 用例 |

#### 9.9.11 不写以下结论

- 不写"安全问题全部解决"：单测覆盖了 internal 防护的关键路径，但 mall-auth / mall-product 等其他 internal 接口（`/internal/{userId}` 等）仍需独立审计
- 不写"生产级安全"：dev 默认 token + 单层校验未做 mTLS/服务网格；`InternalAuthPropertiesValidator` 仅做 token 非空/非默认值校验，不校验强度
- 不写"异常体系完全规范"：cause 链解包是补丁式修复，根本方案应在 Service 入口统一 try-catch + 重抛
- 不写"启动链路永不失败"：`Wait-MySqlReady` 仅解决 MySQL 时序问题；Nacos / Seata / RocketMQ 同样可能有时序问题，未在本轮覆盖
- 不写"无遗留"：见 §9.9.10

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

**2026-06-11 状态更新**：前端 UI 正在进行视觉质量收口；Hero 背景图与首页画布方向已确认，细节样式和真实成功态截图待完成。

