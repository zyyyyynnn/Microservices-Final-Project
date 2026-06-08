param(
    [string]$Collection = ".\docs\test\postman\mallcloud.postman_collection.json",
    [string]$Environment = ".\docs\test\postman\local.postman_environment.json",
    [string]$Report = ".\docs\test\postman\report.html",
    [switch]$SkipHtml
)

$ErrorActionPreference = "Stop"

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $Root

$collectionPath = Resolve-Path -LiteralPath $Collection
$environmentPath = Resolve-Path -LiteralPath $Environment

function Test-HtmlextraReporter {
    $localReporter = Join-Path $Root "node_modules\newman-reporter-htmlextra"
    if (Test-Path -LiteralPath $localReporter) {
        return $true
    }

    $npm = Get-Command npm -ErrorAction SilentlyContinue
    if (-not $npm) {
        return $false
    }

    $npmRoot = (& $npm.Source root -g 2>$null | Select-Object -First 1)
    if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($npmRoot)) {
        return $false
    }

    return Test-Path -LiteralPath (Join-Path $npmRoot "newman-reporter-htmlextra")
}

function Use-NpxNewman {
    if (-not (Get-Command npx -ErrorAction SilentlyContinue)) {
        throw "未找到可用的 newman/reporter 或 npx。请执行 npm install -g newman newman-reporter-htmlextra。"
    }

    $script:runner = "npx"
    $script:args = @(
        "--yes",
        "--package", "newman",
        "--package", "newman-reporter-htmlextra",
        "newman"
    ) + $script:args
}

$args = @(
    "run", $collectionPath.Path,
    "-e", $environmentPath.Path
)

$newman = Get-Command newman -ErrorAction SilentlyContinue
if ($newman -and ($SkipHtml -or (Test-HtmlextraReporter))) {
    $runner = $newman.Source
} else {
    Use-NpxNewman
}

if ($SkipHtml) {
    $args += @("-r", "cli")
} else {
    $reportPath = Join-Path $Root $Report
    $reportDir = Split-Path -Parent $reportPath
    New-Item -ItemType Directory -Force -Path $reportDir | Out-Null
    $args += @("-r", "cli,htmlextra", "--reporter-htmlextra-export", $reportPath)
}

& $runner @args
exit $LASTEXITCODE
