# MallCloud 脚本说明

> 职责：脚本清单、BAT 主入口、PowerShell 参数、返回码、状态文件、日志目录、启动/停止行为和常见脚本故障。

---

## 1. 启动入口定位

| 入口 | 定位 | 使用场景 |
|---|---|---|
| `start-all.bat` / `stop-all.bat` | 主要人工启动与验收入口 | 双击运行、答辩演示、完整启动测试 |
| `scripts/start-all.ps1` / `scripts/stop-all.ps1` | 参数化、自动化和故障排查入口 | 精确控制、CI/脚本调用、复测失败场景 |

文档和验收材料必须保持该入口分工，不得弱化 BAT 主入口定位。

---

## 2. BAT 主入口

普通启动：

```bat
start-all.bat
stop-all.bat
```

常用参数：

```bat
start-all.bat --skip-infrastructure
start-all.bat --skip-backend
start-all.bat --skip-frontend
start-all.bat --clean-logs
start-all.bat --no-build
start-all.bat --no-pause
stop-all.bat --no-pause
```

说明：

- 双击 `start-all.bat` 会在当前窗口执行，结束后 `pause` 等待按键。
- 双击 `stop-all.bat` 同理。
- `--no-pause` 用于自动化调用。
- BAT 会统一输出用户可见的启动摘要。
- 启动失败时返回非 0，失败服务不得写成已启动。
- BAT 内部会调用 PowerShell 做端口探测、状态文件读写和进程校验。

---

## 3. PowerShell 高级入口

```powershell
pwsh .\scripts\start-all.ps1
pwsh .\scripts\stop-all.ps1

pwsh .\scripts\start-all.ps1 -SkipInfrastructure
pwsh .\scripts\start-all.ps1 -SkipBackend
pwsh .\scripts\start-all.ps1 -SkipFrontend
pwsh .\scripts\start-all.ps1 -SkipBuild
pwsh .\scripts\start-all.ps1 -CleanLogs
pwsh .\scripts\start-all.ps1 -AllowPartial
```

常用组合：

```powershell
pwsh .\scripts\start-all.ps1 -SkipInfrastructure -SkipFrontend
pwsh .\scripts\start-all.ps1 -SkipInfrastructure -SkipFrontend -SkipBuild
pwsh .\scripts\start-all.ps1 -SkipInfrastructure -SkipFrontend -AllowPartial
```

参数语义：

| 参数 | 说明 |
|---|---|
| `-SkipInfrastructure` | 不启动 Docker 中间件 |
| `-SkipBackend` | 不启动后端服务 |
| `-SkipFrontend` | 不启动前端；同时跳过 Node/npm 检查 |
| `-SkipBuild` | 跳过 Maven 构建，复用已有 `target` 产物 |
| `-CleanLogs` | 启动前清理 `.runtime/logs/` |
| `-AllowPartial` | 允许记录失败服务并返回 0，仅用于明确接受部分失败的联调 |

返回码：

- 默认存在失败服务时返回 1；
- 全部启动成功或已运行时返回 0；
- 只有显式使用 `-AllowPartial` 时，失败服务才允许记录后返回 0。

---

## 4. 数据库脚本

```powershell
pwsh .\scripts\init-db.ps1 -Force
```

说明：

- 脚本会执行 `db/init/00-create-databases.sql` 和 `db/init/seed.sql`。
- `seed.sql` 不是幂等脚本。
- 无 `-Force` 时默认拒绝执行，避免误重置已有数据。

---

## 5. 测试脚本

Postman/Newman：

```powershell
pwsh .\scripts\run-newman.ps1
```

JMeter：

```powershell
pwsh .\scripts\run-jmeter.ps1 -Scenario search -Users 50
pwsh .\scripts\run-jmeter.ps1 -Scenario order -Users 50
pwsh .\scripts\run-jmeter.ps1 -Scenario seckill -Users 100 -RampUp 10 -Loops 1
```

技术专项冒烟检查：

```powershell
pwsh .\scripts\run-special-checks.ps1
```

测试脚本的结果统一记录到 `docs/FINAL_REPORT.md`。脚本存在不等于测试通过。

---

## 6. 日志与状态

| 内容 | 路径 |
|---|---|
| 日志目录 | `.runtime/logs/` |
| 进程状态 | `.runtime/processes.json` |
| 中间件状态 | `.runtime/infrastructure.json` |

每个服务有独立的 `.log` 和 `.err.log` 文件。

查看错误日志：

```bat
type .runtime\logs\mall-auth.err.log
```

---

## 7. 常见脚本故障

| 问题 | 说明 |
|---|---|
| 缺少 PowerShell 7 | BAT 依赖 `pwsh.exe` 执行探测和状态管理 |
| Java 不是 21 | 启动和构建必须使用 JDK 21 |
| Docker daemon 未运行 | 启动 Docker Desktop 后重试，或使用跳过中间件参数 |
| JAR 不存在 | 先执行构建，或确认 `target` 产物存在 |
| 端口被占用 | 脚本会输出 PID 和命令行；不得写成服务已启动 |
| Node/npm 缺失 | 完整前端启动需要；PowerShell 后端-only 可用 `-SkipFrontend` |
| BAT 调用 `.cmd` 程序 | 需要通过 `call` 保持控制流返回，维护 BAT 时必须注意 |

手动停止冲突进程前，应确认 PID 和命令行确实属于目标服务，避免误杀无关进程。

---

## 8. 脚本不会自动安装

- JDK；
- Maven；
- Node.js；
- npm；
- Docker Desktop；
- Newman；
- JMeter。

Newman/JMeter 脚本可在工具缺失时按脚本逻辑尝试临时获取或下载，实际结果以命令输出为准。
