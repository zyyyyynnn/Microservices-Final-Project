# MallCloud 测试资产说明

> 本目录用于保存可重复执行的测试脚本、环境文件、运行结果和截图。
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
    └── resource-monitoring.png
```

不存在的文件不得在其他文档中写成已生成。

---

## 2. Postman 标准

最终集合应满足：

- 核心接口不少于 6 个；
- 总请求不少于 20 次；
- 使用真实 DTO 字段；
- 登录后自动保存 Token；
- 创建订单后自动保存 `orderNo`；
- 正常和异常用例均有断言；
- 至少验证一次 OpenFeign 调用链；
- 不提交固定伪 Token；
- 不使用 `dummy` 或 `bad_data` 机械填充请求体。

推荐环境变量：

```text
BaseURL=http://localhost:9000
token=
refreshToken=
orderNo=
requestId=
spuId=1001
skuId=9001
```

### 2.1 Newman 执行

安装：

```powershell
npm install -g newman newman-reporter-html
```

执行：

```powershell
newman run .\docs\test\postman\mallcloud.postman_collection.json `
  -e .\docs\test\postman\local.postman_environment.json `
  -r cli,html `
  --reporter-html-export .\docs\test\postman\report.html
```

报告生成后记录：

- 请求总数；
- 断言总数；
- 通过数；
- 失败数；
- 失败原因。

---

## 3. JMeter 标准

### 3.1 商品查询负载

文件：

```text
docs/test/jmeter/search-load.jmx
```

至少执行：

- 50 用户；
- 150 用户；
- 持续 5 分钟或课程允许的稳定时间；
- 记录平均 RT、P95、吞吐量和错误率。

### 3.2 创建订单负载

文件：

```text
docs/test/jmeter/order-load.jmx
```

要求：

- 登录或预置有效 Token；
- 使用可重复的用户和 SKU 数据；
- 避免所有线程竞争同一个不可重复业务 ID；
- 验证库存和订单结果；
- 不只发送 HTTP 请求而不检查业务码。

### 3.3 秒杀压力

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

### 3.4 命令行执行

```powershell
jmeter -n `
  -t .\docs\test\jmeter\search-load.jmx `
  -l .\docs\test\jmeter\results\search-50.jtl `
  -e `
  -o .\docs\test\jmeter\report\search-50
```

每次生成报告前，输出目录必须不存在或为空。

---

## 4. 异常场景

至少保存以下测试记录：

| 场景 | 证据 |
|---|---|
| 无 Token/错误 Token | Postman/Newman 报告 |
| 库存不足 | Postman + DB 查询 |
| 下游服务停止 | 请求结果 + Nacos/Sentinel 截图 |
| Nacos 热更新 | 修改前后截图和日志 |
| Seata 回滚 | 订单和库存 SQL 查询 |
| 重复支付消息 | 订单、库存状态查询 |
| 秒杀限购 | Postman/JMeter 结果 |

---

## 5. 测试数据隔离

- 测试前记录数据库初始状态；
- 性能测试使用专用用户或可清理数据；
- 每轮测试使用唯一请求 ID；
- 重复测试前清理订单、库存锁定和秒杀结果；
- 不直接修改生产或共享环境数据；
- 报告中记录使用的 Commit。

---

## 6. 结果可信度

禁止：

- 手工修改 JTL 或 HTML 结果；
- 用预估值代替 P95；
- 只截成功请求而隐藏失败请求；
- 省略测试机器配置；
- 把未执行脚本标记为通过；
- 使用固定 Token 假装登录链路有效。

允许并建议如实记录：

- 性能目标未达到；
- 某个服务成为瓶颈；
- 某个功能尚未验证；
- 限流阈值需要调整；
- Docker 或本机资源限制影响结果。
