# MallCloud 启动脚本

## 快速开始（推荐 BAT 版本）

```bat
rem 启动全部服务（基础设施 + 后端 + 前端）
start-all.bat

rem 停止全部服务
stop-all.bat
```

双击 `start-all.bat` 或 `stop-all.bat` 即可运行。

## BAT 启动选项

```bat
start-all.bat --skip-infrastructure    rem 跳过 Docker 基础设施
start-all.bat --skip-backend           rem 只启动前端
start-all.bat --skip-frontend          rem 只启动后端
start-all.bat --clean-logs             rem 启动前清理旧日志
start-all.bat --no-build               rem 跳过 Maven 构建
start-all.bat --no-pause               rem 结束后不暂停（自动化用）
```

## PowerShell 版本（备选）

```powershell
pwsh .\scripts\start-all.ps1
pwsh .\scripts\stop-all.ps1

pwsh .\scripts\start-all.ps1 -SkipFrontend
pwsh .\scripts\start-all.ps1 -SkipInfrastructure
pwsh .\scripts\start-all.ps1 -CleanLogs
```

## 测试脚本

Postman/Newman：

```powershell
pwsh .\scripts\run-newman.ps1
```

该脚本优先使用本机已安装的 `newman`；未安装时回退到 `npx` 临时获取 `newman` 和 `newman-reporter-htmlextra`。

JMeter：

```powershell
pwsh .\scripts\run-jmeter.ps1 -Scenario search -Users 50
pwsh .\scripts\run-jmeter.ps1 -Scenario order -Users 50
pwsh .\scripts\run-jmeter.ps1 -Scenario seckill -Users 100 -RampUp 10 -Loops 1
```

该脚本优先使用本机已安装的 `jmeter`；未安装时按需下载 Apache JMeter 到 `.tools/`，输出 JTL 到 `docs/test/jmeter/results/`，输出 HTML 报告到 `docs/test/jmeter/report/`。

## 必要环境

| 依赖 | 版本 | 说明 |
|---|---|---|
| JDK | 21 | 后端编译与运行 |
| Maven | 3.9+ | 后端构建（或使用 mvnw.cmd） |
| Node.js | 18+ | 前端开发服务器 |
| npm | - | 前端包管理 |
| Docker Desktop | - | 基础设施容器 |
| PowerShell | 7+ | BAT 脚本内部调用 |

未全局安装测试工具时，脚本需要本机可访问互联网以便首次获取 Newman/JMeter；工具缓存不提交到仓库。

## 服务端口

| 服务 | 端口 |
|---|---|
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
| 前端 | 5173 |

## 日志与状态

- 日志目录：`.runtime/logs/`
- 状态文件：`.runtime/processes.json`
- 每个服务有独立的 `.log` 和 `.err.log` 文件

查看某个服务的错误日志：

```bat
type .runtime\logs\mall-auth.err.log
```

## 双击运行行为

- `start-all.bat` 双击后会在当前窗口执行，结束后显示 `pause` 等待按键
- `stop-all.bat` 同理
- 不会弹出额外的 PowerShell 窗口（除前端 Vite 进程外）

## 常见闪退原因

| 原因 | 说明 |
|---|---|
| 缺少 `call` | BAT 调用 `.cmd` 程序（npm/mvn）必须用 `call`，否则控制流不返回 |
| 路径含空格或中文 | 脚本使用 `%~dp0` 和双引号处理，一般不会出问题 |
| Docker daemon 未运行 | 启动 Docker Desktop 后重试 |
| JAR 不存在 | 先执行 Maven 构建 |
| 端口被占用 | 脚本会检测并报告冲突进程 |

## 端口冲突处理

如果某个端口被其他进程占用，脚本会输出：

```
[ERROR] 端口 9002 已被其他进程占用
[PID]  5678
[CMD]  java.exe ...
```

手动停止冲突进程：

```bat
taskkill /PID 5678 /F
```

## 脚本不会自动安装

- JDK
- Maven
- Node.js
- npm
- Docker Desktop
- MySQL / Redis / Nacos（由 Docker Compose 管理）
