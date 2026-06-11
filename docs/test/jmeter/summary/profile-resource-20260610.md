# 启动 Profile 与资源采样

## 基本信息

| 项目 | 内容 |
|---|---|
| 日期 | 2026-06-10 / 2026-06-11 |
| 启动入口 | `start-all.bat` / `stop-all.bat` |
| 构建 | `--no-build`，复用已有 JAR |
| 前端 | `--skip-frontend` |
| 采样方式 | `docker stats --no-stream`，`Get-Process java` |
| 说明 | 资源值为单次本地采样，不代表容量上限 |

## Profile 验证

| Profile | 命令 | 结果 | 容器 | 后端服务 | 状态文件 | stop-all 接管 |
|---|---|---|---|---|---|---|
| core | `cmd /c start-all.bat --profile core --no-build --skip-frontend --no-pause` | 通过 | mysql、redis、nacos、seata | gateway、auth、user、product、inventory、cart、order | PID 与项目 JAR 命令行匹配 | 已停止 7 个托管服务 |
| search | `cmd /c start-all.bat --profile search --no-build --skip-frontend --no-pause` | 通过 | mysql、redis、nacos、seata、elasticsearch、rocketmq-namesrv、rocketmq-broker | core + search | PID 与项目 JAR 命令行匹配 | 已停止 8 个托管服务 |
| seckill | `cmd /c start-all.bat --profile seckill --no-build --skip-frontend --no-pause` | 通过 | mysql、redis、nacos、seata、rocketmq-namesrv、rocketmq-broker、sentinel | gateway、auth、user、product、inventory、order、message、seckill | PID 与项目 JAR 命令行匹配 | 已停止 8 个托管服务 |
| full-backend | `cmd /c start-all.bat --skip-infrastructure --no-build --skip-frontend --no-pause` | 通过 | 手动补齐当前已可用中间件后跳过基础设施启动 | gateway、auth、user、product、inventory、cart、order、pay、search、seckill、message、admin-biz、job | PID 与项目 JAR 命令行匹配 | 已由 `stop-all.bat --no-pause` 停止 13 个托管服务，`9100-9112` 端口已释放，状态文件已清除 |
| full-docker-profile | `cmd /c start-all.bat --profile full --no-build --skip-frontend --allow-partial --no-pause` | 有条件通过 | RocketMQ Dashboard 镜像阻断已解除 | 已进入后端启动 | 状态文件已生成 | 前端 npm 启动失败，full 浏览器验收未完成 |

外部端口 `9012` 存在非本项目 Java 进程监听，Profile 切换与 stop-all 均未终止该外部进程。该记录形成后，后端端口已整体迁移到连续区间 `9100-9112`，`mall-job` 当前使用 `9112`。

## BAT 参数

| 命令 | 结果 |
|---|---|
| `cmd /c start-all.bat --help` | 输出帮助 |
| `cmd /v:on /c "start-all.bat --bad-arg --no-pause & echo EXIT=!ERRORLEVEL!"` | `EXIT=1` |
| `cmd /v:on /c "start-all.bat --profile --no-pause & echo EXIT=!ERRORLEVEL!"` | `EXIT=1` |
| `cmd /v:on /c "start-all.bat --profile invalid --no-pause & echo EXIT=!ERRORLEVEL!"` | `EXIT=1` |

## 资源采样

| 模式 | Java 进程 | Java Working Set 总量 | Docker 主要内存 | 备注 |
|---|---:|---:|---|---|
| full-backend | 13 | 2586.4 MB | mysql 445.4MiB；nacos 919.7MiB；redis 7.2MiB；seata 779.9MiB；elasticsearch 1.322GiB；rocketmq-namesrv 266.3MiB；rocketmq-broker 764.7MiB；sentinel 395.9MiB | 端口迁移后本地全量后端启动采样 |
| full-docker-profile | 未采样 | 未采样 | 未采样 | RocketMQ Dashboard 镜像阻断解除；前端启动未完成，未形成完整 full 资源采样 |
| core | 7 | 2525.9 MB | mysql 516.9MiB；nacos 909.2MiB；redis 8.1MiB；seata 1.501GiB | 常规 JVM 参数 |
| seckill | 8 | 3154.4 MB | mysql 518.8MiB；nacos 908.1MiB；redis 8.4MiB；seata 1.501GiB；rocketmq-namesrv 268.4MiB；rocketmq-broker 786.2MiB；sentinel 417.5MiB | 秒杀阶梯后采样 |
| core + LowMemory | 7 | 2121.0 MB | mysql 516.7MiB；nacos 921.7MiB；redis 8.3MiB；seata 1.501GiB | Java 命令行均包含 `-Xms64m -Xmx320m -XX:MaxMetaspaceSize=192m` |

LowMemory 仅用于启动资源验证，正式 JMeter 压测未使用 LowMemory。

资源解释：Profile 的主要作用是按场景减少无关容器和服务；本轮 `core` 与 `full-backend` Java Working Set 差距不大，不作为“Profile 大幅降低 Java 内存”的证据。`core + LowMemory` 对 core 后端 JVM Working Set 的下降有本机实测证据。
