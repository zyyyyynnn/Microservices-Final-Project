# MallCloud 过程文档

本目录保存 MallCloud 微商城微服务课程期末大作业的需求、设计、开发、测试、部署和联调过程文档，也是综合报告与答辩材料的事实来源。文档以当前仓库代码、`db/init/`、`db/demo/` 和实际验证结果为准。

## 文件清单

| 文件 | 用途 |
| --- | --- |
| [01-需求规格说明书.md](01-需求规格说明书.md) | 角色、功能需求、非功能需求和实现边界 |
| [02-产品原型设计.md](02-产品原型设计.md) | 核心页面信息结构、交互说明与 UI 状态规范 |
| [03-用户故事地图.md](03-用户故事地图.md) | 按角色梳理 MVP、增强与后续规划 |
| [04-系统架构设计文档.md](04-系统架构设计文档.md) | 微服务边界、调用链路、Gateway、Nacos、Seata、RocketMQ、Sentinel、Redis、ES |
| [05-数据库设计文档.md](05-数据库设计文档.md) | 业务库、核心表、索引、Seata undo_log 与演示数据口径 |
| [06-API接口文档.md](06-API接口文档.md) | Gateway 暴露接口、统一响应、错误码与 Postman 集合标准 |
| [07-开发计划与里程碑.md](07-开发计划与里程碑.md) | 团队分工、里程碑阶段与当前交付状态 |
| [08-测试计划与测试报告.md](08-测试计划与测试报告.md) | 测试范围、执行入口、结果摘要与可信度要求 |
| [09-本地部署与联调手册.md](09-本地部署与联调手册.md) | Windows 11 本地部署、中间件、数据库、启动、联调与排障 |
| [10-数据导入与联调记录.md](10-数据导入与联调记录.md) | SQL 导入、演示数据、数据验证与业务联调记录 |

## 交付证据对应关系

| 证据类型 | 对应材料 |
| --- | --- |
| 需求范围与功能边界 | `01-需求规格说明书.md`、`03-用户故事地图.md` |
| 页面与交互设计 | `02-产品原型设计.md`、[docs/page-screenshots/](../page-screenshots/README.md)、根 [DESIGN.md](../../DESIGN.md) |
| 架构与关键技术 | `04-系统架构设计文档.md`、[docs/diagrams/](../diagrams/README.md) |
| 数据库与接口 | `05-数据库设计文档.md`、`06-API接口文档.md` |
| 测试与联调 | `08-测试计划与测试报告.md`、`10-数据导入与联调记录.md`、[docs/test/](../test/README.md) |
| 本地运行 | `09-本地部署与联调手册.md`、根目录 `start-all.bat` |

## 与其他分区的关系

- 项目标准与编码规范维护在 [docs/standards/](../standards/README.md)，因 MallCloud 比一般课程项目多了 Agent 与工程标准，单独成区，不混入过程文档。
- 最终验收报告维护在 [docs/documents/FINAL_REPORT.md](../documents/FINAL_REPORT.md)。
- 原始 Newman/JMeter/Sentinel/Nacos/前端证据仍按原目录保留在 [docs/test/](../test/README.md)，不搬入本目录。

## 证据使用原则

- 源码、配置和 SQL 可以证明“实现已存在”，运行时验收以测试报告、联调记录和截图为准。
- 命令只有在记录中明确执行且退出码为 0 时才标记“通过”。
- 未实现或未验证的内容不得写成当前能力。
- 压力测试、全量接口测试和生产级可用性验证不属于当前课程交付范围。

综合报告与答辩材料正文内容以本目录、[docs/diagrams/](../diagrams/README.md)、[docs/page-screenshots/](../page-screenshots/README.md)、[docs/presentation/](../presentation/README.md) 和当前源码/SQL 为事实来源；本仓库不维护额外 Markdown 版综合报告正文。
