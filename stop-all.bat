@echo off
setlocal EnableExtensions DisableDelayedExpansion
chcp 65001 >nul 2>&1

rem =====================================================
rem  MallCloud Stop Script (Windows BAT)
rem  Usage: stop-all.bat [--no-pause]
rem =====================================================

set "ROOT=%~dp0"
set "RUNTIME_DIR=%ROOT%.runtime"
set "STATE_FILE=%RUNTIME_DIR%\processes.json"

rem -- 参数解析 --
set "ARG_NO_PAUSE=0"

:PARSE_ARGS
if "%~1"=="" goto :ARGS_DONE
if /i "%~1"=="--no-pause" ( set "ARG_NO_PAUSE=1" & shift & goto :PARSE_ARGS )
echo [ERROR] Unknown argument: %~1
echo [INFO] Usage: stop-all.bat [--no-pause]
goto :END

:ARGS_DONE

echo.
echo ============================================================
echo  MallCloud Stop
echo ============================================================
echo.

if not exist "%STATE_FILE%" (
    echo [INFO] No state file found: %STATE_FILE%
    echo [INFO] No processes to stop.
    echo.
    echo [INFO] To stop manually:
    echo        tasklist /fi "IMAGENAME eq java.exe"
    echo        taskkill /PID ^<pid^> /T /F
    goto :END
)

set "_stopped=0"
set "_skipped=0"
set "_failed=0"

for /f "tokens=*" %%n in ('pwsh.exe -NoProfile -Command "$s = Get-Content '%STATE_FILE%' -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable; $s.Keys | Sort-Object"') do (
    call :STOP_ONE "%%n"
)

rem 清理状态文件
pwsh.exe -NoProfile -Command "$s = Get-Content '%STATE_FILE%' -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable; $cleaned = @{}; foreach ($k in $s.Keys) { $e = $s[$k]; if ($e.pid) { $p = Get-Process -Id $e.pid -ErrorAction SilentlyContinue; if ($p -and ($p.ProcessName -eq 'java' -or $p.ProcessName -eq 'node')) { $cleaned[$k] = $e } } }; if ($cleaned.Count -gt 0) { $cleaned | ConvertTo-Json -Depth 5 | Set-Content '%STATE_FILE%' -Encoding UTF8 } else { Remove-Item '%STATE_FILE%' -Force -ErrorAction SilentlyContinue }" >nul 2>&1

echo.
echo  Stopped: %_stopped%
echo  Skipped: %_skipped%
if %_failed% GTR 0 echo  Failed:  %_failed%
echo.

:END
if "%ARG_NO_PAUSE%"=="1" exit /b 0
pause
exit /b 0

rem ============================================================
rem  Subroutine: STOP_ONE
rem  Args: %1=service-name
rem ============================================================
:STOP_ONE
set "_name=%~1"

rem 获取 PID
for /f "tokens=*" %%p in ('pwsh.exe -NoProfile -Command "$s = Get-Content '%STATE_FILE%' -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable; $e = $s['%_name%']; if ($e -and $e.pid) { $e.pid } else { '' }"') do set "_pid=%%p"

if not defined _pid (
    echo [WARN] %_name% has no PID, skip
    set /a "_skipped+=1"
    exit /b 0
)
if "%_pid%"=="" (
    echo [WARN] %_name% has no PID, skip
    set /a "_skipped+=1"
    exit /b 0
)

rem 检查 PID 是否存在
tasklist /fi "PID eq %_pid%" 2>nul | findstr /i "%_pid%" >nul 2>&1
if errorlevel 1 (
    echo [WARN] %_name% PID=%_pid% already exited
    set /a "_skipped+=1"
    exit /b 0
)

rem 获取进程名
for /f "tokens=*" %%m in ('pwsh.exe -NoProfile -Command "(Get-Process -Id %_pid% -ErrorAction SilentlyContinue).ProcessName"') do set "_pname=%%m"

if not "%_pname%"=="java" if not "%_pname%"=="node" (
    echo [WARN] %_name% PID=%_pid% process is '%_pname%', not expected, skip
    set /a "_skipped+=1"
    exit /b 0
)

echo [STOP] %_name% PID=%_pid% ...

rem 尝试正常终止进程树
taskkill /PID %_pid% /T >nul 2>&1
if errorlevel 1 (
    echo [INFO] %_name% graceful stop failed, force killing...
    taskkill /PID %_pid% /T /F >nul 2>&1
    if errorlevel 1 (
        echo [FAIL] %_name% PID=%_pid% force kill failed
        set /a "_failed+=1"
    ) else (
        echo [OK]   %_name% force killed
        set /a "_stopped+=1"
    )
) else (
    echo [OK]   %_name% stopped
    set /a "_stopped+=1"
)

exit /b 0
