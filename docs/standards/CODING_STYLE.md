# MallCloud 编码与配置规范

> 文档版本：v2.0
> 文档状态：生效
> 上位标准：`docs/standards/PROJECT_STANDARD.md`

本规范只保留当前项目能够执行的要求。推荐项和后续目标不会写成强制现状。

---

## 1. 基本原则

1. 先修复正确性，再做结构优化；
2. 只修改与当前任务直接相关的代码；
3. 不新增无评分价值的功能和依赖；
4. 不为展示设计模式而过度抽象；
5. 核心失败必须明确返回，不伪造成功兜底；
6. 文档、接口、数据库、配置和代码必须同步；
7. 无法验证时明确标记“未验证”。

---

## 2. 项目结构

业务服务推荐结构：

```text
com.mallcloud.<service>
├── api
│   ├── dto
│   └── vo
├── client
│   └── dto
├── config
├── controller
├── domain
├── mapper
├── service
│   └── impl
├── consumer
├── job
└── <Service>Application.java
```

要求：

- Controller 只处理参数、用户上下文和响应；
- Service 承担业务逻辑和事务；
- Mapper 只处理本服务数据库；
- Feign Client 放在 `client`；
- MQ Listener 放在 `consumer`；
- DTO 与 VO 不直接复用数据库实体。

不要求所有服务机械创建空目录。

---

## 3. 命名规范

### 3.1 Java

| 类型 | 规范 | 示例 |
|---|---|---|
| 类 | PascalCase | `OrderServiceImpl` |
| 方法/变量 | camelCase | `createOrder` |
| 常量 | UPPER_SNAKE_CASE | `HEADER_USER_ID` |
| Controller | `XxxController` | `OrderController` |
| Service 接口 | `XxxService` | `OrderService` |
| Service 实现 | `XxxServiceImpl` | `OrderServiceImpl` |
| Mapper | `XxxMapper` | `OrderInfoMapper` |
| Feign | `XxxClient` | `InventoryClient` |
| 请求对象 | `XxxDTO` | `CreateOrderDTO` |
| 响应对象 | `XxxVO` | `CreateOrderVO` |

### 3.2 数据库

现有项目使用单数和业务复合表名，例如：

```text
user
address
spu
sku
order_info
order_item
pay_record
```

后续保持现有命名风格，不为了统一复数规则批量重命名表。

字段使用 snake_case：

```text
user_id
gmt_create
order_no
```

---

## 4. Java 代码规范

### 4.1 依赖注入

优先使用构造器注入：

```java
@RequiredArgsConstructor
@Service
public class OrderServiceImpl {
    private final OrderInfoMapper orderInfoMapper;
}
```

禁止新增字段级 `@Autowired`，已有代码可在相关修改时逐步修正，不单独大范围重构。

### 4.2 空值与返回值

远程调用必须同时检查返回对象、业务状态和数据：

```java
Result<SkuDTO> result = productClient.getSku(skuId);
if (result == null || !result.isSuccess() || result.getData() == null) {
    throw new BizException(ErrorCode.REMOTE_CALL_ERROR);
}
```

禁止：

- 忽略 `Result` 业务码；
- 发生失败后返回伪造业务数据；
- 使用空 `catch`；
- 只记录日志但继续写入不完整数据。

### 4.3 异常

- 业务异常使用 `BizException`；
- 错误码优先使用 `ErrorCode`；
- 全局异常处理统一生成 `Result`；
- 日志中记录必要业务标识，不输出密码和完整 Token；
- 不捕获后吞掉参与事务的异常。

### 4.4 集合与循环

- 空集合返回 `Collections.emptyList()` 或等价不可变空集合；
- 批量操作优先批量 SQL，但不为少量课程演示数据过度优化；
- 循环内远程调用应评估 N+1 问题，核心链路可在后续根据压测结果优化。

---

## 5. Controller 规范

Controller 示例：

```java
@PostMapping
public Result<CreateOrderVO> create(@Valid @RequestBody CreateOrderDTO dto) {
    Long userId = UserContext.getUserId();
    if (userId == null) {
        throw new BizException(ErrorCode.UNAUTHORIZED);
    }
    return Result.ok(orderService.createOrder(userId, dto));
}
```

要求：

- 使用明确 HTTP 方法；
- 使用 `@Valid` 校验请求；
- 不在 Controller 中访问 Mapper；
- 不在 Controller 中编写复杂状态机；
- 外部接口统一使用 `/api/v1`；
- 内部接口使用 `/internal`，并在部署时限制暴露范围。

---

## 6. Service 与事务

### 6.1 本地事务

单服务多表写入使用 Spring 本地事务。

### 6.2 分布式事务

跨服务一致性确实需要同步事务时使用 Seata：

```java
@GlobalTransactional(name = "create-order", rollbackFor = Exception.class)
```

要求：

- 文档中的参与者必须与实际远程调用一致；
- 不把普通 `@Transactional` 描述为跨服务事务；
- 不捕获异常后返回成功；
- 必须通过失败测试证明回滚。

不要求所有跨服务流程使用 Seata。支付通知、搜索同步和秒杀异步下单可使用 RocketMQ 最终一致性。

---

## 7. OpenFeign 规范

### 7.1 基本要求

- Client 放在 `client` 包；
- 路径与下游 Controller 一致；
- 入参和出参使用 DTO/VO；
- 写操作不得无条件自动重试；
- 调用方必须检查失败结果。

### 7.2 fallback

不是所有 Feign Client 都强制配置 fallbackFactory。

必须优先评估：

- `order → product`
- `order → inventory`
- `message → order`
- `message → inventory`

适合 fallback 的场景：

- 明确返回“服务暂不可用”；
- 查询可返回空结果且不会导致错误写入；
- 能保证状态不被误判为成功。

不适合：

- 库存锁定失败后仍继续创建订单；
- 支付状态更新失败后返回成功；
- 使用固定数据掩盖下游故障。

---

## 8. RocketMQ 规范

- Topic 名集中管理；
- Listener 独立成类；
- 消息至少包含业务唯一标识；
- 解析失败应记录 Topic、业务标识和异常；
- 消费逻辑必须考虑重复消息；
- 当前未实现事务消息时，不使用 `@RocketMQTransactionListener` 示例作为现状；
- 不为课程项目默认增加复杂本地消息表。

建议消息结构：

```json
{
  "messageId": "uuid",
  "bizType": "PAY_RESULT",
  "bizId": "SO202606070001",
  "timestamp": 1780800000000,
  "data": {}
}
```

是否统一改造消息结构应由核心链路整改任务决定，不单独扩大范围。

---

## 9. Sentinel 规范

- 只对核心资源配置规则；
- 资源名与实际请求或注解一致；
- 没有 `@SentinelResource` 时不在文档中声称已使用；
- 限流返回明确业务码或 HTTP 状态；
- 熔断测试必须模拟真实下游超时或失败；
- 阈值通过 JMeter 结果确定，不凭经验写死为最终值。

---

## 10. 数据库规范

- 业务服务只操作自己的数据库；
- SQL 使用参数绑定；
- 金额使用 DECIMAL/BigDecimal；
- 业务唯一标识建立唯一索引；
- 状态变更必须有条件更新，避免并发重复处理；
- 不直接手工修改共享数据库后不留迁移记录；
- DDL 以 `db/init/00-create-databases.sql` 为准；
- 不为了规范统一批量改现有表名和字段名。

### 10.1 幂等写入

优先使用数据库状态或唯一键保证幂等：

```sql
UPDATE order_info
SET status = 1
WHERE order_no = ? AND status = 0;
```

不强制引入自定义 `@Idempotent` 注解。只有多个接口确实需要统一能力时再抽象。

---

## 11. Redis 规范

- Key 使用业务前缀；
- 设置合理 TTL；
- Token 黑名单 TTL 等于 Token 剩余有效期；
- 秒杀原子逻辑使用 Lua 或 Redis 原子命令；
- 不在文档中声明不存在的 Redis 宕机自动查 DB；
- 序列化方式在公共配置中统一。

示例：

```text
mall:jwt:blacklist:{jti}
mall:cart:{userId}
mall:seckill:stock:{activityId}
mall:seckill:user:{activityId}:{userId}
```

---

## 12. 安全规范

### 强制要求

- 密码使用 BCrypt；
- JWT 算法与代码一致，当前为 HS512；
- Auth 与 Gateway 使用相同密钥；
- 生产和测试密钥通过环境变量或 Secret 注入；
- 日志不输出密码、完整 Token 和敏感个人信息；
- 外部请求进行基础参数校验；
- SQL 参数化。

### 待实现或按需实现

以下内容未验证前只能作为后续目标：

- 身份证 AES 落库；
- 通用防重放签名；
- 自定义 `@Idempotent`；
- 方法级 `@PreAuthorize` 权限体系；
- 密钥轮换平台。

---

## 13. 配置规范

- YAML 注释使用 `#`；
- 禁止在 YAML 中使用 SQL 注释 `--`；
- 环境变量名称统一；
- 不提交真实 Token 和密钥；
- 不写入个人绝对路径；
- 本地 `application.yaml` 与 Nacos 配置职责清晰；
- 配置加载方式必须与 Spring Cloud Alibaba 当前版本兼容并实际验证。

---

## 14. 日志规范

日志重点记录：

- 用户 ID；
- 订单号；
- SKU ID；
- requestId；
- Topic；
- 远程服务名；
- 失败原因。

示例：

```java
log.info("创建订单成功 userId={} orderNo={}", userId, orderNo);
log.error("库存服务调用失败 orderNo={} items={}", orderNo, items, cause);
```

避免：

```java
log.info("进入方法");
log.error("失败");
```

---

## 15. 测试规范

### 15.1 单元测试

优先覆盖：

- 订单金额计算和库存不足；
- 支付结果状态更新；
- 库存重复扣减/释放；
- 秒杀重复请求；
- Gateway JWT 白名单和错误 Token。

不要求简单 DTO、getter/setter、纯配置类测试。

### 15.2 集成测试

核心集成测试可使用实际 Docker 中间件。Testcontainers 只有在时间和环境允许时引入，不再作为已完成要求。

### 15.3 覆盖率

不预设虚假的“全部核心 Service 60%”现状。最终报告记录真实覆盖率，并优先保证关键分支有测试。

### 15.4 Maven

执行测试：

```powershell
mvn clean test -DskipTests=false
```

构建：

```powershell
mvn clean package -DskipTests
```

后续应取消父 POM 默认 `skipTests=true`，但该修改属于代码整改阶段。

---

## 16. Git 规范

分支建议：

```text
main
docs/rewrite-final-standard
fix/<scope>
feat/<scope>
test/<scope>
```

Commit：

```text
<type>(<scope>): <subject>
```

示例：

```text
docs(architecture): align order transaction flow
fix(config): replace invalid nacos yaml comments
test(order): add stock rollback scenario
```

每次提交只处理一个清晰主题，不混入无关重构。

---

## 17. 代码评审清单

- [ ] 修改直接对应需求；
- [ ] 未新增不必要依赖；
- [ ] 接口与 DTO 一致；
- [ ] 远程失败不会继续写错误状态；
- [ ] 事务边界明确；
- [ ] 配置合法且无个人路径；
- [ ] 核心日志可定位；
- [ ] 已编译或测试；
- [ ] 文档已同步；
- [ ] 未验证项已明确记录。
