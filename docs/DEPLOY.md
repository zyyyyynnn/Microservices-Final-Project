# MallCloud 部署与运行指南

> 文档版本：v2.0
> 默认环境：Windows 11、PowerShell 7+、UTF-8
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

本文件不再引用不存在的 `dev-up.sh`、`test-up.sh`、`demo-up.sh`、`start-all.*` 或分库 SQL 文件。

---

## 2. 前置要求

| 软件 | 推荐版本 | 检查命令 |
|---|---|---|
| PowerShell | 7+ | `$PSVersionTable.PSVersion` |
| JDK | 17 | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Docker Desktop | 当前稳定版 | `docker version` |
| Docker Compose | v2 | `docker compose version` |
| MySQL Client | 8.x，可选 | `mysql --version` |
| Node.js | 20+ | `node --version` |
| Git | 2.30+ | `git --version` |
| Postman/Newman | 测试阶段 | `newman --version` |
| JMeter | 5.6+ | `jmeter --version` |

建议资源：

- 内存至少 16 GB；
- Docker 分配至少 8 GB；
- 可用磁盘至少 20 GB。

8 GB 内存可能无法同时稳定运行全部中间件和 13 个 Java 服务。

---

## 3. 环境变量

复制模板：

```powershell
Copy-Item .env.example .env
```

说明：Spring Boot 本身不会自动读取根目录 `.env`。通过 IDE 启动时，需要把必要变量配置到 Run Configuration，或使用本地配置默认值。

核心变量：

| 变量 | 示例 | 说明 |
|---|---|---|
| `NACOS_SERVER` | `127.0.0.1:8848` | Nacos 地址 |
| `MYSQL_HOST` | `127.0.0.1` | MySQL 地址 |
| `MYSQL_PORT` | `3306` | MySQL 端口 |
| `MYSQL_USER` | `root` | MySQL 用户 |
| `MYSQL_PWD` | `root` | MySQL 密码 |
| `REDIS_HOST` | `127.0.0.1` | Redis 地址 |
| `ROCKETMQ_NAMESRV` | `127.0.0.1:9876` | NameServer |
| `ES_HOST` | `127.0.0.1` | Elasticsearch 地址 |
| `SEATA_TC_URL` | `127.0.0.1:8091` | Seata TC |
| `SENTINEL_DASHBOARD` | `127.0.0.1:8080` | Sentinel Dashboard |
| `JWT_SECRET` | 自定义长密钥 | Auth 与 Gateway 必须一致 |

开发默认密钥只能用于本地演示，不得用于公开部署。

---

## 4. 启动中间件

在项目根目录执行：

```powershell
.\scripts\start-middleware.ps1
```

等价手动命令：

```powershell
Set-Location .\deploy\docker
docker compose -f docker-compose.middleware.yml up -d
```

检查状态：

```powershell
docker compose -f .\deploy\docker\docker-compose.middleware.yml ps
```

当前中间件编排包括：

- MySQL
- Redis
- Nacos
- RocketMQ NameServer、Broker、Console
- Elasticsearch
- Kibana
- Sentinel Dashboard
- Zipkin
- Seata Server

### 4.1 重要说明

Docker Compose 的 `depends_on` 不等于业务可用。脚本当前只主动等待 Nacos 和 MySQL，其他中间件仍需手动检查。

建议检查：

```powershell
Invoke-WebRequest http://localhost:8848/nacos/ -UseBasicParsing
Invoke-WebRequest http://localhost:9200/ -UseBasicParsing
Invoke-WebRequest http://localhost:8080/ -UseBasicParsing
Invoke-WebRequest http://localhost:9411/ -UseBasicParsing
```

RocketMQ、Seata 和 Redis 可通过容器日志或对应客户端检查。

---

## 5. 初始化数据库

推荐命令：

```powershell
.\scripts\init-db.ps1
```

脚本实际执行：

1. `db/init/00-create-databases.sql`
2. `db/init/seed.sql`

不存在 `db/init/mall_$db.sql` 等分库脚本。

### 5.1 容器内执行方案

本机未安装 MySQL Client 时，可以使用容器执行：

```powershell
Get-Content .\db\init\00-create-databases.sql -Raw -Encoding UTF8 |
  docker exec -i mall-mysql mysql -uroot -proot

Get-Content .\db\init\seed.sql -Raw -Encoding UTF8 |
  docker exec -i mall-mysql mysql -uroot -proot
```

### 5.2 验证

```powershell
docker exec mall-mysql mysql -uroot -proot -e "SELECT COUNT(*) AS users FROM mall_user.user; SELECT COUNT(*) AS spu FROM mall_product.spu; SELECT COUNT(*) AS sku FROM mall_product.sku;"
```

预期：

- 用户 10；
- SPU 5；
- SKU 7。

---

## 6. 修正和导入 Nacos 配置

`deploy/nacos/*.yaml` 是配置模板。导入前必须确认：

- YAML 注释使用 `#`，不得使用 `--`；
- DataId 与服务配置引用一致；
- Namespace 为 `dev` 或实际使用值；
- Group 默认为 `DEFAULT_GROUP`，Seata 配置按实际 Group；
- 环境变量名统一使用项目约定。

建议导入顺序：

```text
common-mysql.yaml
common-redis.yaml
common-rocketmq.yaml
common-sentinel.yaml
common-seata.yaml
mall-gateway.yaml
mall-auth.yaml
mall-user.yaml
...
```

Nacos 配置导入尚未自动化。最终答辩前应记录实际导入步骤和截图。

---

## 7. 编译项目

```powershell
mvn clean package -DskipTests
```

通过标准：

```text
BUILD SUCCESS
```

需要运行测试时：

```powershell
mvn clean test -DskipTests=false
```

父 POM 当前存在默认跳过测试配置，后续代码整改阶段应移除或通过命令覆盖。

---

## 8. 启动微服务

### 8.1 核心链路最小集合

首次联调建议按依赖顺序启动：

1. `mall-user`
2. `mall-auth`
3. `mall-product`
4. `mall-inventory`
5. `mall-cart`
6. `mall-order`
7. `mall-pay`
8. `mall-message`
9. `mall-gateway`

搜索和秒杀验证时再启动：

- `mall-search`
- `mall-seckill`

后台和定时任务不影响普通交易主链路：

- `mall-admin-biz`
- `mall-job`

### 8.2 端口

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

### 8.3 验证注册

浏览器访问：

```text
http://localhost:8848/nacos
```

或使用 Nacos API。最终演示需保存：

- 服务列表截图；
- 核心服务实例健康状态；
- 停止和恢复一个服务的状态变化。

---

## 9. 启动前端

用户前台：

```powershell
Set-Location .\web-portal
npm install
npm run dev
```

商家后台：

```powershell
Set-Location .\web-admin
npm install
npm run dev
```

前端是否已完整对接所有接口需单独验证。后端接口测试不依赖前端完成。

---

## 10. 健康检查

通过 Gateway 或服务端口检查：

```powershell
Invoke-RestMethod http://localhost:9000/actuator/health
Invoke-RestMethod http://localhost:9001/actuator/health
Invoke-RestMethod http://localhost:9006/actuator/health
```

若 Gateway 未配置 Actuator 路由，应直接访问服务端口。

`/actuator/prometheus` 只有在存在相应 Registry 依赖时才可用，不作为默认成功条件。

---

## 11. Docker 全栈状态

`deploy/docker/docker-compose.all.yml` 当前不能作为正式一键启动方式，原因包括：

- 业务服务构建目录缺少完整 Dockerfile；
- Compose 中的中间件依赖和组合启动方式未验证；
- 文档此前引用的 `start-all.sh`、`start-all.ps1` 不存在；
- 前端镜像构建路径仍需验证。

后续若完善，应达到：

```powershell
docker compose `
  -f .\deploy\docker\docker-compose.middleware.yml `
  -f .\deploy\docker\docker-compose.all.yml `
  config
```

无错误，并实际构建、启动、健康检查通过后，才能恢复“一键全栈”表述。

---

## 12. Kubernetes 状态

当前 K8s 目录提供部分中间件和 `mall-gateway` 示例。以下命令只有对应文件存在时才能使用：

```powershell
kubectl apply -f .\deploy\k8s\00-namespace.yaml
kubectl apply -f .\deploy\k8s\01-mysql.yaml
kubectl apply -f .\deploy\k8s\02-redis.yaml
kubectl apply -f .\deploy\k8s\03-nacos.yaml
kubectl apply -f .\deploy\k8s\04-rocketmq.yaml
kubectl apply -f .\deploy\k8s\05-sentinel.yaml
kubectl apply -f .\deploy\k8s\06-seata.yaml
kubectl apply -f .\deploy\k8s\services\mall-gateway.yaml
```

不存在 `05-elasticsearch.yaml` 时不得引用。

当前不执行循环部署 13 个服务，因为对应 manifest 尚未齐全。

---

## 13. 停止环境

停止中间件：

```powershell
docker compose -f .\deploy\docker\docker-compose.middleware.yml down
```

删除数据卷前必须明确风险：

```powershell
docker compose -f .\deploy\docker\docker-compose.middleware.yml down -v
```

该命令会删除演示数据，不作为普通停止命令。

---

## 14. 常见问题

### 14.1 Nacos 配置未加载

检查：

- 配置是否为合法 YAML；
- DataId、Group、Namespace；
- `NACOS_SERVER`；
- 当前 Spring Cloud Alibaba 版本使用的配置导入方式；
- 服务启动日志中的配置加载记录。

### 14.2 Feign 调用失败

检查：

- 下游是否已注册；
- 服务名与 `@FeignClient` 是否一致；
- Controller 路径是否一致；
- 返回值是否为 `Result<T>`；
- 超时配置；
- Sentinel 是否触发。

### 14.3 JWT 校验失败

检查：

- Auth 和 Gateway 使用同一 `JWT_SECRET`；
- Header 为 `Authorization: Bearer <token>`；
- Token 是否过期；
- 登录环境和请求环境是否一致。

### 14.4 Seata 不回滚

检查：

- `undo_log` 是否存在；
- Seata Server 是否启动和注册；
- XID 是否透传；
- 数据源是否由 Seata 代理；
- 异常是否被 catch 后吞掉；
- 远程库存操作是否实际加入全局事务。

### 14.5 RocketMQ 消息未消费

检查：

- NameServer 地址；
- Broker 是否启动；
- Topic 和 Consumer Group；
- Listener 所在服务是否注册并运行；
- 消息 JSON 是否符合消费者解析要求。

### 14.6 Elasticsearch 无结果

检查：

- ES 是否健康；
- 索引是否存在；
- 种子数据是否同步；
- 分词器是否实际安装；
- 搜索服务日志。

---

## 15. 答辩演示建议

推荐控制在 8～10 分钟：

1. 展示架构图和范围控制；
2. 展示 Nacos 核心服务注册；
3. Postman 登录并获取 Token；
4. 商品查询、购物车、下单；
5. 展示订单调用商品和库存；
6. 模拟支付结果并查看订单、库存变化；
7. JMeter 展示秒杀限流或订单负载；
8. 展示一次服务停止/恢复或配置热更新；
9. 总结实测数据和已知限制。

不演示未完成的 Docker 全栈或 K8s 全栈。
