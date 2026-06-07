# MallCloud 快速启动

> 默认环境：Windows 11、PowerShell 7+、UTF-8
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

推荐：

- JDK 17；
- Maven 3.9+；
- Docker Desktop；
- PowerShell 7+；
- Docker 可用内存至少 8 GB。

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
- Nacos；
- RocketMQ NameServer/Broker；
- Sentinel Dashboard；
- Seata Server。

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

## 5. 检查 Nacos 配置

启动业务服务前确认：

- `deploy/nacos/*.yaml` 中没有 `--` 注释；
- Namespace 与服务配置一致；
- Auth 和 Gateway 的 `JWT_SECRET` 一致；
- MySQL、Redis、RocketMQ、Seata 地址正确。

Nacos 配置导入尚未自动化。没有导入远程配置时，应确认本地 `application.yaml` 是否能提供完整启动配置。

---

## 6. 编译

```powershell
mvn clean package -DskipTests
```

通过标准：

```text
BUILD SUCCESS
```

运行现有测试：

```powershell
mvn clean test -DskipTests=false
```

测试失败时不要直接使用 `-DskipTests` 掩盖问题，应先区分测试缺陷、配置缺陷和业务缺陷。

---

## 7. 启动核心服务

建议使用 IDE，按顺序启动：

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

后台和任务服务可最后启动：

| 服务 | 端口 |
|---|---:|
| mall-admin-biz | 9011 |
| mall-job | 9012 |

---

## 8. 验证服务注册

打开 Nacos：

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

若服务未注册，检查启动日志、Nacos 地址、Namespace 和配置加载方式。

---

## 9. 最小接口验证

### 9.1 登录

```powershell
$loginBody = @{
  username = "zhangsan"
  password = "P@ssw0rd123"
  loginType = "PASSWORD"
} | ConvertTo-Json

$login = Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:9000/api/v1/auth/login" `
  -ContentType "application/json; charset=utf-8" `
  -Body $loginBody

$token = $login.data.accessToken
```

### 9.2 商品详情

```powershell
Invoke-RestMethod "http://localhost:9000/api/v1/products/1001"
```

### 9.3 无 Token 鉴权

```powershell
try {
  Invoke-WebRequest "http://localhost:9000/api/v1/orders/UNKNOWN" -UseBasicParsing
} catch {
  $_.Exception.Response.StatusCode.value__
}
```

预期为 401 或项目统一的未授权响应。

### 9.4 创建订单

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

### 9.5 查询订单

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:9000/api/v1/orders/$orderNo" `
  -Headers @{ Authorization = "Bearer $token" }
```

以上命令必须在最终答辩前实际执行验证。字段或路径与代码不一致时，以 Controller/DTO 为准并同步修正文档。

---

## 10. 测试账号

| 用户名 | 密码 | 角色 |
|---|---|---|
| zhangsan | P@ssw0rd123 | USER |
| lisi | P@ssw0rd123 | USER |
| merchant01 | P@ssw0rd123 | MERCHANT |
| admin | Admin@123 | ADMIN |

种子密码摘要必须通过实际登录验证。若无法登录，应修复 `seed.sql`，不能只修改文档中的密码。

---

## 11. Postman 与 JMeter

最终测试资产应存放：

```text
docs/test/
├── README.md
├── postman/
└── jmeter/
```

旧文件 `docs/test/postman-collection.json` 为模板生成物，不作为最终报告。

目标文件：

```text
docs/test/postman/mallcloud.postman_collection.json
docs/test/postman/local.postman_environment.json
docs/test/postman/summary/newman-20260609.md
docs/test/jmeter/search-load.jmx
docs/test/jmeter/order-load.jmx
docs/test/jmeter/seckill-stress.jmx
```

这些文件需要在后续测试阶段创建和执行，当前不得假定已经存在。

---

## 12. 当前不使用的启动方式

以下方式尚未满足正式可用标准：

```powershell
docker compose -f .\deploy\docker\docker-compose.all.yml up -d
```

原因：后端 Dockerfile、组合依赖和镜像构建链路尚未完整验证。

Kubernetes 也只作为部分示例，不执行不存在的 13 服务循环部署。

---

## 13. 停止环境

```powershell
docker compose -f .\deploy\docker\docker-compose.middleware.yml down
```

仅在明确需要清空数据时使用：

```powershell
docker compose -f .\deploy\docker\docker-compose.middleware.yml down -v
```

---

## 14. 下一步

启动成功后按顺序执行：

1. 修复 Nacos 配置模板；
2. 跑真实 Postman 核心链路；
3. 验证 Seata 失败回滚；
4. 验证 PAY_RESULT 消息；
5. 建立 JMeter 三套脚本；
6. 填写 `docs/FINAL_REPORT.md`。
