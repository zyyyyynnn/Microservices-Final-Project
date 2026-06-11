# Newman 端口迁移回归摘要

## 基本信息

| 项目 | 内容 |
|---|---|
| 日期 | 2026-06-11 |
| BaseURL | `http://localhost:9100` |
| 命令 | `pwsh .\scripts\run-newman.ps1 -SkipHtml` |
| HTML 报告 | 未生成；使用 `-SkipHtml`，不提交包含 Token 的原始 HTML |
| 说明 | 本摘要为脱敏记录，不包含 Authorization、Access Token、Refresh Token、Cookie、登录密码或完整响应正文 |

## 执行结果

| 指标 | 结果 |
|---|---:|
| Requests | 28 |
| Assertions | 60 |
| Failed assertions | 0 |
| Average response time | 54ms |
| Total run duration | 3.8s |

## 前置处理

首次执行时，秒杀用例命中 `zhangsan(userId=1001)` 对当前进行中种子活动 `activityId=3` 的历史限购状态，`Create seckill` 返回业务码 `40403`，后续结果查询因未取得 `requestId` 失败。

按测试文档前置要求清理该测试用户在当前活动的限购状态后复跑通过：

```powershell
docker exec mall-redis redis-cli DEL seckill:user:3:1001
docker exec mall-mysql mysql -uroot -proot -e "DELETE FROM mall_seckill.seckill_order WHERE activity_id=3 AND user_id=1001;"
```

该前置处理仅用于恢复 Newman 秒杀成功用例的可重复执行条件，不代表放宽限购断言。
