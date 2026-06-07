# MallCloud 部署与运行指南

> 文档版本：v2.1
> 团队规模：5 人
> 默认环境：Windows 11、PowerShell 7+、UTF-8、JDK 21
> 当前正式路径：Docker 中间件 + 本地 IDE 启动微服务
> 上位标准：`docs/PROJECT_STANDARD.md`

---

## 1. 当前支持范围

| 方式 | 状态 | 用途 |
|---|---|---|
| PowerShell 启动中间件 | 已提供，待完整验证 | 本地开发与答辩 |
| PowerShell 初始化数据库 | 已提供，待完整验证 | 初始化业务库和演示数据 |
| IDE 启动微服务 | 当前推荐 | 开发、联调、演示 |
| `docker-compose.all.yml` 全栈 | 实验性 | 构建文件和组合启动链路待修复 |
| Kubernetes 全栈 | 规划项 | 当前仅有部分中间件和 Gateway 示例 |

本文件不引用不存在的 `dev-up.sh`、`test-up.sh`、`demo-up.sh`、`start-all.*` 或分库 SQL 文件。

---

## 2. 前置要求

| 软件 | 版本 | 检查命令 |
|---|---|---|
| PowerShell | 7+ | `$PSVersionTable.PSVersion` |
| JDK | 21 | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Docker Desktop | 当前稳定版 | `docker version` |
| Docker Compose | v2 | `docker compose version` |
| MySQL Client | 8.x，可选 | `mysql --version` |
| Node.js | 20+ | `node --version` |
| Git | 2.30+ | `git --version` |
| Newman | 测试阶段 | `newman --version` |
| JMeter | 5.6+ | `jmeter --version` |

要求：

- `java -version` 输出 Java 21；
- `mvn -version` 显示 Maven 使用 Java 21；
- Docker 可用内存至少 8 GB；
- 建议物理内存至少 16 GB。

---

## 3. 技术版本

| 组件 | 版本 |
|---|---:|
| Spring Boot | 3.2.4 |
| Spring Cloud | 2023.0.1 |
| Spring Cloud Alibaba | 2023.0.1.0 |
| Nacos | 2.3.2 |
| Sentinel | 1.8.6 |
| RocketMQ | 5.1.4 |
| Seata Server | 2.0.0 |
| Redis | 7 |
| Elasticsearch/Kibana | 8.11.0 |
| MySQL | 8.0 |

Java 21 升级不伴随 Spring 框架整体升级。本期不采用 Java 24。

---

## 4. 环境变量

复制模板：

```powershell
Copy-Item .env.example .env
```

Spring Boot 不会自动读取根目录 `.env`。IDE 启动时应把必要变量配置到 Run Configuration。

| 变量 | 示例 | 说明 |
|---|---|---|
| `NACOS_SERVER` | `127.0.0.1:8848` | Nacos 地址 |
| `MYSQL_HOST` | `host.docker.internal` | MySQL 地址；Windows + Docker Desktop + IDE 启动服务时优先使用该值 |
| `MYSQL_PORT` | `3306` | MySQL 端口 |
| `MYSQL_USER` | `root` | MySQL 用户 |
| `MYSQL_PWD` | `root` | MySQL 密码 |
| `REDIS_HOST` | `127.0.0.1` | Redis 地址 |
| `ROCKETMQ_NAMESRV` | `127.0.0.1:9876` | NameServer |
| `ES_HOST` | `127.0.0.1` | Elasticsearch 地址 |
| `SEATA_TC_URL` | `127.0.0.1:8091` | Seata TC |
| `SENTINEL_DASHBOARD` | `127.0.0.1:8080` | Sentinel Dashboard |
| `JWT_SECRET` | 自定义长密钥 | Auth 与 Gateway 必须一致 |

若本机 `127.0.0.1:3306` 命中 MySQL 的 `root@localhost` 授权导致 JDBC 认证失败，可改用 `MYSQL_HOST=host.docker.internal`。

开发默认密钥只能用于本地演示。

---

## 5. 启动中间件

```powershell
.\scripts\start-middleware.ps1
```

手动方式：

```powershell
Set-Location .\deploy\docker
docker compose -f docker-compose.middleware.yml up -d
```

检查：

```powershell
docker compose -f .\deploy\docker\docker-compose.middleware.yml ps
docker inspect mall-seata --format '{{.Config.Image}}'
```

Seata 预期镜像：

```text
seataio/seata-server:2.0.0
```

当前中间件：

- MySQL；
- Redis；
- Nacos；
- RocketMQ NameServer、Broker、Console；
- Elasticsearch、Kibana；
- Sentinel Dashboard；
- Zipkin；
- Seata Server 2.0.0。

Docker `depends_on` 不等于业务已可用。脚本执行后仍需检查容器状态和日志。

### Seata Server 验证

Seata Server 2.0.0 配置来自 Docker 挂载的 `deploy/docker/seata/conf/application.yml`，注册到 Nacos `dev / SEATA_GROUP`，存储模式为 MySQL DB（`mall_seata` 库）。

验证命令：

```powershell
# 容器状态
docker compose -f .\deploy\docker\docker-compose.middleware.yml ps seata

# 日志确认 DB Store
docker logs mall-seata --tail 50 2>&1 | Select-String 'store mode'
# 预期：use lock store mode: db / use session store mode: db

# Nacos 注册确认
curl "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=seata-server&namespaceId=dev&groupName=SEATA_GROUP"

# 数据库表确认
docker exec mall-mysql mysql -uroot -proot -e "
USE mall_seata;
SHOW TABLES;
SELECT lock_key, lock_value, expire FROM distributed_lock ORDER BY lock_key;
"
# 预期：4 张 Server 表。
# distributed_lock 初始化后至少 4 行（AsyncCommitting/RetryCommitting/RetryRollbacking/TxTimeoutCheck）；
# Seata Server 启动后自动增加 UndologDelete，运行期可能为 5 行。
```

> Seata Server 表（`mall_seata` 库）与业务 AT 分支 undo_log（`mall_order`、`mall_inventory` 等业务库）是不同层级，不要混淆。

当前数据库基线只保留 `db/init/`，不再支持旧数据库环境升级。如需重建演示数据，应先明确备份或重建数据库容器。

---

## 6. 初始化数据库

```powershell
.\scripts\init-db.ps1
```

脚本执行：

1. `db/init/00-create-databases.sql`
2. `db/init/seed.sql`

所有演示账号密码统一为：

```text
123456
```

容器内执行：

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

预期用户 10、SPU 5、SKU 7。

---

## 7. Nacos 配置

导入前确认：

- YAML 注释使用 `#`；
- DataId 与引用一致；
- Namespace 为实际使用值；
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

配置导入尚未自动化，答辩前应保存实际步骤和截图。

---

## 8. 编译与测试

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

父 POM 当前默认跳过测试的配置仍需在后续代码整改阶段处理。

---

## 9. 启动微服务

核心链路推荐顺序：

1. `mall-user`
2. `mall-auth`
3. `mall-product`
4. `mall-inventory`
5. `mall-cart`
6. `mall-order`
7. `mall-pay`
8. `mall-message`
9. `mall-gateway`

搜索和秒杀测试时再启动：

- `mall-search`
- `mall-seckill`

辅助服务：

- `mall-admin-biz`
- `mall-job`

所有 IDE Run Configuration 必须使用 JDK 21。

---

## 10. 端口

| 服务 | 端口 |
|---|---:|
| mall-gateway | 9000 |
| mall-auth | 9001 |
| mall-user | 9002 |
| mall-product | 9003 |
| mall-inventory | 9004 |
| mall-cart | 9005 |
| mall-order | 9006 |
| mall-pay | 9007 |
| mall-search | 9008 |
| mall-seckill | 9009 |
| mall-message | 9010 |
| mall-admin-biz | 9011 |
| mall-job | 9012 |

---

## 11. 注册与健康检查

Nacos：

```text
http://localhost:8848/nacos
```

健康检查：

```powershell
Invoke-RestMethod http://localhost:9000/actuator/health
Invoke-RestMethod http://localhost:9001/actuator/health
Invoke-RestMethod http://localhost:9006/actuator/health
```

最终演示需保存：

- 服务列表；
- 核心实例健康状态；
- 停止和恢复一个服务的状态变化；
- Seata Server 注册或连接状态。

---

## 12. Docker 全栈与 Kubernetes

`docker-compose.all.yml` 当前不是正式一键启动方式，原因：

- 业务服务构建目录缺少完整 Dockerfile；
- 组合依赖尚未验证；
- 部分前端镜像路径未验证。

Kubernetes 当前提供部分中间件和 Gateway 示例，完整 13 服务部署尚未完成。

---

## 13. 常见问题

### 13.1 Java 版本不一致

```powershell
Get-Command java
java -version
mvn -version
```

确保 `JAVA_HOME` 和 IDE Project SDK 都指向 JDK 21。

### 13.2 Seata 不能启动或回滚

检查：

- 镜像是否为 2.0.0；
- `mall_seata` 表是否存在；
- Nacos Namespace、Group；
- 服务端和客户端事务组；
- XID 是否透传；
- 数据源代理；
- `undo_log`；
- 异常是否被吞掉。

### 13.3 JWT 登录失败

检查：

- 种子数据已重新初始化；
- 测试密码为 `123456`；
- Auth 与 Gateway 使用相同 JWT 密钥（默认值已统一）；
- Header 格式正确。

### 13.4 角色字段

`sys_user_auth.role` 字段已在 `db/init/00-create-databases.sql` 中包含，无需单独迁移。

---

## 14. 答辩演示建议

1. 展示 5 人分工和项目范围；
2. 展示 Java 21、Spring 版本和中间件版本；
3. 展示 Nacos 核心服务注册；
4. 使用 `zhangsan / 123456` 登录；
5. 商品、购物车、下单；
6. Feign、Seata 2.0.0、MQ 链路；
7. JMeter 与 Sentinel；
8. 服务停止/恢复或配置热更新；
9. 总结实测数据和已知限制。
