# MallCloud 测试资产说明

> 测试基线：Windows 11、PowerShell 7+、JDK 21
> 演示账号统一密码：`123456`
> 测试结论最终汇总到 `docs/FINAL_REPORT.md`。

---

## 1. 目录约定

```text
docs/test/
├── README.md
├── postman/
│   ├── mallcloud.postman_collection.json
│   ├── local.postman_environment.json
│   └── report.html
├── jmeter/
│   ├── search-load.jmx
│   ├── order-load.jmx
│   ├── seckill-stress.jmx
│   ├── results/
│   └── report/
└── screenshots/
    ├── nacos-services.png
    ├── nacos-refresh-before.png
    ├── nacos-refresh-after.png
    ├── sentinel-limit.png
    ├── seata-rollback.png
    ├── frontend-home-desktop.png
    ├── frontend-home-mobile.png
    ├── frontend-home-productized-desktop.png
    ├── frontend-home-productized-mobile.png
    ├── frontend-login.png
    ├── frontend-register.png
    ├── frontend-account-error.png
    ├── frontend-product-detail-error.png
    ├── frontend-search-error.png
    ├── frontend-cart-error.png
    ├── frontend-checkout-error.png
    ├── frontend-order-detail-error.png
    ├── frontend-pay-error.png
    ├── frontend-seckill-error.png
    ├── frontend-admin-error.png
    └── resource-monitoring.png
```

不存在的文件不得写成已生成。

当前已建立：

- `docs/test/postman/mallcloud.postman_collection.json`
- `docs/test/postman/local.postman_environment.json`
- `docs/test/jmeter/search-load.jmx`
- `docs/test/jmeter/order-load.jmx`
- `docs/test/jmeter/seckill-stress.jmx`
- `scripts/run-newman.ps1`
- `scripts/run-jmeter.ps1`
- `scripts/run-special-checks.ps1`

以上资产已完成 JSON/XML 静态解析校验。2026-06-08 已在当前本地后端环境执行 Newman 回归：28 个请求均完成，56 个断言中 50 个通过、6 个失败；尚未执行 JMeter 负载或压力测试，不代表性能或 Sentinel 结果已通过。

`frontend-home-desktop.png` 和 `frontend-home-mobile.png` 只能证明前端工程可渲染和基础响应式可用，不能证明完整前端页面已交付。前端页面验收必须补充逐页截图、主流程操作结果、状态反馈和未完成项说明。

`frontend-*-error.png` 表示本轮在后端未完整可用或 Gateway 返回 502 时验证了页面错误状态，不代表对应业务成功态已完成。

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

## 3. Postman 标准

最终集合必须：

- 核心接口不少于 6 个；
- 总请求不少于 20 次；
- 使用真实 DTO 字段；
- 登录后自动保存 Token；
- 创建订单后自动保存 `orderNo`；
- 正常和异常用例均有断言；
- 至少验证一次 OpenFeign 调用链；
- 不提交固定 Token；
- 不使用 `dummy` 或 `bad_data` 机械填充请求体。

当前集合已建立，包含登录、公共商品/搜索、Gateway 鉴权边界、用户/购物车/订单/支付、秒杀和后台请求。最近一次 `run-newman.ps1 -SkipHtml` 结果为 28 个请求、56 个断言、50 通过、6 失败；失败项不得写成通过。

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
```

演示账号：

| 用户名 | 密码 | 角色 |
|---|---|---|
| zhangsan | 123456 | USER |
| merchant01 | 123456 | MERCHANT |
| admin | 123456 | ADMIN |

### 3.1 Newman 执行

```powershell
pwsh .\scripts\run-newman.ps1
```

脚本优先使用本机已安装的 `newman`；未安装时回退到 `npx` 临时获取 `newman` 和 `newman-reporter-htmlextra`。

记录：

- 请求总数；
- 断言总数；
- 通过数；
- 失败数；
- 失败原因。

---

## 4. JMeter 标准

### 4.1 商品查询负载

文件：

```text
docs/test/jmeter/search-load.jmx
```

当前脚本已建立，支持 `-JBaseURL`、`-Jusers`、`-Jrampup`、`-Jduration`、`-Jkeyword` 参数覆盖；尚未生成 JTL 或 HTML 报告。Elasticsearch 当前不可达，搜索负载测试不得写入通过结论。

至少执行：

- 50 用户；
- 150 用户；
- 持续 5 分钟或课程允许的稳定时间；
- 记录平均 RT、P95、吞吐量和错误率。

### 4.2 创建订单负载

文件：

```text
docs/test/jmeter/order-load.jmx
```

当前脚本已建立，登录后动态提取 Token，再创建订单并提取 `orderNo`；尚未执行负载测试。

要求：

- 使用登录请求动态获取 Token，或由前置程序生成有效 Token；
- 使用账号密码 `zhangsan / 123456`；
- 使用可重复的用户和 SKU 数据；
- 每个线程使用唯一请求标识；
- 验证库存和订单结果；
- 同时断言 HTTP 状态和业务码。

### 4.3 秒杀压力

文件：

```text
docs/test/jmeter/seckill-stress.jmx
```

当前脚本已建立，登录后发起秒杀并查询结果，用于阶梯压力和 Sentinel 限流观察；尚未执行压力测试。

阶梯并发：

```text
50 → 100 → 200 → 300 → 500
```

记录：

- 首次触发限流的并发；
- P95；
- 错误率；
- Sentinel 返回；
- CPU、内存；
- 是否出现服务完全不可用。

### 4.4 命令行执行

```powershell
pwsh .\scripts\run-jmeter.ps1 -Scenario search -Users 50
pwsh .\scripts\run-jmeter.ps1 -Scenario order -Users 50
pwsh .\scripts\run-jmeter.ps1 -Scenario seckill -Users 100 -RampUp 10 -Loops 1
```

脚本优先使用本机已安装的 `jmeter`；未安装时按需下载 Apache JMeter 到 `.tools/`，并按时间戳生成 JTL 与 HTML 报告目录；同名目录存在时失败。`search/order` 使用 `-Duration` 控制运行时间，`-Loops` 仅对 `seckill` 生效。

---

## 4.5 技术专项冒烟检查

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

如果 Gateway、业务服务或中间件未启动，脚本会返回失败。可使用 `-AllowFailures` 保留失败输出但不让命令返回非 0，便于记录当前环境状态。2026-06-08 当前环境结果：Nacos、Gateway health、搜索热词、搜索商品 HTTP 可达，秒杀活动无 Token 返回 401；搜索商品业务码仍受 Elasticsearch 不可达影响，Sentinel Dashboard 和 Elasticsearch health 连接失败。该脚本不能替代 Newman、JMeter、Sentinel Dashboard 截图、Nacos 热更新截图或 Elasticsearch 查询结果报告。

---

## 4.6 前端页面验收

前端验收不得只检查首页可渲染。最终至少应覆盖：

- 首页 / 商品浏览、商品详情、搜索页；
- 登录、注册、账户资料、地址；
- 购物车、订单确认、订单详情、支付页；
- 秒杀活动、秒杀详情、秒杀结果；
- 后台看板、后台订单、后台商品；
- 技术演示页。

每个页面至少记录桌面端和移动端截图、主操作路径、loading / empty / error / disabled / success 中适用状态、后端接口联调结果和未完成原因。业务页面不得以 raw JSON 或接口调试面板作为主要验收证据。

### 4.6.1 本轮前端页面整改验收矩阵

| 页面 | 路由 | 实现状态 | 验证方式 | 证据或说明 | 未完成原因 |
|---|---|---|---|---|---|
| 首页 / 商品浏览 | `/` | 部分完成 | 浏览器桌面、移动端 | `frontend-home-productized-desktop.png`、`frontend-home-productized-mobile.png` | 商品列表接口未确认，首页使用已知演示 SPU 详情入口 |
| 商品详情 | `/products/:id` | 受后端限制 | 浏览器错误状态 | `frontend-product-detail-error.png` | 当前验证环境 Gateway 返回 502，成功态待后端联调 |
| 搜索页 | `/search` | 受后端限制 | 浏览器错误状态 | `frontend-search-error.png` | mall-search / Elasticsearch 成功数据待联调 |
| 登录页 | `/login` | 部分完成 | 浏览器页面渲染 | `frontend-login.png` | 登录成功态需后端 Auth/Gateway 联调 |
| 注册页 | `/register` | 部分完成 | 浏览器页面渲染 | `frontend-register.png` | 注册成功态待后端联调 |
| 账户资料 / 地址页 | `/account` | 受后端限制 | 浏览器受限路由错误状态 | `frontend-account-error.png` | 用户资料和地址成功态待后端联调 |
| 购物车页 | `/cart` | 受后端限制 | 浏览器错误状态 | `frontend-cart-error.png` | 购物车 Redis 数据和商品远程查询成功态待联调 |
| 订单确认页 | `/checkout` | 受后端限制 | 浏览器错误状态 | `frontend-checkout-error.png` | 地址、购物车和创建订单成功态待联调 |
| 订单详情页 | `/orders/:orderNo` | 受后端限制 | 浏览器错误状态 | `frontend-order-detail-error.png` | 订单查询成功态待后端联调 |
| 支付页 | `/pay/:orderNo` | 受后端限制 | 浏览器错误状态 | `frontend-pay-error.png` | 支付记录、通知和 MQ 成功态待联调 |
| 秒杀活动 / 结果页 | `/seckill` | 受后端限制 | 浏览器错误状态 | `frontend-seckill-error.png` | 秒杀活动、请求和结果轮询成功态待联调 |
| 后台看板 / 订单 / 商品页 | `/admin` | 受后端限制 | 浏览器错误状态 | `frontend-admin-error.png` | 后台聚合接口成功态和角色权限待联调 |
| 技术演示页 | `/tech` | 部分完成 | 浏览器路由验证 | 技术点说明和可验证入口保留 | Sentinel、Nacos 热更新和 ES 成功证据待专项验证 |

---

## 5. 异常场景

| 场景 | 证据 |
|---|---|
| 无 Token/错误 Token | Postman/Newman 报告 |
| 库存不足 | Postman + DB 查询 |
| 下游服务停止 | 请求结果 + Nacos/Sentinel 截图 |
| Nacos 热更新 | 修改前后截图和日志 |
| Seata 2.0.0 回滚 | 订单、库存和事务日志 |
| 重复支付消息 | 订单、库存状态查询 |
| 秒杀限购 | Postman/JMeter 结果 |

### 5.1 Seata 验证

至少保存：

- Seata Server 2.0.0 容器信息；
- 服务注册/连接信息；
- 全局事务 XID；
- 失败前后订单记录；
- 失败前后库存 `locked/available`；
- `undo_log` 或服务端事务日志。

---

## 6. 测试数据隔离

- 测试前记录数据库初始状态；
- 性能测试使用专用用户或可清理数据；
- 每轮测试使用唯一请求 ID；
- 重复测试前清理订单、库存锁定和秒杀结果；
- 不直接修改共享环境后不留记录；
- 密码 `123456` 仅用于本地课程环境；
- 报告记录使用的 Commit。

---

## 7. 结果可信度

禁止：

- 手工修改 JTL 或 HTML 结果；
- 用预估值代替 P95；
- 只截成功请求而隐藏失败请求；
- 省略测试机器配置、JDK 版本和 Commit；
- 把未执行脚本标记为通过；
- 使用固定 Token 假装登录链路有效。

允许并建议如实记录：

- 性能目标未达到；
- 某个服务成为瓶颈；
- Java 21 或 Seata 2.0.0 出现兼容问题；
- 某个功能尚未验证；
- 限流阈值需要调整；
- Docker 或本机资源限制影响结果。
