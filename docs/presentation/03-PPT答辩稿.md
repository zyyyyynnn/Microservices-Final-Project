# 03 PPT 答辩稿

各位老师好，我们的项目是 MallCloud 微商城，一个基于 Spring Cloud Alibaba 的微服务课程项目。项目没有追求堆砌功能，而是围绕一条完整的电商主链路，把 Gateway、服务调用、分布式事务、消息队列、搜索、秒杀和前端验收组织成可运行、可测试、可解释的交付。

系统入口是 Spring Cloud Gateway，端口为 9100。前端所有业务请求都通过 `/api/v1/**` 进入 Gateway，再路由到后端服务。后端按认证、用户、商品、购物车、订单、库存、支付、消息、搜索、秒杀、任务、后台聚合等模块拆分，并使用 Nacos 做注册和配置，Seata 处理订单和库存的一致性，RocketMQ 处理支付结果消息，Redis 和 Sentinel 支撑秒杀入口，Elasticsearch 支撑商品搜索。

普通交易链路从用户登录开始。用户搜索商品、进入详情、加入购物车并创建订单。订单服务会查询商品价格，再调用库存服务锁定库存，创建订单时通过 Seata 管理一致性。支付是课程项目中的模拟支付，支付成功后发布 RocketMQ 消息，订单服务消费消息标记已支付，库存服务消费消息确认扣减。

Gateway 层我们重点做了两类防护。一类是 JWT 鉴权和用户上下文透传；另一类是 internal 内部接口阻断，外部请求访问 `/internal/` 路径会被 Gateway 拦截并返回 404。同时，RANDOM_PORT 测试验证了真实 running server 下 `X-Internal-*` 请求头会在转发下游前被净化，Authorization 也不能绕过 internal 阻断。

前端使用 Vue 3、Vite、TypeScript 和 Element Plus。最终页面截图覆盖首页、搜索、商品详情、购物车、订单详情、支付、秒杀，以及 Admin Dashboard、后台订单、后台商品。这里需要明确一个边界：当前 Admin 后台只有 `/admin` 一个页面，Dashboard、订单、商品都是同页区块，没有独立用户管理页面，所以我们没有伪造用户管理截图。

验收方面，当前最终报告保留了真实可复核结论：Gateway 测试 42/42 通过，RANDOM_PORT Gateway 测试 9/9 通过，运行时回归 14/14 通过，启动日志记录 13 个后端服务 health=UP，全模块 Maven package 通过，前端 build 通过，页面截图完成 PIL 尺寸和 MD5 校验。

最后说明边界：本项目是课程项目和本地演示系统，不宣称生产级安全或生产级高可用；full profile、外部基础设施和未实现页面都按最终报告中的真实结果记录。后续如果继续迭代，可以补独立 Admin 用户管理页面、更完整的浏览器自动化回归和容器化全链路部署。
