@echo off
setlocal EnableExtensions DisableDelayedExpansion
chcp 65001 >nul 2>&1

echo =====================================================
echo  MallCloud 一键启动 (Windows BAT 代理)
echo =====================================================
echo [INFO] 正在将参数传递给 PowerShell 启动脚本...

where pwsh.exe >nul 2>&1
if errorlevel 1 (
    echo [ERROR] pwsh.exe not found. Install PowerShell 7+
    pause
    exit /b 1
)

pwsh.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\start-all.ps1" %*
if errorlevel 1 (
    echo [ERROR] Startup failed.
    pause
    exit /b 1
)

exit /b 0
