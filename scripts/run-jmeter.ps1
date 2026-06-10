param(
    [ValidateSet("search", "order", "seckill")]
    [string]$Scenario = "search",
    [string]$BaseURL = "http://localhost:9000",
    [int]$Users = 50,
    [int]$RampUp = 30,
    [int]$Duration = 300,
    [int]$Loops = 1,
    [string]$Username = "zhangsan",
    [string]$Password = "123456",
    [string]$UsernamePrefix = "jmeter_seckill_",
    [int]$ActivityId = 9001,
    [int]$SkuId = 99003,
    [int]$ResultPollAttempts = 20,
    [int]$ResultPollDelayMs = 500,
    [string]$JMeterVersion = "5.6.3",
    [switch]$InstallOnly
)

$ErrorActionPreference = "Stop"

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $Root

$toolRoot = Join-Path $Root ".tools"
$jmeterHome = Join-Path $toolRoot "apache-jmeter-$JMeterVersion"
$jmeterBat = Join-Path $jmeterHome "bin\jmeter.bat"

function Get-JMeterCommand {
    $installed = Get-Command jmeter -ErrorAction SilentlyContinue
    if ($installed) {
        return $installed.Source
    }
    if (Test-Path -LiteralPath $jmeterBat) {
        return $jmeterBat
    }
    return $null
}

function Install-JMeter {
    if (Get-JMeterCommand) {
        return
    }

    New-Item -ItemType Directory -Force -Path $toolRoot | Out-Null
    $archive = Join-Path $toolRoot "apache-jmeter-$JMeterVersion.zip"
    $url = "https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-$JMeterVersion.zip"

    if (-not (Test-Path -LiteralPath $archive)) {
        Write-Host "[INFO] 下载 JMeter $JMeterVersion 到 $archive"
        $curl = Get-Command curl.exe -ErrorAction SilentlyContinue
        if ($curl) {
            & $curl.Source -L --fail --retry 3 --connect-timeout 30 -o $archive $url
            if ($LASTEXITCODE -ne 0) {
                throw "JMeter 下载失败：curl exit code $LASTEXITCODE"
            }
        } else {
            Invoke-WebRequest -Uri $url -OutFile $archive
        }
    }

    Write-Host "[INFO] 解压 JMeter 到 $toolRoot"
    Expand-Archive -LiteralPath $archive -DestinationPath $toolRoot -Force

    if (-not (Test-Path -LiteralPath $jmeterBat)) {
        throw "JMeter 安装失败：未找到 $jmeterBat"
    }
}

Install-JMeter
$jmeterCommand = Get-JMeterCommand

if (-not $jmeterCommand) {
    throw "未找到 JMeter 命令，且本地 JMeter 安装失败。"
}

if ($InstallOnly) {
    & $jmeterCommand --version
    exit $LASTEXITCODE
}

$scriptMap = @{
    search = ".\docs\test\jmeter\search-load.jmx"
    order = ".\docs\test\jmeter\order-load.jmx"
    seckill = ".\docs\test\jmeter\seckill-stress.jmx"
}

$scriptPath = Resolve-Path -LiteralPath $scriptMap[$Scenario]
$resultDir = Join-Path $Root "docs\test\jmeter\results"
$reportRoot = Join-Path $Root "docs\test\jmeter\report"
$stamp = Get-Date -Format "yyyyMMdd-HHmmss"
$jtlPath = Join-Path $resultDir "$Scenario-$Users-$stamp.jtl"
$reportDir = Join-Path $reportRoot "$Scenario-$Users-$stamp"

New-Item -ItemType Directory -Force -Path $resultDir | Out-Null
New-Item -ItemType Directory -Force -Path $reportRoot | Out-Null

if (Test-Path -LiteralPath $reportDir) {
    throw "JMeter HTML 报告目录已存在：$reportDir"
}

if ($Scenario -ne "seckill" -and $PSBoundParameters.ContainsKey("Loops")) {
    Write-Warning "-Loops only applies to seckill scenario; search/order use -Duration."
}

$args = @(
    "-n",
    "-t", $scriptPath.Path,
    "-l", $jtlPath,
    "-e",
    "-o", $reportDir,
    "-JBaseURL=$BaseURL",
    "-Jusers=$Users",
    "-Jrampup=$RampUp",
    "-Jduration=$Duration",
    "-Jusername=$Username",
    "-Jpassword=$Password",
    "-JskuId=$SkuId",
    "-JresultPollAttempts=$ResultPollAttempts",
    "-JresultPollDelayMs=$ResultPollDelayMs"
)

if ($Scenario -eq "seckill") {
    $args += "-Jloops=$Loops"
    $args += "-JactivityId=$ActivityId"
    $args += "-JusernamePrefix=$UsernamePrefix"
}

& $jmeterCommand @args
$jmeterExitCode = $LASTEXITCODE
if ($jmeterExitCode -ne 0) {
    exit $jmeterExitCode
}

if (Test-Path -LiteralPath $jtlPath) {
    $failedSamples = Import-Csv -LiteralPath $jtlPath | Where-Object { $_.success -eq "false" }
    if (($failedSamples | Measure-Object).Count -gt 0) {
        Write-Error "JMeter completed with failed samples. See $jtlPath"
        exit 1
    }
}

exit 0
