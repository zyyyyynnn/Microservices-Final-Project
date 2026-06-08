# MallCloud 启动脚本

## 快速开始

```powershell
# 启动全部服务（基础设施 + 后端 + 前端）
pwsh .\scripts\start-all.ps1

# 停止全部服务
pwsh .\scripts\stop-all.ps1
```

## 启动选项

```powershell
# 跳过前端，只启动后端
pwsh .\scripts\start-all.ps1 -SkipFrontend

# 跳过后端，只启动前端
pwsh .\scripts\start-all.ps1 -SkipBackend

# 跳过 Docker 基础设施（MySQL/Redis/Nacos 等已手动启动时）
pwsh .\scripts\start-all.ps1 -SkipInfrastructure

# 前端依赖不存在时直接报错，不自动安装
pwsh .\scripts\start-all.ps1 -NoInstall

# 启动前清理旧日志
pwsh .\scripts\start-all.ps1 -CleanLogs
```

## 必要环境

| 依赖 | 版本 | 说明 |
|---|---|---|
| PowerShell | 7+ | 脚本运行环境 |
| JDK | 21 | 后端编译与运行 |
| Maven | 3.9+ | 后端构建 |
| Node.js | 18+ | 前端开发服务器 |
| npm | - | 前端包管理（根据 package-lock.json 自动选择） |
| Docker | - | 基础设施容器（使用 -SkipInfrastructure 可跳过） |

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

## 常见失败原因

| 现象 | 原因 | 解决 |
|---|---|---|
| `未找到 java` | JDK 未安装或不在 PATH | 安装 JDK 21 |
| `需要 JDK 21` | Java 版本不对 | 切换到 JDK 21 |
| `未找到 mvn` | Maven 未安装或不在 PATH | 安装 Maven 3.9+ |
| `Docker Compose 启动失败` | Docker 未运行 | 启动 Docker Desktop |
| `Maven 构建失败` | 编译错误 | 检查 `.runtime/logs/build.log` |
| `端口已被占用` | 上次进程未停止 | 执行 `pwsh .\scripts\stop-all.ps1` |
| `node_modules 不存在` | 前端依赖未安装 | 脚本会自动执行 npm install |
| `npm install 失败` | 网络问题 | 检查网络或使用镜像源 |

## 脚本不会自动安装

- JDK
- Maven
- Node.js
- npm
- Docker Desktop
- MySQL / Redis / Nacos（由 Docker Compose 管理）
