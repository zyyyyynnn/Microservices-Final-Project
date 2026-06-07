# MallCloud 快速启动指南

> 5 分钟跑通全栈演示版（仅中间件 + 核心服务）

---

## 0. 准备

| 工具      | 版本     |
| --------- | -------- |
| JDK       | 17+      |
| Maven     | 3.9+     |
| Docker    | 24+      |
| Node.js   | 20+      |
| 内存      | ≥ 8 GB   |
| 磁盘      | ≥ 20 GB  |

---

## 1. 克隆与初始化

```bash
git clone <repo>
cd mallcloud
```

拷贝环境变量模板：

```bash
cp .env.example .env
# Windows: copy .env.example .env
```

---

## 2. 启动中间件（一行命令）

```bash
cd deploy/docker
docker compose -f docker-compose.middleware.yml up -d
```

等待所有容器 healthy（约 1-2 分钟）：

```bash
docker compose -f docker-compose.middleware.yml ps
```

涉及中间件：
- MySQL 8
- Redis 7
- Nacos 2.3
- RocketMQ 5.x (NameSrv + Broker + Console)
- Elasticsearch 8
- Sentinel Dashboard
- Zipkin
- Seata Server

---

## 3. 初始化数据库

```bash
# 创建库 + 表 + 种子
bash scripts/init-db.sh

# Windows
.\scripts\init-db.ps1
```

预期：创建 7 个业务库（auth/user/product/inventory/order/pay/seckill）+ 1 个 Seata 库（mall_seata）、35 张表（25 业务 + 7 undo_log + 3 Seata）、30 个类目、5 个 SPU、7 个 SKU、10 个测试用户、3 场秒杀活动。

测试账号：

| 用户名     | 密码        | 角色     |
| ---------- | ----------- | -------- |
| zhangsan   | P@ssw0rd123 | USER     |
| lisi       | P@ssw0rd123 | USER     |
| merchant01 | P@ssw0rd123 | MERCHANT |
| admin      | Admin@123   | ADMIN    |

---

## 4. 启动后端服务

### 方式 A：本地 IDE 启动

依次启动以下服务（端口从小到大，避免冲突）：

| 顺序 | 服务              | 端口  |
| ---- | ----------------- | ----- |
| 1    | mall-gateway      | 9000  |
| 2    | mall-auth         | 9001  |
| 3    | mall-user         | 9002  |
| 4    | mall-product      | 9003  |
| 5    | mall-inventory    | 9004  |
| 6    | mall-cart         | 9005  |
| 7    | mall-order        | 9006  |
| 8    | mall-pay          | 9007  |
| 9    | mall-search       | 9008  |
| 10   | mall-seckill      | 9009  |
| 11   | mall-message      | 9010  |
| 12   | mall-admin-biz    | 9011  |
| 13   | mall-job          | 9012  |

### 方式 B：Docker Compose 一键

```bash
cd deploy/docker
docker compose -f docker-compose.all.yml up -d
```

### 验证

打开 Nacos 控制台：http://localhost:8848/nacos （nacos/nacos）
应能看到 13 个服务全部注册成功。

---

## 5. 启动前端

```bash
# 用户前台
cd web-portal
npm install
npm run dev
# → http://localhost:5173

# 商家后台
cd web-admin
npm install
npm run dev
# → http://localhost:5174
```

---

## 6. 第一笔订单（5 分钟体验）

1. 打开 http://localhost:5173
2. 点击"登录" → 账号 `zhangsan` / 密码 `P@ssw0rd123`
3. 首页搜索"iPhone" → 选商品 → 加入购物车
4. 购物车 → 结算 → 选地址 → 提交订单
5. 跳转到支付页 → 点击"模拟支付"
6. 回到用户中心 → 我的订单 → 看到状态"已支付"

---

## 7. 关键中间件控制台

| 中间件    | 地址                                | 账号          |
| --------- | ----------------------------------- | ------------- |
| Nacos     | http://localhost:8848/nacos         | nacos/nacos   |
| Sentinel  | http://localhost:8080               | sentinel/sentinel |
| Zipkin    | http://localhost:9411               | -             |
| Kibana    | http://localhost:5601               | -             |
| Seata     | http://localhost:7091               | -             |
| RocketMQ  | http://localhost:8180               | -             |

---

## 8. 验证分布式特性

### 8.1 验证 Nacos 服务注册

```
打开 Nacos → 服务管理 → 服务列表
应能看到 mall-* 13 个服务，每个有 1+ 个健康实例
```

### 8.2 验证 Gateway 鉴权

```bash
# 不带 token 访问 → 401
curl -i http://localhost:9000/api/v1/orders

# 带 token 访问 → 200
curl -i -H "Authorization: Bearer xxx" http://localhost:9000/api/v1/orders
```

### 8.3 验证 OpenFeign 远程调用

```bash
# 下单会调用 inventory, product, user 三个服务
# 看 order 服务日志：
docker logs -f mall-order
# 出现 "调用库存服务 lock 成功"
```

### 8.4 验证 Seata 分布式事务

```bash
# 修改商品库存为 1，并发 5 个下单请求
# 1 个成功，4 个失败
# 失败的请求对应的库存不会被扣减（自动回滚）
```

### 8.5 验证 Sentinel 限流

```bash
# 启动 JMeter，500 并发访问秒杀接口
# Sentinel Dashboard → 实时监控 → 看到 pass/block 数量
# 限流触发时返回 429
```

### 8.6 验证 Nacos 配置热更新

```bash
# Nacos 控制台 → 配置管理 → mall-product.yaml
# 修改 mall.product.page-size=20 → 30
# 不重启服务，新值立即生效
```

---

## 9. 停止全栈

```bash
cd deploy/docker
docker compose -f docker-compose.all.yml down
# 同时删除 volumes（注意：会丢数据）
docker compose -f docker-compose.all.yml down -v
```

---

## 10. 常见 30 秒修复

| 问题                     | 解决                                              |
| ------------------------ | ------------------------------------------------- |
| 启动报 "Address in use"  | 杀掉占用端口的进程：`lsof -i :9000`              |
| Nacos 反复注册/下线      | 检查心跳时间 + 网卡                              |
| Feign 调不通             | 检查下游服务 `actuator/health`                     |
| 启动报 Seata 找不到      | 确认 Seata Server 已启动并健康                    |
| RocketMQ 连不上          | 检查 broker.conf 中 brokerIP1                     |
| ES 中文搜索无结果        | 确认 IK 分词器已安装                              |
| 前端跨域                 | Gateway 已配 CORS；或前端 dev proxy                |
| 测试看不到 trace         | 检查 Zipkin URL 配置                              |

---

## 11. 接下来

- 跑 Postman 测试集合：`docs/test/postman-collection.json`
- 跑 JMeter 压测：`docs/test/jmeter/mallcloud.jmx`
- 查看架构细节：`docs/ARCHITECTURE.md`
- 查看接口：`docs/API.md`
- 查看数据库：`docs/DATABASE.md`
- 部署到 K8s：`docs/DEPLOY.md`

---

**祝启动顺利！**
