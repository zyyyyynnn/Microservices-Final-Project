param(
    [string]$BaseURL = "http://localhost:9000",
    [string]$SearchURL = "http://localhost:9008",
    [string]$ElasticsearchURL = "http://localhost:9200",
    [string[]]$SpuIds = @("1001", "1002", "1003", "1004", "1005"),
    [string]$Keyword = "iPhone",
    [int]$TimeoutSec = 5,
    [switch]$SkipSync,
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

function Convert-JsonContent {
    param([string]$Content)

    if ([string]::IsNullOrWhiteSpace($Content)) {
        return $null
    }

    return $Content | ConvertFrom-Json -ErrorAction Stop
}

function New-CheckResult {
    param(
        [string]$Step,
        [string]$Target,
        [string]$Expected,
        [string]$Actual,
        [bool]$Ok,
        [string]$Detail
    )

    [pscustomobject]@{
        Step = $Step
        Expected = $Expected
        Actual = $Actual
        Result = if ($Ok) { "OK" } else { "FAIL" }
        Detail = $Detail
        Target = $Target
    }
}

function Invoke-JsonCheck {
    param(
        [string]$Step,
        [string]$Uri,
        [string]$Method = "Get",
        [int[]]$ExpectedStatus = @(200),
        [Nullable[int]]$ExpectedBusinessCode = $null
    )

    try {
        $response = Invoke-WebRequest `
            -Uri $Uri `
            -Method $Method `
            -TimeoutSec $TimeoutSec `
            -SkipHttpErrorCheck

        $statusCode = [int]$response.StatusCode
        $json = Convert-JsonContent $response.Content
        $ok = $ExpectedStatus -contains $statusCode
        $actual = "HTTP $statusCode"
        $detail = "HTTP status checked"

        if ($ok -and $ExpectedBusinessCode -ne $null) {
            if ($null -eq $json -or $null -eq $json.code) {
                $ok = $false
                $detail = "missing business code"
            } elseif ([int]$json.code -ne $ExpectedBusinessCode.Value) {
                $ok = $false
                $actual = "$actual / code $($json.code)"
                $detail = if ($json.message) { $json.message } else { "business code mismatch" }
            } else {
                $actual = "$actual / code $($json.code)"
                $detail = if ($json.message) { $json.message } else { "business code checked" }
            }
        }

        return New-CheckResult $Step $Uri (($ExpectedStatus -join "/") + $(if ($ExpectedBusinessCode -ne $null) { " + code $($ExpectedBusinessCode.Value)" } else { "" })) $actual $ok $detail
    } catch {
        return New-CheckResult $Step $Uri ($ExpectedStatus -join "/") "-" $false $_.Exception.Message
    }
}

function Invoke-ElasticsearchHealth {
    $uri = Join-Url $ElasticsearchURL "/_cluster/health"
    try {
        $response = Invoke-WebRequest `
            -Uri $uri `
            -TimeoutSec $TimeoutSec `
            -SkipHttpErrorCheck

        $statusCode = [int]$response.StatusCode
        $json = Convert-JsonContent $response.Content
        $clusterStatus = if ($json -and $json.status) { [string]$json.status } else { "unknown" }
        $ok = $statusCode -eq 200 -and @("green", "yellow") -contains $clusterStatus
        $detail = if ($ok) { "cluster $clusterStatus" } else { "cluster $clusterStatus" }

        return New-CheckResult "Elasticsearch health" $uri "HTTP 200 + green/yellow" "HTTP $statusCode / $clusterStatus" $ok $detail
    } catch {
        return New-CheckResult "Elasticsearch health" $uri "HTTP 200 + green/yellow" "-" $false $_.Exception.Message
    }
}

function Invoke-SearchVerify {
    $path = "/api/v1/search/products?keyword=$([uri]::EscapeDataString($Keyword))&pageNum=1&pageSize=10"
    $uri = Join-Url $BaseURL $path
    try {
        $response = Invoke-WebRequest `
            -Uri $uri `
            -TimeoutSec $TimeoutSec `
            -SkipHttpErrorCheck

        $statusCode = [int]$response.StatusCode
        $json = Convert-JsonContent $response.Content
        $ok = $statusCode -eq 200
        $actual = "HTTP $statusCode"
        $detail = "HTTP status checked"

        if ($ok) {
            if ($null -eq $json -or [int]$json.code -ne 200) {
                $ok = $false
                $actual = "$actual / code $(if ($json) { $json.code } else { '-' })"
                $detail = if ($json -and $json.message) { $json.message } else { "business code mismatch" }
            } else {
                $total = if ($json.data -and $null -ne $json.data.total) { [long]$json.data.total } else { 0 }
                $listCount = if ($json.data -and $json.data.list) { @($json.data.list).Count } else { 0 }
                $ok = $total -gt 0 -or $listCount -gt 0
                $actual = "$actual / code $($json.code)"
                $detail = "total=$total list=$listCount"
                if (-not $ok) {
                    $detail = "business code 200 but no search result for keyword '$Keyword'"
                }
            }
        }

        return New-CheckResult "Search products business" $uri "HTTP 200 + code 200 + result" $actual $ok $detail
    } catch {
        return New-CheckResult "Search products business" $uri "HTTP 200 + code 200 + result" "-" $false $_.Exception.Message
    }
}

$checks = @()
$esHealth = Invoke-ElasticsearchHealth
$checks += $esHealth
$searchHealth = Invoke-JsonCheck "Search service health" (Join-Url $SearchURL "/actuator/health")
$checks += $searchHealth

if (-not $SkipSync) {
    if ($esHealth.Result -eq "OK" -and $searchHealth.Result -eq "OK") {
        foreach ($spuId in $SpuIds) {
            $syncUri = Join-Url $SearchURL "/internal/search/products/$spuId/sync?status=1"
            $checks += Invoke-JsonCheck "Sync product $spuId" $syncUri "Post" @(200) 200
        }
    } else {
        $checks += New-CheckResult "Sync products" $SearchURL "ES + search service OK" "-" $false "skipped because Elasticsearch or mall-search is unavailable"
    }
}

$gatewayHealth = Invoke-JsonCheck "Gateway health" (Join-Url $BaseURL "/actuator/health")
$checks += $gatewayHealth
if ($gatewayHealth.Result -eq "OK") {
    $checks += Invoke-SearchVerify
} else {
    $searchPath = "/api/v1/search/products?keyword=$([uri]::EscapeDataString($Keyword))&pageNum=1&pageSize=10"
    $checks += New-CheckResult "Search products business" (Join-Url $BaseURL $searchPath) "Gateway OK" "-" $false "skipped because Gateway is unavailable"
}

$checks | Format-Table Step, Expected, Actual, Result, Detail, Target -AutoSize -Wrap

$failed = @($checks | Where-Object { $_.Result -ne "OK" })
if ($failed.Count -gt 0) {
    Write-Warning "$($failed.Count) search initialization check(s) failed. Do not record Elasticsearch search as verified."
    if (-not $AllowFailures) {
        exit 1
    }
}

exit 0
