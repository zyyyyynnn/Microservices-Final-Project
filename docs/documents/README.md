# 综合报告材料

本目录作为 Word/PPT 综合报告的材料入口。当前仓库维护 Markdown 事实来源，不提交由本机办公软件生成的临时文件。

## 推荐 Word 结构

1. 项目概述：引用根 [README.md](../../README.md) 第 1-3 节。
2. 需求与范围：引用 [docs/PRD.md](../PRD.md) 与 [docs/PROJECT_STANDARD.md](../PROJECT_STANDARD.md)。
3. 架构设计：引用 [docs/ARCHITECTURE.md](../ARCHITECTURE.md) 与 [docs/diagrams/](../diagrams/README.md)。
4. 数据库设计：引用 [docs/DATABASE.md](../DATABASE.md)。
5. 核心实现：引用 Gateway、订单、库存、支付消息、秒杀关键代码导读。
6. 前端实现：引用 [DESIGN.md](../../DESIGN.md) 与 [docs/page-screenshots/](../page-screenshots/README.md)。
7. 测试与验收：引用 [docs/FINAL_REPORT.md](../FINAL_REPORT.md)。
8. 边界与后续计划：引用最终报告中的未解决项。

## 事实来源优先级

1. 当前源码、脚本和配置。
2. `docs/FINAL_REPORT.md` 中的最终验证结论。
3. `docs/test/` 下的专项证据。
4. Sprint 过程截图与历史记录。

不得把未实现用户管理页面、未验证生产部署能力或未执行压测结果写入综合报告通过项。
