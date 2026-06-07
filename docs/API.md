# MallCloud 接口文档

> 版本：v1.0.0
> BaseURL：`http://localhost:9000`
> 鉴权：除白名单外，所有接口需要在 Header 携带 `Authorization: Bearer {token}`

---

## 0. 通用约定

### 0.1 公共 Header

| Header            | 必填 | 说明                                |
| ----------------- | ---- | ----------------------------------- |
| Authorization     | 是*  | `Bearer {accessToken}`              |
| X-Trace-Id        | 否   | 链路追踪 ID，不传则网关生成         |
| Content-Type      | 是   | `application/json;charset=UTF-8`    |
| Accept-Language   | 否   | `zh-CN` / `en-US`                   |

网关验证 JWT 后向下游透传 `X-User-Id` 和 `X-User-Roles`，业务服务不直接解析客户端 Token。

### 0.2 统一响应

```json
{
  "code": 200,
  "message": "ok",
  "data": { ... },
  "traceId": "abc123",
  "timestamp": 1717654321000
}
```

### 0.3 业务错误码

> 与 `mall-common/.../enums/ErrorCode.java` 枚举值一一对应。**响应中 `code` 字段统一使用 200 表示成功**（`Result.CODE_SUCCESS`），枚举内部 `SUCCESS(0)` 仅用于内部逻辑判断。

| 码     | 含义               | 备注                           |
| ------ | ------------------ | ------------------------------ |
| 200    | 成功               | Result.CODE_SUCCESS             |
| 10001  | 参数校验失败        | message 含字段名                |
| 10002  | 远程调用失败        |                                |
| 10003  | 系统内部错误        | 联系运维                       |
| 20100  | 未登录              | 跳转登录                       |
| 20101  | Token 过期          | 用 refreshToken 刷新            |
| 20102  | Token 无效          | 重新登录                       |
| 20103  | 无权限              |                                |
| 30100  | 商品不存在          |                                |
| 30101  | 商品已下架          |                                |
| 40100  | 库存不足            |                                |
| 40101  | 库存锁定失败        |                                |
| 40200  | 订单不存在          |                                |
| 40201  | 订单状态非法        |                                |
| 40202  | 分布式事务失败      | 自动回滚                       |
| 40300  | 支付失败            |                                |
| 40400  | 秒杀已售罄          |                                |
| 40401  | 秒杀未开始          |                                |
| 40402  | 秒杀已结束          |                                |
| 40403  | 已达购买上限        | 同一用户限购                    |
| 50001  | 服务降级中          | 提示稍后重试                    |
| 50002  | 请求过于频繁        | 限流触发                       |

### 0.4 分页参数

```
GET /api/v1/xxx?pageNum=1&pageSize=20&sort=createTime,desc
```

分页响应：
```json
{
  "code": 200,
  "data": {
    "list": [...],
    "total": 156,
    "pageNum": 1,
    "pageSize": 20,
    "hasNext": true
  }
}
```

---

## 1. 认证服务 mall-auth

### 1.1 用户登录

```
POST /api/v1/auth/login
```

请求：
```json
{
  "username": "zhangsan",
  "password": "P@ssw0rd123",
  "loginType": "PASSWORD"   // PASSWORD / SMS
}
```

响应：
```json
{
  "code": 200,
  "data": {
    "accessToken": "eyJhbGc...",
    "refreshToken": "eyJhbGc...",
    "expiresIn": 7200,
    "userInfo": {
      "userId": 1001,
      "username": "zhangsan",
      "nickname": "张三",
      "avatar": "https://...",
      "roles": ["USER"]
    }
  }
}
```

### 1.2 刷新 Token

```
POST /api/v1/auth/refresh
```

请求：
```json
{ "refreshToken": "eyJhbGc..." }
```

### 1.3 退出登录

```
POST /api/v1/auth/logout
Header: Authorization: Bearer xxx
```

---

## 2. 用户服务 mall-user

### 2.1 注册

```
POST /api/v1/users/register
```

请求：
```json
{
  "username": "zhangsan",
  "phone": "13800138000",
  "password": "P@ssw0rd123",
  "smsCode": "654321"
}
```

### 2.2 获取当前用户

```
GET /api/v1/users/me
```

### 2.3 更新用户资料

```
PUT /api/v1/users/me
```

```json
{
  "nickname": "张三丰",
  "avatar": "https://...",
  "email": "zs@example.com"
}
```

### 2.4 收货地址

#### 列表

```
GET /api/v1/users/me/addresses
```

#### 新增

```
POST /api/v1/users/me/addresses
```

```json
{
  "receiver": "张三",
  "phone": "13800138000",
  "province": "北京市",
  "city": "北京市",
  "district": "海淀区",
  "detail": "中关村大街1号",
  "isDefault": true
}
```

#### 更新 / 删除

```
PUT    /api/v1/users/me/addresses/{id}
DELETE /api/v1/users/me/addresses/{id}
```

---

## 3. 商品服务 mall-product

### 3.1 类目树

```
GET /api/v1/categories/tree
```

响应：
```json
{
  "code": 200,
  "data": [
    {
      "id": 1, "name": "手机数码",
      "children": [
        { "id": 11, "name": "手机通讯" }
      ]
    }
  ]
}
```

### 3.2 SPU 列表（按类目）

```
GET /api/v1/products?categoryId=11&pageNum=1&pageSize=20
```

### 3.3 SPU 详情

```
GET /api/v1/products/{spuId}
```

响应：
```json
{
  "code": 200,
  "data": {
    "spuId": 1001,
    "name": "iPhone 15 Pro 256G",
    "description": "...",
    "mainImage": "https://...",
    "categoryId": 11,
    "brand": "Apple",
    "status": 1,
    "sales": 1234,
    "skus": [
      {
        "skuId": 9001,
        "spec": "钛原色/256G",
        "price": 8999.00,
        "stock": 100,
        "image": "https://..."
      }
    ],
    "attrs": [
      { "name": "颜色", "value": "钛原色" },
      { "name": "版本", "value": "256G" }
    ]
  }
}
```

### 3.4 商家上下架

```
POST   /api/v1/admin/products           创建
PUT    /api/v1/admin/products/{id}      更新
DELETE /api/v1/admin/products/{id}      删除
POST   /api/v1/admin/products/{id}/on   上架
POST   /api/v1/admin/products/{id}/off  下架
```

---

## 4. 搜索服务 mall-search

### 4.1 商品搜索

```
GET /api/v1/search/products?keyword=iPhone&categoryId=11&minPrice=1000&maxPrice=10000&sort=sales,desc&pageNum=1&pageSize=20
```

参数：

| 参数      | 类型   | 必填 | 说明                       |
| --------- | ------ | ---- | -------------------------- |
| keyword   | string | 否   | 关键字                     |
| categoryId| long   | 否   | 类目 ID                    |
| minPrice  | decimal| 否   | 最低价                     |
| maxPrice  | decimal| 否   | 最高价                     |
| sort      | string | 否   | `price,asc` / `sales,desc` / `_score` |
| pageNum   | int    | 否   | 默认 1                     |
| pageSize  | int    | 否   | 默认 20                    |

响应：
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "spuId": 1001,
        "name": "iPhone 15 Pro",
        "highlightName": "<em>iPhone</em> 15 Pro",
        "price": 8999.00,
        "sales": 1234,
        "mainImage": "https://..."
      }
    ],
    "total": 567,
    "aggregations": {
      "categories": [
        { "id": 11, "name": "手机通讯", "count": 234 }
      ],
      "priceRanges": [
        { "key": "0-1000", "count": 100 },
        { "key": "1000-5000", "count": 200 }
      ]
    }
  }
}
```

### 4.2 搜索热词

```
GET /api/v1/search/hot-words
```

### 4.3 内部 ES 同步

```
POST /internal/search/products/{spuId}/sync?status=1
```

---

## 5. 库存服务 mall-inventory

### 5.1 预扣库存

```
POST /api/v1/inventory/lock
```

请求：
```json
{
  "orderNo": "SO202606060001",
  "items": [
    { "skuId": 9001, "quantity": 2 }
  ]
}
```

响应：
```json
{ "code": 200, "data": true }
```

异常：`40100 库存不足`

### 5.2 扣减库存

```
POST /api/v1/inventory/deduct
```

### 5.3 释放库存

```
POST /api/v1/inventory/release
```

### 5.4 查询库存

```
GET /api/v1/inventory/stock/{skuId}
```

---

## 6. 购物车服务 mall-cart

### 6.1 加入购物车

```
POST /api/v1/carts
```

```json
{
  "skuId": 9001,
  "quantity": 2
}
```

### 6.2 购物车列表

```
GET /api/v1/carts
```

### 6.3 修改数量

```
PUT /api/v1/carts/{skuId}
{ "quantity": 5 }
```

### 6.4 选中/取消

```
PATCH /api/v1/carts/{skuId}
{ "selected": true }
```

### 6.5 删除

```
DELETE /api/v1/carts/{skuId}
```

---

## 7. 订单服务 mall-order

### 7.1 创建订单

```
POST /api/v1/orders
```

请求：
```json
{
  "addressId": 2001,
  "items": [
    { "skuId": 9001, "quantity": 2 }
  ],
  "remark": "尽快发货"
}
```

响应：
```json
{
  "code": 200,
  "data": {
    "orderNo": "SO202606060001",
    "totalAmount": 17998.00,
    "payUrl": "alipays://...",
    "expireTime": 1717657921000
  }
}
```

### 7.2 订单详情

```
GET /api/v1/orders/{orderNo}
```

### 7.3 订单列表

```
GET /api/v1/orders?status=0&pageNum=1&pageSize=20
```

状态：`0=待支付 1=已支付 2=已发货 3=已完成 4=已取消 5=已退款`

### 7.4 取消订单

```
POST /api/v1/orders/{orderNo}/cancel
```

### 7.5 确认收货

```
POST /api/v1/orders/{orderNo}/confirm
```

### 7.6 申请退款

```
POST /api/v1/orders/{orderNo}/refund
{ "reason": "不想要了" }
```

### 7.7 内部订单回写

```
POST /internal/orders/{orderNo}/paid
POST /internal/orders/seckill
```

`/internal/orders/seckill` 请求：
```json
{
  "requestId": "1:2:req",
  "activityId": 1,
  "userId": 2,
  "skuId": 9003,
  "quantity": 1,
  "seckillPrice": 4799.00
}
```

响应：
```json
{
  "code": 200,
  "data": {
    "orderNo": "SK202606070001"
  }
}
```

---

## 8. 支付服务 mall-pay

### 8.1 发起支付

```
POST /api/v1/pay/create
```

```json
{
  "orderNo": "SO202606060001",
  "payChannel": "ALIPAY"   // ALIPAY / WECHAT
}
```

响应：
```json
{
  "code": 200,
  "data": {
    "payNo": "PAY202606060001",
    "payUrl": "https://openapi.alipaydev.com/gateway.do?...",
    "payFormHtml": "<form>...</form>"
  }
}
```

### 8.2 支付回调（沙箱）

```
POST /api/v1/pay/notify
```

### 8.3 支付记录查询

```
GET /api/v1/pay/record/{orderNo}
```

---

## 9. 秒杀服务 mall-seckill

### 9.1 秒杀活动列表

```
GET /api/v1/seckill/activities
```

响应：
```json
{
  "code": 200,
  "data": [
    {
      "activityId": 1,
      "name": "iPhone 15 限时秒杀",
      "startTime": "2026-06-08 10:00:00",
      "endTime": "2026-06-08 12:00:00",
      "status": 0   // 0=未开始 1=进行中 2=已结束
    }
  ]
}
```

### 9.2 活动详情

```
GET /api/v1/seckill/activities/{id}
```

响应：
```json
{
  "code": 200,
  "data": {
    "activityId": 1,
    "name": "iPhone 15 限时秒杀",
    "skuId": 9003,
    "seckillPrice": 4799.00,
    "totalStock": 100,
    "limitPerUser": 1,
    "startTime": "2026-06-08 10:00:00",
    "endTime": "2026-06-08 12:00:00",
    "status": 0
  }
}
```

### 9.3 秒杀下单

```
POST /api/v1/seckill/{activityId}
```

请求：
```json
{
  "skuId": 9001,
  "quantity": 1
}
```

响应：
```json
{
  "code": 200,
  "data": {
    "requestId": "req-uuid",
    "resultUrl": "/api/v1/seckill/result/{requestId}"
  }
}
```

> 客户端轮询 `resultUrl` 获取最终结果（订单号 / 失败原因）。

### 9.4 轮询结果

```
GET /api/v1/seckill/result/{requestId}
```

响应：
```json
{
  "code": 200,
  "data": {
    "status": 1,             // 0=排队中 1=成功 2=失败
    "orderNo": "SK...",
    "message": "秒杀成功"
  }
}
```

---

### 9.5 内部秒杀结果回写

```
POST /internal/seckill/result/{requestId}/success?orderNo=SK...
POST /internal/seckill/result/{requestId}/fail?reason=...
```

---

## 10. 后台服务 mall-admin-biz

### 10.1 后台登录

```
POST /api/v1/admin/auth/login
```

（与普通登录相同，但账号必须具有 MERCHANT 角色）

### 10.2 数据看板

```
GET /api/v1/admin/dashboard
```

响应：
```json
{
  "code": 200,
  "data": {
    "todayOrders": 156,
    "todaySales": 256789.00,
    "totalProducts": 89,
    "pendingOrders": 12,
    "salesTrend": [
      { "date": "06-01", "amount": 12345 },
      { "date": "06-02", "amount": 15678 }
    ],
    "topProducts": [
      { "spuId": 1001, "name": "iPhone 15 Pro", "sales": 45 }
    ]
  }
}
```

### 10.3 商家订单列表

```
GET /api/v1/admin/orders?status=1&pageNum=1&pageSize=20
```

### 10.4 商家商品列表

```
GET /api/v1/admin/products?keyword=iPhone&status=1&pageNum=1&pageSize=20
```

### 10.5 商家发货

```
POST /api/v1/admin/orders/{orderNo}/ship
{ "expressNo": "SF1234567890", "expressCompany": "顺丰" }
```

### 10.6 内部聚合接口

```
GET  /internal/admin/orders/stats
GET  /internal/admin/orders?status=1&pageNum=1&pageSize=20
POST /internal/admin/orders/{orderNo}/ship
GET  /internal/admin/products/stats
GET  /internal/admin/products?keyword=iPhone&status=1&pageNum=1&pageSize=20
```

---

## 11. 公共 / 基础设施接口

### 11.1 健康检查

```
GET /actuator/health
```

### 11.2 服务元信息

```
GET /actuator/info
```

### 11.3 Prometheus 指标

```
GET /actuator/prometheus
```

### 11.4 验证码

```
GET /api/v1/auth/captcha
```

响应：图片二进制 + Header `X-Captcha-Key`

### 11.5 内部定时任务接口

```
POST /internal/jobs/orders/timeout/close
POST /internal/jobs/inventory/reconcile
GET  /internal/jobs/products/on-sale-spu-ids
```

---

## 12. Postman 测试用例清单（强制 ≥ 6 接口 / 20 请求）

| #  | 用例名                | 方法 | 路径                                  | 用例步骤                                                                  |
| -- | --------------------- | ---- | ------------------------------------- | ------------------------------------------------------------------------- |
| 1  | 用户注册              | POST | /api/v1/users/register                | 准备手机号+密码+验证码 → 注册 → 期望 200                                  |
| 2  | 用户登录              | POST | /api/v1/auth/login                    | 账号密码登录 → 获取 token → 持久化到环境变量                              |
| 3  | 商品搜索              | GET  | /api/v1/search/products?keyword=手机  | 不带 token 也能访问 → 期望 200 + 数据                                      |
| 4  | 商品详情              | GET  | /api/v1/products/1001                 | 不带 token 也能访问 → 期望 200                                            |
| 5  | 加入购物车            | POST | /api/v1/carts                         | 带 token → 期望 200                                                       |
| 6  | 创建订单              | POST | /api/v1/orders                        | 带 token → 校验 Seata 分布式事务 → 期望 200，返回 orderNo                  |
| 7  | 发起支付              | POST | /api/v1/pay/create                    | 带 token + orderNo → 期望 200，返回支付链接                                |
| 8  | 库存预扣              | POST | /api/v1/inventory/lock                | 直接调（带 token）→ 期望 200                                             |
| 9  | 秒杀下单              | POST | /api/v1/seckill/1                     | 模拟高并发 3 次请求，验证 1 成功 2 失败（限购）                            |
| 10 | 商家后台登录          | POST | /api/v1/admin/auth/login              | 商家账号 → 期望 200                                                       |
| 11 | 网关鉴权验证          | GET  | /api/v1/orders                        | 不带 token → 期望 401；过期 token → 期望 401；有效 token → 期望 200         |
| 12 | 限流验证              | POST | /api/v1/seckill/1                     | JMeter 1000 并发 → 期望部分请求 429                                       |

详见 `docs/test/postman-collection.json`。

---

**—— 文档结束 ——**
