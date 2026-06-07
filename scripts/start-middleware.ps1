# =====================================================
# MallCloud 启动所有中间件 (Windows PowerShell)
# 使用：.\scripts\start-middleware.ps1
# =====================================================
$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$dockerDir = Join-Path $scriptDir "..\deploy\docker"

Set-Location $dockerDir

Write-Host "=========================================" -ForegroundColor Green
Write-Host "  启动 MallCloud 中间件" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green

docker compose -f docker-compose.middleware.yml up -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "启动失败！" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "等待中间件启动完成..." -ForegroundColor Yellow

# 等待 Nacos
Write-Host -NoNewline "等待 Nacos ... "
$nacosReady = $false
for ($i = 1; $i -le 30; $i++) {
    try {
        $r = Invoke-WebRequest -Uri "http://localhost:8848/nacos/" -UseBasicParsing -TimeoutSec 2 -ErrorAction Stop
        if ($r.StatusCode -eq 200) { $nacosReady = $true; break }
    } catch {}
    Start-Sleep -Seconds 2
    Write-Host -NoNewline "."
}
if ($nacosReady) { Write-Host " OK" -ForegroundColor Green } else { Write-Host " TIMEOUT" -ForegroundColor Red }

# 等待 MySQL
Write-Host -NoNewline "等待 MySQL ... "
$mysqlReady = $false
for ($i = 1; $i -le 30; $i++) {
    $r = docker exec mall-mysql mysqladmin ping -uroot -proot 2>$null
    if ($LASTEXITCODE -eq 0) { $mysqlReady = $true; break }
    Start-Sleep -Seconds 2
    Write-Host -NoNewline "."
}
if ($mysqlReady) { Write-Host " OK" -ForegroundColor Green } else { Write-Host " TIMEOUT" -ForegroundColor Red }

Write-Host ""
Write-Host "=========================================" -ForegroundColor Green
Write-Host "  中间件启动完成！" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green
Write-Host ""
Write-Host "  Nacos:    http://localhost:8848/nacos  (nacos/nacos)"
Write-Host "  Sentinel: http://localhost:8080        (sentinel/sentinel)"
Write-Host "  Zipkin:   http://localhost:9411"
Write-Host "  Kibana:   http://localhost:5601"
Write-Host ""
Write-Host "下一步："
Write-Host "  1. 初始化数据库: .\scripts\init-db.ps1"
Write-Host "  2. 启动微服务"
Write-Host ""
