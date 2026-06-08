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
if not exist "%STATE_FILE%" echo {} > "%STATE_FILE%"

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
if "%JAVA_MAJOR%"=="" set "JAVA_MAJOR=21"
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

rem npm (仅用于安装依赖)
where npm.cmd >nul 2>&1
if errorlevel 1 (
    where npm >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] npm not found
        goto :FAIL
    )
)
echo [OK]   npm

rem Docker (仅在需要启动基础设施时检查)
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

rem 记录启动前已运行的容器
set "PRE_EXISTING="
for /f "tokens=*" %%c in ('docker ps --format "{{.Names}}" 2^>nul') do (
    if defined PRE_EXISTING (set "PRE_EXISTING=!PRE_EXISTING!,%%c") else (set "PRE_EXISTING=%%c")
)

echo [INFO] docker compose up -d ...
docker compose -f "%COMPOSE_FILE%" up -d >"%LOGS_DIR%\infra-compose.log" 2>&1
if errorlevel 1 (
    echo [ERROR] Docker Compose failed. Log: %LOGS_DIR%\infra-compose.log
    type "%LOGS_DIR%\infra-compose.log"
    goto :FAIL
)
echo [OK]   Docker Compose executed

rem 记录本次由脚本拉起的容器（排除启动前已运行的）
pwsh.exe -NoProfile -Command ^
    "$pre = '%PRE_EXISTING%' -split ','; $now = docker ps --format '{{.Names}}' 2>$null; $started = $now | Where-Object { $_ -notin $pre }; $started | ConvertTo-Json | Set-Content '%INFRA_STATE%' -Encoding UTF8" >nul 2>&1

rem 等待关键中间件（TCP 探测，exit 0=成功）
echo [WAIT] MySQL (3306) ...
call :WAIT_TCP 127.0.0.1 3306 30 "MySQL"
if errorlevel 1 echo [WARN] MySQL timeout, continuing

echo [WAIT] Nacos (8848) ...
call :WAIT_HTTP "http://127.0.0.1:8848/nacos/" 40 "Nacos"
if errorlevel 1 echo [WARN] Nacos timeout, continuing

echo [WAIT] Redis (6379) ...
call :WAIT_TCP 127.0.0.1 6379 10 "Redis"
if errorlevel 1 echo [WARN] Redis not ready

echo [WAIT] RocketMQ NameServer (9876) ...
call :WAIT_TCP 127.0.0.1 9876 20 "RocketMQ NameServer"
if errorlevel 1 echo [WARN] RocketMQ NameServer timeout

echo [WAIT] RocketMQ Broker (10911) ...
call :WAIT_TCP 127.0.0.1 10911 20 "RocketMQ Broker"
if errorlevel 1 echo [WARN] RocketMQ Broker timeout

echo [WAIT] Seata (8091) ...
call :WAIT_TCP 127.0.0.1 8091 20 "Seata"
if errorlevel 1 echo [WARN] Seata timeout

echo [WAIT] Sentinel (8080) ...
call :WAIT_TCP 127.0.0.1 8080 15 "Sentinel"
if errorlevel 1 echo [WARN] Sentinel timeout

echo [WAIT] Elasticsearch (9200) ...
call :WAIT_HTTP "http://127.0.0.1:9200/_cluster/health" 20 "Elasticsearch"
if errorlevel 1 echo [WARN] Elasticsearch timeout

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

rem 检查是否已在运行（读取 processes.json）
set "_fe_existing_pid="
for /f "tokens=*" %%p in ('pwsh.exe -NoProfile -Command ^
    "$s = if (Test-Path '%STATE_FILE%') { Get-Content '%STATE_FILE%' -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable } else { @{} }; if ($s.ContainsKey('frontend') -and $s['frontend'].pid) { $s['frontend'].pid } else { '' }"') do set "_fe_existing_pid=%%p"

if defined "_fe_existing_pid" (
    if not "%_fe_existing_pid%"=="" (
        rem 检查 PID 存在且命令行包含 mall-frontend
        for /f "tokens=*" %%m in ('pwsh.exe -NoProfile -Command ^
            "$p = Get-Process -Id %_fe_existing_pid% -ErrorAction SilentlyContinue; if ($p -and $p.ProcessName -eq 'node') { $cmd = (Get-CimInstance Win32_Process -Filter 'ProcessId=%_fe_existing_pid%').CommandLine; if ($cmd -match 'mall-frontend|vite') { 'MATCH' } else { 'MISMATCH' } } else { 'GONE' }"') do set "_fe_check=%%m"
        if "%_fe_check%"=="MATCH" (
            rem 进一步验证端口
            pwsh.exe -NoProfile -Command ^
                "$c=[Net.Sockets.TcpClient]::new(); try { $iar=$c.BeginConnect('127.0.0.1',5173,$null,$null); if(-not $iar.AsyncWaitHandle.WaitOne(2000,$false)){exit 1}; $c.EndConnect($iar); exit 0 } catch { exit 1 } finally { $c.Close() }" >nul 2>&1
            if not errorlevel 1 (
                echo [SKIP] Frontend already running, PID=%_fe_existing_pid%, port=5173
                goto :SKIP_FRONTEND
            )
        )
        rem PID 不匹配或端口未监听，清理状态
        pwsh.exe -NoProfile -Command ^
            "$s = Get-Content '%STATE_FILE%' -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable; $s.Remove('frontend'); $s | ConvertTo-Json -Depth 5 | Set-Content '%STATE_FILE%' -Encoding UTF8" >nul 2>&1
    )
)

rem 检查端口冲突（排除我们自己记录的 PID）
for /f "tokens=*" %%p in ('pwsh.exe -NoProfile -Command ^
    "$c = Get-NetTCPConnection -LocalPort 5173 -State Listen -ErrorAction SilentlyContinue; if ($c) { $c[0].OwningProcess } else { '' }"') do set "_fe_port_pid=%%p"

if defined "_fe_port_pid" (
    if not "%_fe_port_pid%"=="" (
        if not "%_fe_port_pid%"=="%_fe_existing_pid%" (
            for /f "tokens=*" %%c in ('pwsh.exe -NoProfile -Command "(Get-CimInstance Win32_Process -Filter 'ProcessId=%_fe_port_pid%' -ErrorAction SilentlyContinue).CommandLine"') do set "_fe_port_cmd=%%c"
            echo [ERROR] Port 5173 already in use by another process
            echo [PID]  %_fe_port_pid%
            echo [CMD]  %_fe_port_cmd%
            goto :FAIL
        )
    )
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

rem 直接启动 node vite（不经过 npm.cmd，确保记录真实 node PID）
set "_vite_bin=%FRONTEND_DIR%\node_modules\vite\bin\vite.js"
if not exist "%_vite_bin%" (
    echo [ERROR] Vite not found: %_vite_bin%
    goto :FAIL
)

echo [INFO] Starting frontend (port 5173) ...
for /f "tokens=*" %%i in ('pwsh.exe -NoProfile -Command ^
    "$p = Start-Process -FilePath 'node.exe' -ArgumentList '%_vite_bin%','--host','127.0.0.1','--port','5173' -WorkingDirectory '%FRONTEND_DIR%' -RedirectStandardOutput '%LOGS_DIR%\mall-frontend.log' -RedirectStandardError '%LOGS_DIR%\mall-frontend.err.log' -WindowStyle Hidden -PassThru; $p.Id"') do set "FE_PID=%%i"

if not defined FE_PID (
    echo [ERROR] Frontend process creation failed
    goto :FAIL
)

rem 等待前端端口
echo [WAIT] Frontend PID=%FE_PID% port=5173 ...
call :WAIT_TCP 127.0.0.1 5173 20 "Frontend"
if errorlevel 1 (
    rem 检查进程是否还在
    tasklist /fi "PID eq %FE_PID%" 2>nul | findstr /i "node" >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] Frontend process exited. Log: %LOGS_DIR%\mall-frontend.err.log
        goto :FAIL
    )
    echo [WARN] Frontend port 5173 timeout, process still running
)

echo [OK]   Frontend PID=%FE_PID% port=5173

rem 记录前端到状态文件
pwsh.exe -NoProfile -Command ^
    "$s = if (Test-Path '%STATE_FILE%') { Get-Content '%STATE_FILE%' -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable } else { @{} }; $s['frontend'] = @{name='frontend';type='frontend';pid=%FE_PID%;port=5173;command='%_vite_bin%';workingDirectory='%FRONTEND_DIR%';status='Ready';startedAt=(Get-Date -Format 'o')}; $s | ConvertTo-Json -Depth 5 | Set-Content '%STATE_FILE%' -Encoding UTF8" >nul 2>&1

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

pwsh.exe -NoProfile -Command ^
    "$fmt = '{0,-22} {1,-10} {2,-10} {3,-12}'; Write-Host ($fmt -f 'Service','PID','Port','Status') -ForegroundColor White; Write-Host ($fmt -f '-------','---','----','------') -ForegroundColor Gray; $s = if (Test-Path '%STATE_FILE%') { Get-Content '%STATE_FILE%' -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable } else { @{} }; foreach ($k in ($s.Keys | Sort-Object)) { $e = $s[$k]; $pidStr = if ($e.pid) { [string]$e.pid } else { '-' }; $portStr = if ($e.port) { [string]$e.port } else { '-' }; $color = if ($e.status -eq 'Ready' -or $e.status -eq 'Healthy') { 'Green' } elseif ($e.status -eq 'Timeout') { 'Yellow' } else { 'Red' }; Write-Host ($fmt -f $e.name, $pidStr, $portStr, $e.status) -ForegroundColor $color }"

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
rem  Subroutine: WAIT_TCP
rem  Args: %1=host %2=port %3=timeout-seconds %4=label
rem  Returns: 0=success, 1=timeout
rem ============================================================
:WAIT_TCP
set "_wt_host=%~1"
set "_wt_port=%~2"
set "_wt_timeout=%~3"
set "_wt_label=%~4"
set /a "_wt_i=0"
:WAIT_TCP_LOOP
set /a "_wt_i+=1"
if %_wt_i% GTR %_wt_timeout% exit /b 1
pwsh.exe -NoProfile -Command ^
    "$c=[Net.Sockets.TcpClient]::new(); try { $iar=$c.BeginConnect('%_wt_host%',%_wt_port%,$null,$null); if(-not $iar.AsyncWaitHandle.WaitOne(2000,$false)){exit 1}; $c.EndConnect($iar); exit 0 } catch { exit 1 } finally { $c.Close() }" >nul 2>&1
if not errorlevel 1 exit /b 0
timeout /t 1 /nobreak >nul
goto :WAIT_TCP_LOOP

rem ============================================================
rem  Subroutine: WAIT_HTTP
rem  Args: %1=url %2=timeout-seconds %3=label
rem  Returns: 0=success, 1=timeout
rem ============================================================
:WAIT_HTTP
set "_wh_url=%~1"
set "_wh_timeout=%~2"
set "_wh_label=%~3"
set /a "_wh_i=0"
:WAIT_HTTP_LOOP
set /a "_wh_i+=1"
if %_wh_i% GTR %_wh_timeout% exit /b 1
pwsh.exe -NoProfile -Command ^
    "try { $r=Invoke-WebRequest -Uri '%_wh_url%' -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop; if($r.StatusCode -ge 200 -and $r.StatusCode -lt 500){exit 0}else{exit 1} } catch { exit 1 }" >nul 2>&1
if not errorlevel 1 exit /b 0
timeout /t 1 /nobreak >nul
goto :WAIT_HTTP_LOOP

rem ============================================================
rem  Subroutine: START_SERVICE
rem  Args: %1=service-name %2=port
rem  Returns: 0=success, 1=failure
rem ============================================================
:START_SERVICE
setlocal EnableDelayedExpansion
set "_svc_name=%~1"
set "_svc_port=%~2"
set "_svc_jar="
set "_existing_pid="
set "_port_pid="

rem -- 查找 JAR（严格 0/1/多候选） --
set "_jar_count=0"
for /f "tokens=*" %%j in ('pwsh.exe -NoProfile -Command ^
    "Get-ChildItem -Path '%ROOT%%_svc_name%\target\%_svc_name%-*.jar' -ErrorAction SilentlyContinue | Where-Object { $_.Name -notmatch '^original-|sources\.jar$|javadoc\.jar$' } | ForEach-Object { $_.FullName }"') do (
    set "_svc_jar=%%j"
    set /a "_jar_count+=1"
)
if "!_jar_count!"=="0" (
    echo [ERROR] !_svc_name! JAR not found in target/
    echo [INFO] Run Maven build first, or use --no-build after building
    endlocal & exit /b 1
)
if not "!_jar_count!"=="1" (
    echo [ERROR] !_svc_name! found !_jar_count! candidate JARs, expected 1
    pwsh.exe -NoProfile -Command "Get-ChildItem -Path '%ROOT%!_svc_name!\target\!_svc_name!-*.jar' | ForEach-Object { $_.FullName }"
    endlocal & exit /b 1
)

rem -- 检查状态文件中的已有记录 --
for /f "tokens=*" %%p in ('pwsh.exe -NoProfile -Command ^
    "$s = if (Test-Path '%STATE_FILE%') { Get-Content '%STATE_FILE%' -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable } else { @{} }; if ($s.ContainsKey('!_svc_name!') -and $s['!_svc_name!'].pid) { $s['!_svc_name!'].pid } else { '' }"') do set "_existing_pid=%%p"

if defined "_existing_pid" (
    if not "!_existing_pid!"=="" (
        rem 检查 PID 存在且命令行匹配
        for /f "tokens=*" %%m in ('pwsh.exe -NoProfile -Command ^
            "$p = Get-Process -Id !_existing_pid! -ErrorAction SilentlyContinue; if ($p -and $p.ProcessName -eq 'java') { $cmd = (Get-CimInstance Win32_Process -Filter 'ProcessId=!_existing_pid!').CommandLine; if ($cmd -match '!_svc_name!') { 'MATCH' } else { 'MISMATCH' } } else { 'GONE' }"') do set "_pid_check=%%m"

        if "!_pid_check!"=="MATCH" (
            rem PID 和命令行匹配，进一步验证端口
            pwsh.exe -NoProfile -Command ^
                "$c=[Net.Sockets.TcpClient]::new(); try { $iar=$c.BeginConnect('127.0.0.1',!_svc_port!,$null,$null); if(-not $iar.AsyncWaitHandle.WaitOne(2000,$false)){exit 1}; $c.EndConnect($iar); exit 0 } catch { exit 1 } finally { $c.Close() }" >nul 2>&1
            if not errorlevel 1 (
                echo [SKIP] !_svc_name! already running, PID=!_existing_pid!, port=!_svc_port!
                endlocal & exit /b 0
            )
            rem PID 存在但端口未监听，清理状态并重新启动
            echo [WARN] !_svc_name! PID=!_existing_pid! exists but port !_svc_port! not listening, restarting
            pwsh.exe -NoProfile -Command ^
                "$s = Get-Content '%STATE_FILE%' -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable; $s.Remove('!_svc_name!'); $s | ConvertTo-Json -Depth 5 | Set-Content '%STATE_FILE%' -Encoding UTF8" >nul 2>&1
            taskkill /PID !_existing_pid! /T /F >nul 2>&1
        )
        if "!_pid_check!"=="MISMATCH" (
            echo [WARN] !_svc_name! state PID=!_existing_pid! command mismatch, clearing
            pwsh.exe -NoProfile -Command ^
                "$s = Get-Content '%STATE_FILE%' -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable; $s.Remove('!_svc_name!'); $s | ConvertTo-Json -Depth 5 | Set-Content '%STATE_FILE%' -Encoding UTF8" >nul 2>&1
        )
    )
)

rem -- 检查端口是否已被未知进程占用 --
for /f "tokens=*" %%p in ('pwsh.exe -NoProfile -Command ^
    "$c = Get-NetTCPConnection -LocalPort !_svc_port! -State Listen -ErrorAction SilentlyContinue; if ($c) { $c[0].OwningProcess } else { '' }"') do set "_port_pid=%%p"

if defined "_port_pid" (
    if not "!_port_pid!"=="" (
        for /f "tokens=*" %%c in ('pwsh.exe -NoProfile -Command "(Get-CimInstance Win32_Process -Filter 'ProcessId=!_port_pid!' -ErrorAction SilentlyContinue).CommandLine"') do set "_port_cmd=%%c"
        echo [ERROR] Port !_svc_port! already in use by another process
        echo [PID]  !_port_pid!
        echo [CMD]  !_port_cmd!
        endlocal & exit /b 1
    )
)

rem -- 启动 Java 进程 --
echo [START] !_svc_name! (port !_svc_port!) ...

for /f "tokens=*" %%i in ('pwsh.exe -NoProfile -Command ^
    "$p = Start-Process -FilePath 'java.exe' -ArgumentList '-jar','!_svc_jar!' -WorkingDirectory '%ROOT%!_svc_name!' -RedirectStandardOutput '%LOGS_DIR%\!_svc_name!.log' -RedirectStandardError '%LOGS_DIR%\!_svc_name!.err.log' -WindowStyle Hidden -PassThru; $p.Id"') do set "_svc_pid=%%i"

if not defined "_svc_pid" (
    echo [ERROR] !_svc_name! process creation failed
    endlocal & exit /b 1
)

rem -- 等待端口就绪 --
echo [WAIT] !_svc_name! PID=!_svc_pid! waiting for port !_svc_port! ...
set /a "_w=0"
:WAIT_SVC
set /a "_w+=1"
if !_w! GTR 60 (
    echo [ERROR] !_svc_name! port !_svc_port! timeout (60s)
    echo [LOG]  %LOGS_DIR%\!_svc_name!.log
    echo [ERR]  %LOGS_DIR%\!_svc_name!.err.log
    pwsh.exe -NoProfile -Command ^
        "$s = if (Test-Path '%STATE_FILE%') { Get-Content '%STATE_FILE%' -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable } else { @{} }; $s['!_svc_name!'] = @{name='!_svc_name!';type='backend';pid=!_svc_pid!;port=!_svc_port!;jar='!_svc_jar!';status='Timeout';startedAt=(Get-Date -Format 'o')}; $s | ConvertTo-Json -Depth 5 | Set-Content '%STATE_FILE%' -Encoding UTF8" >nul 2>&1
    endlocal & exit /b 1
)

rem 检查进程是否提前退出
tasklist /fi "PID eq !_svc_pid!" 2>nul | findstr /i "java" >nul 2>&1
if errorlevel 1 (
    echo [ERROR] !_svc_name! process exited prematurely (PID=!_svc_pid!)
    echo [ERR]  Last 10 lines:
    pwsh.exe -NoProfile -Command "Get-Content '%LOGS_DIR%\!_svc_name!.err.log' -Tail 10 -ErrorAction SilentlyContinue"
    endlocal & exit /b 1
)

pwsh.exe -NoProfile -Command ^
    "$c=[Net.Sockets.TcpClient]::new(); try { $iar=$c.BeginConnect('127.0.0.1',!_svc_port!,$null,$null); if(-not $iar.AsyncWaitHandle.WaitOne(2000,$false)){exit 1}; $c.EndConnect($iar); exit 0 } catch { exit 1 } finally { $c.Close() }" >nul 2>&1
if errorlevel 1 (
    timeout /t 2 /nobreak >nul
    goto :WAIT_SVC
)

rem -- 尝试健康检查 --
set "_svc_status=Ready"
pwsh.exe -NoProfile -Command ^
    "try { $r=Invoke-WebRequest -Uri 'http://127.0.0.1:!_svc_port!/actuator/health' -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop; if($r.StatusCode -eq 200){exit 0}else{exit 1} } catch { exit 1 }" >nul 2>&1
if not errorlevel 1 set "_svc_status=Healthy"

echo [OK]   !_svc_name! PID=!_svc_pid! port=!_svc_port! !_svc_status!

rem -- 记录到状态文件 --
pwsh.exe -NoProfile -Command ^
    "$s = if (Test-Path '%STATE_FILE%') { Get-Content '%STATE_FILE%' -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable } else { @{} }; $s['!_svc_name!'] = @{name='!_svc_name!';type='backend';pid=!_svc_pid!;port=!_svc_port!;jar='!_svc_jar!';workingDirectory='%ROOT%!_svc_name!';stdoutLog='%LOGS_DIR%\!_svc_name!.log';stderrLog='%LOGS_DIR%\!_svc_name!.err.log';status='!_svc_status!';startedAt=(Get-Date -Format 'o')}; $s | ConvertTo-Json -Depth 5 | Set-Content '%STATE_FILE%' -Encoding UTF8" >nul 2>&1

endlocal & exit /b 0
