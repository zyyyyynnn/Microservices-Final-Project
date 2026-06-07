# MallCloud 接口文档

> 文档版本：v2.0
> BaseURL：`http://localhost:9000`
> 上位标准：`docs/PROJECT_STANDARD.md`
> 说明：路径以当前 Controller 为基础；请求与响应仍需在代码整改后通过 Postman 最终确认。

---

## 1. 通用约定

### 1.1 请求头

| Header | 必填 | 说明 |
|---|---|---|
| `Authorization` | 受限接口必填 | `Bearer <accessToken>` |
| `Content-Type` | JSON 请求必填 | `application/json;charset=UTF-8` |
| `X-Trace-Id` | 否 | 链路定位 ID；最终行为以网关实现为准 |

Gateway 验证 JWT 后向下游透传：

- `X-User-Id`
- `X-User-Roles`

业务服务通过用户上下文读取用户 ID，不直接信任客户端自定义用户 Header。

### 1.2 白名单

当前 Gateway 白名单包括：

- `POST /api/v1/auth/login`
- `GET /api/v1/auth/captcha`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/users/register`
- `/api/v1/search/**`
- `/api/v1/products/**`
- `/api/v1/categories/**`

白名单接口不要求 Token。Postman 异常测试不得把“注册不带 Token”错误断言为 401。

### 1.3 统一响应

```json
{
  "code": 200,
  "message": "ok",
  "data": {},
  "traceId": "可选",
  "timestamp": 1717654321000
}
```

成功以 `code = 200` 为准。HTTP 状态码和业务码应同时断言。

### 1.4 主要错误码

| 业务码 | 含义 |
|---:|---|
| 10001 | 参数错误 |
| 10002 | 远程调用失败 |
| 10003 | 系统错误 |
| 20100 | 未登录 |
| 20101 | Token 过期 |
| 20102 | Token 无效 |
| 20103 | 无权限 |
| 30100 | 商品不存在 |
| 30101 | 商品已下架 |
| 40100 | 库存不足 |
| 40101 | 库存锁定失败 |
| 40200 | 订单不存在 |
| 40201 | 订单状态非法 |
| 40300 | 支付失败 |
| 40400 | 秒杀已售罄 |
| 40401 | 秒杀未开始 |
| 40402 | 秒杀已结束 |
| 40403 | 已达购买上限 |
| 50001 | 服务降级中 |
| 50002 | 请求过于频繁 |

完整定义以 `mall-common` 中的 `ErrorCode` 为准。

---

## 2. 认证接口

### 2.1 登录

```http
POST /api/v1/auth/login
Content-Type: application/json
```

请求：

```json
{
  "username": "zhangsan",
  "password": "P@ssw0rd123",
  "loginType": "PASSWORD"
}
```

成功响应应包含：

```json
{
  "code": 200,
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "expiresIn": 7200,
    "userInfo": {}
  }
}
```

Postman 后置脚本：

```javascript
const body = pm.response.json();
pm.expect(body.code).to.eql(200);
pm.environment.set("token", body.data.accessToken);
pm.environment.set("refreshToken", body.data.refreshToken);
```

### 2.2 刷新 Token

```http
POST /api/v1/auth/refresh
Content-Type: application/json
```

```json
{
  "refreshToken": "{{refreshToken}}"
}
```

### 2.3 退出

```http
POST /api/v1/auth/logout
Authorization: Bearer {{token}}
```

---

## 3. 用户接口

### 3.1 注册

```http
POST /api/v1/users/register
```

该接口属于白名单。最终请求字段以 `RegisterDTO` 为准，Postman 集合必须从实际 DTO 生成，禁止使用 `dummy` 请求体。

示例：

```json
{
  "username": "test_user_01",
  "phone": "13900000001",
  "password": "P@ssw0rd123"
}
```

### 3.2 当前用户

```http
GET /api/v1/users/me
Authorization: Bearer {{token}}
```

### 3.3 更新资料

```http
PUT /api/v1/users/me
Authorization: Bearer {{token}}
Content-Type: application/json
```

### 3.4 地址

```http
GET    /api/v1/users/me/addresses
POST   /api/v1/users/me/addresses
PUT    /api/v1/users/me/addresses/{id}
DELETE /api/v1/users/me/addresses/{id}
```

新增地址示例：

```json
{
  "receiver": "张三",
  "phone": "13800138001",
  "province": "北京市",
  "city": "北京市",
  "district": "海淀区",
  "detail": "中关村大街 1 号",
  "isDefault": true
}
```

---

## 4. 商品与类目接口

### 4.1 类目树

```http
GET /api/v1/categories/tree
```

### 4.2 商品列表

```http
GET /api/v1/products?categoryId=111&pageNum=1&pageSize=20
```

### 4.3 商品详情

```http
GET /api/v1/products/{spuId}
```

演示数据可使用：

```text
spuId = 1001
skuId = 9001
```

### 4.4 SKU 内部查询

订单服务通过 OpenFeign 查询 SKU。内部接口不作为外部用户接口，测试时应通过创建订单间接验证，避免绕过正常链路。

### 4.5 后台商品管理

```http
POST   /api/v1/admin/products
PUT    /api/v1/admin/products/{id}
POST   /api/v1/admin/products/{id}/on
POST   /api/v1/admin/products/{id}/off
DELETE /api/v1/admin/products/{id}
```

是否具备完整角色鉴权需在后续代码整改中验证。

---

## 5. 搜索接口

### 5.1 商品搜索

```http
GET /api/v1/search/products?keyword=iPhone&pageNum=1&pageSize=20
```

基础断言：

- HTTP 200；
- 业务码 200；
- `data` 不为空；
- 使用种子数据时结果应包含对应商品，前提是索引已初始化。

### 5.2 热词

```http
GET /api/v1/search/hot-words
```

### 5.3 内部同步

```http
POST /internal/search/products/{spuId}/sync?status=1
```

内部接口不通过外部 Gateway 主流程暴露时，测试应从服务内部或消息链路执行。

---

## 6. 购物车接口

### 6.1 加入购物车

```http
POST /api/v1/carts
Authorization: Bearer {{token}}
Content-Type: application/json
```

```json
{
  "skuId": 9001,
  "quantity": 1
}
```

### 6.2 查询购物车

```http
GET /api/v1/carts
Authorization: Bearer {{token}}
```

### 6.3 修改、选中和删除

```http
PUT    /api/v1/carts/{skuId}
PATCH  /api/v1/carts/{skuId}/check
DELETE /api/v1/carts/{skuId}
```

具体 PATCH 路径与请求体必须以实际 Controller 为准后再写入最终 Postman 集合。

---

## 7. 库存接口

库存外部接口主要用于调试；正式订单测试应通过 `mall-order → mall-inventory` OpenFeign 链路验证。

### 7.1 锁定库存

```http
POST /api/v1/inventory/lock
Authorization: Bearer {{token}}
```

当前订单服务向库存服务发送的是订单项列表。最终请求结构以 `InventoryController` 和 `LockDTO` 为准。

### 7.2 确认扣减

```http
POST /api/v1/inventory/deduct
```

### 7.3 释放库存

```http
POST /api/v1/inventory/release
```

### 7.4 查询库存

```http
GET /api/v1/inventory/stock/{skuId}
```

---

## 8. 订单接口

### 8.1 创建订单

```http
POST /api/v1/orders
Authorization: Bearer {{token}}
Content-Type: application/json
```

当前 `CreateOrderDTO`：

```json
{
  "addressId": 1,
  "items": [
    {
      "skuId": 9001,
      "quantity": 1
    }
  ],
  "remark": "课程测试订单"
}
```

创建订单会验证以下 OpenFeign 调用：

```text
mall-order → mall-product
mall-order → mall-inventory
```

成功响应应包含 `orderNo`。Postman 后置脚本：

```javascript
const body = pm.response.json();
pm.expect(body.code).to.eql(200);
pm.expect(body.data.orderNo).to.be.a("string");
pm.environment.set("orderNo", body.data.orderNo);
```

### 8.2 查询订单

```http
GET /api/v1/orders/{{orderNo}}
Authorization: Bearer {{token}}
```

### 8.3 当前实现限制

当前外部 `OrderController` 主要提供创建和按订单号查询。取消、确认、退款等接口只有在实际 Controller 存在并验证后才能加入最终接口清单。

---

## 9. 支付接口

支付采用模拟实现，不接入真实第三方平台。

```http
POST /api/v1/pay/create
POST /api/v1/pay/notify
GET  /api/v1/pay/record/{orderNo}
```

支付成功通知的核心验收结果：

1. 产生 `PAY_RESULT` 消息；
2. `mall-message` 消费消息；
3. `mall-order` 更新支付状态；
4. `mall-inventory` 确认扣减库存。

请求体必须以实际 Pay Controller/DTO 为准，不在未核对时虚构字段。

---

## 10. 秒杀接口

### 10.1 活动

```http
GET /api/v1/seckill/activities
GET /api/v1/seckill/activities/{id}
```

### 10.2 执行

```http
POST /api/v1/seckill/{activityId}
Authorization: Bearer {{token}}
```

### 10.3 查询结果

```http
GET /api/v1/seckill/result/{requestId}
Authorization: Bearer {{token}}
```

秒杀最终测试应验证：

- 活动时间；
- 用户限购；
- 库存边界；
- Sentinel 限流；
- 异步订单结果。

---

## 11. 后台和任务接口

### 11.1 后台

```http
POST /api/v1/admin/auth/login
GET  /api/v1/admin/dashboard
GET  /api/v1/admin/orders
GET  /api/v1/admin/products
POST /api/v1/admin/orders/{orderNo}/ship
```

后台功能为辅助能力，不作为核心链路测试重点。

### 11.2 定时任务内部接口

```http
POST /internal/jobs/orders/timeout/close
POST /internal/jobs/inventory/reconcile
GET  /internal/jobs/products/on-sale-spu-ids
```

内部接口需限制访问范围，不应无条件暴露到公网 Gateway。

---

## 12. 健康检查

```http
GET /actuator/health
GET /actuator/info
GET /actuator/prometheus
```

是否暴露 `prometheus` 取决于 Micrometer Registry 依赖和实际配置。没有对应依赖时只使用 health/info。

---

## 13. 最终 Postman 集合标准

旧的自动生成集合包含占位请求体和固定 Token，不作为最终测试证据。最终集合建议控制在 20～30 个真实请求。

### 13.1 必测请求

| # | 请求 | 目的 |
|---:|---|---|
| 1 | 登录成功 | 保存 Token |
| 2 | 登录失败 | 错误密码 |
| 3 | 商品列表 | 公共路由 |
| 4 | 商品详情 | 种子数据 |
| 5 | 搜索商品 | ES 查询 |
| 6 | 无 Token 查询订单 | Gateway 401 |
| 7 | 错误 Token 查询订单 | Gateway 401 |
| 8 | 当前用户 | Token 正常 |
| 9 | 地址列表 | 用户数据 |
| 10 | 加入购物车 | 写入 Redis |
| 11 | 查询购物车 | 验证商品远程调用 |
| 12 | 创建订单 | 验证商品和库存 Feign |
| 13 | 查询订单 | 保存的 orderNo |
| 14 | 库存不足订单 | 业务异常 |
| 15 | 创建支付 | 模拟支付 |
| 16 | 支付结果通知 | MQ 链路 |
| 17 | 再次查询订单 | 状态已更新 |
| 18 | 查询库存 | 确认扣减 |
| 19 | 秒杀活动 | 公共查询 |
| 20 | 秒杀请求 | 正常请求 |
| 21 | 重复秒杀 | 限购异常 |
| 22 | 后台登录 | 辅助验证 |

### 13.2 环境变量

```text
BaseURL=http://localhost:9000
token=
refreshToken=
orderNo=
requestId=
spuId=1001
skuId=9001
```

Token 和动态业务 ID 必须由前置请求自动写入，不提交固定伪 Token。

### 13.3 报告

建议命令：

```powershell
newman run .\docs\test\postman\mallcloud.postman_collection.json `
  -e .\docs\test\postman\local.postman_environment.json `
  -r cli,html `
  --reporter-html-export .\docs\test\postman\report.html
```

Newman 和 HTML Reporter 是否安装需在 `docs/test/README.md` 中明确。
