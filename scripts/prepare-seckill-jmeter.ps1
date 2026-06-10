param(
    [int]$ActivityId = 1,
    [int]$UserCount = 100,
    [int]$UserIdStart = 200001,
    [string]$UsernamePrefix = "jmeter_seckill_",
    [string]$PasswordHash = '$2y$10$k8Z56rLWfoKE6XNip7PenuX5tiKdD.QB93WSNZLHH4Y2fOg7.16Ku',
    [string]$MysqlContainer = "mall-mysql",
    [string]$MysqlUser = "root",
    [string]$MysqlPassword = "root",
    [string]$RedisContainer = "mall-redis"
)

$ErrorActionPreference = "Stop"

if ($UserCount -lt 1) {
    throw "UserCount must be greater than 0."
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
DELETE FROM seckill_order
WHERE activity_id = $ActivityId
  AND user_id BETWEEN $UserIdStart AND $userIdEnd;
"@

Write-Host "[INFO] Preparing $UserCount seckill JMeter users: $UsernamePrefix 1..$UserCount"
$sql | docker exec -i $MysqlContainer mysql "-u$MysqlUser" "-p$MysqlPassword"
if ($LASTEXITCODE -ne 0) {
    throw "MySQL preparation failed."
}

Write-Host "[INFO] Clearing Redis seckill stock and user keys for activity $ActivityId"
docker exec $RedisContainer redis-cli DEL "seckill:stock:$ActivityId" | Out-Null
for ($userId = $UserIdStart; $userId -le $userIdEnd; $userId++) {
    docker exec $RedisContainer redis-cli DEL "seckill:user:$ActivityId`:$userId" | Out-Null
}

Write-Host "[OK] Seckill JMeter data prepared."
Write-Host "[OK] Users: $UsernamePrefix 1..$UserCount / password: 123456"
Write-Host "[OK] Cleaned activity: $ActivityId, userId range: $UserIdStart-$userIdEnd"
