# MallCloud 快速启动

> 团队规模：5 人
> 默认环境：Windows 11、PowerShell 7+、UTF-8、JDK 21
> 当前正式路径：Docker 中间件 + 本地 IDE 启动微服务
> 目标：先跑通核心交易链路，不要求一次启动全部 13 个服务

---

## 1. 准备

```powershell
java -version
mvn -version
docker version
docker compose version
$PSVersionTable.PSVersion
```

要求：

- JDK 21；
- Maven 3.9+；
- Docker Desktop；
- PowerShell 7+；
- Docker 可用内存至少 8 GB。

`java -version` 与 `mvn -version` 输出中的 Java 版本均应为 21。

---

## 2. 进入项目目录

```powershell
Set-Location <项目根目录>
```

可选：

```powershell
Copy-Item .env.example .env
```

`.env` 不会自动注入 IDE 启动的 Spring Boot 服务。需要的变量应配置到 IDE Run Configuration。

---

## 3. 启动中间件

```powershell
.\scripts\start-middleware.ps1
```

检查：

```powershell
docker compose -f .\deploy\docker\docker-compose.middleware.yml ps
```

至少确认：

- MySQL；
- Redis；
- Nacos 2.3.2；
- RocketMQ 5.1.4 NameServer/Broker；
- Sentinel 1.8.6 Dashboard；
- Seata Server 2.0.0。

验证 Seata 镜像：

```powershell
docker inspect mall-seata --format '{{.Config.Image}}'
```

预期：

```text
seataio/seata-server:2.0.0
```

常用地址：

| 服务 | 地址 |
|---|---|
| Nacos | `http://localhost:8848/nacos` |
| Sentinel | `http://localhost:8080` |
| RocketMQ Console | `http://localhost:8180` |
| Elasticsearch | `http://localhost:9200` |
| Kibana | `http://localhost:5601` |
| Zipkin | `http://localhost:9411` |

---

## 4. 初始化数据库

```powershell
.\scripts\init-db.ps1
```

本机没有 MySQL Client 时：

```powershell
Get-Content .\db\init\00-create-databases.sql -Raw -Encoding UTF8 |
  docker exec -i mall-mysql mysql -uroot -proot

Get-Content .\db\init\seed.sql -Raw -Encoding UTF8 |
  docker exec -i mall-mysql mysql -uroot -proot
```

验证：

```powershell
docker exec mall-mysql mysql -uroot -proot -e "SELECT COUNT(*) AS users FROM mall_user.user; SELECT COUNT(*) AS spu FROM mall_product.spu; SELECT COUNT(*) AS sku FROM mall_product.sku;"
```

预期：

```text
users = 10
spu = 5
sku = 7
```

---

## 5. 测试账号

所有演示账号统一密码：

```text
123456
```

| 用户名 | 密码 | 角色 |
|---|---|---|
| zhangsan | 123456 | USER |
| lisi | 123456 | USER |
| merchant01 | 123456 | MERCHANT |
| admin | 123456 | ADMIN |

密码摘要以 `db/init/seed.sql` 为准。若登录失败，应修复种子数据，不得只修改文档。

---

## 6. 检查 Nacos 配置

启动业务服务前确认：

- `deploy/nacos/*.yaml` 中没有 `--` 注释；
- Namespace 与服务配置一致；
- Auth 和 Gateway 的 `JWT_SECRET` 一致；
- MySQL、Redis、RocketMQ、Seata 地址正确；
- Seata Server 和客户端配置兼容 2.0.0。

Nacos 配置导入尚未自动化。没有导入远程配置时，应确认本地 `application.yaml` 是否能提供完整启动配置。

---

## 7. 编译

```powershell
java -version
mvn -version
mvn clean package -DskipTests
```

通过标准：

```text
BUILD SUCCESS
```

运行测试：

```powershell
mvn clean test -DskipTests=false
```

测试失败时不要用 `-DskipTests` 掩盖问题，应区分测试缺陷、配置缺陷和业务缺陷。

---

## 8. 启动核心服务

建议使用 IDE，按顺序启动：

Windows + Docker Desktop 本地启动服务时，IDE Run Configuration 至少配置：

```text
NACOS_SERVER=127.0.0.1:8848
MYSQL_HOST=host.docker.internal
MYSQL_PORT=3306
MYSQL_USER=root
MYSQL_PWD=root
REDIS_HOST=127.0.0.1
JWT_SECRET=mallcloud-dev-jwt-secret-20260609-rotated-after-report-leak-change-me-hs512-signing-key
```

| 顺序 | 服务 | 端口 |
|---:|---|---:|
| 1 | mall-user | 9002 |
| 2 | mall-auth | 9001 |
| 3 | mall-product | 9003 |
| 4 | mall-inventory | 9004 |
| 5 | mall-cart | 9005 |
| 6 | mall-order | 9006 |
| 7 | mall-pay | 9007 |
| 8 | mall-message | 9010 |
| 9 | mall-gateway | 9000 |

搜索或秒杀测试时再启动：

| 服务 | 端口 |
|---|---:|
| mall-search | 9008 |
| mall-seckill | 9009 |

后台和任务服务最后启动：

| 服务 | 端口 |
|---|---:|
| mall-admin-biz | 9011 |
| mall-job | 9012 |

---

## 9. 验证服务注册

打开：

```text
http://localhost:8848/nacos
```

核心链路至少应看到：

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

Seata Server 也应按项目配置正确注册或连接。

---

## 10. 最小接口验证

### 10.1 登录

```powershell
$loginBody = @{
  username = "zhangsan"
  password = "123456"
  loginType = "PASSWORD"
} | ConvertTo-Json

$login = Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:9000/api/v1/auth/login" `
  -ContentType "application/json; charset=utf-8" `
  -Body $loginBody

$token = $login.data.accessToken
```

### 10.2 商品详情

```powershell
Invoke-RestMethod "http://localhost:9000/api/v1/products/1001"
```

### 10.3 无 Token 鉴权

```powershell
try {
  Invoke-WebRequest "http://localhost:9000/api/v1/orders/UNKNOWN" -UseBasicParsing
} catch {
  $_.Exception.Response.StatusCode.value__
}
```

预期为 401 或项目统一的未授权响应。

### 10.4 创建订单

```powershell
$orderBody = @{
  addressId = 1
  items = @(
    @{
      skuId = 9001
      quantity = 1
    }
  )
  remark = "quick-start"
} | ConvertTo-Json -Depth 5

$order = Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:9000/api/v1/orders" `
  -Headers @{ Authorization = "Bearer $token" } `
  -ContentType "application/json; charset=utf-8" `
  -Body $orderBody

$orderNo = $order.data.orderNo
```

### 10.5 查询订单

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:9000/api/v1/orders/$orderNo" `
  -Headers @{ Authorization = "Bearer $token" }
```

以上命令必须在最终答辩前实际执行验证。

---

## 11. Postman 与 JMeter

最终测试资产：

```text
docs/test/
├── README.md
├── postman/
└── jmeter/
```

目标文件：

```text
docs/test/postman/mallcloud.postman_collection.json
docs/test/postman/local.postman_environment.json
docs/test/postman/summary/newman-20260609.md
docs/test/jmeter/search-load.jmx
docs/test/jmeter/order-load.jmx
docs/test/jmeter/seckill-stress.jmx
```

Postman 环境中的登录密码统一使用 `123456`，不得保存固定 Token。

---

## 12. 当前不使用的启动方式

以下方式尚未满足正式可用标准：

```powershell
docker compose -f .\deploy\docker\docker-compose.all.yml up -d
```

原因：后端 Dockerfile、组合依赖和镜像构建链路尚未完整验证。

Kubernetes 只作为部分示例，不执行不存在的 13 服务循环部署。

---

## 13. 停止环境

```powershell
docker compose -f .\deploy\docker\docker-compose.middleware.yml down
```

清空数据卷：

```powershell
docker compose -f .\deploy\docker\docker-compose.middleware.yml down -v
```

该命令会删除演示数据，仅在明确需要重置环境时使用。

---

## 14. 下一步

当前已验收通过的完整链路：

```text
登录
→ 商品详情
→ 购物车
→ 创建订单
→ 库存锁定
→ 支付通知
→ 消息消费
→ 订单已支付
→ 库存扣减
```

待完成：

1. 后端真实成功态联调和前端逐页成功截图证据；
2. Elasticsearch 搜索完整验收；
3. 秒杀完整链路验收；
4. Sentinel 规则限流/熔断实测；
5. Nacos 热更新实测；
6. Postman/Newman 完整后端环境报告；
7. JMeter 负载、压力和 HTML 报告；
8. 最终答辩材料。

---

## 15. 前端启动

当前仓库已新增 `mall-frontend`，技术栈为 Vue 3 + Vite + TypeScript + Element Plus + Axios + Pinia。

当前前端已完成一轮产品化页面整改，覆盖商品、搜索、账户、购物车、订单、支付、秒杀和后台页面。完整交付仍需在后端真实可用环境中补充成功态联调、逐页截图和主流程操作证据。

```powershell
Set-Location .\mall-frontend
npm install
npm run dev
```

默认访问：

```text
http://localhost:5173
```

Vite 开发服务器通过代理把 `/api/v1/**` 转发到 `http://localhost:9000`，前端不得直接调用内部微服务端口。后端核心服务未启动时，页面会展示接口错误，不使用 mock 数据伪造成功。
