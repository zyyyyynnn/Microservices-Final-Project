# MallCloud 后续开发执行 Prompt

> 使用对象：后续执行 Agent 或开发成员
> 团队规模：5 人
> 最高标准：`docs/PROJECT_STANDARD.md`
> 技术基线：Java 21 LTS、Spring Boot 3.2.4、Spring Cloud Alibaba 2023.0.1.0、Seata 2.0.0

---

## 1. 任务目标

按以下顺序完成后续开发：

```text
验证 Java 21 与 Seata 2.0.0
  → 修复配置与构建
  → 验证服务注册和 Gateway
  → 打通交易主链路
  → 完善必要的治理能力
  → 重建真实测试资产
  → 填写最终报告
```

项目不增加新的微服务和业务模块，不追求功能数量。所有修改必须服务于课程评分标准和核心链路质量。

---

## 2. 开始前必须阅读

1. `docs/PROJECT_STANDARD.md`
2. `docs/PRD.md`
3. `docs/ARCHITECTURE.md`
4. `docs/API.md`
5. `docs/DATABASE.md`
6. `docs/CODING_STYLE.md`
7. `docs/DEPLOY.md`
8. `docs/QUICK_START.md`
9. `docs/test/README.md`
10. `docs/FINAL_REPORT.md`

若代码与文档冲突：

- 先确认实际可运行行为；
- 再按项目标准决定正确目标；
- 同时修改代码和相关文档；
- 不静默保留冲突。

---

## 3. 当前事实

- 团队固定为 5 人；
- 13 个服务模块和基础分层已存在；
- 父 POM 已切换为 Java 21；
- 不使用 Java 21 预览特性；
- 不采用 Java 24；
- Docker/K8s 的 Seata Server 已统一为 2.0.0；
- 演示账号统一密码为 `123456`；
- 当前推荐部署为 Docker 中间件 + IDE 启动服务；
- Docker 全栈和 Kubernetes 全栈不是正式可用路径；
- Nacos 配置模板存在语法和加载方式待验证问题；
- 父 POM 当前默认跳过测试；
- 核心单元测试数量不足；
- 旧模板 Postman 集合已废弃；
- JMeter 脚本和报告尚未完成；
- 普通下单调用商品与库存服务，不直接调用支付服务；
- 支付结果由 RocketMQ 和 `mall-message` 更新订单与库存；
- `STOCK_ROLLBACK` 当前是普通消息；
- Java 代码当前未使用 `@SentinelResource`；
- 不要求所有 Feign Client 配置 fallbackFactory。

不得假定“地基全部可运行”或“只需填充业务代码”。

---

## 4. 第一阶段：版本、配置与构建

### 完成标准

- `java -version` 为 Java 21；
- `mvn -version` 使用 Java 21；
- 全部 Maven 模块编译成功；
- Seata Server 镜像为 2.0.0；
- Seata 能注册、连接并完成一次回滚；
- Nacos YAML 合法；
- 环境变量名统一；
- 数据库脚本可执行；
- 测试不会被默认配置永久跳过。

### 任务

1. 执行：

```powershell
java -version
mvn -version
mvn clean package -DskipTests
```

2. 检查 Seata：

```powershell
docker inspect mall-seata --format '{{.Config.Image}}'
```

预期：

```text
seataio/seata-server:2.0.0
```

3. 修复 `deploy/nacos/*.yaml` 中的非法 `--` 注释；
4. 检查 Nacos 地址、Namespace、DataId 和 Group；
5. 确认 Spring Cloud Alibaba 2023 的配置导入方式；
6. 统一 `NACOS_SERVER` 与 `NACOS_SERVER_ADDR`；
7. 验证 `scripts/start-middleware.ps1`；
8. 验证 `scripts/init-db.ps1`；
9. 调整父 POM 测试默认行为，使 `mvn test` 实际执行测试。

### 禁止

- 不升级 Spring Boot/Cloud/Alibaba 大版本；
- 不采用 Java 24；
- 不批量改写 DTO 为 record；
- 不默认启用虚拟线程；
- 不完善 Docker/K8s 全栈，除非基础运行已稳定。

---

## 5. 第二阶段：服务注册和 Gateway

### 完成标准

- 核心服务在 Nacos 健康注册；
- Gateway 能按路径路由；
- 白名单可无 Token 访问；
- 受限资源无 Token、错误 Token 返回未授权；
- 有效 Token 能向下游传递用户 ID 和角色。

### 测试账号

```text
zhangsan / 123456
merchant01 / 123456
admin / 123456
```

### 核心服务

```text
mall-gateway
mall-auth
mall-user
mall-product
mall-inventory
mall-cart
mall-order
mall-pay
mall-message
```

---

## 6. 第三阶段：交易主链路

```text
登录
  → 商品查询
  → 购物车
  → 创建订单
  → order 调 product
  → order 调 inventory.lock
  → 写订单
  → 支付结果消息
  → message 调 order.markPaid
  → message 调 inventory.deduct
```

### 6.1 订单创建

检查：

- DTO 参数校验；
- 商品不存在；
- 商品服务调用失败；
- 数量和金额计算；
- 库存锁定失败；
- 订单和订单项写入；
- 失败时库存状态；
- 返回 orderNo。

### 6.2 Seata 2.0.0

验证：

- Server 是否正确启动；
- 客户端是否注册或连接；
- `@GlobalTransactional` 是否生效；
- XID 是否透传到库存服务；
- 数据源是否被代理；
- 订单写入失败后库存锁定是否回滚；
- `undo_log` 是否正常。

不能通过注解存在推断事务已经生效。

### 6.3 支付消息

检查：

- `PAY_RESULT` 消息结构；
- JSON 解析；
- 订单状态条件更新；
- 库存确认扣减；
- 重复消息幂等；
- 订单更新成功但库存失败时的明确错误和恢复策略。

课程项目优先使用状态幂等和明确重试，不直接引入复杂本地消息表。

---

## 7. 第四阶段：必要的服务治理

### 7.1 Feign fallback

只优先处理：

```text
order → product
order → inventory
message → order
message → inventory
```

要求：

- 返回明确失败；
- 不伪造成功数据；
- 写操作不盲目重试；
- 异常日志包含服务名和业务 ID。

### 7.2 Sentinel

只选择两个主要资源：

- 创建订单；
- 秒杀请求。

可以按 Web 路径配置，或在核心方法增加少量 `@SentinelResource`。选择后同步架构文档。

### 7.3 Nacos 热更新

选择一个低风险参数，完成：

- 配置修改前调用；
- Nacos 修改；
- 不重启服务再次调用；
- 保存日志与截图。

---

## 8. 第五阶段：搜索与秒杀

普通交易主链路稳定后再处理。

### 搜索

- 验证 Elasticsearch 健康；
- 创建或初始化索引；
- 搜索种子商品；
- 验证 `ES_SYNC`；
- 检查 IK 插件是否存在；
- 插件不存在时使用标准分析器，不阻塞核心演示。

### 秒杀

- 活动时间校验；
- 用户限购；
- Redis 原子预扣；
- requestId 防重；
- MQ 异步创建订单；
- 失败回写；
- Sentinel 限流；
- 不超卖测试。

---

## 9. 第六阶段：测试资产

### 9.1 Postman

创建：

```text
docs/test/postman/mallcloud.postman_collection.json
docs/test/postman/local.postman_environment.json
```

要求：

- 20～30 个真实请求；
- 环境变量保存 `username=zhangsan`、`password=123456`；
- 登录保存 Token；
- 创建订单保存 orderNo；
- 正常和异常场景；
- HTTP 状态和业务码断言；
- Newman HTML 报告。

### 9.2 JMeter

创建：

```text
docs/test/jmeter/search-load.jmx
docs/test/jmeter/order-load.jmx
docs/test/jmeter/seckill-stress.jmx
```

执行：

- 50 用户；
- 75～150 用户；
- 阶梯压力到 500 用户；
- 保存 JTL 和 HTML 报告；
- 记录 CPU、内存、JDK 21 和代码 Commit。

### 9.3 异常测试

至少完成：

- 下游服务停止或超时；
- Sentinel 限流/熔断；
- Nacos 热更新；
- 库存不足；
- Seata 2.0.0 回滚；
- 重复支付消息。

---

## 10. 第七阶段：最终报告与答辩

填写 `docs/FINAL_REPORT.md`，不得保留“待填写”。

答辩主流程：

1. 5 人团队分工；
2. Java 21 与组件版本；
3. Nacos 注册；
4. 使用 `zhangsan / 123456` 登录；
5. 商品、购物车、订单；
6. Feign、Seata 2.0.0、MQ；
7. JMeter 与 Sentinel；
8. 一个异常或热更新场景；
9. 测试结果和已知限制。

不演示未完成的 Docker/K8s 全栈。

---

## 11. 每次任务输出格式

```text
结论：
- 完成/部分完成/未完成

修改：
- 文件与关键改动

验证：
- 执行命令
- 实际结果

未验证：
- 原因

文档同步：
- 已更新的文档
```

禁止使用“应该可以”作为验证结果。
