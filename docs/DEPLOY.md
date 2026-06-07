# MallCloud 部署文档

> 版本：v1.0.0
> 适用场景：本地开发 / 联调测试 / 演示答辩

---

## 1. 部署总览

| 环境       | 用途         | 部署方式                  | 一键脚本             |
| ---------- | ------------ | ------------------------- | -------------------- |
| dev        | 个人开发     | 本地 IDE + 远程中间件      | `scripts/dev-up.sh`  |
| test       | 联调         | Docker Compose 全栈        | `scripts/test-up.sh` |
| demo       | 演示 / 答辩 | K8s (minikube)             | `scripts/demo-up.sh` |

---

## 2. 前置要求

| 软件          | 版本       | 用途                       |
| ------------- | ---------- | -------------------------- |
| JDK           | 17         | 编译运行                   |
| Maven         | 3.9+       | 构建                       |
| Node.js       | 20+        | 前端构建                   |
| Docker        | 24+        | 容器化                     |
| Docker Compose | 2.20+     | 编排                       |
| minikube      | 1.32+      | K8s 本地集群（demo 环境）   |
| kubectl       | 与 K8s 同步 | 集群管理                    |
| Helm          | 3.x        | K8s 包管理（可选）          |
| Git           | 2.30+      | 版本管理                    |
| Postman       | 10+        | 接口测试                    |
| JMeter        | 5.6+       | 性能测试                    |

---

## 3. 本地开发环境

### 3.1 启动中间件（仅中间件）

```bash
cd deploy/docker
docker compose -f docker-compose.middleware.yml up -d
```

包含：MySQL / Redis / Nacos / RocketMQ / Elasticsearch / Sentinel Dashboard / Zipkin / Seata Server

### 3.2 初始化数据库

```bash
# 1. 创建所有库
mysql -h127.0.0.1 -uroot -proot < db/init/00-create-databases.sql

# 2. 逐个执行 DDL（用 scripts/init-db.sh 一键执行）
for db in auth user product inventory order pay seckill; do
  mysql -h127.0.0.1 -uroot -proot mall_$db < db/init/mall_$db.sql
done

# 3. 种子数据
mysql -h127.0.0.1 -uroot -proot < db/init/seed.sql
```

### 3.3 启动微服务

```bash
# 编译
mvn clean install -DskipTests

# 启动某个服务
cd mall-gateway && mvn spring-boot:run
```

或使用 IDE 启动 `Application` 类。

### 3.4 启动前端

```bash
# 用户前台
cd web-portal
npm install
npm run dev          # http://localhost:5173

# 商家后台
cd web-admin
npm install
npm run dev          # http://localhost:5174
```

---

## 4. Docker Compose 部署（test 环境）

### 4.1 启动全栈

```bash
cd deploy/docker
./start-all.sh       # Linux/macOS
# 或
./start-all.ps1      # Windows PowerShell
```

包含：13 个微服务 + 8 个中间件，一键启动。

### 4.2 服务访问入口

| 服务                | 地址                                  | 备注               |
| ------------------- | ------------------------------------- | ------------------ |
| 用户前台            | http://localhost                     | Nginx 80 端口      |
| 商家后台            | http://localhost/admin                |                    |
| API 网关            | http://localhost:9000                  |                    |
| Nacos 控制台        | http://localhost:8848/nacos            | nacos/nacos         |
| Sentinel Dashboard  | http://localhost:8080                  | sentinel/sentinel   |
| Zipkin UI           | http://localhost:9411                  |                    |
| Kibana              | http://localhost:5601                  | 日志（可选）        |
| RocketMQ Console    | http://localhost:8180                  |                    |
| Seata 控制台        | http://localhost:7091                  | 1.8 后内置          |

### 4.3 停止全栈

```bash
./stop-all.sh
```

### 4.4 查看日志

```bash
docker compose -f docker-compose.all.yml logs -f mall-gateway
```

---

## 5. Kubernetes (minikube) 部署（demo 环境）

### 5.1 启动 minikube

```bash
minikube start --driver=docker --cpus=4 --memory=8192
minikube addons enable ingress
minikube dashboard
```

### 5.2 镜像准备

```bash
# 在 minikube Docker daemon 中构建
eval $(minikube docker-env)

# 批量构建并打 tag
./scripts/build-images.sh
```

### 5.3 部署中间件

```bash
kubectl apply -f deploy/k8s/00-namespace.yaml
kubectl apply -f deploy/k8s/01-mysql.yaml
kubectl apply -f deploy/k8s/02-redis.yaml
kubectl apply -f deploy/k8s/03-nacos.yaml
kubectl apply -f deploy/k8s/04-rocketmq.yaml
kubectl apply -f deploy/k8s/05-elasticsearch.yaml
kubectl apply -f deploy/k8s/06-seata.yaml
```

### 5.4 部署业务服务

```bash
for svc in auth user product inventory cart order pay search seckill message admin-biz job; do
  kubectl apply -f deploy/k8s/services/mall-$svc.yaml
done
kubectl apply -f deploy/k8s/services/mall-gateway.yaml
```

### 5.5 部署 Ingress

```bash
kubectl apply -f deploy/k8s/ingress.yaml
```

### 5.6 访问服务

```bash
# 获取 minikube IP
minikube ip   # 192.168.49.2

# 浏览器访问（hosts 绑定）
echo "192.168.49.2 mallcloud.local" | sudo tee -a /etc/hosts
open http://mallcloud.local
```

### 5.7 扩缩容演示

```bash
# 秒杀压测前手动扩容
kubectl scale deployment mall-seckill --replicas=5
kubectl scale deployment mall-order --replicas=5

# 压测后缩容
kubectl scale deployment mall-seckill --replicas=1
```

---

## 6. 环境变量清单

所有服务通用：

| 变量               | 必填 | 默认                    | 说明                          |
| ------------------ | ---- | ----------------------- | ----------------------------- |
| SPRING_PROFILES_ACTIVE | 否 | dev                  | 环境标识                      |
| NACOS_SERVER       | 是   | nacos:8848              | 注册中心地址                  |
| MYSQL_HOST         | 是   | mysql                   | 数据库地址                    |
| MYSQL_PORT         | 否   | 3306                    |                               |
| MYSQL_DB           | 是   | -                       | 数据库名                      |
| MYSQL_USER         | 否   | root                    |                               |
| MYSQL_PWD          | 是   | -                       |                               |
| REDIS_HOST         | 是   | redis                   |                               |
| REDIS_PORT         | 否   | 6379                    |                               |
| JWT_SECRET         | 是   | -                       | JWT 签名密钥                  |
| SEATA_TC_URL       | 否   | seata:8091              | Seata TC 地址                 |
| SENTINEL_DASHBOARD | 否   | sentinel:8080           | Sentinel Dashboard            |
| ZIPKIN_URL         | 否   | http://zipkin:9411      | 链路追踪                      |
| LOG_PATH           | 否   | /var/log/mallcloud     | 日志路径                      |

---

## 7. 健康检查端点

| 路径                | 说明                       |
| ------------------- | -------------------------- |
| /actuator/health    | 综合健康                   |
| /actuator/health/liveness  | 存活探针             |
| /actuator/health/readiness | 就绪探针             |
| /actuator/info      | 服务元信息                 |
| /actuator/prometheus| 指标                       |

K8s 配置示例：

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 9001
  initialDelaySeconds: 30
  periodSeconds: 10
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 9001
  initialDelaySeconds: 20
  periodSeconds: 5
```

---

## 8. 常见问题

### 8.1 服务注册失败

- 检查 Nacos 是否启动：`docker ps | grep nacos`；
- 检查 `spring.cloud.nacos.server-addr` 是否正确；
- 检查 namespace 是否存在。

### 8.2 Feign 调用超时

- 调大 `feign.client.config.read-timeout`；
- 检查下游服务是否健康：`/actuator/health`。

### 8.3 Seata 事务不回滚

- 检查 undo_log 表是否存在；
- 检查 Seata Server 是否在 Nacos 注册成功；
- 全局异常是否被 catch 吞掉。

### 8.4 RocketMQ 消息堆积

- 增加消费者实例数；
- 调整 `consumeThreadMin/Max`；
- 检查消费者业务是否有慢 SQL。

### 8.5 ES 搜索无结果

- 检查索引是否存在：`curl http://es:9200/_cat/indices`；
- 检查数据是否同步：监听 `ES_SYNC` Topic 消费日志；
- 重新全量：`POST /mall_product/_reindex`。

### 8.6 JWT 401

- 检查 Header 格式：`Authorization: Bearer {token}`（注意空格）；
- 检查 token 是否过期：用 refreshToken 刷新；
- 检查密钥是否一致：所有服务 `JWT_SECRET` 必须相同。

### 8.7 容器间通信

- 容器必须加入同一 docker network；
- 服务间用**服务名**访问，不要用 `localhost`；
- K8s 中使用 Service 名称。

---

## 9. 监控与运维命令

```bash
# 查看所有服务状态
docker compose -f deploy/docker/docker-compose.all.yml ps

# 实时跟踪所有日志
docker compose -f deploy/docker/docker-compose.all.yml logs -f

# 进入容器排查
docker exec -it mall-mysql mysql -uroot -proot

# 查看 Seata 全局事务
docker exec -it mall-seata curl http://localhost:7091/api/v1/overview/metrics

# K8s 排查
kubectl get pods -n mallcloud
kubectl describe pod mall-order-xxx
kubectl logs -f mall-order-xxx
```

---

## 10. 升级与回滚

### Docker Compose

```bash
# 滚动重启单个服务
docker compose up -d --no-deps --build mall-gateway

# 回滚到上一个版本
git checkout HEAD~1 -- mall-gateway
docker compose up -d --no-deps --build mall-gateway
```

### K8s

```bash
# 查看历史版本
kubectl rollout history deployment/mall-order

# 回滚
kubectl rollout undo deployment/mall-order

# 查看状态
kubectl rollout status deployment/mall-order
```

---

## 11. 备份策略

| 数据        | 方式                | 频率         | 保留时间 |
| ----------- | ------------------- | ------------ | -------- |
| MySQL       | mysqldump + 压缩     | 每天凌晨 2 点 | 30 天   |
| Nacos 配置   | API 导出            | 每次发布前   | 永久     |
| Redis       | RDB + AOF            | 实时 + 5min  | 7 天    |
| ES          | snapshot + 远端仓库  | 每天         | 30 天   |
| RocketMQ    | broker 同步双写      | 实时         | -        |

---

## 12. 演示剧本（答辩用）

1. **0:00 - 1:00** 项目介绍 PPT（架构图 + 技术栈）
2. **1:00 - 3:00** 启动全栈（Docker Compose 一行命令）
3. **3:00 - 5:00** 用户前台演示：浏览 → 搜索 → 加购 → 下单 → 支付
4. **5:00 - 7:00** 后台演示：商家登录 → 上下架商品 → 查订单
5. **7:00 - 8:00** 秒杀演示：JMeter 1000 并发 → Dashboard 看限流
6. **8:00 - 9:00** 异常演示：kill 订单服务 → Sentinel 熔断 → 恢复
7. **9:00 - 10:00** 总结亮点

---

**—— 文档结束 ——**
