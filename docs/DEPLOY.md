# MallCloud 部署与运行指南

> 职责：部署结构、中间件、环境变量、数据库初始化、服务端口、配置来源、启动细节和故障排查。
> 上位标准：`docs/PROJECT_STANDARD.md`

---

## 1. 支持范围

| 方式 | 状态 | 用途 |
|---|---|---|
| 根目录 BAT 一键启停 | 已提供 | 人工启动、答辩演示、完整启动测试 |
| PowerShell 启停脚本 | 已提供 | 参数化启动、自动化调用、故障排查 |
| PowerShell 中间件脚本 | 已提供 | 本地中间件启动 |
| IDE 单服务启动 | 补充入口 | 单服务开发调试 |
| `docker-compose.all.yml` 全栈 | 实验性 | 构建和组合启动链路待验证 |
| Kubernetes 全栈 | 规划项 | 当前仅有部分示例 |

正式人工启动路径以根目录 `start-all.bat` / `stop-all.bat` 为准。PowerShell 脚本保留为高级入口和脚本内部能力，不承担普通人工启动主入口职责。

---

## 2. 前置要求

| 软件 | 要求 | 检查命令 |
|---|---|---|
| PowerShell | 7+ | `$PSVersionTable.PSVersion` |
| JDK | 21 | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Docker Desktop | 可用 | `docker version` |
| Docker Compose | v2 | `docker compose version` |
| Node.js / npm | 前端启动需要 | `node --version`、`npm --version` |
| MySQL Client | 可选 | `mysql --version` |

完整技术版本矩阵见 `docs/PROJECT_STANDARD.md`。

---

## 3. 环境变量

Spring Boot 不会自动读取根目录 `.env`。IDE 启动时应把必要变量配置到 Run Configuration；脚本启动时由脚本设置或继承环境变量。

| 变量 | 示例 | 说明 |
|---|---|---|
| `NACOS_SERVER` | `127.0.0.1:8848` | Nacos 地址 |
| `MYSQL_HOST` | `127.0.0.1` 或 `host.docker.internal` | MySQL 地址 |
| `MYSQL_PORT` | `3306` | MySQL 端口 |
| `MYSQL_USER` | `root` | MySQL 用户 |
| `MYSQL_PWD` | `root` | MySQL 密码 |
| `REDIS_HOST` | `127.0.0.1` | Redis 地址 |
| `ROCKETMQ_NAMESRV` | `127.0.0.1:9876` | RocketMQ NameServer |
| `ES_HOST` | `127.0.0.1` | Elasticsearch 地址 |
| `SEATA_TC_URL` | `127.0.0.1:8091` | Seata TC |
| `SENTINEL_DASHBOARD` | `127.0.0.1:8080` | Sentinel Dashboard |
| `JWT_SECRET` | 自定义长密钥 | Auth 与 Gateway 必须一致 |

开发默认密钥只能用于本地演示。

---

## 4. 中间件

BAT 完整启动会按自身流程启动中间件。单独启动中间件可用：

```powershell
pwsh .\scripts\start-middleware.ps1
```

手动方式：

```powershell
docker compose -f .\deploy\docker\docker-compose.middleware.yml up -d
```

检查：

```powershell
docker compose -f .\deploy\docker\docker-compose.middleware.yml ps
docker inspect mall-seata --format '{{.Config.Image}}'
```

中间件包括：

- MySQL；
- Redis；
- Nacos；
- RocketMQ NameServer、Broker、Console；
- Elasticsearch、Kibana；
- Sentinel Dashboard；
- Zipkin；
- Seata Server 2.0.0。

Docker `depends_on` 不等于业务已可用。脚本执行后仍需检查容器状态和日志。

---

## 5. 数据库初始化

数据库初始化会重建业务表并写入演示数据，必须显式确认：

```powershell
pwsh .\scripts\init-db.ps1 -Force
```

脚本执行：

1. `db/init/00-create-databases.sql`
2. `db/init/seed.sql`

手动容器内执行：

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

---

## 6. Nacos 配置

导入前确认：

- YAML 注释使用 `#`；
- DataId 与引用一致；
- Namespace 与服务配置一致；
- Group 明确；
- 环境变量名称统一；
- Seata 客户端和服务端配置兼容 2.0.0。

建议导入顺序：

```text
common-mysql.yaml
common-redis.yaml
common-rocketmq.yaml
common-sentinel.yaml
common-seata.yaml
mall-gateway.yaml
mall-auth.yaml
其他服务配置
```

配置导入和热更新验收需保存实际步骤和截图。未验证热更新不得写成通过。

---

## 7. 编译

```powershell
java -version
mvn -version
mvn clean package -DskipTests
```

通过标准：

- Java 21；
- Maven 使用 Java 21；
- `BUILD SUCCESS`。

运行测试：

```powershell
mvn clean test -DskipTests=false
```

测试失败时记录原因，不用跳过测试掩盖问题。

---

## 8. 启动微服务

### 8.1 BAT 主入口

人工启动、完整启动测试和答辩演示优先使用：

```bat
start-all.bat
stop-all.bat
```

常用参数：

```bat
start-all.bat --skip-infrastructure
start-all.bat --skip-backend
start-all.bat --skip-frontend
start-all.bat --clean-logs
start-all.bat --no-build
start-all.bat --no-pause
stop-all.bat --no-pause
```

BAT 结束后会输出状态摘要。失败服务、端口占用、超时或 JAR 缺失必须如实记录。

### 8.2 PowerShell 高级入口

用于参数化启动、自动化调用和故障排查：

```powershell
pwsh .\scripts\start-all.ps1 -SkipInfrastructure -SkipFrontend
pwsh .\scripts\start-all.ps1 -SkipInfrastructure -SkipFrontend -SkipBuild
pwsh .\scripts\start-all.ps1 -SkipInfrastructure -SkipFrontend -AllowPartial
pwsh .\scripts\stop-all.ps1
```

默认存在失败服务时返回非 0；只有显式使用 `-AllowPartial` 时才允许记录失败并返回 0。

### 8.3 IDE 补充入口

IDE 适合单服务调试。核心链路推荐顺序：

1. `mall-user`
2. `mall-auth`
3. `mall-product`
4. `mall-inventory`
5. `mall-cart`
6. `mall-order`
7. `mall-pay`
8. `mall-message`
9. `mall-gateway`

搜索和秒杀专项再启动：

- `mall-search`
- `mall-seckill`

辅助服务：

- `mall-admin-biz`
- `mall-job`

所有 IDE Run Configuration 必须使用 JDK 21。

---

## 9. 前端

```powershell
Set-Location .\mall-frontend
npm install
npm run dev
```

构建验证：

```powershell
npm run build
```

开发环境默认使用 `http://localhost:5173`，Vite 代理将 `/api/v1/**` 转发到 Gateway `http://localhost:9100`。

---

## 10. 端口

| 服务 | 端口 |
|---|---:|
| mall-frontend | 5173 |
| mall-gateway | 9100 |
| mall-auth | 9101 |
| mall-user | 9102 |
| mall-product | 9103 |
| mall-inventory | 9104 |
| mall-cart | 9105 |
| mall-order | 9106 |
| mall-pay | 9107 |
| mall-search | 9108 |
| mall-seckill | 9109 |
| mall-message | 9110 |
| mall-admin-biz | 9111 |
| mall-job | 9112 |

---

## 11. 日志与状态

| 内容 | 路径 |
|---|---|
| 运行日志 | `.runtime/logs/` |
| 进程状态 | `.runtime/processes.json` |
| 中间件状态 | `.runtime/infrastructure.json` |

服务启动失败时优先查看对应 `.err.log`。端口占用时按脚本输出记录 PID 和命令行，不得写成服务已启动。

---

## 12. 注册与健康检查

Nacos：

```text
http://localhost:8848/nacos
```

Gateway 健康：

```powershell
Invoke-RestMethod http://localhost:9100/actuator/health
```

最终演示需保存：

- 服务列表；
- 核心实例健康状态；
- 停止和恢复一个服务的状态变化；
- Seata Server 注册或连接状态。

---

## 13. Docker 全栈与 Kubernetes

`docker-compose.all.yml` 当前不是正式一键启动方式，原因：

- 业务服务镜像构建链路尚未完整验收；
- 组合依赖尚未验证；
- 前端镜像路径未形成交付证据。

Kubernetes 当前提供部分示例，不作为正式验收路径。

---

## 14. 常见问题

| 问题 | 检查项 |
|---|---|
| Java 版本不一致 | `Get-Command java`、`java -version`、`mvn -version` |
| Docker 不可用 | Docker Desktop 状态、`docker info` |
| Seata 不能启动或回滚 | 镜像、`mall_seata` 表、Nacos Namespace/Group、XID、数据源代理、`undo_log` |
| JWT 登录失败 | 种子数据、测试密码、Auth/Gateway JWT 密钥、Header 格式 |
| 搜索失败 | Elasticsearch 健康、索引、`mall-search` 日志 |
| 秒杀失败 | Redis、RocketMQ、`mall-seckill` 日志 |
