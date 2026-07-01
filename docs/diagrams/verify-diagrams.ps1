[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$themeCss = Join-Path $root 'theme.css'
$expected = @(
    '01-system-architecture',
    '02-trade-flow',
    '03-gateway-security',
    '04-seata-order-inventory',
    '05-rocketmq-pay-result',
    '06-seckill-rate-limit',
    '07-local-deployment'
)

if (-not (Test-Path -LiteralPath $themeCss -PathType Leaf)) {
    throw "缺少图表主题文件: $themeCss"
}

$themeContent = Get-Content -LiteralPath $themeCss -Raw -Encoding UTF8
if ($themeContent -notmatch '\.cluster-label\s+\.nodeLabel' -or
    $themeContent -notmatch 'font-size:\s*17px\s*!important' -or
    $themeContent -notmatch 'line-height:\s*1\.22\s*!important' -or
    $themeContent -notmatch '\.cluster-label\s+div') {
    throw '标题裁切防护缺失: cluster-label 必须固定为 17px / 1.22 行高，并覆盖 div/p/span'
}

$rows = foreach ($name in $expected) {
    $mmd = Join-Path $root "mmd/$name.mmd"
    $svg = Join-Path $root "svg/$name.svg"
    $png = Join-Path $root "png/$name.png"

    foreach ($path in @($mmd, $svg, $png)) {
        if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
            throw "缺少图表文件: $path"
        }
    }

    $image = [System.Drawing.Image]::FromFile($png)
    try {
        $width = $image.Width
        $height = $image.Height
    }
    finally {
        $image.Dispose()
    }

    $size = (Get-Item -LiteralPath $png).Length
    if ($width -lt 2400) {
        throw "$name PNG 宽度不足: ${width}px < 2400px"
    }
    if ($size -lt 30000) {
        throw "$name PNG 文件过小: $size bytes"
    }
    if (-not (Select-String -LiteralPath $svg -Pattern '<svg' -Quiet)) {
        throw "$name SVG 内容无效"
    }

    [pscustomobject]@{
        Diagram = $name
        PNG = "${width}x${height}"
        SizeKB = [math]::Round($size / 1KB, 1)
        Result = 'PASS'
    }
}

$readme = Join-Path $root 'README.md'
$content = Get-Content -LiteralPath $readme -Raw -Encoding UTF8
$links = [regex]::Matches($content, '\[[^\]]+\]\((?<path>[^)]+)\)')
foreach ($link in $links) {
    $target = $link.Groups['path'].Value
    if ($target -match '^(https?://|#)') {
        continue
    }
    $resolved = Join-Path $root ($target -split '#')[0]
    if (-not (Test-Path -LiteralPath $resolved)) {
        throw "README 本地链接缺失: $target"
    }
}

$rows | Format-Table -AutoSize
Write-Host "Diagrams verified: mmd=7 svg=7 png=7 links=$($links.Count) titleGuard=PASS" -ForegroundColor Green
