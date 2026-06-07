# MallCloud 编码规范

> 版本：v1.0.0
> 目标：统一团队编码风格，降低协作成本

---

## 1. 命名规范

### 1.1 包名

全部小写，结构清晰：

```
com.mallcloud
├── mall-common
│   ├── annotation
│   ├── constant
│   ├── enums
│   ├── exception
│   ├── response
│   └── util
├── mall-gateway
│   ├── config
│   ├── filter
│   └── MallGatewayApplication.java
├── mall-order
│   ├── api
│   │   ├── dto
│   │   └── vo
│   ├── client
│   ├── controller
│   ├── service
│   │   └── impl
│   ├── mapper
│   ├── domain
│   ├── config
│   └── MallOrderApplication.java
```

### 1.2 类名

| 类型          | 命名                       | 示例                       |
| ------------- | -------------------------- | -------------------------- |
| Controller    | XxxController              | OrderController            |
| Service       | XxxService / XxxServiceImpl| OrderService / OrderServiceImpl |
| Mapper        | XxxMapper                  | OrderMapper                |
| Entity        | Xxx / XxxDO / XxxPO        | OrderInfo / OrderDO        |
| DTO           | XxxDTO                     | CreateOrderDTO             |
| VO            | XxxVO                      | OrderVO                    |
| Client (Feign)| XxxClient                  | InventoryClient            |
| Config        | XxxConfig                  | RedisConfig                |
| 异常          | XxxException               | OrderException             |
| 常量          | XxxConstants               | OrderConstants             |
| 工具          | XxxUtil / XxxUtils         | JwtUtil                    |

### 1.3 变量 / 方法

- 变量：lowerCamelCase，避免缩写（`userName` 不是 `un`）；
- 常量：UPPER_SNAKE_CASE；
- 布尔：`isXxx` / `hasXxx` / `canXxx`；
- 方法动词：`get / list / query / create / update / delete / save / count / check / validate`；
- 集合：`list / map / set` 前缀，例 `userList`、`categoryMap`；
- 布尔方法：`is / has` 前缀，例 `isActive`、`hasPermission`。

### 1.4 数据库

- 表名：snake_case + 复数：`users` / `order_info` / `spu_images`；
- 字段：snake_case：`user_id` / `gmt_create`；
- 索引：`idx_xxx` / `ux_xxx`（unique）；
- 主键：`id`，外键 `{table}_id`。

---

## 2. 代码风格

### 2.1 IDE 统一

- IntelliJ IDEA + Alibaba Java Coding Guidelines 插件；
- 启用 EditorConfig，统一缩进 = 4 空格，UTF-8，120 列。

### 2.2 import

- 不使用通配符；
- 按顺序：java.* / javax.* / org.* / com.* / 项目内；
- 同包内合并。

### 2.3 注释

- 类注释：必须，含作者、日期、用途
  ```java
  /**
   * 订单服务
   *
   * @author zhangsan
   * @since 2026-03-01
   */
  ```
- 方法注释：public 方法必须，含参数、返回值、说明
  ```java
  /**
   * 创建订单（Seata 全局事务）
   *
   * @param dto 下单参数
   * @return 订单号 + 支付链接
   * @throws BizException 库存不足/价格错误
   */
  ```
- 字段注释：枚举 / 常量必加；
- 行内注释：仅解释"为什么"而非"做了什么"。

### 2.4 异常处理

- 业务异常：`BizException` + 错误码；
- 不在 Controller 捕获所有异常，由 `@RestControllerAdvice` 统一处理；
- 不使用 `Exception`/`Throwable` 捕获（除非框架入口）；
- 严禁 `catch (Exception e) { e.printStackTrace(); }`。

### 2.5 日志

```java
log.info("创建订单 orderNo={}, userId={}, amount={}", orderNo, userId, amount);
log.error("订单创建失败 orderNo={}", orderNo, e);
```

- 用占位符 `{}`，不用 `+` 拼接；
- 禁止在循环中打 INFO；
- 敏感字段（密码、身份证、token）脱敏。

### 2.6 集合初始化

```java
// 显式指定容量
List<Order> list = new ArrayList<>(16);
Map<Long, Order> map = new HashMap<>(16);
```

### 2.7 避免魔法值

```java
// 错误
if (status == 1) { ... }

// 正确
if (status == OrderStatus.PAID.getCode()) { ... }
```

---

## 3. RESTful API 规范

### 3.1 路径

- 全小写，连字符分隔：`/api/v1/order-items`；
- 复数资源：`/api/v1/orders`；
- 不要在路径中暴露 ID 类型：`/api/v1/orders/{id}`（id 在内层指明）；
- 嵌套资源用次级路径：`/api/v1/orders/{id}/items`。

### 3.2 方法语义

| 操作     | 方法   | 路径                                | Body     |
| -------- | ------ | ----------------------------------- | -------- |
| 列表     | GET    | /api/v1/orders                      | -        |
| 详情     | GET    | /api/v1/orders/{id}                 | -        |
| 创建     | POST   | /api/v1/orders                      | DTO      |
| 全量更新 | PUT    | /api/v1/orders/{id}                 | DTO      |
| 部分更新 | PATCH  | /api/v1/orders/{id}                 | partial  |
| 删除     | DELETE | /api/v1/orders/{id}                 | -        |
| 子操作   | POST   | /api/v1/orders/{id}/cancel          | reason   |

### 3.3 返回值

```java
@GetMapping("/{id}")
public Result<OrderVO> getById(@PathVariable Long id) { ... }
```

- 全部接口返回 `Result<T>`；
- 分页返回 `Result<PageData<T>>`；
- 异常由全局处理返回。

---

## 4. 微服务通信规范

### 4.1 Feign Client

- 放在 `client` 包，命名 `XxxClient`；
- 必须指定 `fallbackFactory`；
- 不在 Feign 接口中写业务逻辑；
- 入参 / 出参用 DTO/VO，不直接用 DO。

### 4.2 MQ

- Topic 常量集中在 `MqTopic` 类；
- 生产者封装在 `mall-common-mq`；
- 消费者独立 Listener 类，标注 `@RocketMQMessageListener`；
- 消息体实现 `Serializable`，含 `Message` 基类（traceId、bizType、timestamp）。

---

## 5. 数据库规范

### 5.1 命名

- 表：snake_case 复数：`users` / `order_items`；
- 主键：`id BIGINT AUTO_INCREMENT`；
- 必备字段：`gmt_create DATETIME` / `gmt_modified DATETIME`；
- 软删除：`is_deleted TINYINT DEFAULT 0`；
- 金额：`DECIMAL(12,2)`，不用 FLOAT；
- 状态：TINYINT + 注释枚举。

### 5.2 索引

- 单表索引 ≤ 5；
- 联合索引字段 ≤ 4；
- 区分度低的字段（status、is_deleted）不单独建索引；
- TEXT / JSON 字段建全文索引需 MySQL 5.7+。

### 5.3 禁止

- `SELECT *`；
- `WHERE` 条件中对字段做函数运算；
- 循环中单条 INSERT（改批量）；
- 大事务（超过 1s）；
- DDL 在业务库执行（必须走 `db/init/00-create-databases.sql` 修改 + 迁移脚本）。

---

## 6. 安全规范

- 密码 BCrypt（强度 10）；
- 敏感字段 AES 加密；
- SQL 全部用 `#{}` 参数化；
- 外部输入做白名单校验；
- 文件上传校验 Content-Type + 后缀 + 大小；
- 接口加幂等（`@Idempotent`）；
- 防重放：Token + 时间戳 + 签名。

---

## 7. 测试规范

### 7.1 单元测试

- JUnit 5 + Mockito；
- 命名：`methodName_stateUnderTest_expectedBehavior`；
- 例：`createOrder_stockNotEnough_throwBizException`；
- 覆盖率：核心 Service ≥ 60%。

### 7.2 集成测试

- `@SpringBootTest` + Testcontainers（MySQL/Redis）；
- 关键路径：下单全链路、秒杀全链路。

### 7.3 不写测试

- 单纯的 getter / setter；
- 简单 DTO 转换；
- 配置类。

---

## 8. Git 规范

### 8.1 分支

```
main            主干（受保护）
develop         集成分支
feature/xxx     功能分支
hotfix/xxx      紧急修复
release/v1.0.0  发布分支
```

### 8.2 Commit

格式：`<type>(<scope>): <subject>`

```
feat(order): add create order api
fix(pay): fix notify signature
docs(readme): update deploy steps
refactor(common): extract result util
test(order): add create order test
chore(deps): upgrade spring boot to 3.2.4
```

### 8.3 提交频率

- 每天至少 1 次；
- 单次 commit 改动 < 400 行；
- 提交前跑 `mvn test`。

### 8.4 Code Review

- PR 至少 1 人 approve；
- 关键模块（订单/支付/秒杀）必须 2 人 approve。

---

## 9. 文档规范

- 公共 API 必须在 `API.md` 中维护；
- 库表变更必须更新 `DATABASE.md` + `00-create-databases.sql` + 迁移脚本；
- 部署变更必须更新 `DEPLOY.md`；
- 复杂业务必须有流程图 / 时序图（Mermaid）。

---

## 10. 提交检查清单（PR 前自检）

- [ ] 代码格式化（Alibaba 规范）
- [ ] 编译通过
- [ ] 单元测试通过，新功能补测试
- [ ] 公共方法有注释
- [ ] 异常用 `BizException` + 错误码
- [ ] 日志用 `{}` 占位符
- [ ] 敏感信息已脱敏
- [ ] 涉及 Nacos 配置的更新配置中心
- [ ] 涉及 DB 的有迁移脚本
- [ ] 涉及接口的有 Postman 用例
- [ ] 涉及部署的有文档更新

---

**—— 文档结束 ——**
