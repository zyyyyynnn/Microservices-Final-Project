# MallCloud Agent 工作协议

> 团队规模：5 人
> 最高标准：`docs/PROJECT_STANDARD.md`
> 默认环境：Windows 11、PowerShell 7+、UTF-8、JDK 21、Maven 3.9+

---

## 1. 必读顺序

执行任何代码、配置或文档修改前，按顺序阅读：

1. `docs/PROJECT_STANDARD.md`
2. `docs/PRD.md`
3. `docs/ARCHITECTURE.md`
4. `docs/API.md`
5. `docs/DATABASE.md`
6. `docs/CODING_STYLE.md`
7. `docs/DEPLOY.md`
8. `docs/QUICK_START.md`

测试任务还需阅读：

- `docs/test/README.md`
- `docs/FINAL_REPORT.md`

---

## 2. 当前技术基线

| 技术 | 版本 |
|---|---:|
| Java | 21 LTS |
| Spring Boot | 3.2.4 |
| Spring Cloud | 2023.0.1 |
| Spring Cloud Alibaba | 2023.0.1.0 |
| Nacos | 2.3.2 |
| Sentinel | 1.8.6 |
| RocketMQ | 5.1.4 |
| Seata Server | 2.0.0 |
| Redis | 7 |
| Elasticsearch | 8.11 |
| MySQL | 8.0 |

规则：

- 所有构建、IDE 和测试使用 JDK 21；
- 不使用 Java 21 预览特性；
- 不迁移 Java 24；
- 不借 JDK 升级批量重写现有代码；
- 虚拟线程默认关闭，只有完成基线压测后才允许单服务对比测试。

---

## 3. 当前项目状态

- 微服务结构和主要代码已建立；
- 配置、构建和核心链路需要验证；
- 父 POM 已切换到 Java 21；
- Docker/K8s 的 Seata 镜像已切换到 2.0.0；
- Postman 最终集合需要重建；
- JMeter 脚本和报告尚未完成；
- Docker/Kubernetes 全栈不是正式交付路径；
- 演示账号统一密码为 `123456`。

不得假定某项能力已经完成，必须以代码、配置和运行结果为依据。

---

## 4. 执行顺序

```text
修复事实错误
  → 验证 Java 21 构建
  → 验证 Seata 2.0.0
  → 完善必要实现
  → 编译/运行验证
  → 补针对性测试
  → 更新报告
```

禁止：

- 先新增功能再修核心缺陷；
- 为扩大规模新增微服务；
- 为所有 Feign Client 机械增加 fallback；
- 引入与评分无直接关系的新框架；
- 把规划能力写成已实现；
- 使用不可执行命令、固定伪 Token 或伪性能结果。

---

## 5. 完成标准

每个任务开始前确定：

1. 要解决的问题；
2. 允许修改的文件；
3. 完成后的可观察结果；
4. 验证命令或测试；
5. 需要同步的文档。

任务完成必须满足：

- 修改范围与需求一致；
- 在 JDK 21 下编译通过，或明确说明失败原因；
- 核心场景通过测试；
- 相关文档同步；
- 未验证项明确记录；
- 未引入无关重构和额外功能。

---

## 6. 当前优先级

### P0：版本与基础可运行

1. 验证 `java -version` 与 `mvn -version` 均为 Java 21；
2. 执行全模块构建；
3. 验证 Seata Server 2.0.0 启动、注册与配置；
4. 修复 Nacos YAML 和配置加载；
5. 验证数据库初始化；
6. 验证核心服务注册和 Gateway JWT。

### P1：交易主链路

1. 登录并查询用户；
2. 查询商品；
3. 购物车；
4. 创建订单；
5. 商品和库存 Feign；
6. Seata 回滚；
7. 支付消息；
8. 订单和库存状态更新。

### P2：课程技术亮点

1. Sentinel 限流/熔断；
2. 秒杀限购和库存边界；
3. Elasticsearch 搜索；
4. Nacos 热更新。

### P3：测试和答辩

1. 重建真实 Postman 集合；
2. 建立三套 JMeter 脚本；
3. 保存结果、截图和环境信息；
4. 填写 `docs/FINAL_REPORT.md`；
5. 整理 5 人分工和演示脚本。

---

## 7. 代码修改规则

### 7.1 精准修改

- 只改完成任务必须修改的内容；
- 不顺手统一全项目命名；
- 不大范围重构现有模块；
- 不改变与任务无关的接口或数据库结构；
- 保持现有 Java 包和模块风格。

### 7.2 微服务边界

- 业务服务只访问自己的数据库；
- 跨服务同步调用使用 OpenFeign；
- 异步状态使用 RocketMQ；
- 不新增服务；
- 不形成循环 Feign 调用；
- 聚合服务不复制核心业务写逻辑。

### 7.3 失败处理

- 核心失败必须返回失败；
- 不通过 fallback 返回伪成功；
- 不 catch 后吞掉事务异常；
- 不使用固定业务数据掩盖下游不可用；
- 重试仅用于明确幂等操作。

### 7.4 事务

- 本地多表写使用本地事务；
- 实际跨服务一致性使用 Seata；
- Seata Server 基线为 2.0.0；
- 文档事务参与者必须与代码调用一致；
- MQ 最终一致性流程不强行改成全局事务。

---

## 8. 配置规则

- YAML 注释使用 `#`；
- 禁止使用 `--`；
- 不提交真实密钥或 Token；
- 不提交个人绝对路径；
- Auth 与 Gateway 的 JWT 密钥一致；
- 修改 Nacos 配置后验证加载和热更新；
- 不假定根目录 `.env` 自动注入 Spring Boot；
- 所有 IDE Run Configuration 使用 JDK 21。

---

## 9. 测试规则

### 9.1 环境验证

```powershell
java -version
mvn -version
docker inspect mall-seata --format '{{.Config.Image}}'
```

预期：

- Java 21；
- Maven 使用 Java 21；
- Seata 镜像 `seataio/seata-server:2.0.0`。

### 9.2 必测场景

- 登录成功和失败，测试密码为 `123456`；
- Gateway 无 Token、错误 Token、有效 Token；
- 商品查询；
- 创建订单；
- 库存不足；
- 订单和库存分布式回滚；
- 支付结果重复消费；
- 秒杀重复请求；
- 下游服务停止；
- Nacos 热更新。

### 9.3 禁止方式

- 生成大量重复请求充数；
- 使用 `dummy` 请求体；
- 使用固定伪 Token；
- 只断言 HTTP 200；
- 填写未运行的 P95 或吞吐量；
- 隐藏失败请求。

---

## 10. Git 与提交

```text
<type>(<scope>): <subject>
```

示例：

```text
build: upgrade project to Java 21
chore(deploy): upgrade Seata server to 2.0.0
chore(seed): unify test account password
test(order): verify Seata rollback
docs(report): record Java 21 load-test results
```

每个提交保持单一主题。

---

## 11. 输出要求

完成任务后说明：

- 修改了什么；
- 为什么修改；
- 如何验证；
- 实际验证结果；
- 哪些内容未验证；
- 影响了哪些文档。

不得使用“应该可以”作为验证结果。
