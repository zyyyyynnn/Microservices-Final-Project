#Requires -Version 7.0
# =====================================================
# MallCloud 一键启动脚本 (PowerShell 7+)
# 用法: pwsh .\scripts\start-all.ps1 [选项]
# =====================================================
param(
    [switch]$NoInstall,
    [switch]$SkipInfrastructure,
    [switch]$SkipFrontend,
    [switch]$SkipBackend,
    [switch]$SkipBuild,
    [switch]$CleanLogs,
    [switch]$AllowPartial
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

# ── 路径 ──────────────────────────────────────────────
$ScriptDir  = $PSScriptRoot
$ProjectRoot = Split-Path -Parent $ScriptDir
$RuntimeDir  = Join-Path $ProjectRoot ".runtime"
$LogsDir     = Join-Path $RuntimeDir "logs"
$StateFile   = Join-Path $RuntimeDir "processes.json"
$DockerDir   = Join-Path $ProjectRoot "deploy\docker"
$FrontendDir = Join-Path $ProjectRoot "mall-frontend"

# ── 后端服务定义 ──────────────────────────────────────
$BackendServices = @(
    @{ Name = "mall-user";       Port = 9002 }
    @{ Name = "mall-auth";       Port = 9001 }
    @{ Name = "mall-product";    Port = 9003 }
    @{ Name = "mall-inventory";  Port = 9004 }
    @{ Name = "mall-cart";       Port = 9005 }
    @{ Name = "mall-order";      Port = 9006 }
    @{ Name = "mall-pay";        Port = 9007 }
    @{ Name = "mall-message";    Port = 9010 }
    @{ Name = "mall-search";     Port = 9008 }
    @{ Name = "mall-seckill";    Port = 9009 }
    @{ Name = "mall-admin-biz";  Port = 9011 }
    @{ Name = "mall-job";        Port = 9012 }
    @{ Name = "mall-gateway";    Port = 9000 }
)

$FrontendPort = 5173

# ── 工具函数 ──────────────────────────────────────────
function Write-Banner($msg) {
    Write-Host ""
    Write-Host "=========================================" -ForegroundColor Cyan
    Write-Host "  $msg" -ForegroundColor Cyan
    Write-Host "=========================================" -ForegroundColor Cyan
    Write-Host ""
}

function Write-Ok($msg)   { Write-Host "  [OK]   $msg" -ForegroundColor Green }
function Write-Warn($msg) { Write-Host "  [WARN] $msg" -ForegroundColor Yellow }
function Write-Err($msg)  { Write-Host "  [FAIL] $msg" -ForegroundColor Red }
function Write-Info($msg) { Write-Host "  [....] $msg" -ForegroundColor Gray }

function Test-Port($port, $timeout = 2) {
    try {
        $tcp = [System.Net.Sockets.TcpClient]::new()
        $result = $tcp.BeginConnect("127.0.0.1", $port, $null, $null)
        $success = $result.AsyncWaitHandle.WaitOne($timeout * 1000)
        $tcp.Close()
        return $success
    } catch { return $false }
}

function Wait-Port($port, $label, $timeoutSec = 120, $intervalSec = 3, $process = $null) {
    Write-Info "等待 $label (端口 $port) ..."
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    while ($sw.Elapsed.TotalSeconds -lt $timeoutSec) {
        if (Test-Port $port) { return $true }
        if ($process -and $process.HasExited) { return $false }
        Start-Sleep -Seconds $intervalSec
    }
    return $false
}

function Wait-Http($url, $label, $timeoutSec = 120, $intervalSec = 3) {
    Write-Info "等待 $label ($url) ..."
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    while ($sw.Elapsed.TotalSeconds -lt $timeoutSec) {
        try {
            $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
            if ($r.StatusCode -ge 200 -and $r.StatusCode -lt 500) { return $true }
        } catch {}
        Start-Sleep -Seconds $intervalSec
    }
    return $false
}

function Get-ExistingPid($port) {
    try {
        $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
        if ($conn) { return $conn[0].OwningProcess }
    } catch {}
    return $null
}

# ── 初始化目录 ────────────────────────────────────────
if ($CleanLogs -and (Test-Path $LogsDir)) {
    Write-Info "清理旧日志: $LogsDir"
    Remove-Item -Recurse -Force $LogsDir
}
New-Item -ItemType Directory -Path $RuntimeDir -Force | Out-Null
New-Item -ItemType Directory -Path $LogsDir -Force | Out-Null

# 加载已有状态
$Processes = @{}
if (Test-Path $StateFile) {
    try {
        $raw = Get-Content $StateFile -Raw -Encoding UTF8
        if ($raw.Trim().Length -gt 0) {
            $Processes = $raw | ConvertFrom-Json -AsHashtable
        }
    } catch {}
}

# ── 环境检查 ──────────────────────────────────────────
Write-Banner "MallCloud 环境检查"

# PowerShell 版本
if ($PSVersionTable.PSVersion.Major -ge 7) {
    Write-Ok "PowerShell $($PSVersionTable.PSVersion)"
} else {
    Write-Err "需要 PowerShell 7+，当前: $($PSVersionTable.PSVersion)"
    exit 1
}

# Java
$javaHomeExe = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME "bin\java.exe" } else { $null }
if ($javaHomeExe -and (Test-Path $javaHomeExe)) {
    $JavaCmd = $javaHomeExe
} else {
    $javaExe = Get-Command java -ErrorAction SilentlyContinue
    $JavaCmd = if ($javaExe) { $javaExe.Source } else { $null }
}
if (-not $JavaCmd) {
    Write-Err "未找到 java，请安装 JDK 21"
    exit 1
}
$javaVersion = & $JavaCmd -version 2>&1 | Select-Object -First 1
$javaMajor = $null
if ($javaVersion -match '"([^"]+)"') {
    $rawJavaVersion = $Matches[1]
    if ($rawJavaVersion -match '^1\.(\d+)') {
        $javaMajor = [int]$Matches[1]
    } elseif ($rawJavaVersion -match '^(\d+)') {
        $javaMajor = [int]$Matches[1]
    }
}
if ($javaMajor) {
    if ($javaMajor -eq 21) {
        Write-Ok "JDK $javaMajor ($JavaCmd)"
    } else {
        Write-Err "需要 JDK 21，当前: JDK $javaMajor ($JavaCmd)"
        exit 1
    }
} else {
    Write-Warn "无法解析 Java 版本: $javaVersion"
}

# Maven
$mvnExe = Get-Command mvn -ErrorAction SilentlyContinue
if (-not $mvnExe) {
    Write-Err "未找到 mvn，请安装 Maven 3.9+"
    exit 1
}
$mvnVersion = & mvn -version 2>&1 | Select-Object -First 1
Write-Ok "Maven: $mvnVersion"

# Node.js / npm (仅启动前端时需要)
if (-not $SkipFrontend) {
    $nodeExe = Get-Command node -ErrorAction SilentlyContinue
    if (-not $nodeExe) {
        Write-Err "未找到 node，请安装 Node.js，或使用 -SkipFrontend 仅启动后端"
        exit 1
    }
    $nodeVersion = & node --version 2>&1
    Write-Ok "Node.js $nodeVersion"

    $npmExe = Get-Command npm -ErrorAction SilentlyContinue
    if (-not $npmExe) {
        Write-Err "未找到 npm，或使用 -SkipFrontend 仅启动后端"
        exit 1
    }
    $npmVersion = & npm --version 2>&1
    Write-Ok "npm $npmVersion"
} else {
    Write-Info "已跳过前端，跳过 Node.js/npm 检查"
}

# Docker (如需启动基础设施)
if (-not $SkipInfrastructure) {
    $dockerExe = Get-Command docker -ErrorAction SilentlyContinue
    if (-not $dockerExe) {
        Write-Err "未找到 docker。使用 -SkipInfrastructure 跳过基础设施启动"
        exit 1
    }
    Write-Ok "Docker 已安装"
}

Write-Host ""

# ── 启动基础设施 ──────────────────────────────────────
if (-not $SkipInfrastructure) {
    Write-Banner "启动基础设施 (Docker Compose)"
    $composeFile = Join-Path $DockerDir "docker-compose.middleware.yml"
    if (-not (Test-Path $composeFile)) {
        Write-Err "未找到 $composeFile"
        exit 1
    }

    Push-Location $DockerDir
    try {
        & docker compose -f docker-compose.middleware.yml up -d 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Err "Docker Compose 启动失败"
            exit 1
        }
        Write-Ok "Docker Compose 已执行"

        # 等待 MySQL
        $mysqlOk = $false
        Write-Info "等待 MySQL ..."
        for ($i = 1; $i -le 30; $i++) {
            $ping = docker exec mall-mysql mysqladmin ping -uroot -proot 2>$null
            if ($LASTEXITCODE -eq 0) { $mysqlOk = $true; break }
            Start-Sleep -Seconds 2
        }
        if ($mysqlOk) { Write-Ok "MySQL 就绪" } else { Write-Warn "MySQL 超时，继续启动" }

        # 等待 Nacos
        $nacosOk = Wait-Http "http://localhost:8848/nacos/" "Nacos" 60
        if ($nacosOk) { Write-Ok "Nacos 就绪" } else { Write-Warn "Nacos 超时，继续启动" }

        # 等待 Redis
        $redisOk = Test-Port 6379 2
        if ($redisOk) { Write-Ok "Redis 就绪" } else { Write-Warn "Redis 未就绪" }

    } finally {
        Pop-Location
    }
    Write-Host ""
}

# ── 构建后端 ──────────────────────────────────────────
if (-not $SkipBackend -and -not $SkipBuild) {
    Write-Banner "构建后端项目"
    Push-Location $ProjectRoot
    try {
        Write-Info "执行 mvn clean package -DskipTests -T 1C ..."
        $buildLog = Join-Path $LogsDir "build.log"
        & mvn clean package -DskipTests -T 1C *> $buildLog
        if ($LASTEXITCODE -ne 0) {
            Write-Err "Maven 构建失败，详见: $buildLog"
            exit 1
        }
        Write-Ok "Maven 构建成功"
    } finally {
        Pop-Location
    }
    Write-Host ""
} elseif (-not $SkipBackend) {
    Write-Info "已跳过 Maven 构建"
}

# ── 启动后端服务 ──────────────────────────────────────
$Results = [System.Collections.ArrayList]::new()

function Save-ResultsState {
    $stateObj = @{}
    foreach ($r in $script:Results) {
        $stateObj[$r.Name] = $r
    }
    $stateObj | ConvertTo-Json -Depth 5 | Set-Content -Path $StateFile -Encoding UTF8
}

function Add-Result([hashtable]$Result) {
    [void]$script:Results.Add($Result)
    Save-ResultsState
}

if (-not $SkipBackend) {
    Write-Banner "启动后端服务"

    foreach ($svc in $BackendServices) {
        $name = $svc.Name
        $port = $svc.Port
        $logFile = Join-Path $LogsDir "$name.log"
        $moduleDir = Join-Path $ProjectRoot $name
        $jarPath = Join-Path $moduleDir "target\$name.jar"
        $jarPattern = Join-Path $moduleDir "target\$name-*.jar"

        # 检查是否已在运行
        $existingPid = Get-ExistingPid $port
        if ($existingPid) {
            $existingProc = Get-Process -Id $existingPid -ErrorAction SilentlyContinue
            $isKnownService = $false
            if ($Processes.ContainsKey($name) -and $Processes[$name].PID -eq $existingPid -and $existingProc -and $existingProc.ProcessName -in @("java", "node")) {
                $isKnownService = $true
            }

            if ($isKnownService) {
                Write-Warn "$name 端口 $port 已由记录中的 PID $existingPid 占用，跳过"
                Add-Result @{
                    Name = $name; PID = $existingPid; Port = $port
                    Status = "AlreadyRunning"; Type = "backend"; Log = $logFile
                }
            } else {
                $procName = if ($existingProc) { $existingProc.ProcessName } else { "unknown" }
                Write-Err "$name 端口 $port 已被非本服务进程 PID $existingPid ($procName) 占用"
                Add-Result @{
                    Name = $name; PID = $existingPid; Port = $port
                    Status = "PortOccupied"; Type = "backend"; Log = $null
                }
            }
            continue
        }

        # 查找 jar
        $jar = Get-Item -LiteralPath $jarPath -ErrorAction SilentlyContinue
        if (-not $jar) {
            $jar = Get-ChildItem -Path $jarPattern -ErrorAction SilentlyContinue | Select-Object -First 1
        }
        if (-not $jar) {
            Write-Err "$name 未找到 jar: $jarPath 或 $jarPattern"
            Add-Result @{
                Name = $name; PID = $null; Port = $port
                Status = "JarNotFound"; Type = "backend"; Log = $null
            }
            continue
        }

        # 启动
        Write-Info "启动 $name (端口 $port) ..."
        $env:NACOS_SERVER       = "127.0.0.1:8848"
        $env:MYSQL_HOST         = "127.0.0.1"
        $env:REDIS_HOST         = "127.0.0.1"
        $env:ROCKETMQ_NAMESRV   = "127.0.0.1:9876"
        $env:ES_HOST            = "127.0.0.1"
        $env:ES_PORT            = "9200"
        $env:SENTINEL_DASHBOARD = "127.0.0.1:8080"

        $proc = Start-Process -FilePath $JavaCmd `
            -ArgumentList "-jar", $jar.FullName `
            -WorkingDirectory $moduleDir `
            -RedirectStandardOutput $logFile `
            -RedirectStandardError (Join-Path $LogsDir "$name.err.log") `
            -WindowStyle Hidden `
            -PassThru

        if (-not $proc) {
            Write-Err "$name 进程创建失败"
            Add-Result @{
                Name = $name; PID = $null; Port = $port
                Status = "ProcessFailed"; Type = "backend"; Log = $logFile
            }
            continue
        }

        # 等待端口
        $portOk = Wait-Port $port $name 90 3 $proc
        if ($portOk) {
            Write-Ok "$name PID=$($proc.Id) 端口=$port 已就绪"
            Add-Result @{
                Name = $name; PID = $proc.Id; Port = $port
                Status = "Running"; Type = "backend"
                Log = $logFile; StartTime = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
            }
        } else {
            $status = if ($proc.HasExited) { "Exited" } else { "Timeout" }
            Write-Warn "$name PID=$($proc.Id) 端口 $port 未就绪，状态=$status，请检查日志"
            Add-Result @{
                Name = $name; PID = $proc.Id; Port = $port
                Status = $status; Type = "backend"
                Log = $logFile; StartTime = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
            }
        }
    }
    Write-Host ""
}

# ── 启动前端 ──────────────────────────────────────────
if (-not $SkipFrontend) {
    Write-Banner "启动前端"

    if (-not (Test-Path $FrontendDir)) {
        Write-Err "未找到前端目录: $FrontendDir"
    } else {
        $packageJson = Join-Path $FrontendDir "package.json"
        if (-not (Test-Path $packageJson)) {
            Write-Err "未找到 $packageJson"
        } else {
            $nodeModules = Join-Path $FrontendDir "node_modules"

            # 安装依赖
            if (-not (Test-Path $nodeModules)) {
                if ($NoInstall) {
                    Write-Err "node_modules 不存在且指定了 -NoInstall"
                    exit 1
                }
                Write-Info "安装前端依赖 ..."
                Push-Location $FrontendDir
                try {
                    & npm install *> (Join-Path $LogsDir "npm-install.log")
                    if ($LASTEXITCODE -ne 0) {
                        Write-Err "npm install 失败"
                        exit 1
                    }
                    Write-Ok "依赖安装完成"
                } finally {
                    Pop-Location
                }
            } else {
                Write-Ok "node_modules 已存在"
            }

            # 检查端口
            $existingPid = Get-ExistingPid $FrontendPort
            if ($existingPid) {
                Write-Warn "前端端口 $FrontendPort 已被 PID $existingPid 占用，跳过"
                Add-Result @{
                    Name = "frontend"; PID = $existingPid; Port = $FrontendPort
                    Status = "AlreadyRunning"; Type = "frontend"
                    Log = (Join-Path $LogsDir "frontend.log")
                }
            } else {
                # 启动
                Write-Info "启动前端开发服务器 (端口 $FrontendPort) ..."
                $frontendLog = Join-Path $LogsDir "frontend.log"
                $frontendErr = Join-Path $LogsDir "frontend.err.log"

                $proc = Start-Process -FilePath "npm" `
                    -ArgumentList "run", "dev" `
                    -WorkingDirectory $FrontendDir `
                    -RedirectStandardOutput $frontendLog `
                    -RedirectStandardError $frontendErr `
                    -WindowStyle Hidden `
                    -PassThru

                if (-not $proc) {
                    Write-Err "前端进程创建失败"
                } else {
                    $portOk = Wait-Port $FrontendPort "frontend" 60
                    if ($portOk) {
                        Write-Ok "前端 PID=$($proc.Id) 端口=$FrontendPort 已就绪"
                        Add-Result @{
                            Name = "frontend"; PID = $proc.Id; Port = $FrontendPort
                            Status = "Running"; Type = "frontend"
                            Log = $frontendLog
                            StartTime = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
                        }
                    } else {
                        Write-Warn "前端 PID=$($proc.Id) 端口 $FrontendPort 未就绪"
                        Add-Result @{
                            Name = "frontend"; PID = $proc.Id; Port = $FrontendPort
                            Status = "Timeout"; Type = "frontend"
                            Log = $frontendLog
                            StartTime = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
                        }
                    }
                }
            }
        }
    }
    Write-Host ""
}

# ── 保存状态 ──────────────────────────────────────────
Save-ResultsState

# ── 输出摘要 ──────────────────────────────────────────
Write-Banner "MallCloud 启动结果"

$running   = $Results | Where-Object { $_.Status -eq "Running" }
$already   = $Results | Where-Object { $_.Status -eq "AlreadyRunning" }
$failed    = $Results | Where-Object { $_.Status -notin @("Running", "AlreadyRunning") }
$runningCount = @($running).Count
$alreadyCount = @($already).Count
$failedCount = @($failed).Count

$fmt = "{0,-22} {1,-10} {2,-10} {3,-12}"
Write-Host ($fmt -f "服务", "PID", "端口", "状态") -ForegroundColor White
Write-Host ($fmt -f "----", "---", "----", "----") -ForegroundColor Gray

foreach ($r in $Results) {
    $pidStr  = if ($r.PID) { [string]$r.PID } else { "-" }
    $portStr = if ($r.Port) { [string]$r.Port } else { "-" }
    $color = switch ($r.Status) {
        "Running"        { "Green" }
        "AlreadyRunning" { "Yellow" }
        default          { "Red" }
    }
    Write-Host ($fmt -f $r.Name, $pidStr, $portStr, $r.Status) -ForegroundColor $color
}

Write-Host ""
Write-Host "  运行中:  $($runningCount + $alreadyCount)" -ForegroundColor Green
if ($failedCount -gt 0) {
    Write-Host "  失败/超时: $failedCount" -ForegroundColor Red
}

# 输出访问地址
$gatewayResult = $Results | Where-Object { $_.Name -eq "mall-gateway" }
$frontendResult = $Results | Where-Object { $_.Name -eq "frontend" }

Write-Host ""
if ($gatewayResult -and $gatewayResult.Status -in @("Running", "AlreadyRunning")) {
    Write-Host "  网关地址: http://localhost:9000" -ForegroundColor Cyan
}
if ($frontendResult -and $frontendResult.Status -in @("Running", "AlreadyRunning")) {
    Write-Host "  前端地址: http://localhost:5173" -ForegroundColor Cyan
}
Write-Host "  日志目录: $LogsDir" -ForegroundColor Gray
Write-Host "  状态文件: $StateFile" -ForegroundColor Gray
Write-Host "  停止命令: pwsh .\scripts\stop-all.ps1" -ForegroundColor Gray
Write-Host ""

if ($failedCount -gt 0 -and -not $AllowPartial) {
    exit 1
}

exit 0
