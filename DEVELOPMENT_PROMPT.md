# MallCloud 执行 Agent 开发 Prompt

> 本 Prompt 是交给执行 Agent 的唯一入口指令。Agent 读完本文件后即可开始编码，无需再逐个读文档。
> 生成日期：2026-06-06
> 项目评级：A- (85/100)，地基全部就绪，可直接进入业务开发。

---

## 0. 一句话任务

你是一个 Spring Boot 微服务开发 Agent。你的任务是在 **MallCloud** 项目中实现 13 个微服务的完整业务代码。项目骨架、数据库、中间件部署、Nacos 公共配置、K8s 清单已全部就绪，你只需要**填充业务代码**。

---

## 1. 你现在看到的项目状态

### 1.1 已完成（不要动）

| 层级 | 内容 | 位置 |
|---|---|---|
| 父 POM | Spring Boot 3.2.x + Spring Cloud 2023.0.1 + SCA 2023.0.1.0 | `pom.xml` |
| mall-common | Result/PageData/BizException/GlobalExceptionHandler/ErrorCode/CommonConstants/FeignUserInterceptor/AssertUtil/BizNoUtil/UserContext（11 个 Java 文件） | `mall-common/` |
| 数据库 | 8 库 26 表（含 7 undo_log）+ seed.sql | `db/init/00-create-databases.sql`, `db/init/seed.sql` |
| Docker Compose | 中间件 + 全栈两个 compose 文件 | `deploy/docker/` |
| K8s | 6 中间件 yaml + gateway + ingress + secrets.template | `deploy/k8s/` |
| Nacos 公共配置 | common-mysql/redis/rocketmq/seata/sentinel.yaml + mall-gateway.yaml | `deploy/nacos/` |
| 脚本 | start-middleware.ps1/sh, init-db.ps1/sh | `scripts/` |
| 文档 | AGENTS.md, DESIGN.md, PRD.md, ARCHITECTURE.md, API.md, DATABASE.md, CODING_STYLE.md, DEPLOY.md, QUICK_START.md | 根目录 + `docs/` |

### 1.2 需要你填充的（核心任务）

| 服务 | 现有文件 | 需要新建 |
|---|---|---|
| mall-gateway | Application + JwtAuthFilter | 路由配置完善、白名单、CORS |
| mall-auth | Application + AuthController 空壳 | AuthService/UserDetailsService/JwtUtil/RefreshToken 逻辑/Mapper/Entity |
| mall-user | Application + UserController 空壳 | UserService/AddressService/Mapper/Entity/DTO/VO |
| mall-product | Application + ProductController 空壳 | ProductService/CategoryService/SkuService/Mapper/Entity/DTO/VO |
| mall-inventory | Application + Controller 空壳 | InventoryService/StockMapper/StockLogMapper/Entity/Feign接口 |
| mall-cart | Application + Controller 空壳 | CartService（Redis Hash）/DTO/VO |
| mall-order | Application + OrderController + CreateOrderDTO + OrderItemDTO + CreateOrderVO + OrderService 接口 | OrderServiceImpl/OrderMapper/OrderItemMapper/OrderLogMapper/Entity/Feign Client |
| mall-pay | Application + PayController 空壳 | PayService/Alipay沙箱集成/Mapper/Entity/MQ生产者 |
| mall-search | Application + Controller 空壳 | SearchService/ES客户端/Repository/DTO/VO |
| mall-seckill | Application + SeckillController 空壳 | SeckillService/Redis Lua预扣/MQ异步下单/Mapper/Entity/DTO/VO |
| mall-message | Application + Controller 空壳 | MQ Listener 集合（ORDER_CREATED/PAY_RESULT/SECKILL_REQUEST/STOCK_ROLLBACK/ES_SYNC） |
| mall-admin-biz | Application + Controller 空壳 | DashboardService/商品管理聚合/订单管理聚合/Feign Client |
| mall-job | Application + Controller 空壳 | 定时任务（订单超时取消/库存对账/ES全量同步） |

---

## 2. 技术栈锁定（不允许替换）

| 类别 | 选型 | 版本 |
|---|---|---|
| 语言 | Java | 17 |
| 框架 | Spring Boot | 3.2.x |
| 微服务 | Spring Cloud + Spring Cloud Alibaba | 2023.0.1 / 2023.0.1.0 |
| 注册/配置 | Nacos | 2.3.x |
| 网关 | Spring Cloud Gateway | 4.x |
| 服务调用 | OpenFeign + Sentinel 降级 | 4.x |
| 分布式事务 | Seata (AT 模式) | 1.8.x |
| 消息队列 | RocketMQ | 5.x |
| 缓存 | Redis | 7.x |
| 搜索 | Elasticsearch | 8.x |
| ORM | MyBatis-Plus | 3.5.x |
| 安全 | Spring Security + JWT (jjwt) | - |
| 前端 | Vue 3 + Vite + Element Plus | - |

---

## 3. 核心约束（违反即失败）

### 3.1 代码规范

```
- 包路径：com.mallcloud.mall{service}（如 com.mallcloud.mallorder）
- 注入方式：@RequiredArgsConstructor 构造器注入，禁止 @Autowired 字段注入
- 日志：@Slf4j + log.info/error，禁止 System.out.println
- 异常：业务异常用 BizException + ErrorCode 枚举，禁止空 catch
- 查询：禁止 SELECT *，禁止循环单条 INSERT
- Controller 层：只做参数校验 + 包装 Result<T>，业务逻辑下沉到 Service
- Service 层：返回值不用 Result<T>，直接返回 VO/DTO/布尔
- public 方法必须有 JavaDoc 注释
```

### 3.2 微服务边界

```
- 禁止直接调用其他服务的数据库，必须走 Feign 或 MQ
- 禁止跨服务改 Nacos 公共配置
- 禁止循环 Feign 依赖（A→B→A）
- 跨服务数据变更必须用 @GlobalTransactional（Seata）或 MQ 异步
- Redis 操作统一走 mall-common 中的封装
```

### 3.3 配置管理

```
- 公共配置走 Nacos：common-mysql.yaml / common-redis.yaml / common-rocketmq.yaml / common-seata.yaml / common-sentinel.yaml
- 服务私有配置走 Nacos：mall-{service}.yaml（你需要为每个服务创建并放到 deploy/nacos/）
- 本地 application.yaml 只放：spring.application.name, server.port, Nacos 地址, shared-dataids
- 敏感配置（JWT_SECRET/DB密码）走环境变量 ${}，不硬编码
```

### 3.4 前端纪律（若涉及前端任务）

```
- 零动效：禁止 transition/animation/@keyframes
- 色彩 Token 化：禁止硬编码 #hex/rgba，必须 var(--color-xxx)
- 无阴影：禁止 box-shadow，层级靠 1px 边框
- 样式作用域：业务 CSS 必须 <style scoped>
- API 路径：必须经 Gateway :9000
- 设计语言：白蓝线条极简风，详见 DESIGN.md
```

---

## 4. 服务开发顺序（推荐）

按依赖拓扑排序，先底层后上层：

```
第 1 批（无上游依赖）：
  1. mall-auth      — 认证（JWT 签发/刷新/黑名单）
  2. mall-user      — 用户/地址 CRUD
  3. mall-product   — 类目/SPU/SKU CRUD + 上下架
  4. mall-inventory — 库存预扣/扣减/释放

第 2 批（依赖第 1 批）：
  5. mall-cart      — Redis 购物车
  6. mall-order     — 下单（Seata 全局事务：调 inventory + pay）
  7. mall-pay       — 支付沙箱 + 支付回调 MQ

第 3 批（依赖第 2 批）：
  8. mall-search    — ES 搜索（消费 ES_SYNC 消息）
  9. mall-seckill   — Redis 预扣 + MQ 异步下单
  10. mall-message  — MQ Listener 集合

第 4 扡（聚合层）：
  11. mall-admin-biz — 后台聚合（Feign 调 product + order）
  12. mall-job      — 定时任务

第 5 扡（入口）：
  13. mall-gateway  — 路由 + JWT Filter 完善
```

---

## 5. 每个服务的开发模板

以 `mall-order` 为例，一个完整服务应包含：

```
mall-order/src/main/java/com/mallcloud/mallorder/
├── MallOrderApplication.java          # @SpringBootApplication + @EnableFeignClients + @EnableDiscoveryClient
├── api/
│   ├── dto/
│   │   ├── CreateOrderDTO.java        # 入参
│   │   └── OrderItemDTO.java
│   └── vo/
│       ├── OrderVO.java               # 出参
│       └── OrderListVO.java
├── client/                            # Feign Client 接口
│   ├── InventoryClient.java           # @FeignClient("mall-inventory")
│   ├── ProductClient.java
│   ├── UserClient.java
│   └── PayClient.java
├── controller/
│   └── OrderController.java           # @RestController, 只做校验+Result包装
├── service/
│   ├── OrderService.java              # 接口
│   └── impl/
│       └── OrderServiceImpl.java      # @Service, 业务逻辑, @GlobalTransactional
├── mapper/
│   ├── OrderMapper.java               # extends BaseMapper<OrderInfo>
│   └── OrderItemMapper.java
├── domain/
│   ├── OrderInfo.java                 # @TableName("order_info")
│   ├── OrderItem.java
│   └── OrderLog.java
└── config/
    └── OrderConfig.java               # 配置类（如需）
```

每个服务还需要：
- `src/main/resources/application.yaml`（本地配置）
- `deploy/nacos/mall-{service}.yaml`（Nacos 私有配置）

---

## 6. 关键业务流程实现要点

### 6.1 下单链路（Seata 全局事务）

```java
@GlobalTransactional(name = "create-order", rollbackFor = Exception.class)
public CreateOrderVO createOrder(CreateOrderDTO dto) {
    // 1. 校验用户（Feign → mall-user）
    // 2. 校验商品+价格（Feign → mall-product）
    // 3. 预扣库存（Feign → mall-inventory/lock）
    // 4. 创建订单（本地事务）
    // 5. 创建支付单（Feign → mall-pay/create）
    // 6. 发送 ORDER_CREATED 消息（MQ → message）
    return createOrderVO;
}
```

### 6.2 秒杀链路（Redis + MQ）

```java
// 1. Sentinel 限流（QPS 500）
// 2. Redis Lua 预扣库存（原子操作）
// 3. 用户限购检查（Redis SET NX）
// 4. 投递 SECKILL_REQUEST 消息到 MQ
// 5. 返回 requestId，客户端轮询结果
// 6. mall-message 消费 → 调 mall-order 创建订单
```

### 6.3 支付回调链路

```java
// 1. mall-pay 接收支付宝沙箱回调
// 2. 验签
// 3. 发送 PAY_RESULT 消息到 MQ
// 4. mall-order 消费 → 更新订单状态为"已支付"
// 5. mall-inventory 消费 → 确认扣减（从 locked → deducted）
```

### 6.4 Gateway JWT 鉴权

```java
// 白名单放行：/api/v1/auth/login, /api/v1/users/register, /api/v1/search/**, /api/v1/products/**, /api/v1/categories/**
// 非白名单：解析 Authorization: Bearer {token}
// 校验签名 + 过期时间
// 提取 userId/roles 写入请求头 X-User-Id / X-User-Roles
// 传递给下游服务
```

---

## 7. 验证标准（每个服务完成后必须通过）

### 7.1 编译验证

```powershell
# 编译单个服务
mvn clean compile -pl mall-order -am

# 编译全部
mvn clean install -DskipTests
# 期望：BUILD SUCCESS
```

### 7.2 服务注册验证

```powershell
# 启动服务后检查 Nacos
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=mall-order
# 期望：至少 1 个健康实例
```

### 7.3 接口验证（Postman / curl）

```powershell
# 1. 登录获取 Token
curl -X POST http://localhost:9000/api/v1/auth/login -H "Content-Type: application/json" -d '{"username":"zhangsan","password":"P@ssw0rd123","loginType":"PASSWORD"}'

# 2. 用 Token 访问
curl -H "Authorization: Bearer {token}" http://localhost:9000/api/v1/orders

# 3. 不带 Token → 期望 401
curl http://localhost:9000/api/v1/orders
```

### 7.4 硬约束扫描（每次提交前）

```powershell
# 0 命中才算通过
rg -c "System\.out\.println" --type java      # 必须 0
rg -c "@Autowired" --type java                # 必须 0
rg -c "SELECT \*" --type java                 # 必须 0
rg -c "catch\s*\(\s*Exception" --type java    # 检查是否空 catch
```

---

## 8. 提交规范

格式：`<type>(<scope>): <subject>`

```
feat(order): add create order with seata global transaction
fix(pay): fix alipay notify signature verification
feat(seckill): implement redis lua stock deduction
feat(gateway): complete jwt filter with whitelist
docs(api): add seckill polling endpoint
test(order): add create order unit test
```

提交频率：每完成一个服务或一个完整功能即提交。单次 commit < 400 行。

---

## 9. 文档同步要求

| 改动类型 | 必须同步的文档 |
|---|---|
| 新增/修改接口 | `docs/API.md` |
| 新增/修改表结构 | `docs/DATABASE.md` + `db/init/00-create-databases.sql` |
| 新增/修改 Nacos 配置 | `deploy/nacos/*.yaml` |
| 新增/修改部署方式 | `docs/DEPLOY.md` |
| 新增服务端口/依赖 | `docs/PRD.md` §6.2 |

---

## 10. 关键参考文档索引

| 文档 | 路径 | 核心内容 |
|---|---|---|
| 架构设计 | `docs/ARCHITECTURE.md` | Nacos/Gateway/Feign/Sentinel/Seata/RocketMQ/Redis/ES/JWT 设计细节 |
| 接口契约 | `docs/API.md` | 全部 API 路径/请求/响应/错误码，Postman 12 用例 |
| 数据库 | `docs/DATABASE.md` | 7 库 25 表结构 + ER 图 + 索引原则 |
| 编码规范 | `docs/CODING_STYLE.md` | 命名/注释/异常/日志/Git 提交规范 |
| 部署指南 | `docs/DEPLOY.md` | 本地开发/Docker Compose/K8s 三套部署方案 |
| 快速启动 | `docs/QUICK_START.md` | 5 分钟跑通全栈的步骤 |
| 设计规范 | `DESIGN.md` | 前端白蓝线条极简风 + Token 体系 + 零动效 |

---

## 11. 服务端口速查

| 服务 | 端口 | 数据库 | 核心职责 |
|---|---|---|---|
| mall-gateway | 9000 | - | 路由 + JWT 鉴权 + Sentinel 限流 |
| mall-auth | 9001 | mall_auth | 登录/注册/Token 签发/刷新/黑名单 |
| mall-user | 9002 | mall_user | 用户 CRUD / 地址管理 |
| mall-product | 9003 | mall_product | 类目/SPU/SKU / 上下架 / ES 同步 |
| mall-inventory | 9004 | mall_inventory | 库存预扣/扣减/释放/对账 |
| mall-cart | 9005 | Redis | 购物车（Redis Hash） |
| mall-order | 9006 | mall_order | 下单/取消/退款/订单状态机 |
| mall-pay | 9007 | mall_pay | 支付沙箱/回调/退款 |
| mall-search | 9008 | ES | 全文搜索/热词/聚合 |
| mall-seckill | 9009 | mall_seckill + Redis | 秒杀活动/Redis预扣/MQ异步下单 |
| mall-message | 9010 | - | MQ Listener 集合 |
| mall-admin-biz | 9011 | - | 后台聚合（Feign） |
| mall-job | 9012 | - | 定时任务 |

---

## 12. MQ Topic 速查

| Topic | 生产者 | 消费者 | 用途 |
|---|---|---|---|
| ORDER_CREATED | order | pay, message | 订单创建后触发支付+通知 |
| PAY_RESULT | pay | order, inventory | 支付结果→更新订单+确认库存 |
| SECKILL_REQUEST | seckill | order | 秒杀请求→异步创建订单 |
| STOCK_ROLLBACK | order | inventory | 订单取消→库存回滚 |
| ES_SYNC | product | search | 商品上下架→ES同步 |
| NOTIFY_MERCHANT | order | admin-biz | 新订单通知商家（延时消息） |

---

## 13. 测试账号

| 用户名 | 密码 | 角色 | 用途 |
|---|---|---|---|
| zhangsan | P@ssw0rd123 | USER | 前台购物测试 |
| lisi | P@ssw0rd123 | USER | 第二个用户测试 |
| merchant01 | P@ssw0rd123 | MERCHANT | 商家后台测试 |
| admin | Admin@123 | ADMIN | 管理员测试 |

---

## 14. 禁止行为清单

- ❌ 删改 `db/init/00-create-databases.sql` 已有内容（只在末尾追加）
- ❌ 把密钥/Token/密码 commit 到仓库
- ❌ 在 `application.yaml` 硬编码配置（必须走 Nacos + `${ENV}`）
- ❌ 用 `@Transactional` 跨服务调用（必须用 `@GlobalTransactional`）
- ❌ 在 Controller 里写业务逻辑
- ❌ 引入新依赖/新框架（除非被明确要求且已记录到 PRD/ARCHITECTURE）
- ❌ 用 `System.out.println`
- ❌ 空 catch 块
- ❌ `SELECT *`
- ❌ 前端使用 `transition`/`animation`/`box-shadow`/硬编码颜色

---

**—— Prompt 结束 ——**

> 读完本文件后，从第 1 批服务（mall-auth / mall-user / mall-product / mall-inventory）开始开发。
> 每完成一个服务，运行 `mvn clean compile -pl {service} -am` 验证编译，然后提交。
