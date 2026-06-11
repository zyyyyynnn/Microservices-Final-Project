# MallCloud 快速启动

> 目标：新环境从零启动、完成最小验证，并为失败定位提供入口。
> 默认环境：Windows 11、PowerShell 7+、UTF-8、JDK 21、Maven 3.9+

---

## 1. 启动入口

普通启动、完整启动测试和人工验收优先使用根目录 BAT：

```bat
start-all.bat
stop-all.bat
```

BAT 面向人工双击、答辩演示和完整启动测试，是当前主要验收入口。

需要精确参数控制、自动化调用或故障排查时使用 PowerShell：

```powershell
pwsh .\scripts\start-all.ps1 -SkipInfrastructure -SkipFrontend
pwsh .\scripts\start-all.ps1 -SkipInfrastructure -SkipFrontend -SkipBuild
pwsh .\scripts\start-all.ps1 -SkipInfrastructure -SkipFrontend -AllowPartial
pwsh .\scripts\stop-all.ps1
```

IDE 仅作为单服务开发调试补充入口。

---

## 2. 准备检查

```powershell
java -version
mvn -version
docker version
docker compose version
$PSVersionTable.PSVersion
```

要求：

- JDK 21；
- Maven 使用 Java 21；
- Docker Desktop 可用；
- PowerShell 7+；
- Docker 可用内存至少 8 GB。

---

## 3. 进入项目目录

```powershell
Set-Location <项目根目录>
```

`.env` 不会自动注入 IDE 启动的 Spring Boot 服务。需要的变量应配置到 IDE Run Configuration 或启动脚本环境中。

---

## 4. 初始化数据库

数据库初始化会重建业务表并写入演示数据，必须显式确认：

```powershell
pwsh .\scripts\init-db.ps1 -Force
```

验证演示数据：

```powershell
docker exec mall-mysql mysql -uroot -proot -e "SELECT COUNT(*) AS users FROM mall_user.user; SELECT COUNT(*) AS spu FROM mall_product.spu; SELECT COUNT(*) AS sku FROM mall_product.sku;"
```

演示账号统一密码：

```text
123456
```

| 用户名 | 密码 | 角色 |
|---|---|---|
| zhangsan | 123456 | USER |
| merchant01 | 123456 | MERCHANT |
| admin | 123456 | ADMIN |

密码摘要以 `db/init/seed.sql` 为准。若登录失败，应修复种子数据或配置，不得只修改文档。

---

## 5. 完整启动

优先使用 BAT：

```bat
start-all.bat
```

常用参数：

```bat
start-all.bat --skip-infrastructure
start-all.bat --skip-frontend
start-all.bat --no-build
start-all.bat --no-pause
start-all.bat --profile core
start-all.bat --profile search
start-all.bat --profile seckill
start-all.bat --profile core --low-memory
```

启动失败不得写成成功。若 `mall-job` 旧端口 9012 被外部进程占用，当前 mall-job 已迁移到 9112，应如实记录为端口冲突或未启动。
Profile 资源采样和 LowMemory 验证结果以 `docs/FINAL_REPORT.md` 为准；`full` Profile 若因 Docker 镜像源拉取失败中断，不得写成已启动。

停止：

```bat
stop-all.bat
```

---

## 6. 高级启动与故障排查

PowerShell 用于参数化启动、自动化复测和故障排查：

```powershell
pwsh .\scripts\start-all.ps1 -SkipInfrastructure -SkipFrontend
pwsh .\scripts\start-all.ps1 -SkipInfrastructure -SkipFrontend -SkipBuild
pwsh .\scripts\start-all.ps1 -SkipInfrastructure -SkipFrontend -AllowPartial
pwsh .\scripts\stop-all.ps1
```

语义：

- 默认存在失败服务时返回非 0；
- `-AllowPartial` 仅用于允许记录非核心失败并继续联调；
- `-SkipBuild` 复用已有 `target` 产物；
- `-SkipFrontend` 时不要求 Node/npm；
- 日志位于 `.runtime/logs/`；
- 状态文件位于 `.runtime/processes.json`。

---

## 7. 最小验证

### 7.1 服务注册

打开：

```text
http://localhost:8848/nacos
```

至少确认核心服务已注册：

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

搜索、秒杀、后台和任务服务按实际启动结果记录，不得补写。

### 7.2 登录

```powershell
$loginBody = @{
  username = "zhangsan"
  password = "123456"
  loginType = "PASSWORD"
} | ConvertTo-Json

$login = Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:9100/api/v1/auth/login" `
  -ContentType "application/json; charset=utf-8" `
  -Body $loginBody

$token = $login.data.accessToken
```

### 7.3 商品详情

```powershell
Invoke-RestMethod "http://localhost:9100/api/v1/products/1001"
```

### 7.4 创建订单

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
  -Uri "http://localhost:9100/api/v1/orders" `
  -Headers @{ Authorization = "Bearer $token" } `
  -ContentType "application/json; charset=utf-8" `
  -Body $orderBody

$orderNo = $order.data.orderNo
```

### 7.5 查询订单

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:9100/api/v1/orders/$orderNo" `
  -Headers @{ Authorization = "Bearer $token" }
```

以上命令必须在最终答辩前实际执行验证。

---

## 8. 前端启动

```powershell
Set-Location .\mall-frontend
npm install
npm run dev
```

默认访问：

```text
http://localhost:5173
```

前端通过 Vite 代理把 `/api/v1/**` 转发到 Gateway `http://localhost:9100`。后端核心服务未启动时，页面应展示明确错误，不使用 mock 数据伪造成功。

---

## 9. 常见失败定位

| 问题 | 定位入口 |
|---|---|
| Java 版本不一致 | `java -version`、`mvn -version` |
| 中间件未启动 | `docker compose -f .\deploy\docker\docker-compose.middleware.yml ps` |
| 服务启动失败 | `.runtime/logs/*.err.log` |
| 端口占用 | BAT 输出或 `Get-NetTCPConnection` |
| Nacos 未注册 | Nacos 控制台服务列表 |
| 登录失败 | 种子数据、JWT 密钥、Auth/Gateway 配置 |
| 搜索失败 | Elasticsearch 健康和 `mall-search` 日志 |
| 秒杀失败 | Redis、RocketMQ、`mall-seckill` 日志 |

---

## 10. 下一步

当前阶段任务以 [../DEVELOPMENT_PROMPT.md](../DEVELOPMENT_PROMPT.md) 为准。当前真实测试结果和未完成项以 [FINAL_REPORT.md](FINAL_REPORT.md) 为准。
