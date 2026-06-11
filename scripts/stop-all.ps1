#Requires -Version 7.0
# =====================================================
# MallCloud 停止脚本 (PowerShell 7+)
# 用法: pwsh .\scripts\stop-all.ps1
# =====================================================
$ErrorActionPreference = "Continue"

$ScriptDir  = $PSScriptRoot
$ProjectRoot = Split-Path -Parent $ScriptDir
$RuntimeDir  = Join-Path $ProjectRoot ".runtime"
$StateFile   = Join-Path $RuntimeDir "processes.json"

function Write-Ok($msg)   { Write-Host "  [OK]   $msg" -ForegroundColor Green }
function Write-Warn($msg) { Write-Host "  [WARN] $msg" -ForegroundColor Yellow }
function Write-Err($msg)  { Write-Host "  [FAIL] $msg" -ForegroundColor Red }
function Write-Info($msg) { Write-Host "  [....] $msg" -ForegroundColor Gray }

function Get-ProcessCommandLine([int]$ProcessId) {
    try {
        $proc = Get-CimInstance Win32_Process -Filter "ProcessId = $ProcessId" -ErrorAction Stop
        return $proc.CommandLine
    } catch {
        return $null
    }
}

function Test-ManagedBackendProcess([int]$ProcessId, [string]$ServiceName, [string]$ExpectedJar = $null) {
    $proc = Get-Process -Id $ProcessId -ErrorAction SilentlyContinue
    if (-not $proc -or $proc.ProcessName -ne "java") {
        return $false
    }

    $commandLine = Get-ProcessCommandLine $ProcessId
    if ([string]::IsNullOrWhiteSpace($commandLine)) {
        return $false
    }

    $normalized = $commandLine.Replace("/", "\")
    if (-not [string]::IsNullOrWhiteSpace($ExpectedJar)) {
        $normalizedJar = $ExpectedJar.Replace("/", "\")
        if ($normalized -like "*$normalizedJar*") {
            return $true
        }
    }

    $targetDir = (Join-Path (Join-Path $ProjectRoot $ServiceName) "target").Replace("/", "\")
    return ($normalized -like "*$targetDir\*" -and $normalized -like "*.jar*")
}

Write-Host ""
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  MallCloud 停止服务" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# 检查状态文件
if (-not (Test-Path $StateFile)) {
    Write-Warn "未找到状态文件: $StateFile"
    Write-Warn "没有可停止的进程记录。"
    Write-Host ""
    Write-Host "如需手动停止，请使用任务管理器或以下命令：" -ForegroundColor Gray
    Write-Host "  Get-Process -Name java | Stop-Process" -ForegroundColor Gray
    Write-Host ""
    exit 0
}

# 读取状态
$raw = Get-Content $StateFile -Raw -Encoding UTF8
if ($raw.Trim().Length -eq 0) {
    Write-Warn "状态文件为空，没有可停止的进程。"
    exit 0
}

$Processes = $raw | ConvertFrom-Json -AsHashtable

$stopped   = 0
$skipped   = 0
$failed    = 0

foreach ($name in $Processes.Keys) {
    $entry = $Processes[$name]
    $processId = $entry.PID

    if (-not $processId) {
        Write-Warn "$name 无 PID 记录，跳过"
        $skipped++
        continue
    }

    # 检查进程是否存在
    $proc = Get-Process -Id $processId -ErrorAction SilentlyContinue
    if (-not $proc) {
        Write-Warn "$name PID=$processId 已退出"
        $skipped++
        continue
    }

    # 检查进程名称是否合理（java 或 node）
    $procName = $proc.ProcessName
    if ($procName -notin @("java", "node")) {
        Write-Warn "$name PID=$processId 进程名称为 '$procName'，已不是预期进程，跳过"
        $skipped++
        continue
    }
    if ($procName -eq "java" -and -not (Test-ManagedBackendProcess $processId $name $entry.Jar)) {
        Write-Warn "$name PID=$processId 命令行未匹配本项目 $name jar，跳过"
        $skipped++
        continue
    }

    Write-Info "停止 $name PID=$processId ..."

    # 先尝试正常关闭
    try {
        $proc.CloseMainWindow() | Out-Null
        $exited = $proc.WaitForExit(5000)
        if ($exited) {
            Write-Ok "$name 已正常停止"
            $stopped++
            continue
        }
    } catch {}

    # 强制终止
    Write-Info "$name 正常关闭超时，强制终止 ..."
    try {
        Stop-Process -Id $processId -Force -ErrorAction Stop
        Start-Sleep -Seconds 1
        $proc2 = Get-Process -Id $processId -ErrorAction SilentlyContinue
        if ($proc2) {
            Write-Err "$name PID=$processId 强制终止失败"
            $failed++
        } else {
            Write-Ok "$name 已强制终止"
            $stopped++
        }
    } catch {
        Write-Err "$name PID=$processId 终止异常: $_"
        $failed++
    }
}

# 清理状态文件中的已停止进程
$cleaned = @{}
foreach ($name in $Processes.Keys) {
    $entry = $Processes[$name]
    if ($entry.PID) {
        $proc = Get-Process -Id $entry.PID -ErrorAction SilentlyContinue
        if ($proc -and ($proc.ProcessName -eq "node" -or (Test-ManagedBackendProcess $entry.PID $name $entry.Jar))) {
            $cleaned[$name] = $entry
        }
    }
}

if ($cleaned.Count -gt 0) {
    $cleaned | ConvertTo-Json -Depth 5 | Set-Content -Path $StateFile -Encoding UTF8
} else {
    # 所有进程已停止，删除状态文件
    Remove-Item -Path $StateFile -Force -ErrorAction SilentlyContinue
}

Write-Host ""
Write-Host "  已停止: $stopped" -ForegroundColor Green
if ($skipped -gt 0) { Write-Host "  跳过:   $skipped" -ForegroundColor Yellow }
if ($failed -gt 0)  { Write-Host "  失败:   $failed" -ForegroundColor Red }
Write-Host ""
