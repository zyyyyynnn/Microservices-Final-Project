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

#### 9.6.9 Sprint 3.4 候选建议（不得直接执行）

1. `mall-order` 写入完整 address 快照而非仅 `addressId`（订单详情收货信息"—"）。
2. `app.css` 4 处预存在硬编码颜色替换为 token 引用。
3. UI 调优：商品详情 mobile 留白；首页商品卡实时库存回填。
4. `start-all.ps1` 端口冲突保护扩到 6379 (Redis) / 8848 (Nacos) / 9876 / 10911 (RocketMQ)。
5. mall-search / mall-seckill 业务联调：活动列表 + 抢购 + 搜索结果真实断言。
6. PSScriptAnalyzer 安装与执行。
7. AdminView dashboard 增加 `salesTrend` 折线图与 `topProducts` Top 5 列表的视觉展示（当前仅在 `data` 字段中返回，前端未渲染）。

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

