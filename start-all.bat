@echo off
setlocal EnableExtensions DisableDelayedExpansion
chcp 65001 >nul 2>&1

rem =====================================================
rem  MallCloud 一键启动 (Windows BAT)
rem  用法: start-all.bat [options]
rem =====================================================

set "ROOT=%~dp0"
set "RUNTIME_DIR=%ROOT%.runtime"
set "LOGS_DIR=%RUNTIME_DIR%\logs"
set "STATE_FILE=%RUNTIME_DIR%\processes.json"
set "INFRA_STATE=%RUNTIME_DIR%\infrastructure.json"
set "COMPOSE_FILE=%ROOT%deploy\docker\docker-compose.middleware.yml"
set "FRONTEND_DIR=%ROOT%mall-frontend"

rem -- 参数解析 --
set "ARG_SKIP_INFRA=0"
set "ARG_SKIP_BACKEND=0"
set "ARG_SKIP_FRONTEND=0"
set "ARG_CLEAN_LOGS=0"
set "ARG_NO_BUILD=0"
set "ARG_NO_PAUSE=0"

:PARSE_ARGS
if "%~1"=="" goto :ARGS_DONE
if /i "%~1"=="--skip-infrastructure" ( set "ARG_SKIP_INFRA=1" & shift & goto :PARSE_ARGS )
if /i "%~1"=="--skip-backend"        ( set "ARG_SKIP_BACKEND=1" & shift & goto :PARSE_ARGS )
if /i "%~1"=="--skip-frontend"       ( set "ARG_SKIP_FRONTEND=1" & shift & goto :PARSE_ARGS )
if /i "%~1"=="--clean-logs"          ( set "ARG_CLEAN_LOGS=1" & shift & goto :PARSE_ARGS )
if /i "%~1"=="--no-build"            ( set "ARG_NO_BUILD=1" & shift & goto :PARSE_ARGS )
if /i "%~1"=="--no-pause"            ( set "ARG_NO_PAUSE=1" & shift & goto :PARSE_ARGS )
echo [ERROR] Unknown argument: %~1
echo [INFO] Usage: start-all.bat [--skip-infrastructure] [--skip-backend] [--skip-frontend] [--clean-logs] [--no-build] [--no-pause]
goto :FAIL

:ARGS_DONE

rem -- 初始化目录 --
if not exist "%RUNTIME_DIR%" mkdir "%RUNTIME_DIR%"
if not exist "%LOGS_DIR%" mkdir "%LOGS_DIR%"
if "%ARG_CLEAN_LOGS%"=="1" (
    echo [INFO] Cleaning old logs...
    if exist "%LOGS_DIR%\*" del /q "%LOGS_DIR%\*" 2>nul
)

rem -- 初始化状态文件 --
if not exist "%STATE_FILE%" (
    echo {} > "%STATE_FILE%"
)

echo.
echo ============================================================
echo  MallCloud Startup
echo ============================================================
echo.

rem ============================================================
rem  环境检查
rem ============================================================
echo [ENV] Checking prerequisites...
echo.

rem PowerShell 7+
where pwsh.exe >nul 2>&1
if errorlevel 1 (
    echo [ERROR] pwsh.exe not found. Install PowerShell 7+
    echo [FIX]   https://github.com/PowerShell/PowerShell/releases
    goto :FAIL
)
echo [OK]   PowerShell 7+

rem Java
where java.exe >nul 2>&1
if errorlevel 1 (
    echo [ERROR] java.exe not found. Install JDK 21
    echo [FIX]   https://adoptium.net/temurin/releases/?version=21
    goto :FAIL
)
set "JAVA_MAJOR="
for /f "usebackq tokens=*" %%v in (`pwsh.exe -NoProfile -NoLogo -Command "([regex]::Match((& java -version 2>&1 | Select-Object -First 1), '(\d+)')).Groups[1].Value"`) do set "JAVA_MAJOR=%%v"
if "%JAVA_MAJOR%"=="" (
    echo [WARN] Cannot detect Java version, assuming JDK 21
    set "JAVA_MAJOR=21"
)
if not "%JAVA_MAJOR%"=="21" (
    echo [ERROR] JDK 21 required, found: JDK %JAVA_MAJOR%
    echo [FIX]   https://adoptium.net/temurin/releases/?version=21
    goto :FAIL
)
echo [OK]   JDK %JAVA_MAJOR%

rem Maven
set "MVN_CMD="
if exist "%ROOT%mvnw.cmd" (
    set "MVN_CMD=call "%ROOT%mvnw.cmd""
    echo [OK]   Maven Wrapper
) else (
    where mvn.cmd >nul 2>&1
    if errorlevel 1 (
        where mvn >nul 2>&1
        if errorlevel 1 (
            echo [ERROR] mvn not found. Install Maven 3.9+ or add Maven Wrapper
            echo [FIX]   https://maven.apache.org/download.cgi
            goto :FAIL
        )
        set "MVN_CMD=call mvn"
    ) else (
        set "MVN_CMD=call mvn.cmd"
    )
    echo [OK]   Maven
)

rem Node.js
where node.exe >nul 2>&1
if errorlevel 1 (
    echo [ERROR] node.exe not found. Install Node.js 18+
    echo [FIX]   https://nodejs.org/
    goto :FAIL
)
for /f "tokens=*" %%v in ('node --version 2^>nul') do set "NODE_VER=%%v"
echo [OK]   Node.js %NODE_VER%

rem npm
where npm.cmd >nul 2>&1
if errorlevel 1 (
    where npm >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] npm not found
        goto :FAIL
    )
)
echo [OK]   npm

rem Docker (only when infrastructure is needed)
if "%ARG_SKIP_INFRA%"=="1" (
    echo [SKIP] Docker check (--skip-infrastructure)
    goto :SKIP_DOCKER_CHECK
)
where docker.exe >nul 2>&1
if errorlevel 1 (
    echo [ERROR] docker.exe not found. Install Docker Desktop
    echo [FIX]   https://www.docker.com/products/docker-desktop/
    echo [INFO] Or use --skip-infrastructure
    goto :FAIL
)
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker daemon not running. Start Docker Desktop
    echo [INFO] Or use --skip-infrastructure
    goto :FAIL
)
docker compose version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] docker compose not available
    goto :FAIL
)
echo [OK]   Docker + Compose
:SKIP_DOCKER_CHECK

echo.
echo [ENV] All prerequisites met.
echo.

rem ============================================================
rem  启动基础设施
rem ============================================================
if "%ARG_SKIP_INFRA%"=="1" (
    echo [SKIP] Infrastructure (--skip-infrastructure)
    goto :SKIP_INFRA
)

echo ============================================================
echo  Starting Infrastructure (Docker Compose)
echo ============================================================
echo.

if not exist "%COMPOSE_FILE%" (
    echo [ERROR] Compose file not found: %COMPOSE_FILE%
    goto :FAIL
)

echo [INFO] docker compose up -d ...
docker compose -f "%COMPOSE_FILE%" up -d >"%LOGS_DIR%\infra-compose.log" 2>&1
if errorlevel 1 (
    echo [ERROR] Docker Compose failed. Log: %LOGS_DIR%\infra-compose.log
    type "%LOGS_DIR%\infra-compose.log"
    goto :FAIL
)
echo [OK]   Docker Compose executed

rem 等待 MySQL
echo [WAIT] MySQL (port 3306) ...
set /a "_w=0"
:WAIT_MYSQL
set /a "_w+=1"
if %_w% GTR 30 (
    echo [WARN] MySQL timeout, continuing
    goto :MYSQL_DONE
)
pwsh.exe -NoProfile -Command "exit ([System.Net.Sockets.TcpClient]::new().BeginConnect('127.0.0.1',3306,$null,$null).AsyncWaitHandle.WaitOne(2000))" >nul 2>&1
if errorlevel 1 (
    timeout /t 2 /nobreak >nul
    goto :WAIT_MYSQL
)
echo [OK]   MySQL ready
:MYSQL_DONE

rem 等待 Nacos
echo [WAIT] Nacos (port 8848) ...
set /a "_w=0"
:WAIT_NACOS
set /a "_w+=1"
if %_w% GTR 40 (
    echo [WARN] Nacos timeout, continuing
    goto :NACOS_DONE
)
pwsh.exe -NoProfile -Command "try { $r = Invoke-WebRequest -Uri 'http://127.0.0.1:8848/nacos/' -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop; exit ($r.StatusCode -ge 200 -and $r.StatusCode -lt 500) } catch { exit 0 }" >nul 2>&1
if errorlevel 1 (
    timeout /t 3 /nobreak >nul
    goto :WAIT_NACOS
)
echo [OK]   Nacos ready
:NACOS_DONE

rem 检查 Redis
echo [WAIT] Redis (port 6379) ...
pwsh.exe -NoProfile -Command "exit ([System.Net.Sockets.TcpClient]::new().BeginConnect('127.0.0.1',6379,$null,$null).AsyncWaitHandle.WaitOne(2000))" >nul 2>&1
if errorlevel 1 (
    echo [WARN] Redis not ready
) else (
    echo [OK]   Redis ready
)

rem 检查 Elasticsearch
echo [WAIT] Elasticsearch (port 9200) ...
set /a "_w=0"
:WAIT_ES
set /a "_w+=1"
if %_w% GTR 20 (
    echo [WARN] Elasticsearch timeout, continuing
    goto :ES_DONE
)
pwsh.exe -NoProfile -Command "try { Invoke-WebRequest -Uri 'http://127.0.0.1:9200/_cluster/health' -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop | Out-Null; exit 1 } catch { exit 0 }" >nul 2>&1
if errorlevel 1 (
    echo [OK]   Elasticsearch ready
    goto :ES_DONE
)
timeout /t 3 /nobreak >nul
goto :WAIT_ES
:ES_DONE

echo.
:SKIP_INFRA

rem ============================================================
rem  Maven 构建
rem ============================================================
if "%ARG_SKIP_BACKEND%"=="1" goto :SKIP_BUILD
if "%ARG_NO_BUILD%"=="1" (
    echo [SKIP] Maven build (--no-build)
    goto :SKIP_BUILD
)

echo ============================================================
echo  Maven Build
echo ============================================================
echo.

echo [INFO] mvn -DskipTests package
echo [INFO] Log: %LOGS_DIR%\build.log

%MVN_CMD% -DskipTests package >"%LOGS_DIR%\build.log" 2>&1
if errorlevel 1 (
    echo [ERROR] Maven build failed
    echo [LOG]   %LOGS_DIR%\build.log
    echo.
    echo [INFO] Last 20 lines:
    pwsh.exe -NoProfile -Command "Get-Content '%LOGS_DIR%\build.log' -Tail 20"
    goto :FAIL
)
echo [OK]   Maven build succeeded
echo.

:SKIP_BUILD

rem ============================================================
rem  启动后端服务
rem ============================================================
if "%ARG_SKIP_BACKEND%"=="1" (
    echo [SKIP] Backend services (--skip-backend)
    goto :SKIP_BACKEND
)

echo ============================================================
echo  Starting Backend Services
echo ============================================================
echo.

set "NACOS_SERVER=127.0.0.1:8848"
set "MYSQL_HOST=127.0.0.1"
set "REDIS_HOST=127.0.0.1"
set "ROCKETMQ_NAMESRV=127.0.0.1:9876"

rem Group 1: mall-user
call :START_SERVICE mall-user 9002
if errorlevel 1 goto :FAIL

rem Group 2: mall-auth
call :START_SERVICE mall-auth 9001
if errorlevel 1 goto :FAIL

rem Group 3: mall-product, mall-inventory
call :START_SERVICE mall-product 9003
if errorlevel 1 goto :FAIL
call :START_SERVICE mall-inventory 9004
if errorlevel 1 goto :FAIL

rem Group 4: mall-cart, mall-order, mall-pay
call :START_SERVICE mall-cart 9005
if errorlevel 1 goto :FAIL
call :START_SERVICE mall-order 9006
if errorlevel 1 goto :FAIL
call :START_SERVICE mall-pay 9007
if errorlevel 1 goto :FAIL

rem Group 5: mall-message, mall-search, mall-seckill
call :START_SERVICE mall-message 9010
if errorlevel 1 goto :FAIL
call :START_SERVICE mall-search 9008
if errorlevel 1 goto :FAIL
call :START_SERVICE mall-seckill 9009
if errorlevel 1 goto :FAIL

rem Group 6: mall-admin-biz, mall-job
call :START_SERVICE mall-admin-biz 9011
if errorlevel 1 goto :FAIL
call :START_SERVICE mall-job 9012
if errorlevel 1 goto :FAIL

rem Group 7: mall-gateway
call :START_SERVICE mall-gateway 9000
if errorlevel 1 goto :FAIL

echo.
:SKIP_BACKEND

rem ============================================================
rem  启动前端
rem ============================================================
if "%ARG_SKIP_FRONTEND%"=="1" (
    echo [SKIP] Frontend (--skip-frontend)
    goto :SKIP_FRONTEND
)

echo ============================================================
echo  Starting Frontend
echo ============================================================
echo.

if not exist "%FRONTEND_DIR%\package.json" (
    echo [ERROR] Frontend not found: %FRONTEND_DIR%\package.json
    goto :FAIL
)

rem 检查端口冲突
pwsh.exe -NoProfile -Command "$s = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, 5173); try { $s.Start(); $s.Stop(); exit 0 } catch { exit 1 }" >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Port 5173 already in use
    for /f "tokens=*" %%p in ('pwsh.exe -NoProfile -Command "(Get-NetTCPConnection -LocalPort 5173 -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1).OwningProcess"') do set "OCC_PID=%%p"
    echo [PID]  %OCC_PID%
    goto :FAIL
)

rem 安装依赖
if not exist "%FRONTEND_DIR%\node_modules" (
    echo [INFO] Installing frontend dependencies...
    pushd "%FRONTEND_DIR%"
    if exist "package-lock.json" (
        call npm ci >"%LOGS_DIR%\npm-install.log" 2>&1
    ) else (
        call npm install >"%LOGS_DIR%\npm-install.log" 2>&1
    )
    if errorlevel 1 (
        popd
        echo [ERROR] npm install failed. Log: %LOGS_DIR%\npm-install.log
        goto :FAIL
    )
    popd
    echo [OK]   Dependencies installed
) else (
    echo [OK]   node_modules exists
)

rem 启动前端
echo [INFO] Starting frontend dev server (port 5173) ...
for /f "tokens=*" %%i in ('pwsh.exe -NoProfile -Command "$p = Start-Process -FilePath 'npm.cmd' -ArgumentList 'run','dev' -WorkingDirectory '%FRONTEND_DIR%' -RedirectStandardOutput '%LOGS_DIR%\mall-frontend.log' -RedirectStandardError '%LOGS_DIR%\mall-frontend.err.log' -WindowStyle Hidden -PassThru; $p.Id"') do set "FE_PID=%%i"

if not defined FE_PID (
    echo [ERROR] Frontend process creation failed
    goto :FAIL
)

rem 等待前端端口
echo [WAIT] Frontend PID=%FE_PID% port=5173 ...
set /a "_w=0"
:WAIT_FE
set /a "_w+=1"
if %_w% GTR 20 (
    echo [WARN] Frontend port 5173 not ready. Log: %LOGS_DIR%\mall-frontend.log
    goto :FE_DONE
)
pwsh.exe -NoProfile -Command "exit ([System.Net.Sockets.TcpClient]::new().BeginConnect('127.0.0.1',5173,$null,$null).AsyncWaitHandle.WaitOne(2000))" >nul 2>&1
if errorlevel 1 (
    tasklist /fi "PID eq %FE_PID%" 2>nul | findstr /i "node" >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] Frontend process exited. Log: %LOGS_DIR%\mall-frontend.err.log
        goto :FAIL
    )
    timeout /t 3 /nobreak >nul
    goto :WAIT_FE
)
echo [OK]   Frontend PID=%FE_PID% port=5173 ready

rem 记录前端到状态文件
pwsh.exe -NoProfile -Command "$s = if (Test-Path '%STATE_FILE%') { Get-Content '%STATE_FILE%' -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable } else { @{} }; $s['frontend'] = @{name='frontend';type='frontend';pid=%FE_PID%;port=5173;status='Ready';startedAt=(Get-Date -Format 'o')}; $s | ConvertTo-Json -Depth 5 | Set-Content '%STATE_FILE%' -Encoding UTF8" >nul 2>&1

:FE_DONE
echo.
:SKIP_FRONTEND

rem ============================================================
rem  输出摘要
rem ============================================================
echo.
echo ============================================================
echo  MallCloud Startup Result
echo ============================================================
echo.

pwsh.exe -NoProfile -Command "$fmt = '{0,-22} {1,-10} {2,-10} {3,-12}'; Write-Host ($fmt -f 'Service','PID','Port','Status') -ForegroundColor White; Write-Host ($fmt -f '-------','---','----','------') -ForegroundColor Gray; $s = if (Test-Path '%STATE_FILE%') { Get-Content '%STATE_FILE%' -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable } else { @{} }; foreach ($k in ($s.Keys | Sort-Object)) { $e = $s[$k]; $pidStr = if ($e.pid) { [string]$e.pid } else { '-' }; $portStr = if ($e.port) { [string]$e.port } else { '-' }; $color = if ($e.status -eq 'Ready' -or $e.status -eq 'Healthy') { 'Green' } elseif ($e.status -eq 'Timeout') { 'Yellow' } else { 'Red' }; Write-Host ($fmt -f $e.name, $pidStr, $portStr, $e.status) -ForegroundColor $color }"

echo.
echo [INFO] Logs:     %LOGS_DIR%
echo [INFO] State:    %STATE_FILE%
echo [INFO] Stop:     stop-all.bat
echo.

:SUCCESS
if "%ARG_NO_PAUSE%"=="1" exit /b 0
pause
exit /b 0

:FAIL
echo.
echo [ERROR] Startup failed. Check the errors above.
echo [INFO] Logs: %LOGS_DIR%
if "%ARG_NO_PAUSE%"=="1" exit /b 1
pause
exit /b 1

rem ============================================================
rem  Subroutine: START_SERVICE
rem  Args: %1=service-name %2=port
rem ============================================================
:START_SERVICE
setlocal EnableDelayedExpansion
set "_svc_name=%~1"
set "_svc_port=%~2"
set "_svc_jar="

rem 查找 JAR
for /f "tokens=*" %%j in ('pwsh.exe -NoProfile -Command "Get-ChildItem -Path '%ROOT%%_svc_name%\target\%_svc_name%-*.jar' -ErrorAction SilentlyContinue | Where-Object { $_.Name -notmatch '^original-|sources\.jar$|javadoc\.jar$' } | Select-Object -First 1 -ExpandProperty FullName"') do set "_svc_jar=%%j"

if not defined "_svc_jar" (
    echo [ERROR] %_svc_name% JAR not found
    echo [INFO] Run Maven build first, or check target directory
    endlocal
    exit /b 1
)

rem 检查状态文件中的已有记录
for /f "tokens=*" %%p in ('pwsh.exe -NoProfile -Command "$s = if (Test-Path '%STATE_FILE%') { Get-Content '%STATE_FILE%' -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable } else { @{} }; if ($s.ContainsKey('%_svc_name%') -and $s['%_svc_name%'].pid) { $s['%_svc_name%'].pid } else { '' }"') do set "_existing_pid=%%p"

if defined "_existing_pid" (
    if not "!_existing_pid!"=="" (
        rem 检查 PID 是否仍存在且命令行匹配
        for /f "tokens=*" %%m in ('pwsh.exe -NoProfile -Command "$p = Get-Process -Id !_existing_pid! -ErrorAction SilentlyContinue; if ($p -and $p.ProcessName -eq 'java') { $cmd = (Get-CimInstance Win32_Process -Filter 'ProcessId=!_existing_pid!').CommandLine; if ($cmd -match '!_svc_name!') { 'MATCH' } else { 'MISMATCH' } } else { 'GONE' }"') do set "_pid_check=%%m"
        if "!_pid_check!"=="MATCH" (
            echo [SKIP] !_svc_name! already running, PID=!_existing_pid!, port=!_svc_port!
            endlocal
            exit /b 0
        )
        if "!_pid_check!"=="MISMATCH" (
            echo [WARN] !_svc_name! state PID=!_existing_pid! does not match, clearing
            pwsh.exe -NoProfile -Command "$s = Get-Content '%STATE_FILE%' -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable; $s.Remove('%_svc_name%'); $s | ConvertTo-Json -Depth 5 | Set-Content '%STATE_FILE%' -Encoding UTF8" >nul 2>&1
        )
    )
)

rem 检查端口是否已被占用
for /f "tokens=*" %%p in ('pwsh.exe -NoProfile -Command "$c = Get-NetTCPConnection -LocalPort %_svc_port% -State Listen -ErrorAction SilentlyContinue; if ($c) { $c[0].OwningProcess } else { '' }"') do set "_port_pid=%%p"

if defined "_port_pid" (
    if not "!_port_pid!"=="" (
        if "!_port_pid!"=="!_existing_pid!" (
            echo [SKIP] !_svc_name! port !_svc_port! already listened by PID=!_port_pid!
            endlocal
            exit /b 0
        )
        for /f "tokens=*" %%c in ('pwsh.exe -NoProfile -Command "(Get-CimInstance Win32_Process -Filter 'ProcessId=!_port_pid!' -ErrorAction SilentlyContinue).CommandLine"') do set "_port_cmd=%%c"
        echo [ERROR] Port !_svc_port! already in use by another process
        echo [PID]  !_port_pid!
        echo [CMD]  !_port_cmd!
        endlocal
        exit /b 1
    )
)

rem 启动 Java 进程
echo [START] %_svc_name% (port %_svc_port%) ...

for /f "tokens=*" %%i in ('pwsh.exe -NoProfile -Command "$p = Start-Process -FilePath 'java.exe' -ArgumentList '-jar','%_svc_jar%' -WorkingDirectory '%ROOT%%_svc_name%' -RedirectStandardOutput '%LOGS_DIR%\%_svc_name%.log' -RedirectStandardError '%LOGS_DIR%\%_svc_name%.err.log' -WindowStyle Hidden -PassThru; $p.Id"') do set "_svc_pid=%%i"

if not defined "_svc_pid" (
    echo [ERROR] !_svc_name! process creation failed
    endlocal
    exit /b 1
)

rem 等待端口就绪
echo [WAIT] !_svc_name! PID=!_svc_pid! waiting for port !_svc_port! ...
set /a "_w=0"
:WAIT_SVC
set /a "_w+=1"
if !_w! GTR 30 (
    echo [WARN] !_svc_name! port !_svc_port! timeout
    echo [LOG]  %LOGS_DIR%\!_svc_name!.log
    echo [ERR]  %LOGS_DIR%\!_svc_name!.err.log
    pwsh.exe -NoProfile -Command "$s = if (Test-Path '%STATE_FILE%') { Get-Content '%STATE_FILE%' -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable } else { @{} }; $s['!_svc_name!'] = @{name='!_svc_name!';type='backend';pid=!_svc_pid!;port=!_svc_port!;jar='!_svc_jar!';status='Timeout';startedAt=(Get-Date -Format 'o')}; $s | ConvertTo-Json -Depth 5 | Set-Content '%STATE_FILE%' -Encoding UTF8" >nul 2>&1
    endlocal
    exit /b 0
)
rem 检查进程是否提前退出
tasklist /fi "PID eq !_svc_pid!" 2>nul | findstr /i "java" >nul 2>&1
if errorlevel 1 (
    echo [ERROR] !_svc_name! process exited (PID=!_svc_pid!)
    echo [ERR]  Last 10 lines:
    pwsh.exe -NoProfile -Command "Get-Content '%LOGS_DIR%\!_svc_name!.err.log' -Tail 10 -ErrorAction SilentlyContinue"
    endlocal
    exit /b 1
)
pwsh.exe -NoProfile -Command "exit ([System.Net.Sockets.TcpClient]::new().BeginConnect('127.0.0.1',!_svc_port!,$null,$null).AsyncWaitHandle.WaitOne(2000))" >nul 2>&1
if errorlevel 1 (
    timeout /t 3 /nobreak >nul
    goto :WAIT_SVC
)

echo [OK]   !_svc_name! PID=!_svc_pid! port=!_svc_port! ready

rem 记录到状态文件
pwsh.exe -NoProfile -Command "$s = if (Test-Path '%STATE_FILE%') { Get-Content '%STATE_FILE%' -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable } else { @{} }; $s['!_svc_name!'] = @{name='!_svc_name!';type='backend';pid=!_svc_pid!;port=!_svc_port!;jar='!_svc_jar!';workingDirectory='%ROOT%!_svc_name!';stdoutLog='%LOGS_DIR%\!_svc_name!.log';stderrLog='%LOGS_DIR%\!_svc_name!.err.log';status='Ready';startedAt=(Get-Date -Format 'o')}; $s | ConvertTo-Json -Depth 5 | Set-Content '%STATE_FILE%' -Encoding UTF8" >nul 2>&1

endlocal
exit /b 0
