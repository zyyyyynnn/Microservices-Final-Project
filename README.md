# MallCloud 微商城

> Spring Cloud Alibaba 全家桶电商微服务系统（微服务期末大作业）
> 4 人团队 / 13 个微服务 / 7 库分库 / 秒杀 + ES 搜索 + Seata 分布式事务

---

## 📚 文档导航

| 文档 | 说明 |
|---|---|
| [PRD.md](docs/PRD.md) | 需求规格说明书 |
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | 架构详细设计 |
| [API.md](docs/API.md) | 接口文档 |
| [DATABASE.md](docs/DATABASE.md) | 数据库设计 |
| [DEPLOY.md](docs/DEPLOY.md) | 部署文档 |
| [CODING_STYLE.md](docs/CODING_STYLE.md) | 编码规范 |
| [QUICK_START.md](docs/QUICK_START.md) | 快速启动 |

## 🚀 一分钟体验

```bash
# 1. 启动中间件
bash scripts/start-middleware.sh

# 2. 初始化数据库
bash scripts/init-db.sh

# 3. 启动后端（IDE 或 docker）
# 4. 启动前端
cd web-portal && npm install && npm run dev
```

打开浏览器：
- 用户前台：http://localhost:5173
- 商家后台：http://localhost:5174
- Nacos 控制台：http://localhost:8848/nacos （nacos/nacos）
- Sentinel 控制台：http://localhost:8080 （sentinel/sentinel）

## 🛠 技术栈

**后端**
- Spring Boot 3.2 + Spring Cloud 2023 + Spring Cloud Alibaba 2023
- Nacos（注册/配置中心）
- Sentinel（限流/熔断）
- Seata（分布式事务）
- Spring Cloud Gateway（网关）
- OpenFeign（远程调用）
- RocketMQ 5（消息队列）
- Redis 7（缓存/分布式锁）
- Elasticsearch 8（全文搜索）
- MyBatis-Plus 3.5（ORM）
- MySQL 8（数据库）
- JWT（统一鉴权）
- Micrometer + Zipkin（链路追踪）
- Docker Compose + K8s(minikube)（部署）

**前端**
- Vue 3 + Vite + TypeScript
- Element Plus（后台）
- Vant（前台，备选）
- Axios + Pinia + Vue Router

## 📁 目录结构

```
mallcloud/
├── docs/                      # 项目文档
├── db/init/                   # 数据库初始化脚本
├── deploy/                    # 部署相关
│   ├── docker/                # Docker Compose
│   ├── k8s/                   # Kubernetes
│   ├── nacos/                 # Nacos 配置模板
│   └── sentinel/              # Sentinel 规则
├── scripts/                   # 运维脚本
├── mall-common/               # 公共模块
├── mall-gateway/              # 网关
├── mall-auth/                 # 认证
├── mall-user/                 # 用户
├── mall-product/              # 商品
├── mall-inventory/            # 库存
├── mall-cart/                 # 购物车
├── mall-order/                # 订单
├── mall-pay/                  # 支付
├── mall-search/               # 搜索
├── mall-seckill/              # 秒杀
├── mall-message/              # 消息
├── mall-admin-biz/            # 后台业务
├── mall-job/                  # 定时任务
├── web-portal/                # 用户前台
├── web-admin/                 # 商家后台
└── pom.xml                    # 父 POM
```

## 👥 团队分工（4 人）

| 成员 | 角色 | 负责 |
|---|---|---|
| 张三 | 架构师 | mall-common / mall-gateway / mall-auth / 部署 |
| 李四 | 商品负责人 | mall-product / mall-search / mall-category |
| 王五 | 交易负责人 | mall-order / mall-pay / mall-inventory / seckill |
| 赵六 | 前端 & 后台 | web-portal / web-admin / mall-admin-biz / 文档 |

## 🎯 评分对照

| 评分项 | 我们的实现 |
|---|---|
| 项目功能与完整性 (20) | 13 个微服务，覆盖商品/订单/支付/搜索/秒杀 |
| 技术规范与架构设计 (20) | Spring Cloud Alibaba 完整体系，Nacos + Sentinel + Seata + Gateway + OpenFeign + RocketMQ |
| 代码测试 (25) | Postman ≥ 6 接口 / ≥ 20 请求 + JMeter 负载 + 压力 + 异常 + 配置热更新 |
| 代码质量与注释 (15) | Alibaba 编码规范 + 完整 JavaDoc + 统一异常 |
| 文档与报告质量 (10) | 7 大文档 + 图文并茂 + ER / 时序 / 架构图 |
| 演示与答辩 (10) | Docker Compose 一键起 + minikube 演示 + PPT |

## 📝 License

仅用于课程学习。
