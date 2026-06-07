# MallCloud 后续开发执行 Prompt

> 使用对象：开发成员
> 团队规模：5 人
> 最高标准：`docs/PROJECT_STANDARD.md`
> 技术基线：Java 21 LTS、Spring Boot 3.2.4、Spring Cloud Alibaba 2023.0.1.0、Seata 2.0.0

---

## 1. 任务目标

按以下顺序完成后续开发：

```text
[已完成] 运行基线（Java 21 / Maven / MySQL / Redis / Nacos / Seata）
  → [已完成] 认证与网关运行闭环（mall-user / mall-auth / mall-gateway）
  → [已完成] 交易主链路（Product / Inventory / Cart / Order / Seata AT）
  → [已完成] 支付与 MQ
  → [当前] 前端完整演示系统
  → 技术亮点与专项验证
  → 测试资产
  → 最终报告与答辩材料
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
- 父 POM 已切换为 Java 21，已取消默认 `<skipTests>true</skipTests>`；
- Java 21 全模块构建和现有 13 个测试已通过；
- 不使用 Java 21 预览特性；
- 不采用 Java 24；
- MySQL 8.0、Redis 7、Nacos 2.3.2 已完成基础运行验证；
- Nacos YAML 中非法 `--` 注释已修复；
- Seata Server 2.0.0 的 DB Store 和 Nacos 注册已验证；
- Seata Server Schema 已统一为官方四表（global_table / branch_table / lock_table / distributed_lock）；
- 当前数据库基线只保留 `db/init/`；
- Docker/K8s 的 Seata Server 已统一为 2.0.0；
- 演示账号统一密码为 `123456`；
- 当前推荐部署为 Docker 中间件 + IDE 启动服务；
- Docker 全栈和 Kubernetes 全栈不是正式可用路径；
- 旧模板 Postman 集合已废弃，需重建；
- JMeter 脚本和报告尚未完成；
- 普通下单调用商品与库存服务，不直接调用支付服务；
- 支付结果由 RocketMQ 和 `mall-message` 更新订单与库存；
- `STOCK_ROLLBACK` 当前是普通消息；
- Java 代码当前未使用 `@SentinelResource`；
- 不要求所有 Feign Client 配置 fallbackFactory；
- 业务服务 Nacos 配置加载已在核心链路验收中验证，Nacos 热更新仍待验证；
- Seata AT 真实业务回滚已通过验收。
- 当前仓库尚无前端源码；README 与 DESIGN 已声明前端技术栈和产品设计基线；
- 前端完整演示系统是必做阶段，不是可选项。

---

## 4. 第一阶段：运行基线回归检查（已完成，后续仅做回归）

> **已完成并验收通过：** Java 21、Maven 构建、现有 13 个测试、MySQL、Redis、Nacos、Seata Server 2.0.0。

### 回归命令

新任务开始前执行以下命令确认基线未被破坏：

```powershell
java -version
mvn -version
mvn clean package -DskipTests
mvn clean test
docker compose -f .\deploy\docker\docker-compose.middleware.yml ps
```

### 原完成标准（已达成）

- `java -version` 为 Java 21；
- `mvn -version` 使用 Java 21；
- 全部 Maven 模块编译成功；
- 现有测试全部通过（13 个，0 失败）；
- 父 POM 已取消默认 `<skipTests>true</skipTests>`；
- Nacos YAML 合法；
- 环境变量名统一；
- 数据库脚本可执行；
- Seata Server 镜像为 2.0.0；
- Seata DB Store 和 Nacos 注册已验证。

### 约束

- 不升级 Spring Boot/Cloud/Alibaba 大版本；
- 不采用 Java 24；
- 不默认启用虚拟线程。

---

## 5. 第二阶段：服务注册和 Gateway（已完成并验收通过）

> **已完成并验收通过。** mall-user、mall-auth、mall-gateway 三个服务启动、注册、登录、JWT 鉴权、用户上下文透传、mall-user 停止/恢复故障场景均已验证。

### 完成标准

- `mall-user`、`mall-auth`、`mall-gateway` 能启动；
- 三个服务注册到 Nacos；
- 明确配置来源（本地 application.yaml + Nacos 远程配置）；
- 登录成功（`zhangsan / 123456`）；
- Gateway JWT 正常；
- 白名单可无 Token 访问；
- 受限资源无 Token、错误 Token 返回未授权；
- 有效 Token 能向下游传递用户 ID 和角色。

### 测试账号

```text
zhangsan / 123456
merchant01 / 123456
admin / 123456
```

### 第一批服务（仅这三个）

```text
mall-user
mall-auth
mall-gateway
```

### 后续服务（第二阶段后半段，不在第一批）

```text
mall-product
mall-inventory
mall-cart
mall-order
mall-pay
mall-message
```

---

## 6. 第三阶段：交易主链路（已完成并验收通过）

> **已完成并验收通过。** Cart、Order 主链路、Seata AT 回滚均已验证。

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

### 6.2 Seata AT 业务回滚

验证：

- `mall-order` 和 `mall-inventory` 是否成功连接 TC；
- `@GlobalTransactional` 是否实际生效；
- XID 是否透传到库存服务；
- 业务数据源是否被 Seata 代理；
- 库存锁定成功后订单写入失败是否整体回滚；
- 订单是否未落库；
- 库存 `locked` 和 `available` 是否恢复；
- 业务库 `undo_log` 是否正常；
- Seata Server 日志是否能关联到本次全局事务。

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

## 7. 第四阶段：支付与消息（已完成并验收通过）

> **已完成并验收通过。** Pay Notify 白名单边界、RocketMQ / Message 支付结果链路（通知→消息→订单已支付→库存扣减、重复通知幂等）均已验证。

---

## 8. 第五阶段：前端完整演示系统（当前任务）

> **当前正式开发起点。** 后端核心链路已完成运行验收，下一步必须搭建前端完整演示系统，用浏览器展示 MallCloud 微商城主要功能。

### 完成标准

- 新建 Vue 3 + Vite + TypeScript 前端工程；
- 使用 Element Plus、Axios、Pinia；
- 能通过 Gateway 调用后端接口；
- 登录后保存并携带 Access Token；
- 支持 Token 失效、无权限和接口错误提示；
- 覆盖公共端、用户端、秒杀端、商家/管理员端和技术演示页；
- 不使用 mock 伪造未完成后端能力；
- 浏览器真实验证通过；
- 保存截图或录屏证据；
- 同步 README、DESIGN、FINAL_REPORT 和测试文档。

### 功能范围

公共端：
- 登录；
- 注册；
- 商品分类；
- 商品列表；
- 商品详情；
- 商品搜索。

用户端：
- 用户资料；
- 收货地址；
- 购物车；
- 创建订单；
- 订单列表；
- 订单详情；
- 模拟支付；
- 支付结果展示。

秒杀端：
- 活动列表；
- 活动详情；
- 发起秒杀；
- 秒杀结果查询；
- 限购、库存不足、重复请求提示。

商家 / 管理员端：
- 后台入口；
- 商品列表；
- 商品新增 / 编辑 / 上下架；
- 订单列表；
- 订单发货；
- 基础看板。

技术演示页：
- Gateway 鉴权状态；
- 当前用户与角色；
- Seata 回滚验证说明；
- RocketMQ 支付消息链路说明；
- Sentinel 限流入口；
- Elasticsearch 搜索入口；
- Nacos 热更新说明或验证入口。

### 禁止

- 不直接绕过 Gateway 调后端服务；
- 不把 mock 数据当作真实后端能力；
- 不新增无关 UI 框架；
- 不实现真实支付；
- 不追求生产级复杂前端；
- 不跳过浏览器验证。

---

## 9. 第六阶段：技术亮点与专项验证

### 9.1 Feign fallback

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

### 9.2 Sentinel

只选择两个主要资源：

- 创建订单；
- 秒杀请求。

可以按 Web 路径配置，或在核心方法增加少量 `@SentinelResource`。选择后同步架构文档。

### 9.3 Nacos 热更新

选择一个低风险参数，完成：

- 配置修改前调用；
- Nacos 修改；
- 不重启服务再次调用；
- 保存日志与截图。

---

## 10. 第七阶段：搜索与秒杀

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

## 11. 第八阶段：测试资产

### 11.1 Postman

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

### 11.2 JMeter

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

### 11.3 异常测试

至少完成：

- 下游服务停止或超时；
- Sentinel 限流/熔断；
- Nacos 热更新；
- 库存不足；
- Seata 2.0.0 回滚；
- 重复支付消息。

---

## 12. 第九阶段：最终报告与答辩

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

## 13. 每次任务输出格式

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
