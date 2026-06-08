# =====================================================
# MallCloud 初始化数据库 (Windows PowerShell)
# =====================================================
param(
    [switch]$Force
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectDir = Split-Path -Parent $scriptDir
$dbDir = Join-Path $projectDir "db\init"

$mysqlHost = if ($env:MYSQL_HOST) { $env:MYSQL_HOST } else { "127.0.0.1" }
$mysqlPort = if ($env:MYSQL_PORT) { $env:MYSQL_PORT } else { "3306" }
$mysqlUser = if ($env:MYSQL_USER) { $env:MYSQL_USER } else { "root" }
$mysqlPwd  = if ($env:MYSQL_PWD)  { $env:MYSQL_PWD }  else { "root" }
$mysqlArgs = @("-h$mysqlHost", "-P$mysqlPort", "-u$mysqlUser", "-p$mysqlPwd", "--default-character-set=utf8mb4")

if (-not $Force) {
    throw "init-db.ps1 会重建业务表并写入演示数据，seed.sql 不是幂等脚本。确认是新环境或允许重置数据后使用 -Force。"
}

function Invoke-MysqlFile {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    Get-Content -Raw -Encoding UTF8 -LiteralPath $Path | & mysql @mysqlArgs
    if ($LASTEXITCODE -ne 0) {
        throw "$(Split-Path -Leaf $Path) 执行失败，退出码 $LASTEXITCODE"
    }
}

Write-Host "=========================================" -ForegroundColor Green
Write-Host "  初始化 MallCloud 数据库" -ForegroundColor Green
Write-Host "  Host: $mysqlHost`:$mysqlPort" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green

# 1. 创建库 + 表
$sql1 = Join-Path $dbDir "00-create-databases.sql"
Write-Host ">> 执行 00-create-databases.sql ..." -ForegroundColor Cyan
Invoke-MysqlFile -Path $sql1

# 2. 种子数据
$sql2 = Join-Path $dbDir "seed.sql"
Write-Host ">> 执行 seed.sql ..." -ForegroundColor Cyan
Invoke-MysqlFile -Path $sql2

# 3. 验证
Write-Host ""
Write-Host "=========================================" -ForegroundColor Green
Write-Host "  数据库初始化完成！" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green
Write-Host ""
Write-Host "测试账号（统一密码 123456）："
Write-Host "  USER:     zhangsan    / 123456"
Write-Host "  MERCHANT: merchant01  / 123456"
Write-Host "  ADMIN:    admin       / 123456"
Write-Host ""
