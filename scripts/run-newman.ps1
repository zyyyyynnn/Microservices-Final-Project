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

$newman = Get-Command newman -ErrorAction SilentlyContinue
$args = @(
    "run", $collectionPath.Path,
    "-e", $environmentPath.Path
)

if ($newman) {
    $runner = $newman.Source
} else {
    if (-not (Get-Command npx -ErrorAction SilentlyContinue)) {
        throw "未找到 newman 或 npx。请先安装 Node.js/npm，并执行 npm install -g newman newman-reporter-htmlextra。"
    }
    $runner = "npx"
    $args = @(
        "--yes",
        "--package", "newman",
        "--package", "newman-reporter-htmlextra",
        "newman"
    ) + $args
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
