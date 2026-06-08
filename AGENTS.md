# MallCloud Agent 工作协议

> 最高标准：`docs/PROJECT_STANDARD.md`
> 当前阶段任务：`DEVELOPMENT_PROMPT.md`
> 默认环境：Windows 11、PowerShell 7+、UTF-8、JDK 21、Maven 3.9+

---

## 1. 文档优先级与必读顺序

执行任何代码、配置、测试或文档修改前，先按任务范围阅读权威文档。

基础文档：

1. `docs/PROJECT_STANDARD.md`
2. `docs/PRD.md`
3. `docs/ARCHITECTURE.md`
4. `docs/API.md`
5. `docs/DATABASE.md`
6. `docs/CODING_STYLE.md`
7. `docs/DEPLOY.md`
8. `docs/QUICK_START.md`

专项文档：

- 当前阶段任务：`DEVELOPMENT_PROMPT.md`
- 产品、前端、页面、路由、交互或文案：`DESIGN.md`
- 测试方法：`docs/test/README.md`
- 当前测试结果和答辩材料：`docs/FINAL_REPORT.md`
- 脚本入口和参数：`scripts/README.md`

若代码、脚本、配置和文档冲突，先核对实际文件与可运行行为，再修改最小必要范围；不确定时明确标记为未验证。

---

## 2. 任务执行原则

- 只改完成任务必须修改的内容。
- 不顺手重构，不做无关优化，不扩大改动范围。
- 保持现有风格、命名、目录和项目习惯。
- 不新增无评分价值的功能、微服务、框架或组件库。
- 不把规划能力写成已完成。
- 不把脚本存在、接口存在、页面入口存在写成已验证通过。
- 不使用 mock、固定 Token、伪业务数据或不可执行命令伪造成功。
- 未执行的测试不得填写 P95、吞吐量、错误率或通过率。

每个任务开始前明确：

1. 要解决的问题；
2. 允许修改的文件；
3. 完成后的可观察结果；
4. 验证命令或测试；
5. 需要同步的文档。

---

## 3. 修改范围与代码边界

- 微服务结构和主要模块边界保持稳定。
- 业务服务只访问自己的数据库。
- 跨服务同步调用使用 OpenFeign，异步状态使用 RocketMQ。
- 不新增服务，不形成循环 Feign 调用。
- 聚合服务不复制核心业务写逻辑。
- 核心失败必须返回失败，不通过 fallback 返回伪成功。
- 不 catch 后吞掉事务异常。
- 重试仅用于明确幂等操作。
- 本地多表写使用本地事务，跨服务一致性使用 Seata。
- MQ 最终一致性流程不强行改成全局事务。

---

## 4. 配置与安全规则

- YAML 注释只使用 `#`。
- 不提交真实密钥、Token、个人绝对路径或本机专属配置。
- Auth 与 Gateway 的 JWT 密钥必须一致。
- 修改 Nacos 配置后必须验证加载；热更新必须单独实测。
- 不假定根目录 `.env` 自动注入 Spring Boot。
- 所有构建、IDE 和测试使用 JDK 21。

---

## 5. 测试与验证规则

验证优先级：

1. 运行或复现；
2. 自动化测试；
3. 静态检查；
4. 最小手动验证。

测试要求：

- 同时断言 HTTP 状态和业务码。
- 登录、Gateway 鉴权、商品、购物车、订单、库存、支付消息、秒杀、Nacos、Sentinel、JMeter 等测试按 `docs/test/README.md` 执行。
- 当前真实结果只写入 `docs/FINAL_REPORT.md`。
- 失败项不得隐藏，不得写成通过。
- 未运行 Newman/JMeter 时，不得填写精确结果。

常用静态检查：

```powershell
git diff --check
git status --short
```

---

## 6. 前端交付规则

- `DESIGN.md` 是产品流程、用户交互、页面状态、角色权限和设计约束的基线。
- 前端业务请求必须经 Gateway `/api/v1/**`，不得直接调用内部微服务端口。
- 不得用 raw JSON、`<pre>`、`JSON.stringify` 或接口调试台作为业务页面主要内容。
- 不得用 mock 伪装后端未完成能力。
- 商品、购物车、订单、支付、秒杀、后台页面必须面向真实用户任务。
- 适用页面必须覆盖 loading、empty、error、disabled、success 状态。
- 浏览器工具可用时，用户可见变更必须做真实浏览器验证。
- 后端未提供或未联调的能力必须标记为受后端限制或待联调。

---

## 7. CodeGraph MCP

项目已接入 CodeGraph MCP。涉及代码理解、调用关系、影响面或符号定位时，应优先使用 CodeGraph，再做必要的文件级核对。

常用能力：

| 工具 | 用途 |
|---|---|
| `codegraph_status` | 检查索引健康 |
| `codegraph_files` | 查看索引文件结构 |
| `codegraph_search` | 搜索符号 |
| `codegraph_explore` | 批量读取相关符号源码 |
| `codegraph_callers` / `codegraph_callees` | 分析调用关系 |
| `codegraph_impact` | 分析影响面 |

日常修改后执行：

```powershell
codegraph sync
```

首次初始化或索引损坏时执行：

```powershell
codegraph index
```

---

## 8. Git 与提交

提交格式：

```text
<type>(<scope>): <subject>
```

示例：

```text
fix(scripts): tighten startup semantics
docs(test): separate test methods from results
feat(frontend): productize demo pages
```

每个提交保持单一主题。不得回滚用户或他人无关改动，除非明确要求。

---

## 9. 完成后输出格式

完成任务后说明：

- 修改了什么；
- 为什么修改；
- 如何验证；
- 实际验证结果；
- 哪些内容未验证；
- 影响了哪些文档；
- 当前分支、提交和推送状态。

不得使用“应该可以”作为完成标准或验证结果。
