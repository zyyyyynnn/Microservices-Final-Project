# MallCloud 项目标准

本目录保存 MallCloud 的项目开发与交付标准、编码与配置规范。MallCloud 比一般课程项目多了 Agent 工作协议与工程化标准，因此单独成区，不混入过程文档。

## 文件清单

| 文件 | 用途 |
| --- | --- |
| [PROJECT_STANDARD.md](PROJECT_STANDARD.md) | 项目范围、技术基线、能力状态定义、单一真相源、架构实现标准、测试与文档质量标准 |
| [CODING_STYLE.md](CODING_STYLE.md) | 编码与配置规范、命名、分层、事务、OpenFeign、RocketMQ、Sentinel、Redis、安全、日志、测试与 Git 规范 |

## 使用方式

- `PROJECT_STANDARD.md` 是项目最高标准，需求、设计、代码、配置、测试、部署与答辩材料均需符合其约束。
- `CODING_STYLE.md` 是编码与配置的强制规范，只保留当前项目能够执行的要求，推荐项和后续目标不写成强制现状。
- 根目录 [AGENTS.md](../../AGENTS.md) 是 Agent 工作协议，引用本目录标准作为最高基准。

## 与其他分区的关系

- 过程文档（需求、架构、API、数据库等）维护在 [docs/process/](../process/README.md)。
- 最终验收报告维护在 [docs/documents/FINAL_REPORT.md](../documents/FINAL_REPORT.md)。
- 当标准与过程文档冲突时，以本目录标准为准；当标准与实际代码/可运行行为冲突时，先核对实际文件与可运行行为，再修改最小必要范围。
