# 综合报告材料

本目录保存 **MallCloud 微商城** 微服务课程期末大作业的最终验收报告与综合报告组装说明。当前仓库维护 Markdown 事实来源，不提交由本机办公软件生成的临时文件。

## 1. 当前文件

| 文件 | 说明 |
| --- | --- |
| [FINAL_REPORT.md](FINAL_REPORT.md) | 当前 Markdown 最终验收报告，含最终结论、验证结果、边界和专项证据索引 |

## 2. 事实来源

后续如生成 Word 综合报告，正文内容以以下材料为事实来源：

- [docs/process/](../process/README.md)：需求、原型、用户故事、架构、数据库、接口、开发计划、测试报告、部署与联调记录
- [docs/standards/](../standards/README.md)：项目标准与编码规范
- [docs/diagrams/](../diagrams/README.md)：系统图与流程图
- [docs/page-screenshots/](../page-screenshots/README.md)：最终页面截图矩阵
- [docs/presentation/](../presentation/README.md)：答辩材料
- [docs/test/](../test/README.md)：原始 Newman/JMeter/Sentinel/Nacos/前端证据
- 根 [DESIGN.md](../../DESIGN.md)：产品设计基线
- 当前源码与 `db/init/`、`db/demo/`

## 3. 推荐 Word 结构

1. 项目概述：引用根 [README.md](../../README.md) 第 1-3 节。
2. 需求与范围：引用 [01-需求规格说明书.md](../process/01-需求规格说明书.md) 与 [PROJECT_STANDARD.md](../standards/PROJECT_STANDARD.md)。
3. 产品原型与用户故事：引用 [02-产品原型设计.md](../process/02-产品原型设计.md)、[03-用户故事地图.md](../process/03-用户故事地图.md) 与 [DESIGN.md](../../DESIGN.md)。
4. 架构设计：引用 [04-系统架构设计文档.md](../process/04-系统架构设计文档.md) 与 [docs/diagrams/](../diagrams/README.md)。
5. 数据库设计：引用 [05-数据库设计文档.md](../process/05-数据库设计文档.md)。
6. 接口设计：引用 [06-API接口文档.md](../process/06-API接口文档.md)。
7. 核心实现：引用 Gateway、订单、库存、支付消息、秒杀关键代码导读。
8. 前端实现：引用 [DESIGN.md](../../DESIGN.md) 与 [docs/page-screenshots/](../page-screenshots/README.md)。
9. 开发计划与测试：引用 [07-开发计划与里程碑.md](../process/07-开发计划与里程碑.md)、[08-测试计划与测试报告.md](../process/08-测试计划与测试报告.md)。
10. 部署与联调：引用 [09-本地部署与联调手册.md](../process/09-本地部署与联调手册.md)、[10-数据导入与联调记录.md](../process/10-数据导入与联调记录.md)。
11. 测试与验收：引用 [FINAL_REPORT.md](FINAL_REPORT.md)。
12. 边界与后续计划：引用最终报告中的未解决项。

## 4. 事实来源优先级

1. 当前源码、脚本和配置。
2. [FINAL_REPORT.md](FINAL_REPORT.md) 中的最终验证结论。
3. `docs/test/` 下的专项证据。
4. Sprint 过程截图与历史记录。

不得把未实现用户管理页面、未验证生产部署能力或未执行压测结果写入综合报告通过项。本仓库不维护额外 Markdown 版综合报告正文，避免与 `FINAL_REPORT.md` 形成双真相源。
