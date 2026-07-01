# 02 PPT 大纲

## 1. 项目概览

- 标题：MallCloud 微商城
- 关键词：Spring Cloud Alibaba、微服务课程项目、本地可运行、真实验证
- 配图：系统总体架构图

## 2. 业务主链路

- 登录、商品、购物车、订单、库存、支付、订单查询
- 配图：核心交易链路图

## 3. 微服务拆分

- Gateway、Auth、User、Product、Cart、Order、Inventory、Pay、Message、Search、Seckill、Job、Admin Biz
- 强调 `mall-common` 是公共模块，不作为业务服务单独演示

## 4. Gateway 与安全边界

- JWT 鉴权
- internal 路径外部阻断
- `X-Internal-*` 请求头净化
- 配图：Gateway 鉴权与内部路径防护图

## 5. 分布式事务与库存一致性

- Seata 全局事务
- 订单写入与库存锁定
- 失败回滚边界
- 配图：Seata 订单-库存一致性图

## 6. 支付消息与异步处理

- 模拟支付
- RocketMQ `PAY_RESULT`
- 订单标记已支付与库存确认扣减
- 配图：RocketMQ 支付结果处理图

## 7. 搜索与秒杀

- Elasticsearch 搜索验证
- Redis 秒杀库存与请求状态
- Sentinel 限流/熔断验证
- 配图：秒杀请求处理与限流图

## 8. 前端与 Admin 后台

- Vue 3 + Element Plus
- 前端统一走 Gateway
- Admin 当前只有 `/admin` 同页 Dashboard、后台订单、后台商品
- 配图：页面截图矩阵

## 9. 测试与验收证据

- Gateway 42/42 PASS
- RANDOM_PORT 9/9 PASS
- 运行时回归 14/14 PASS
- 13 服务 health=UP
- Maven package PASS、前端 build PASS
- 截图 PIL/MD5

## 10. 边界与后续计划

- 非生产级系统
- 未实现 Admin 用户管理页面
- full profile 与外部基础设施能力按真实结果表述
- 后续可补：后台独立路由、更多自动化浏览器回归、容器化全链路稳定性
