# MallCloud 当前阶段执行 Prompt

> 当前阶段：技术亮点与专项验证
> 最高标准：`docs/PROJECT_STANDARD.md`
> Agent 通用规则：`AGENTS.md`
> 测试方法：`docs/test/README.md`
> 当前结果：`docs/FINAL_REPORT.md`

---

## 1. 当前目标

在已完成核心交易链路和前端产品化页面整改一轮的基础上，推进技术专项验证、测试资产复测和最终答辩证据补齐。

当前优先顺序：

1. 执行订单正式负载；
2. 执行秒杀 50/100/200/300/500 阶梯压力测试；
3. 补齐前端真实成功态联调和截图；
4. 最终报告与答辩材料。

以上未完成内容不得写成已完成。

---

## 2. 当前事实

- 核心认证、交易、Seata AT 回滚、支付消息链路已完成运行验收。
- 前端产品化页面已完成一轮整改，但成功态业务闭环、逐页成功截图和真实接口数据仍待补充。
- Postman/Newman 集合已建立并执行过当前环境回归，结果以 `docs/FINAL_REPORT.md` 为准。
- JMeter 脚本已建立，搜索负载、订单短冒烟、秒杀 10 用户请求受理短冒烟、秒杀 1 用户完整链路短冒烟和秒杀 10 用户完整链路连续 3 次验证已执行，订单正式负载和秒杀阶梯压力报告仍待执行。
- Elasticsearch 搜索、Sentinel 规则限流/熔断、Nacos 普通业务配置热更新已完成专项验收。
- 秒杀 10 用户完整链路阻断已修复并完成连续 3 次验证；完整阶梯压力测试仍待专项验收。
- 根目录 `start-all.bat` / `stop-all.bat` 是普通启动、完整启动测试和人工验收主入口。
- `scripts/start-all.ps1` / `scripts/stop-all.ps1` 是参数化、自动化和故障排查入口。

---

## 3. 本阶段允许修改

按具体任务最小化修改以下范围：

- Elasticsearch 搜索恢复所需配置、索引初始化、搜索服务代码和文档；
- Newman 失败项对应的接口、数据、配置或断言；
- JMeter 脚本参数、执行说明和报告索引；
- Sentinel 规则、限流/熔断验证入口和证据文档；
- Nacos 热更新验证所需低风险配置与证据文档；
- 前端成功态联调所需页面、状态处理和验收矩阵；
- `docs/FINAL_REPORT.md` 中真实结果、证据路径和已知限制。

具体任务若声明更窄范围，以该任务为准。

---

## 4. 禁止事项

- 不新增微服务。
- 不新增无必要依赖、框架或平行组件库。
- 不修改接口契约或数据库结构，除非当前任务明确要求并同步文档。
- 不使用 mock、固定 Token、伪业务数据或脚本占位伪造成果。
- 不把 HTTP 可达写成业务通过。
- 不把测试资产建立写成测试通过。
- 不把前端错误态截图写成成功态联调完成。
- 不填写未运行的 P95、吞吐量、错误率或通过率。
- 不演示未完成的 Docker 全栈或 Kubernetes 全栈。

---

## 5. 完成标准

每个专项完成时至少满足：

- 修改范围与任务一致；
- JDK 21 下可编译，或明确失败原因；
- 相关服务能通过 Gateway 访问；
- 成功、失败和边界状态都有真实返回或明确限制说明；
- Newman/JMeter/Sentinel/Nacos/前端截图等证据按实际执行保存；
- `docs/FINAL_REPORT.md` 只记录真实结果；
- 未验证项明确保留为待验证或未执行。

---

## 6. 验证要求

按任务选择执行：

```powershell
java -version
mvn -version
mvn clean package -DskipTests
mvn clean test -DskipTests=false
start-all.bat --no-pause
stop-all.bat --no-pause
pwsh .\scripts\run-newman.ps1
pwsh .\scripts\run-jmeter.ps1 -Scenario search -Users 1 -RampUp 1
pwsh .\scripts\run-special-checks.ps1
git diff --check
```

普通启动、完整启动测试和人工验收优先使用根目录 BAT。需要精确参数控制、自动化调用或故障排查时使用 PowerShell 脚本。

---

## 7. 完成后输出格式

```text
结论：
- 完成 / 部分完成 / 未完成

修改：
- 文件与关键改动

验证：
- 执行命令
- 实际结果

未验证：
- 原因

文档同步：
- 已更新的文档

Git：
- 分支
- Commit
- Push
- Working tree
```
