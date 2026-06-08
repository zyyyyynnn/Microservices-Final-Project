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
| 代码分支/Commit | main（本轮提交信息以最终 Git 输出为准） |
| 测试日期 | 2026-06-08 |
| 测试环境 | Windows 11 / PowerShell 7+ / JDK 21 |
| 部署方式 | Docker 中间件 + 本地 IDE 服务 |

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
| Nacos 注册 | 已验证 | 历史核心链路 9 个服务 healthy=true；本轮脚本启动验证中，除 `mall-job` 因 9012 外部占用未启动外，12 个后端服务注册到 Nacos `dev` 命名空间且 healthy=true |
| Nacos 配置热更新 | 待验证 | |
| Gateway 路由与 JWT | 已验证 | 无 Token→401、有效 Token→200 |
| OpenFeign | 已验证 | order→product、order→inventory |
| Seata 2.0.0 回滚 | 已验证 | 故障注入→订单未落库→库存恢复 |
| RocketMQ 消费 | 已验证 | PAY_RESULT→订单已支付→库存扣减 |
| Sentinel 限流/熔断 | 待验证 | |
| Elasticsearch 搜索 | 待验证 | |
| Postman 集合 | 已建立，当前后端环境部分通过 | `run-newman.ps1 -SkipHtml` 已执行：28 个请求均完成，56 个断言中 50 个通过、6 个失败；失败项为搜索商品业务码 10003、库存查询 500、秒杀请求业务码 10001、秒杀结果查询 500 |
| JMeter 脚本 | 已建立，工具链已验证 | `docs/test/jmeter/search-load.jmx`、`order-load.jmx`、`seckill-stress.jmx`；JMeter 5.6.3 已可执行，负载/压力测试尚未运行，当前 Sentinel Dashboard 与 Elasticsearch 不可达 |
| Newman/JMeter 执行入口 | 已建立 | `scripts/run-newman.ps1` 优先使用本机 Newman，缺失时回退 npx；`scripts/run-jmeter.ps1` 优先使用本机 JMeter，缺失时下载本地 JMeter 到 `.tools/` |
| 技术专项冒烟入口 | 已建立，当前环境部分通过 | `scripts/run-special-checks.ps1 -AllowFailures` 已执行：Nacos、Gateway health、搜索热词、搜索商品 HTTP 可达，秒杀活动无 Token 返回 401；搜索商品业务码仍受 Elasticsearch 不可达影响，Sentinel Dashboard 和 Elasticsearch health 连接失败，不能标记为专项验收通过 |
| 前端演示系统 | 部分实现，受后端限制 | 已完成产品化页面整改和浏览器基础验证；后端未完整联调时可见 502/错误状态；成功态业务闭环、逐页成功截图和真实接口数据仍待补充 |

---

## 5. Postman 接口测试

报告位置：

```text
docs/test/postman/summary/newman-20260609.md
```

| 指标 | 结果 |
|---|---|
| 核心接口数量 | 待填写，要求 ≥ 6 |
| 请求总数 | 28 |
| 断言总数 | 56 |
| 通过 | 50 |
| 失败 | 6；原因：搜索商品返回业务码 10003（Elasticsearch 当前不可达）、库存查询返回 500、秒杀请求返回业务码 10001、秒杀结果查询返回 500 |

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

---

## 7. JMeter 负载与压力测试

### 7.1 测试环境

当前状态：JMeter 5.6.3 命令已验证可执行；本轮已恢复 Gateway 和 12 个后端服务启动，但 Sentinel Dashboard 与 Elasticsearch 当前不可达，尚未执行负载或压力测试。以下指标不得在运行前填写估算值。

| 项目 | 内容 |
|---|---|
| CPU | |
| 内存 | |
| 操作系统 | Windows 11 |
| JDK | 21 |
| Docker 资源限制 | |
| 代码 Commit | |

### 7.2 商品查询与订单

| 场景 | 并发用户 | 平均 RT | P95 | 吞吐量 | 错误率 |
|---|---:|---:|---:|---:|---:|
| 商品查询 | 50 | | | | |
| 商品查询 | 150 | | | | |
| 创建订单 | 50 | | | | |
| 创建订单 | 75～150 | | | | |

### 7.3 秒杀阶梯压力

| 并发用户 | P95 | 吞吐量 | 错误率 | Sentinel 触发 | CPU | 内存 |
|---:|---:|---:|---:|---|---:|---:|
| 50 | | | | | | |
| 100 | | | | | | |
| 200 | | | | | | |
| 300 | | | | | | |
| 500 | | | | | | |

记录是否达到 P95 < 1s、首次性能下降、首次错误和系统瓶颈。

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
- 本地一键后端启动当前可拉起 12 个后端服务；`mall-job` 因本机 9012 被外部 `ArmourySocketServer` 占用未启动；
- Kubernetes 只有示例；
- 部分辅助接口未覆盖；
- 未部署完整监控平台；
- 前端已完成一轮产品化页面整改，但后端真实成功态联调、逐页成功截图和主流程操作证据仍待补充；
- 某些性能目标未达到；
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
