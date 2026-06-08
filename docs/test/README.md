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
    └── resource-monitoring.png
```

不存在的文件不得写成已生成。

`frontend-home-desktop.png` 和 `frontend-home-mobile.png` 只能证明前端工程可渲染和基础响应式可用，不能证明完整前端页面已交付。前端页面验收必须补充逐页截图、主流程操作结果、状态反馈和未完成项说明。

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
npm install -g newman newman-reporter-html

newman run .\docs\test\postman\mallcloud.postman_collection.json `
  -e .\docs\test\postman\local.postman_environment.json `
  -r cli,html `
  --reporter-html-export .\docs\test\postman\report.html
```

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
jmeter -n `
  -t .\docs\test\jmeter\search-load.jmx `
  -l .\docs\test\jmeter\results\search-50.jtl `
  -e `
  -o .\docs\test\jmeter\report\search-50
```

输出目录必须不存在或为空。

---

## 4.5 前端页面验收

前端验收不得只检查首页可渲染。最终至少应覆盖：

- 首页 / 商品浏览、商品详情、搜索页；
- 登录、注册、账户资料、地址；
- 购物车、订单确认、订单详情、支付页；
- 秒杀活动、秒杀详情、秒杀结果；
- 后台看板、后台订单、后台商品；
- 技术演示页。

每个页面至少记录桌面端和移动端截图、主操作路径、loading / empty / error / disabled / success 中适用状态、后端接口联调结果和未完成原因。业务页面不得以 raw JSON 或接口调试面板作为主要验收证据。

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
