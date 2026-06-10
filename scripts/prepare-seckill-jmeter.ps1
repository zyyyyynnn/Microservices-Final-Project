param(
    [int]$ActivityId = 9001,
    [int]$SkuId = 9003,
    [int]$TotalStock = 100,
    [int]$LimitPerUser = 1,
    [int]$DurationMinutes = 120,
    [int]$UserCount = 100,
    [int]$UserIdStart = 200001,
    [string]$UsernamePrefix = "jmeter_seckill_",
    [string]$PasswordHash = '$2y$10$k8Z56rLWfoKE6XNip7PenuX5tiKdD.QB93WSNZLHH4Y2fOg7.16Ku',
    [ValidateSet("host", "docker")]
    [string]$MysqlMode = "host",
    [string]$MysqlContainer = "mall-mysql",
    [string]$MysqlHost = $(if ($env:MYSQL_HOST) { $env:MYSQL_HOST } else { "127.0.0.1" }),
    [string]$MysqlPort = $(if ($env:MYSQL_PORT) { $env:MYSQL_PORT } else { "3306" }),
    [string]$MysqlUser = "root",
    [string]$MysqlPassword = $(if ($env:MYSQL_PWD) { $env:MYSQL_PWD } else { "root" }),
    [string]$RedisContainer = "mall-redis"
)

$ErrorActionPreference = "Stop"

if ($UserCount -lt 1) {
    throw "UserCount must be greater than 0."
}

if ($TotalStock -lt 1) {
    throw "TotalStock must be greater than 0."
}

if ($LimitPerUser -lt 1) {
    throw "LimitPerUser must be greater than 0."
}

$userIdEnd = $UserIdStart + $UserCount - 1

$values = for ($i = 1; $i -le $UserCount; $i++) {
    $userId = $UserIdStart + $i - 1
    $username = "$UsernamePrefix$i"
    $phone = "139" + $userId.ToString().PadLeft(8, "0")
    "($userId, '$username', '$phone', '秒杀压测$i', 1)"
}

$authValues = for ($i = 1; $i -le $UserCount; $i++) {
    $userId = $UserIdStart + $i - 1
    $username = "$UsernamePrefix$i"
    "($userId, 'PASSWORD', '$username', '$PasswordHash', 'USER')"
}

$sql = @"
USE mall_user;
INSERT INTO user (id, username, phone, nickname, status)
VALUES
$($values -join ",`n")
ON DUPLICATE KEY UPDATE
  username = VALUES(username),
  phone = VALUES(phone),
  nickname = VALUES(nickname),
  status = VALUES(status);

USE mall_auth;
INSERT INTO sys_user_auth (user_id, identity_type, identifier, credential, role)
VALUES
$($authValues -join ",`n")
ON DUPLICATE KEY UPDATE
  credential = VALUES(credential),
  role = VALUES(role),
  status = 1;

USE mall_seckill;
INSERT INTO seckill_activity (id, name, sku_id, seckill_price, total_stock, limit_per_user, start_time, end_time, status)
VALUES ($ActivityId, 'JMeter 秒杀压测专用活动', $SkuId, 4799.00, $TotalStock, $LimitPerUser, DATE_SUB(NOW(), INTERVAL 5 MINUTE), DATE_ADD(NOW(), INTERVAL $DurationMinutes MINUTE), 0)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  sku_id = VALUES(sku_id),
  seckill_price = VALUES(seckill_price),
  total_stock = VALUES(total_stock),
  limit_per_user = VALUES(limit_per_user),
  start_time = VALUES(start_time),
  end_time = VALUES(end_time),
  status = VALUES(status);

DELETE FROM seckill_order
WHERE activity_id = $ActivityId;

SELECT id, sku_id, total_stock, limit_per_user, status, start_time, end_time
FROM seckill_activity
WHERE id = $ActivityId;
"@

Write-Host "[INFO] Preparing $UserCount seckill JMeter users: $($UsernamePrefix)1..$UserCount"
if ($MysqlMode -eq "docker") {
    $sql | docker exec -i $MysqlContainer mysql "-u$MysqlUser" "-p$MysqlPassword"
} else {
    $mysqlArgs = @(
        "--host=$MysqlHost",
        "--port=$MysqlPort",
        "--user=$MysqlUser",
        "--password=$MysqlPassword",
        "--default-character-set=utf8mb4"
    )
    $sql | & mysql @mysqlArgs
}
if ($LASTEXITCODE -ne 0) {
    throw "MySQL preparation failed."
}

Write-Host "[INFO] Clearing Redis seckill stock and user keys for activity $ActivityId"
docker exec $RedisContainer redis-cli DEL "seckill:stock:$ActivityId" | Out-Null
$pattern = "seckill:user:$ActivityId`:*"
$keys = docker exec $RedisContainer redis-cli --scan --pattern $pattern
foreach ($key in $keys) {
    if (-not [string]::IsNullOrWhiteSpace($key)) {
        docker exec $RedisContainer redis-cli DEL $key | Out-Null
    }
}

Write-Host "[OK] Seckill JMeter data prepared."
Write-Host "[OK] Activity: $ActivityId / skuId: $SkuId / totalStock: $TotalStock / limitPerUser: $LimitPerUser"
Write-Host "[OK] Users: $($UsernamePrefix)1..$UserCount / password: 123456"
Write-Host "[OK] Reset all seckill_order rows and Redis seckill keys for activity $ActivityId"
