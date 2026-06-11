param(
    [string]$BaseURL = "http://localhost:9100",
    [string]$NacosURL = "http://localhost:8848",
    [string]$SentinelURL = "http://localhost:8080",
    [string]$ElasticsearchURL = "http://localhost:9200",
    [string]$Keyword = "iPhone",
    [string]$AccessToken = "",
    [int]$TimeoutSec = 5,
    [switch]$SkipGateway,
    [switch]$SkipMiddleware,
    [switch]$AllowFailures
)

$ErrorActionPreference = "Stop"

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $Root

function Join-Url {
    param(
        [string]$Base,
        [string]$Path
    )

    return ($Base.TrimEnd("/") + "/" + $Path.TrimStart("/"))
}

function Invoke-Check {
    param(
        [string]$Name,
        [string]$Uri,
        [int[]]$ExpectedStatus = @(200),
        [hashtable]$Headers = @{}
    )

    try {
        $response = Invoke-WebRequest `
            -Uri $Uri `
            -Headers $Headers `
            -TimeoutSec $TimeoutSec `
            -SkipHttpErrorCheck

        $statusCode = [int]$response.StatusCode
        $ok = $ExpectedStatus -contains $statusCode
        $detail = "HTTP $statusCode"
    } catch {
        $statusCode = 0
        $ok = $false
        $detail = $_.Exception.Message
    }

    [pscustomobject]@{
        Check = $Name
        Target = $Uri
        Expected = ($ExpectedStatus -join "/")
        Actual = if ($statusCode -gt 0) { $statusCode } else { "-" }
        Result = if ($ok) { "OK" } else { "FAIL" }
        Detail = $detail
    }
}

$headers = @{}
if (-not [string]::IsNullOrWhiteSpace($AccessToken)) {
    $headers.Authorization = "Bearer $AccessToken"
}

$checks = @()

if (-not $SkipMiddleware) {
    $checks += Invoke-Check "Nacos console" (Join-Url $NacosURL "/nacos/")
    $checks += Invoke-Check "Sentinel dashboard" (Join-Url $SentinelURL "/")
    $checks += Invoke-Check "Elasticsearch health" (Join-Url $ElasticsearchURL "/_cluster/health")
}

if (-not $SkipGateway) {
    $checks += Invoke-Check "Gateway health" (Join-Url $BaseURL "/actuator/health")
    $checks += Invoke-Check "Search hot words" (Join-Url $BaseURL "/api/v1/search/hot-words")
    $searchPath = "/api/v1/search/products?keyword=$([uri]::EscapeDataString($Keyword))&pageNum=1&pageSize=10"
    $checks += Invoke-Check "Search products" (Join-Url $BaseURL $searchPath)

    $seckillExpected = if ($headers.Authorization) { @(200) } else { @(200, 401) }
    $checks += Invoke-Check "Seckill activities" (Join-Url $BaseURL "/api/v1/seckill/activities") $seckillExpected $headers
}

$checks | Format-Table Check, Expected, Actual, Result, Detail, Target -AutoSize -Wrap

$failed = @($checks | Where-Object { $_.Result -ne "OK" })
if ($failed.Count -gt 0) {
    Write-Warning "$($failed.Count) special check(s) failed. Do not record these items as verified."
    if (-not $AllowFailures) {
        exit 1
    }
}

exit 0
