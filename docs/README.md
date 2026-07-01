# MallCloud 文档总索引

本目录是 MallCloud 最终课程交付材料入口。过程证据仍保留在 `docs/test/`，最终答辩和报告优先引用本索引下的交付分区。

## 交付分区

| 分区 | 入口 | 用途 |
| --- | --- | --- |
| 最终验收报告 | [FINAL_REPORT.md](FINAL_REPORT.md) | 最终结论、验证结果、边界和专项证据索引 |
| 项目标准 | [PROJECT_STANDARD.md](PROJECT_STANDARD.md) | 项目范围、完成定义、技术边界 |
| 产品需求 | [PRD.md](PRD.md) | 课程项目需求与不做范围 |
| 架构设计 | [ARCHITECTURE.md](ARCHITECTURE.md) | 微服务模块、调用链路、部署结构 |
| API 文档 | [API.md](API.md) | Gateway 暴露接口与业务码 |
| 数据库文档 | [DATABASE.md](DATABASE.md) | 表结构、初始化和数据口径 |
| 快速启动 | [QUICK_START.md](QUICK_START.md) | 本地启动、账号和排障入口 |
| 部署说明 | [DEPLOY.md](DEPLOY.md) | 本地部署、Docker 中间件和 profile 口径 |
| 测试方法 | [test/README.md](test/README.md) | Postman、JMeter、专项测试和历史证据目录 |
| 图表交付区 | [diagrams/README.md](diagrams/README.md) | Word/PPT 可用系统图与流程图 |
| 页面截图交付区 | [page-screenshots/README.md](page-screenshots/README.md) | 最终页面截图矩阵、尺寸和 MD5 |
| 答辩材料 | [presentation/README.md](presentation/README.md) | 视频脚本、PPT 大纲、答辩稿、关键代码导读 |
| 综合报告材料 | [documents/README.md](documents/README.md) | Word 综合报告组装说明与事实来源 |

## 过程证据保留方式

- `docs/test/screenshots/sprint3/` 保留 Sprint 过程截图，不作为最终图库主入口。
- Newman、JMeter、Sentinel、Nacos 和前端专项证据仍按原目录保留。
- 历史 Sprint 中的失败、限制和命名误导截图不删除，但在最终报告中统一归入“专项证据/历史记录”。

## 当前诚实边界

- Admin 前端只有 `/admin` 同页 Dashboard、后台订单、后台商品区块；没有用户管理页面。
- 本地 full profile 与外部基础设施能力按 `FINAL_REPORT.md` 的真实复跑结果描述，不扩大为生产部署能力。
- 页面截图和图表只呈现当前已实现能力，不补画未实现后台用户管理、优惠券、骑手或商家外卖业务。
