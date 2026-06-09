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
│   └── report.html                  # 运行 HTML 报告后生成，当前未生成
├── jmeter/
│   ├── search-load.jmx
│   ├── order-load.jmx
│   ├── seckill-stress.jmx
│   ├── results/
│   └── report/
└── screenshots/
```

不存在的文件不得写成已生成。测试结果、截图索引、通过率、失败项和性能指标统一写入 `docs/FINAL_REPORT.md`。

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
BaseURL=http://localhost:9000
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
pwsh .\scripts\run-jmeter.ps1 -Scenario seckill -Users 100 -RampUp 10 -Loops 1
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

建议阶梯：

```text
50 → 100 → 200 → 300 → 500
```

记录：

- 首次触发限流的并发；
- P95；
- 吞吐量；
- 错误率；
- Sentinel 返回；
- CPU、内存；
- 是否出现服务完全不可用。

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

### 5.1 Elasticsearch 搜索索引初始化

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
- `SearchURL` 默认 `http://localhost:9008`，用于调用内部同步接口；
- `BaseURL` 默认 `http://localhost:9000`，用于验证 Gateway 搜索业务入口；
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
- 技术演示页。

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
