# 最终页面截图交付区

本目录收录最终报告和 PPT 可直接引用的页面截图。Sprint 过程截图仍保留在 `docs/test/screenshots/sprint3/`，本目录只放最终精选资产，不包含 admin 用户访问 `/orders`、`/pay`、`/seckill`、`/cart` 后 fallback 到 `/admin` 的重复截图。

2026-07-01 UI 精致感升级后，移动端截图已从交付矩阵中移除，仅保留桌面端 1440×900 截图，以集中体现核心交易链路的视觉与交互完成度。

## 截图矩阵

| 页面 | URL | 视口 | 文件 | 实际尺寸 | MD5 | 来源 | 结果 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 首页 | `/` | desktop | `01-home-desktop-1440x900.png` | 1440×900 | `c1d314f0d014b0a49da64e852ef52658` | 前端核心链路验收 | 通过 |
| 搜索 | `/search?keyword=iPhone` | desktop | `03-search-desktop-1440x900.png` | 1440×900 | `7c87add3e2573ba1760be7b2858ba8f3` | sprint3 24 | 通过 |
| 商品详情 | `/products/1001` | desktop | `05-product-detail-desktop-1440x900.png` | 1440×900 | `2044921637336b1fc85a42301dd8431b` | 前端核心链路验收 | 通过 |
| 购物车 | `/cart` | desktop | `07-cart-desktop-1440x900.png` | 1440×900 | `3f1646dc0f90b20fded1807dc2bfaff8` | sprint3 26 | 通过 |
| 订单详情 | `/orders/{orderNo}` | desktop | `09-order-detail-desktop-1440x900.png` | 1440×900 | `5f4e3efb04197c16b14036b7d7a31157` | sprint3 18 | 通过 |
| 支付 | `/pay/{orderNo}` | desktop | `11-pay-desktop-1440x900.png` | 1440×900 | `95cc04bb4bd4b3d1b28b6cfc99ed1f32` | sprint3 20 | 通过 |
| 秒杀 | `/seckill` | desktop | `13-seckill-desktop-1440x900.png` | 1440×900 | `e98d402c4940632064f3c34d528bc2c8` | sprint3 22 | 通过 |
| Admin Dashboard | `/admin` | desktop | `15-admin-dashboard-desktop-1440x900.png` | 1440×900 | `56d12474cd13b42529368ad9581b7f6c` | sprint3 36 | 通过 |
| Admin 后台订单 | `/admin` 同页区块 | desktop | `17-admin-orders-desktop-1440x900.png` | 1440×900 | `509abfe9532d8eb7290a3ec076385568` | sprint3 38 | 通过 |
| Admin 后台商品 | `/admin` 同页区块 | desktop | `19-admin-products-desktop-1440x900.png` | 1440×900 | `99521650fe111a19de9a89d70d7fe668` | sprint3 40 | 通过 |

## 边界说明

- Admin 后台当前只有 `/admin` 单路由，同页包含 Dashboard、后台订单、后台商品三个区块。
- 未实现独立用户管理页面，不生成用户管理截图。
- 过程目录中 28-35 号 admin fallback 重复图不进入最终图库。
- PIL 核验 10/10 尺寸与文件名一致；MD5 10/10 唯一。
- 移动端 390×844 截图已于 2026-07-01 UI 精致感升级中移除，桌面端 1440×900 截图足以体现核心交易链路完成度。
