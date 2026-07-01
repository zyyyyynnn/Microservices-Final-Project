# MallCloud 文档总索引

## 1. 文档定位

本目录归档 **MallCloud 微商城** 的 Spring Cloud Alibaba 微服务课程期末大作业交付材料。项目定位是“可本地部署、可演示、可追溯的课程项目”，不包装为生产级商业微商城系统。

## 2. 交付材料清单

| 材料 | 入口 | 内容 |
| --- | --- | --- |
| 课程过程文档 | [process/](process/README.md) | 需求、原型、用户故事、架构、数据库、接口、开发计划、测试报告、部署手册和联调记录 |
| 项目标准 | [standards/](standards/README.md) | 项目开发与交付标准、编码与配置规范 |
| 最终验收报告 | [documents/](documents/README.md) | 当前 Markdown 最终验收报告，后续 Word 综合报告的事实来源说明 |
| 答辩材料 | [presentation/](presentation/README.md) | PPT 大纲、PPT 答辩稿、视频录制脚本、关键代码导读 |
| 图表交付区 | [diagrams/](diagrams/README.md) | Mermaid 主源、SVG、PNG 和统一主题 |
| 页面截图交付区 | [page-screenshots/](page-screenshots/README.md) | 最终页面截图矩阵、尺寸和 MD5 |
| 测试资产与历史证据 | [test/](test/README.md) | Postman、JMeter、Sentinel、Nacos、前端专项与 Sprint 过程证据目录 |

## 3. 过程文档

过程文档以当前源码、`db/init/`、`db/demo/`、联调记录和测试报告为事实来源。已通过的能力应能对应命令输出、数据库记录、截图或测试用例；未覆盖项不得写成当前能力。

入口：[docs/process/README.md](process/README.md)。

## 4. 项目标准

MallCloud 比一般课程项目多了 Agent 工作协议与工程化标准，因此项目标准与编码规范单独成区，不混入过程文档。

入口：[docs/standards/README.md](standards/README.md)。

## 5. 答辩材料

答辩材料包括 PPT 大纲、PPT 答辩稿、视频录制脚本和关键代码导读。关键代码导读用于老师追问“打开代码看看”的现场场景。答辩口径以最终验收报告为准：不宣称生产级系统，不伪造未实现页面，不把未验证能力写成通过。

入口：[docs/presentation/README.md](presentation/README.md)。

## 6. 图表与截图

- 图表以 `.mmd` 为主源，统一使用低饱和雾霾蓝主题，并导出 SVG 与高清 PNG（宽度全部 ≥ 2400px）。
- 页面截图目录已归档本地真实运行页面截图，desktop 10 张 1440×900、mobile 10 张 390×844，PIL 尺寸与 MD5 已校验。
- 过程截图仍保留在 `docs/test/screenshots/sprint3/`，不作为最终图库主入口。

## 7. 综合报告说明

[documents/FINAL_REPORT.md](documents/FINAL_REPORT.md) 是当前 Markdown 最终验收报告。后续如生成 Word 综合报告，应以 `docs/process`、`docs/standards`、`docs/diagrams`、`docs/page-screenshots`、`docs/presentation`、`docs/test` 证据和当前源码/SQL 为事实来源，不维护重复正文。

## 8. 设计规范

产品流程、用户交互、页面状态、角色权限和视觉 token 的设计基线维护在根目录 [DESIGN.md](../DESIGN.md)，不复制到 `docs/` 内，避免双真相源。

## 9. 交付边界

- Admin 前端只有 `/admin` 同页 Dashboard、后台订单、后台商品区块；没有用户管理页面。
- 本地 full profile 与外部基础设施能力按 [documents/FINAL_REPORT.md](documents/FINAL_REPORT.md) 的真实复跑结果描述，不扩大为生产部署能力。
- 页面截图和图表只呈现当前已实现能力，不补画未实现后台用户管理、优惠券、物流或真实支付。
- 不提交临时草稿、工具缓存、运行日志、构建产物或依赖目录。
- 不提交数据库连接信息、鉴权密钥等本地敏感配置。
