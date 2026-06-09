param(
    [string]$BaseURL = "http://localhost:9000",
    [string]$SearchURL = "http://localhost:9008",
    [string]$ElasticsearchURL = "http://localhost:9200",
    [string[]]$SpuIds = @("1001", "1002", "1003", "1004", "1005"),
    [string]$Keyword = "iPhone",
    [string[]]$ExpectedSpuIds = @("1001", "1002"),
    [int]$VerifyAttempts = 10,
    [int]$VerifyDelayMs = 500,
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
    $attempts = [Math]::Max(1, $VerifyAttempts)
    $lastResult = $null

    for ($attempt = 1; $attempt -le $attempts; $attempt++) {
        try {
            $response = Invoke-WebRequest `
                -Uri $uri `
                -TimeoutSec $TimeoutSec `
                -SkipHttpErrorCheck

            $statusCode = [int]$response.StatusCode
            $json = Convert-JsonContent $response.Content
            $ok = $statusCode -eq 200
            $actual = "HTTP $statusCode"
            $detail = "attempt $attempt/$attempts"

            if ($ok) {
                if ($null -eq $json -or [int]$json.code -ne 200) {
                    $ok = $false
                    $actual = "$actual / code $(if ($json) { $json.code } else { '-' })"
                    $detail = "$detail; $(if ($json -and $json.message) { $json.message } else { "business code mismatch" })"
                } else {
                    $check = Test-SearchPayload $json
                    $ok = $check.Ok
                    $actual = "$actual / code $($json.code)"
                    $detail = "$detail; $($check.Detail)"
                }
            }

            $lastResult = New-CheckResult "Search products business" $uri "HTTP 200 + code 200 + expected result" $actual $ok $detail
        } catch {
            $lastResult = New-CheckResult "Search products business" $uri "HTTP 200 + code 200 + expected result" "-" $false "attempt $attempt/$attempts; $($_.Exception.Message)"
        }

        if ($lastResult.Result -eq "OK" -or $attempt -ge $attempts) {
            return $lastResult
        }

        Start-Sleep -Milliseconds $VerifyDelayMs
    }

    return $lastResult
}

function Test-SearchPayload {
    param([object]$Json)

    $items = if ($Json.data -and $Json.data.list) { @($Json.data.list) } else { @() }
    $total = if ($Json.data -and $null -ne $Json.data.total) { [long]$Json.data.total } else { 0 }
    $itemIds = @($items | ForEach-Object { if ($null -ne $_.spuId) { [string]$_.spuId } })

    if ($ExpectedSpuIds -and $ExpectedSpuIds.Count -gt 0) {
        $expected = @($ExpectedSpuIds | ForEach-Object { [string]$_ })
        $matched = @($itemIds | Where-Object { $expected -contains $_ })
        if ($matched.Count -gt 0) {
            return [pscustomobject]@{
                Ok = $true
                Detail = "total=$total list=$($items.Count) matchedSpuIds=$($matched -join ',')"
            }
        }

        return [pscustomobject]@{
            Ok = $false
            Detail = "expectedSpuIds=$($expected -join ',') actualSpuIds=$($itemIds -join ',')"
        }
    }

    if (-not [string]::IsNullOrWhiteSpace($Keyword)) {
        $matchedByName = @($items | Where-Object {
            $name = if ($_.name) { [string]$_.name } else { "" }
            $highlightName = if ($_.highlightName) { [string]$_.highlightName } else { "" }
            $name.IndexOf($Keyword, [StringComparison]::OrdinalIgnoreCase) -ge 0 `
                -or $highlightName.IndexOf($Keyword, [StringComparison]::OrdinalIgnoreCase) -ge 0
        })

        if ($matchedByName.Count -gt 0) {
            return [pscustomobject]@{
                Ok = $true
                Detail = "total=$total list=$($items.Count) matchedName=$($matchedByName.Count)"
            }
        }

        return [pscustomobject]@{
            Ok = $false
            Detail = "no result name matched keyword '$Keyword'"
        }
    }

    $ok = $total -gt 0 -or $items.Count -gt 0
    return [pscustomobject]@{
        Ok = $ok
        Detail = if ($ok) { "total=$total list=$($items.Count)" } else { "business code 200 but result is empty" }
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
