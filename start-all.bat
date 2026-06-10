@echo off
setlocal EnableExtensions DisableDelayedExpansion
chcp 65001 >nul 2>&1

echo =====================================================
echo  MallCloud 一键启动 (Windows BAT 代理)
echo =====================================================

where pwsh.exe >nul 2>&1
if errorlevel 1 (
    echo [ERROR] pwsh.exe not found. Install PowerShell 7+
    pause
    exit /b 1
)

set "PS_ARGS="
set "NOPAUSE=0"

:parse_args
if "%~1"=="" goto run_pwsh

if /I "%~1"=="--help" (
    echo [INFO] Supported parameters:
    echo        --profile ^<core^|search^|seckill^|full^>
    echo        --skip-infrastructure --skip-frontend --skip-backend
    echo        --no-build --clean-logs --allow-partial --low-memory
    echo        --no-install --no-pause --help
    exit /b 0
)
if /I "%~1"=="--profile" (
    if "%~2"=="" (
        echo [ERROR] --profile requires a value
        exit /b 1
    )
    if /I not "%~2"=="core" if /I not "%~2"=="search" if /I not "%~2"=="seckill" if /I not "%~2"=="full" (
        echo [ERROR] Invalid profile: %~2
        exit /b 1
    )
    set "PS_ARGS=%PS_ARGS% -Profile %~2"
    shift
    shift
    goto parse_args
)
if /I "%~1"=="--skip-infrastructure" (
    set "PS_ARGS=%PS_ARGS% -SkipInfrastructure"
    shift
    goto parse_args
)
if /I "%~1"=="--skip-frontend" (
    set "PS_ARGS=%PS_ARGS% -SkipFrontend"
    shift
    goto parse_args
)
if /I "%~1"=="--skip-backend" (
    set "PS_ARGS=%PS_ARGS% -SkipBackend"
    shift
    goto parse_args
)
if /I "%~1"=="--no-build" (
    set "PS_ARGS=%PS_ARGS% -SkipBuild"
    shift
    goto parse_args
)
if /I "%~1"=="--clean-logs" (
    set "PS_ARGS=%PS_ARGS% -CleanLogs"
    shift
    goto parse_args
)
if /I "%~1"=="--allow-partial" (
    set "PS_ARGS=%PS_ARGS% -AllowPartial"
    shift
    goto parse_args
)
if /I "%~1"=="--low-memory" (
    set "PS_ARGS=%PS_ARGS% -LowMemory"
    shift
    goto parse_args
)
if /I "%~1"=="--no-install" (
    set "PS_ARGS=%PS_ARGS% -NoInstall"
    shift
    goto parse_args
)
if /I "%~1"=="--no-pause" (
    set "NOPAUSE=1"
    shift
    goto parse_args
)

echo [ERROR] Unknown parameter: %~1
echo [INFO] Supported parameters: --profile ^<core^|search^|seckill^|full^>, --skip-infrastructure, --skip-frontend, --skip-backend, --no-build, --clean-logs, --allow-partial, --low-memory, --no-install, --no-pause, --help
exit /b 1

:run_pwsh
echo [INFO] 正在将参数传递给 PowerShell 启动脚本...
pwsh.exe -NoProfile -ExecutionPolicy Bypass -Command "& '%~dp0scripts\start-all.ps1' %PS_ARGS%"
set "EXT_CODE=%errorlevel%"
if %EXT_CODE% neq 0 (
    echo [ERROR] Startup failed.
    if "%NOPAUSE%"=="0" pause
    exit /b %EXT_CODE%
)

if "%NOPAUSE%"=="0" pause
exit /b 0
