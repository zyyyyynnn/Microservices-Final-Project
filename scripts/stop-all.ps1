# =====================================================
# MallCloud 一键停 (Windows PowerShell)
# =====================================================
$ErrorActionPreference = "Continue"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$dockerDir = Join-Path $scriptDir "..\deploy\docker"

Set-Location $dockerDir

Write-Host "停止全栈..." -ForegroundColor Yellow
docker compose -f docker-compose.all.yml down 2>$null

Write-Host "停止中间件..." -ForegroundColor Yellow
docker compose -f docker-compose.middleware.yml down

$ans = Read-Host "是否同时删除数据卷？(y/N)"
if ($ans -eq "y" -or $ans -eq "Y") {
  Write-Host "删除数据卷..." -ForegroundColor Red
  docker compose -f docker-compose.middleware.yml down -v
  Write-Host "完成。" -ForegroundColor Green
} else {
  Write-Host "保留数据卷。" -ForegroundColor Green
}
