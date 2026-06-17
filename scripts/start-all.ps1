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
    [switch]$AllowPartial,
    [ValidateSet("core", "search", "seckill", "full")]
    [string]$Profile = "full",
    [switch]$LowMemory
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
    @{ Name = "mall-user";       Port = 9102 }
    @{ Name = "mall-auth";       Port = 9101 }
    @{ Name = "mall-product";    Port = 9103 }
    @{ Name = "mall-inventory";  Port = 9104 }
    @{ Name = "mall-cart";       Port = 9105 }
    @{ Name = "mall-order";      Port = 9106 }
    @{ Name = "mall-pay";        Port = 9107 }
    @{ Name = "mall-message";    Port = 9110 }
    @{ Name = "mall-search";     Port = 9108 }
    @{ Name = "mall-seckill";    Port = 9109 }
    @{ Name = "mall-admin-biz";  Port = 9111 }
    @{ Name = "mall-job";        Port = 9112 }
    @{ Name = "mall-gateway";    Port = 9100 }
)

$FrontendPort = 5173

# ── 启动日志落盘（Sprint 3.8 任务 D）────────────────────
# 关键 stdout 同步追加到 .runtime/logs/start-all.log（已 .gitignore 排除，不入库）；
# 包含：启动参数 / 端口检查 / MySQL ready 探针 / 后端服务启动 / 13/13 health / 失败 abort。
# 注：Initialize-LogFile 在 script scope（不放在 function 内），
#     因为 $PSBoundParameters 在 function scope 不可见，导致 Params 行空。
$logDir = Join-Path $RuntimeDir "logs"
if (-not (Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir -Force | Out-Null
}
$LogFile = Join-Path $logDir "start-all.log"
if ($CleanLogs -and (Test-Path $LogFile)) {
    Remove-Item $LogFile -Force
}
$params = ($PSBoundParameters.GetEnumerator() |
    ForEach-Object { "$($_.Key)=$($_.Value)" }) -join " "
$hdr = @(
    "==== MallCloud start-all.ps1 ===="
    "Timestamp: $(Get-Date -Format 'yyyy-MM-ddTHH:mm:ss')"
    "Params: $params"
    "PWD: $ProjectRoot"
    "PowerShell: $($PSVersionTable.PSVersion)"
    ""
)
Add-Content -Path $LogFile -Value $hdr -Encoding UTF8

function Write-LogLine($line) {
    if ($LogFile) {
        Add-Content -Path $LogFile -Value $line -Encoding UTF8
    }
}

# ── 工具函数 ──────────────────────────────────────────
function Write-Banner($msg) {
    $sep = "========================================="
    Write-Host ""
    Write-Host $sep -ForegroundColor Cyan
    Write-Host "  $msg" -ForegroundColor Cyan
    Write-Host $sep -ForegroundColor Cyan
    Write-Host ""
    if ($LogFile) {
        Write-LogLine ""
        Write-LogLine $sep
        Write-LogLine "  $msg"
        Write-LogLine $sep
        Write-LogLine ""
    }
}

function Write-Ok($msg)   { $line="  [OK]   $msg"; Write-Host $line -ForegroundColor Green;  Write-LogLine $line }
function Write-Warn($msg) { $line="  [WARN] $msg"; Write-Host $line -ForegroundColor Yellow; Write-LogLine $line }
function Write-Err($msg)  { $line="  [FAIL] $msg"; Write-Host $line -ForegroundColor Red;    Write-LogLine $line }
function Write-Info($msg) { $line="  [....] $msg"; Write-Host $line -ForegroundColor Gray;   Write-LogLine $line }

function Test-Port($port, $timeout = 2) {
    try {
        $tcp = [System.Net.Sockets.TcpClient]::new()
        $result = $tcp.BeginConnect("127.0.0.1", $port, $null, $null)
        $success = $result.AsyncWaitHandle.WaitOne($timeout * 500)
        $tcp.Close()
        if ($success) { return $true }
    } catch { }

    try {
        $tcp = [System.Net.Sockets.TcpClient]::new([System.Net.Sockets.AddressFamily]::InterNetworkV6)
        $result = $tcp.BeginConnect("::1", $port, $null, $null)
        $success = $result.AsyncWaitHandle.WaitOne($timeout * 500)
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
            $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 2 -ErrorAction Stop
            if ($r.StatusCode -ge 200 -and $r.StatusCode -lt 500) { return $true }
        } catch {}
        Start-Sleep -Seconds $intervalSec
    }
    return $false
}

# MySQL 连通性 + 就绪检测
# 优先级（Sprint 3.7 加固）：
#   1) TCP 端口探测 3306：作为前置快筛，避免在端口未监听时反复 docker exec 浪费
#   2) docker exec select 1：真实业务握手（mysqladmin ping 偶尔在 GTID 模式假阳性）
#   3) docker exec mysqladmin ping：docker fallback
#   4) 本机 mysqladmin ping：本机 fallback
#   5) 退化：纯 TCP 端口可达 + WARN（极端环境兜底）
# 返回 true 表示已就绪，false 表示超时
# 凭证来源：仅使用脚本内已声明的 root/root 占位（开发环境，与 deploy/docker/mysql/init.sql
# 的 MySQL_ROOT_PASSWORD 对齐），不引入任何新变量；如未来需要自定义，优先走 env MYSQL_USER / MYSQL_PASSWORD
function Wait-MySqlReady($timeoutSec = 90, $intervalSec = 2) {
    Write-Info "等待 MySQL 就绪 (127.0.0.1:3306) ..."
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    $tcpOnlyWarned = $false
    while ($sw.Elapsed.TotalSeconds -lt $timeoutSec) {
        if (-not (Test-Port 3306 1)) {
            Start-Sleep -Seconds $intervalSec
            continue
        }

        $dockerExe = Get-Command docker -ErrorAction SilentlyContinue
        if ($dockerExe) {
            # 优先 select 1：与 mysqladmin ping 不同，它会真正执行 SQL 并往返服务器，
            # 避免 mysqld 已接受连接但仍未完成初始化握手时被误判 ready。
            $select = & docker exec mall-mysql mysql -uroot -proot -N -B -e "SELECT 1" 2>$null
            if ($LASTEXITCODE -eq 0 -and "$select".Trim() -eq "1") {
                Write-Ok "MySQL 已就绪 (docker exec select 1)"
                return $true
            }
            $ping = & docker exec mall-mysql mysqladmin ping -uroot -proot 2>$null
            if ($LASTEXITCODE -eq 0) {
                Write-Ok "MySQL 已就绪 (docker exec mysqladmin ping)"
                return $true
            }
        }

        $mysqladmin = Get-Command mysqladmin -ErrorAction SilentlyContinue
        if ($mysqladmin) {
            & mysqladmin -h127.0.0.1 -uroot -proot ping 2>$null | Out-Null
            if ($LASTEXITCODE -eq 0) {
                Write-Ok "MySQL 已就绪 (本机 mysqladmin ping)"
                return $true
            }
        }
        $mysql = Get-Command mysql -ErrorAction SilentlyContinue
        if ($mysql) {
            $sel = & mysql -h127.0.0.1 -uroot -proot -N -B -e "SELECT 1" 2>$null
            if ($LASTEXITCODE -eq 0 -and "$sel".Trim() -eq "1") {
                Write-Ok "MySQL 已就绪 (本机 mysql select 1)"
                return $true
            }
        }

        if (-not $tcpOnlyWarned) {
            Write-Warn "MySQL 端口已开但 select 1 / mysqladmin 探针未就绪，继续等待（最多 $($timeoutSec)s）"
            $tcpOnlyWarned = $true
        }
        Start-Sleep -Seconds $intervalSec
    }

    # 超时退化为 TCP-only 兜底：明确 WARN（不悄悄返回）
    if (Test-Port 3306 1) {
        Write-Warn "MySQL 探针全部失败但 3306 仍可达，强制按 TCP 端口通过。请运行 docker logs mall-mysql 排查。"
        return $true
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

function Get-ProcessCommandLine([int]$ProcessId) {
    try {
        $proc = Get-CimInstance Win32_Process -Filter "ProcessId = $ProcessId" -ErrorAction Stop
        return $proc.CommandLine
    } catch {
        return $null
    }
}

function Test-ManagedBackendProcess([int]$ProcessId, [string]$ServiceName) {
    $proc = Get-Process -Id $ProcessId -ErrorAction SilentlyContinue
    if (-not $proc -or $proc.ProcessName -ne "java") {
        return $false
    }

    $commandLine = Get-ProcessCommandLine $ProcessId
    if ([string]::IsNullOrWhiteSpace($commandLine)) {
        return $false
    }

    $targetDir = (Join-Path (Join-Path $ProjectRoot $ServiceName) "target").Replace("/", "\")
    $normalized = $commandLine.Replace("/", "\")
    $jarPattern = "\\$([regex]::Escape($ServiceName))(?:-[^\\\s`"']+)?\.jar"

    return ($normalized -like "*$targetDir*" -and $normalized -match $jarPattern)
}

function Stop-ManagedBackendProcess([string]$ServiceName, [int]$ProcessId, [int]$Port) {
    $proc = Get-Process -Id $ProcessId -ErrorAction SilentlyContinue
    if (-not $proc) {
        Write-Warn "$ServiceName 旧 PID $ProcessId 已不存在"
        return $true
    }

    if (-not (Test-ManagedBackendProcess $ProcessId $ServiceName)) {
        Write-Warn "跳过终止 $ServiceName 的旧 PID $ProcessId：命令行未匹配本项目 $ServiceName jar"
        return $false
    }

    Write-Info "终止不在 Profile 内的旧服务 $ServiceName (PID: $ProcessId)"
    try {
        Stop-Process -Id $ProcessId -Force -ErrorAction Stop
        for ($i = 1; $i -le 20; $i++) {
            $stillRunning = Get-Process -Id $ProcessId -ErrorAction SilentlyContinue
            $portBusy = Test-Port $Port 1
            if (-not $stillRunning -and -not $portBusy) {
                return $true
            }
            Start-Sleep -Milliseconds 500
        }
        Write-Err "$ServiceName PID=$ProcessId 停止后端口 $Port 未释放"
        return $false
    } catch {
        Write-Err "$ServiceName PID=$ProcessId 停止失败: $_"
        return $false
    }
}

# ── 基础设施端口归属检查 ────────────────────────────────────
# 防止后端连到错误的本机 MySQL（参考 Sprint 2 首轮失败根因）。
# 至少检查 3306 是否被 Docker / WSL 转发进程合法占用。
$script:PortCheckAllowed = @(
    "com.docker.backend", "com.docker.proxy", "docker-proxy", "vpnkit", "wslrelay"
)
$script:PortCheckBlocked = @(
    "mysqld", "mariadbd", "mariadb"
)
$script:PortCheckBlockedPathHints = @(
    "MySQL", "MariaDB"
)

function Get-PortOwnerDetail {
    param([int]$Port)
    $rows = @()
    try {
        $conns = Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue
    } catch {
        return $rows
    }
    if (-not $conns) { return $rows }
    foreach ($c in $conns) {
        $proc = Get-Process -Id $c.OwningProcess -ErrorAction SilentlyContinue
        $procName = if ($proc) { $proc.ProcessName } else { "unknown" }
        $procPath = if ($proc) { $proc.Path } else { "" }
        $rows += [PSCustomObject]@{
            LocalAddress = $c.LocalAddress
            OwningProcess = $c.OwningProcess
            ProcessName   = $procName
            Path          = $procPath
        }
    }
    return $rows
}

function Test-InfrastructurePortOwnership {
    # 公共：检查单个端口的归属，返回 [pscustomobject]@{ Status; Allowed; Details; ... }
    # Status: Allowed | Blocked | Skipped | Warn | NoListener
    param(
        [Parameter(Mandatory)] [int]$Port,
        [Parameter(Mandatory)] [string]$DisplayName,
        [string]$LogPath
    )
    if (-not $LogPath) {
        $LogPath = Join-Path $LogsDir "startup-port-check.log"
    }
    $logDir = Split-Path -Path $LogPath -Parent
    if ($logDir -and -not (Test-Path $logDir)) {
        New-Item -ItemType Directory -Path $logDir -Force | Out-Null
    }
    $timestamp = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
    $infraHost = $env:MALL_INFRA_HOST
    $lines = New-Object System.Collections.Generic.List[string]
    $lines.Add("=== 启动端口归属检查 $timestamp ===")
    $lines.Add("MALL_INFRA_HOST=$infraHost")
    $lines.Add("Port=$Port DisplayName=$DisplayName")
    $lines.Add("")

    # 1) MALL_INFRA_HOST 指向非本机：跳过
    if ($infraHost -and $infraHost -notin @("127.0.0.1", "localhost", "::1")) {
        $lines.Add("判定: Skipped")
        $lines.Add("原因: MALL_INFRA_HOST=$infraHost 非本机地址；脚本不强制检查本机 $Port 归属")
        $lines.Add("建议: 确保后端可通过 $infraHost 访问 MySQL")
        $lines | Out-File -FilePath $LogPath -Encoding UTF8 -Append
        Write-Host ""
        Write-Host "  [WARN] 已设置 MALL_INFRA_HOST=$infraHost，跳过本机 $Port Docker 转发归属检查。" -ForegroundColor Yellow
        Write-Host ""
        return [pscustomobject]@{
            Status = "Skipped"; Allowed = $true; Port = $Port; LogPath = $LogPath
        }
    }

    # 2) 收集监听
    $owners = Get-PortOwnerDetail -Port $Port
    if ($null -eq $owners -or @($owners).Count -eq 0) {
        $lines.Add("状态: No listener on port $Port")
        $lines.Add("判定: Warn")
        $lines.Add("建议: 端口 $Port 当前无监听。-SkipInfrastructure 时确认 Docker $DisplayName 已手动启动；否则继续启动后端将连接失败。")
        $lines | Out-File -FilePath $LogPath -Encoding UTF8 -Append
        Write-Host ""
        Write-Host "  [WARN] 端口 $Port ($DisplayName) 当前没有监听，后续 Docker MySQL 可能尚未启动。" -ForegroundColor Yellow
        Write-Host ""
        return [pscustomobject]@{
            Status = "NoListener"; Allowed = $true; Port = $Port; LogPath = $LogPath
        }
    }

    # 3) 逐个判定
    $allowed = New-Object System.Collections.Generic.List[object]
    $blocked = New-Object System.Collections.Generic.List[object]
    $lines.Add("监听者明细:")
    foreach ($o in $owners) {
        $lines.Add(("  LocalAddress={0} PID={1} ProcessName={2} Path={3}" -f $o.LocalAddress, $o.OwningProcess, $o.ProcessName, $o.Path))
    }
    $lines.Add("")

    foreach ($o in $owners) {
        $name = "$($o.ProcessName)".Trim()
        $path = "$($o.Path)".Trim()

        $isAllowed = $false
        $isBlocked = $false

        # Allow by exact ProcessName match
        foreach ($allowName in $script:PortCheckAllowed) {
            if ($name -ieq $allowName) { $isAllowed = $true; break }
        }
        # Allow by path containing docker/wsl
        if (-not $isAllowed -and $path) {
            if ($path -match '(?i)(docker|wsl)') { $isAllowed = $true }
        }
        # Block by exact ProcessName match (mysqld / mariadbd)
        if (-not $isAllowed) {
            foreach ($blockName in $script:PortCheckBlocked) {
                if ($name -ieq $blockName) { $isBlocked = $true; break }
            }
        }
        # Block by path containing MySQL/MariaDB
        if (-not $isAllowed -and $path) {
            foreach ($hint in $script:PortCheckBlockedPathHints) {
                if ($path -match $hint) { $isBlocked = $true; break }
            }
        }

        if ($isAllowed) {
            $allowed.Add($o)
        } else {
            # 未列入白名单或命中黑名单：默认按 Blocked 处理
            $isBlocked = $true
            $blocked.Add($o)
        }
    }

    if (($blocked -as [System.Collections.IEnumerable]) -and $blocked.Count -gt 0) {
        $lines.Add("判定: Blocked")
        $lines.Add("阻塞进程:")
        foreach ($b in $blocked) {
            $lines.Add(("  PID={0} ProcessName={1} Path={2}" -f $b.OwningProcess, $b.ProcessName, $b.Path))
        }
        $lines.Add("")
        $lines.Add("建议: 端口 $Port ($DisplayName) 已被非 Docker 进程占用，后端将连接到错误的 MySQL 实例。")
        $lines.Add("请以管理员 PowerShell 执行：")
        $lines.Add("  Stop-Service MySQL84 -Force")
        $lines.Add("  Set-Service MySQL84 -StartupType Manual")
        $lines.Add("然后重新运行 start-all.ps1")
        $lines.Add("注意: 脚本只提示，不会自动停止 MySQL84（需管理员权限）。")
        $lines | Out-File -FilePath $LogPath -Encoding UTF8 -Append

        Write-Host ""
        Write-Host "  [FAIL] 端口 $Port ($DisplayName) 已被非 Docker 进程占用，后端将连接到错误的 MySQL 实例。" -ForegroundColor Red
        foreach ($b in $blocked) {
            Write-Host ("         阻塞进程: PID={0} ProcessName={1} Path={2}" -f $b.OwningProcess, $b.ProcessName, $b.Path) -ForegroundColor Red
        }
        Write-Host "         请以管理员 PowerShell 执行 Stop-Service MySQL84 -Force 后重试。" -ForegroundColor Red
        Write-Host "         脚本只提示，不会自动停止 MySQL84（需管理员权限）。" -ForegroundColor Red
        Write-Host ""
        return [pscustomobject]@{
            Status = "Blocked"; Allowed = $false; Port = $Port; LogPath = $LogPath
            Blocked = $blocked
        }
    }

    # All allowed
    $lines.Add("判定: Allowed")
    $lines.Add("允许进程 (Docker / WSL 转发):")
    foreach ($a in $allowed) {
        $lines.Add(("  PID={0} ProcessName={1} Path={2}" -f $a.OwningProcess, $a.ProcessName, $a.Path))
    }
    $lines | Out-File -FilePath $LogPath -Encoding UTF8 -Append

    $first = $allowed | Select-Object -First 1
    $procLabel = if ($first) { $first.ProcessName } else { "Docker/WSL" }
    Write-Host "  [OK] 端口 $Port ($DisplayName) 由 Docker / WSL 转发接管 (进程: $procLabel)" -ForegroundColor Green
    return [pscustomobject]@{
        Status = "Allowed"; Allowed = $true; Port = $Port; LogPath = $LogPath
        AllowedEntries = $allowed
    }
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
$javaVersionOutput = & $JavaCmd -version 2>&1 | Out-String
$javaMajor = $null
if ($javaVersionOutput -match '(?is)(?:java|openjdk).*?(?:version\s+)?"?(?:1\.)?(\d+)') {
    $javaMajor = [int]$Matches[1]
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
    Write-Banner "启动基础设施 (Docker Compose) - Profile: $Profile - LowMemory: $LowMemory"
    $composeFile = Join-Path $DockerDir "docker-compose.middleware.yml"
    if (-not (Test-Path $composeFile)) {
        Write-Err "未找到 $composeFile"
        exit 1
    }

    $composeServices = @()
    if ($Profile -eq "core") {
        $composeServices = @("mysql", "redis", "nacos", "seata")
    } elseif ($Profile -eq "search") {
        $composeServices = @("mysql", "redis", "nacos", "seata", "elasticsearch", "rocketmq-namesrv", "rocketmq-broker")
    } elseif ($Profile -eq "seckill") {
        $composeServices = @("mysql", "redis", "nacos", "seata", "rocketmq-namesrv", "rocketmq-broker", "sentinel")
    }

    Push-Location $DockerDir
    try {
        if ($composeServices.Count -gt 0) {
            $allServices = @("mysql", "redis", "nacos", "seata", "elasticsearch", "kibana", "rocketmq-namesrv", "rocketmq-broker", "rocketmq-console", "sentinel", "zipkin")
            $toStop = @($allServices | Where-Object { $_ -notin $composeServices })
            if ($toStop.Count -gt 0) {
                Write-Info "停止不在 Profile 内的基础设施: $($toStop -join ' ')"
                & docker compose -f docker-compose.middleware.yml stop @toStop 2>&1
                if ($LASTEXITCODE -ne 0) {
                    Write-Err "Docker Compose stop 失败"
                    exit 1
                }
            }
            & docker compose -f docker-compose.middleware.yml up -d @composeServices 2>&1
            if ($LASTEXITCODE -ne 0) {
                Write-Err "Docker Compose up 失败"
                exit 1
            }
        } else {
            & docker compose -f docker-compose.middleware.yml up -d 2>&1
            if ($LASTEXITCODE -ne 0) {
                Write-Err "Docker Compose up 失败"
                exit 1
            }
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

        # Wait others based on profile
        $seataOk = Test-Port 8091 30
        if ($seataOk) { Write-Ok "Seata 就绪" } else { Write-Warn "Seata 未就绪" }

        if ($Profile -in @("full", "seckill")) {
            $rmqNameOk = Test-Port 9876 30
            if ($rmqNameOk) { Write-Ok "RocketMQ NameServer 就绪" } else { Write-Warn "RocketMQ NameServer 未就绪" }
            
            $rmqBrokerOk = Test-Port 10911 30
            if ($rmqBrokerOk) { Write-Ok "RocketMQ Broker 就绪" } else { Write-Warn "RocketMQ Broker 未就绪" }
        }

        if ($Profile -in @("full", "search")) {
            $esOk = Wait-Http "http://localhost:9200/_cluster/health" "Elasticsearch" 30
            if ($esOk) { Write-Ok "Elasticsearch 就绪" } else { Write-Warn "Elasticsearch 未就绪" }
        }

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

# ── 基础设施端口归属检查（启动后端前） ────────────────────────
# 即使 -SkipInfrastructure 也要执行；防止本机 MySQL84 抢占 3306 导致后端连到错误实例。
if (-not $SkipBackend) {
    Write-Banner "基础设施端口归属检查"
    $portCheckLog = Join-Path $LogsDir "startup-port-check.log"
    # 重置日志：每次启动只保留本轮结果，避免上轮残留干扰排查
    if (Test-Path $portCheckLog) { Remove-Item $portCheckLog -Force }
    $portCheckResult = Test-InfrastructurePortOwnership -Port 3306 -DisplayName "MySQL" -LogPath $portCheckLog
    if ($portCheckResult.Status -eq "Blocked") {
        Write-Host ""
        Write-Host "  [ABORT] 启动中止：请按上述提示停止 MySQL84 后重试。" -ForegroundColor Red
        Write-Host ""
        exit 1
    }
    Write-Host ""
}

# ── MySQL 就绪等待（启动后端前） ────────────────────────────
# -SkipInfrastructure 路径下，mall-inventory / mall-order / mall-pay 等需要 MySQL 的服务
# 直接拉起时若 MySQL 容器尚未就绪，会因 dataSource 初始化失败导致进程 Exited
# （Sprint 3.5 已观察到）。此处显式等待 MySQL ready，超时清晰报错。
if (-not $SkipBackend) {
    $mysqlReady = Wait-MySqlReady 90 2
    if (-not $mysqlReady) {
        Write-Host ""
        Write-Host "  [ABORT] MySQL 在 90 秒内未就绪 (127.0.0.1:3306)。" -ForegroundColor Red
        if ($SkipInfrastructure) {
            Write-Host "         你使用了 -SkipInfrastructure，请确认 MySQL 容器 (mall-mysql) 已启动：" -ForegroundColor Red
            Write-Host "           docker start mall-mysql" -ForegroundColor Red
            Write-Host "         或运行 .\scripts\start-middleware.ps1 拉起 MySQL 后重试。" -ForegroundColor Red
        } else {
            Write-Host "         Docker Compose 已执行但 MySQL 仍超时，请检查 docker logs mall-mysql。" -ForegroundColor Red
        }
        Write-Host ""
        exit 1
    }
    Write-Host ""
}

# ── 启动后端服务 ──────────────────────────────────────
$Results = [System.Collections.ArrayList]::new()

function Save-ResultsState {
    $stateObj = if ($script:Processes) { $script:Processes.Clone() } else { @{} }
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

    $filteredServices = @()
    if ($Profile -eq "core") {
        $filteredServices = @("mall-user", "mall-auth", "mall-product", "mall-inventory", "mall-cart", "mall-order", "mall-gateway")
    } elseif ($Profile -eq "search") {
        $filteredServices = @("mall-user", "mall-auth", "mall-product", "mall-inventory", "mall-cart", "mall-order", "mall-gateway", "mall-search")
    } elseif ($Profile -eq "seckill") {
        $filteredServices = @("mall-user", "mall-auth", "mall-product", "mall-inventory", "mall-order", "mall-message", "mall-seckill", "mall-gateway")
    } else {
        $filteredServices = $BackendServices | Select-Object -ExpandProperty Name
    }

    foreach ($svc in $BackendServices) {
        $name = $svc.Name
        if ($name -notin $filteredServices) {
            Write-Info "跳过服务 $name (基于 Profile: $Profile)"
            if ($script:Processes.ContainsKey($name)) {
                $oldPid = $script:Processes[$name].PID
                $stopOk = $true
                if ($oldPid) {
                    $stopOk = Stop-ManagedBackendProcess $name $oldPid $svc.Port
                }
                if ($stopOk) {
                    $script:Processes.Remove($name)
                    Save-ResultsState
                } else {
                    Add-Result @{
                        Name = $name; PID = $oldPid; Port = $svc.Port
                        Status = "StopFailed"; Type = "backend"; Log = $null
                    }
                }
            }
            continue
        }
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
            if ($Processes.ContainsKey($name) -and $Processes[$name].PID -eq $existingPid -and (Test-ManagedBackendProcess $existingPid $name)) {
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
        $LanIP = if ($env:MALL_INFRA_HOST) { $env:MALL_INFRA_HOST } else { "127.0.0.1" }
        $env:NACOS_SERVER       = "${LanIP}:8848"
        $env:MYSQL_HOST         = $LanIP
        $env:REDIS_HOST         = $LanIP
        $env:ROCKETMQ_NAMESRV   = "${LanIP}:9876"
        $env:ES_HOST            = $LanIP
        $env:ES_PORT            = "9200"
        $env:SENTINEL_DASHBOARD = "${LanIP}:8080"
        $jvmArgs = @("-jar", $jar.FullName)
        if ($LowMemory) {
            $jvmArgs = @("-Xms64m", "-Xmx320m", "-XX:MaxMetaspaceSize=192m") + $jvmArgs
        }

        $proc = Start-Process -FilePath $JavaCmd `
            -ArgumentList $jvmArgs `
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
                Log = $logFile; Jar = $jar.FullName
                StartTime = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
            }
        } else {
            $status = if ($proc.HasExited) { "Exited" } else { "Timeout" }
            Write-Warn "$name PID=$($proc.Id) 端口 $port 未就绪，状态=$status，请检查日志"
            Add-Result @{
                Name = $name; PID = $proc.Id; Port = $port
                Status = $status; Type = "backend"
                Log = $logFile; Jar = $jar.FullName
                StartTime = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
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

                $npmCommand = Get-Command npm.cmd -ErrorAction SilentlyContinue
                if (-not $npmCommand) {
                    $npmCommand = Get-Command npm -ErrorAction SilentlyContinue
                }
                if (-not $npmCommand) {
                    Write-Err "未找到 npm 或 npm.cmd，无法启动前端"
                    Add-Result @{
                        Name = "frontend"; PID = $null; Port = $FrontendPort
                        Status = "NpmNotFound"; Type = "frontend"
                        Log = $frontendErr
                    }
                    $proc = $null
                } else {
                    $NpmCmd = $npmCommand.Source

                    $proc = Start-Process -FilePath "cmd.exe" `
                        -ArgumentList "/c", "`"$NpmCmd`" run dev" `
                        -WorkingDirectory $FrontendDir `
                        -RedirectStandardOutput $frontendLog `
                        -RedirectStandardError $frontendErr `
                        -WindowStyle Hidden `
                        -PassThru
                }

                if (-not $proc -and $npmCommand) {
                    Write-Err "前端进程创建失败"
                } elseif (-not $npmCommand) {
                    # 记录已在上方处理
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
    Write-Host "  网关地址: http://localhost:9100" -ForegroundColor Cyan
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
