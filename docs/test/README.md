# MallCloud 测试资产说明

> 职责：测试资产目录、Postman/Newman 使用方法、JMeter 使用方法、断言规范、结果文件目录、专项检查说明和前端验收要求。
> 当前测试结果统一记录到 `docs/FINAL_REPORT.md`。

---

## 1. 目录约定

```text
docs/test/
├── README.md
├── postman/
│   ├── mallcloud.postman_collection.json
│   ├── local.postman_environment.json
│   └── summary/
├── jmeter/
│   ├── search-load.jmx
│   ├── order-load.jmx
│   ├── seckill-stress.jmx
│   ├── results/
│   ├── report/
│   └── summary/
├── nacos/
│   └── summary/
├── sentinel/
│   └── summary/
└── screenshots/
```

不存在的文件不得写成已生成。测试结果、截图索引、通过率、失败项和性能指标统一写入 `docs/FINAL_REPORT.md`。
Newman HTML 报告包含请求头和响应体，可能包含动态 Token，不提交到仓库；提交证据时使用 `postman/summary/` 下的脱敏摘要。

---

## 2. 测试环境记录

每次正式测试必须记录：

```powershell
java -version
mvn -version
docker version
docker compose version
docker inspect mall-seata --format '{{.Config.Image}}'
git rev-parse HEAD
```

通过基线：

- Java 21；
- Maven 使用 Java 21；
- Seata 镜像为 `seataio/seata-server:2.0.0`；
- 报告中记录代码 Commit。

---

## 3. Postman/Newman

集合和环境：

```text
docs/test/postman/mallcloud.postman_collection.json
docs/test/postman/local.postman_environment.json
```

推荐环境变量：

```text
BaseURL=http://localhost:9100
username=zhangsan
password=123456
token=
refreshToken=
orderNo=
requestId=
spuId=1001
skuId=9001
seckillSkuId=9003
```

演示账号：

| 用户名 | 密码 | 角色 |
|---|---|---|
| zhangsan | 123456 | USER |
| merchant01 | 123456 | MERCHANT |
| admin | 123456 | ADMIN |

集合要求：

- 核心接口不少于 6 个；
- 总请求不少于 20 次；
- 使用真实 DTO 字段；
- 登录后自动保存 Token；
- 创建订单后自动保存 `orderNo`；
- 正常和异常用例均有断言；
- 至少验证一次 OpenFeign 调用链；
- 不提交固定 Token；
- 不使用 `dummy` 或机械填充请求体。

执行：

```powershell
pwsh .\scripts\run-newman.ps1
```

秒杀 Newman 用例使用 `zhangsan` 和当前进行中的种子活动，正式回归前必须清理该测试用户对目标活动的限购状态，或重置 Redis 与数据库测试数据。先通过秒杀活动接口或数据库确认当前目标 `activityId`，再清理对应 key 和订单。当前本地 Docker 环境示例：

```powershell
$activityId = 3
docker exec mall-redis redis-cli DEL "seckill:user:$activityId:1001"
docker exec mall-mysql mysql -uroot -proot -e "DELETE FROM mall_seckill.seckill_order WHERE activity_id=$activityId AND user_id=1001;"
```

不得通过放宽秒杀限购断言来规避重复执行问题。

记录到 `docs/FINAL_REPORT.md`：

- 请求总数；
- 断言总数；
- 通过数；
- 失败数；
- 失败原因；
- 报告路径。

---

## 4. JMeter

脚本：

```text
docs/test/jmeter/search-load.jmx
docs/test/jmeter/order-load.jmx
docs/test/jmeter/seckill-stress.jmx
```

执行：

```powershell
pwsh .\scripts\run-jmeter.ps1 -Scenario search -Users 50 -Duration 300
pwsh .\scripts\run-jmeter.ps1 -Scenario order -Users 50 -Duration 300
pwsh .\scripts\prepare-seckill-jmeter.ps1 -ActivityId 9001 -SkuId 99003 -TotalStock 100 -UserCount 100
pwsh .\scripts\run-jmeter.ps1 -Scenario seckill -Users 100 -RampUp 10 -Loops 1 -ActivityId 9001 -SkuId 99003 -UsernamePrefix jmeter_seckill_ -ResultPollAttempts 120 -ResultPollDelayMs 500
```

搜索和订单场景使用 `-Duration` 控制持续时间；`-Loops` 只用于秒杀场景。

### 4.1 搜索负载

要求：

- 验证 Elasticsearch 健康后再执行；
- 支持关键字参数；
- 同时断言 HTTP 状态和业务码；
- 保存 JTL 与 HTML 报告。

建议场景：

- 50 用户；
- 150 用户；
- 持续 5 分钟或课程允许的稳定时间。

### 4.2 创建订单负载

要求：

- 登录请求动态获取 Token，或由前置程序生成有效 Token；
- 使用账号密码 `zhangsan / 123456`；
- 使用可重复的用户和 SKU 数据；
- 每个线程使用唯一请求标识；
- 验证库存和订单结果；
- 同时断言 HTTP 状态和业务码。

### 4.3 秒杀压力

正式压力测试前先准备可重复的多用户数据：

```powershell
pwsh .\scripts\prepare-seckill-jmeter.ps1 -ActivityId 9001 -SkuId 99003 -TotalStock 100 -UserCount 100
```

该脚本会准备 `jmeter_seckill_1..N` 测试用户，创建或重置专用活动 `9001`，清理该活动下游秒杀订单、订单明细、库存流水、SKU 库存，并清理 Redis 中该活动的库存缓存和限购 Key。默认活动 `9001` 对应专用压测 `skuId=99003`，该 SKU 从种子 SKU `9003` 复制，默认总库存 100、每用户限购 1。脚本默认通过宿主机 MySQL 连接执行；如后端连接 Docker MySQL，可显式使用 `-MysqlMode docker`。

压测执行示例：

```powershell
pwsh .\scripts\run-jmeter.ps1 -Scenario seckill -Users 100 -RampUp 10 -Loops 1 -ActivityId 9001 -SkuId 99003 -UsernamePrefix jmeter_seckill_ -ResultPollAttempts 120 -ResultPollDelayMs 500
```

进入阶梯压力前，必须先完成 10 用户完整链路验证，并确认 JMeter 失败样本为 0、最终成功 10、`orderNo` 去重数 10、Redis 剩余库存 90、真实库存锁定增量 10。

建议阶梯：

```text
50 → 100 → 200 → 300 → 500
```

当前固定库存阶梯复测已按 120 次、500ms 结果轮询窗口执行，最新脱敏摘要见 `docs/test/jmeter/summary/seckill-ladder-20260610-234029.md`。报告中的 `Total P95` 和 `Total TPS` 必须保持为 JMeter 全部样本聚合口径，不得写成秒杀最终订单 TPS 或完整链路 P95。

记录：

- 首次触发限流的并发；
- 成功受理数量；
- 最终成功订单数；
- 最终失败数量；
- 库存不足数量；
- 限购数量；
- Sentinel 429 数量；
- 其他业务失败数量；
- 系统异常数量；
- P95；
- 吞吐量；
- 错误率；
- Sentinel 返回；
- CPU、内存；
- 是否出现服务完全不可用。

执行后必须核验：

- `seckill_order` 中测试用户订单数；
- `seckill_order` 中该活动全部订单数；
- 去重用户数；
- Redis 剩余库存；
- 订单数不得超过活动总库存；
- JMeter 结果查询必须断言 HTTP 200、业务码 200、最终 `status=1` 和非空 `orderNo`；
- 不得通过放宽限购或业务码断言来规避重复执行问题。

未实际运行不得填写 P95、吞吐量或错误率。

---

## 5. 技术专项冒烟检查

```powershell
pwsh .\scripts\run-special-checks.ps1
```

该脚本只做只读可达性检查，覆盖：

- Nacos 控制台；
- Sentinel Dashboard；
- Elasticsearch `_cluster/health`；
- Gateway `/actuator/health`；
- 搜索热词和商品搜索；
- 秒杀活动接口。

说明：

- HTTP 可达不等于业务通过；
- `run-special-checks.ps1` 只判断 HTTP 可达；搜索业务通过必须以 `init-search-index.ps1`、Newman 或专项报告中的业务码断言为准；
- `-AllowFailures` 只用于记录当前环境状态；
- 该脚本不能替代 Newman、JMeter、Sentinel Dashboard 截图、Nacos 热更新截图或 Elasticsearch 查询结果报告。

### 5.1 Sentinel 限流验证

Sentinel 专项至少记录：

- Dashboard HTTP 可达；
- 业务应用是否上报到 Dashboard；
- 资源名；
- 规则来源；
- 请求数量；
- 通过数和限流数；
- Sentinel 返回内容；
- 验证后是否清理临时规则。

临时 command center 规则只能证明当前运行实例可被限流，不等同于 Nacos 持久化规则加载成功。熔断验证必须单独模拟真实慢调用或异常比例，不得用限流结果替代。

Sentinel Nacos 持久化规则验证还应额外记录：

- Nacos 命名空间、Group、DataId；
- 运行时 `getRules?type=flow` 或 `getRules?type=degrade` 查询结果；
- 临时规则热更新结果；
- 规则回滚结果；
- 是否仍存在 Spring Config 解析错误。

熔断专项还应记录：

- 熔断策略类型，例如慢调用比例、异常比例或异常数；
- 临时验证阈值和回滚后的仓库规则；
- 正常态请求结果；
- 触发熔断后的 HTTP 状态和 Sentinel 返回；
- Dashboard 或 command center 统计值；
- 临时低阈值不得写成生产阈值或容量结论。

### 5.2 Nacos 普通业务配置热更新

Nacos 普通业务配置热更新至少记录：

- 命名空间、Group、DataId；
- 配置项名称；
- 修改前业务接口结果；
- 热更新后的业务接口结果；
- 是否重启服务；
- 回滚后的业务接口结果；
- 证据摘要路径。

Sentinel 规则热更新不能替代普通业务配置热更新。验证用临时配置必须在结束后回滚到仓库基线。

### 5.3 Elasticsearch 搜索索引初始化

```powershell
pwsh .\scripts\init-search-index.ps1
```

用途：

- 检查 Elasticsearch `_cluster/health`；
- 通过 `mall-search` 内部同步接口把真实种子商品 `1001`～`1005` 写入搜索索引；
- 检查 Gateway health 后，通过 Gateway `/api/v1/search/products` 校验 HTTP 状态、统一响应业务码和搜索结果数量。

常用参数：

```powershell
pwsh .\scripts\init-search-index.ps1 -Keyword iPhone
pwsh .\scripts\init-search-index.ps1 -SpuIds 1001,1002,1005
pwsh .\scripts\init-search-index.ps1 -ExpectedSpuIds 1001,1002
pwsh .\scripts\init-search-index.ps1 -VerifyAttempts 10 -VerifyDelayMs 500
pwsh .\scripts\init-search-index.ps1 -SkipSync
pwsh .\scripts\init-search-index.ps1 -AllowFailures
```

说明：

- 该脚本不伪造商品数据，索引内容来自 `mall-product` 真实商品详情接口；
- `SearchURL` 默认 `http://localhost:9108`，用于调用内部同步接口；
- `BaseURL` 默认 `http://localhost:9100`，用于验证 Gateway 搜索业务入口；
- 默认搜索关键字为 `iPhone`，预期结果包含种子商品 `1001` 或 `1002`；
- 搜索验证会按 `VerifyAttempts` / `VerifyDelayMs` 轮询，降低 ES 写入后刷新延迟导致的假失败；
- `-AllowFailures` 只用于记录当前环境状态，不得写成搜索专项通过；
- Newman 搜索用例通过前，仍不得把 Elasticsearch 搜索标记为已验证。

---

## 6. 前端页面验收

前端验收不得只检查首页可渲染。最终至少覆盖：

- 首页 / 商品浏览、商品详情、搜索页；
- 登录、注册、账户资料、地址；
- 购物车、订单确认、订单详情、支付页；
- 秒杀活动、秒杀详情、秒杀结果；
- 后台看板、后台订单、后台商品；
- 技术能力通过真实业务链路与测试报告体现，不再设置单独前端技术演示页。

每个页面至少记录：

- 路由；
- 实现状态：已完成 / 部分完成 / 未完成 / 受后端限制；
- 桌面端和移动端截图；
- 主操作路径；
- loading / empty / error / disabled / success 中适用状态；
- 后端接口联调结果；
- 未完成原因。

业务页面不得以 raw JSON 或接口调试面板作为主要验收证据。错误态截图不能证明业务成功态完成。

验收矩阵格式：

| 页面 | 路由 | 实现状态 | 验证方式 | 证据或说明 | 未完成原因 |
|---|---|---|---|---|---|
| 示例 | `/example` | 待填写 | 待填写 | 待填写 | 待填写 |

---

## 7. 异常场景

至少覆盖：

| 场景 | 证据 |
|---|---|
| 无 Token/错误 Token | Postman/Newman 报告 |
| 库存不足 | Postman + DB 查询 |
| 下游服务停止 | 请求结果 + Nacos/Sentinel 截图 |
| Nacos 热更新 | 修改前后截图和日志 |
| Seata 2.0.0 回滚 | 订单、库存和事务日志 |
| 重复支付消息 | 订单、库存状态查询 |
| 秒杀限购 | Postman/JMeter 结果 |

---

## 8. 结果可信度

禁止：

- 手工修改 JTL 或 HTML 结果；
- 用预估值代替 P95；
- 只截成功请求而隐藏失败请求；
- 省略测试机器配置、JDK 版本和 Commit；
- 把未执行脚本标记为通过；
- 使用固定 Token 假装登录链路有效；
- 把测试资产建立写成测试通过。

允许并建议如实记录：

- 性能目标未达到；
- 某个服务成为瓶颈；
- Java 21 或 Seata 2.0.0 出现兼容问题；
- 某个功能尚未验证；
- 限流阈值需要调整；
- Docker 或本机资源限制影响结果。
