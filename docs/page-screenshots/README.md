# 最终页面截图交付区

本目录收录最终报告和 PPT 可直接引用的页面截图。Sprint 过程截图仍保留在 `docs/test/screenshots/sprint3/`，本目录只放最终精选资产，不包含 admin 用户访问 `/orders`、`/pay`、`/seckill`、`/cart` 后 fallback 到 `/admin` 的重复截图。

## 截图矩阵

| 页面 | URL | 视口 | 文件 | 实际尺寸 | MD5 | 来源 | 结果 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 首页 | `/` | desktop | `01-home-desktop-1440x900.png` | 1440×900 | `c1d314f0d014b0a49da64e852ef52658` | 前端核心链路验收 | 通过 |
| 首页 | `/` | mobile | `02-home-mobile-390x844.png` | 390×844 | `f6b3a409981cdf91f0b6b9471ffa2d82` | 2026-07-01 Playwright 真实访问补采 | 通过 |
| 搜索 | `/search?keyword=iPhone` | desktop | `03-search-desktop-1440x900.png` | 1440×900 | `7c87add3e2573ba1760be7b2858ba8f3` | sprint3 24 | 通过 |
| 搜索 | `/search?keyword=iPhone` | mobile | `04-search-mobile-390x844.png` | 390×844 | `8137df8d814f43ca4414f3c3a1ffb7ce` | sprint3 25 | 通过 |
| 商品详情 | `/products/1001` | desktop | `05-product-detail-desktop-1440x900.png` | 1440×900 | `2044921637336b1fc85a42301dd8431b` | 前端核心链路验收 | 通过 |
| 商品详情 | `/products/1001` | mobile | `06-product-detail-mobile-390x844.png` | 390×844 | `8c5c3c25ba5a0e7e044ea987a08cd528` | sprint2 06 | 通过 |
| 购物车 | `/cart` | desktop | `07-cart-desktop-1440x900.png` | 1440×900 | `3f1646dc0f90b20fded1807dc2bfaff8` | sprint3 26 | 通过 |
| 购物车 | `/cart` | mobile | `08-cart-mobile-390x844.png` | 390×844 | `210e0d7634c5d207936281c943b3a045` | sprint3 27 | 通过 |
| 订单详情 | `/orders/{orderNo}` | desktop | `09-order-detail-desktop-1440x900.png` | 1440×900 | `5f4e3efb04197c16b14036b7d7a31157` | sprint3 18 | 通过 |
| 订单详情 | `/orders/{orderNo}` | mobile | `10-order-detail-mobile-390x844.png` | 390×844 | `0c675c896cfc4a988bad7677ae7b9a0a` | sprint3 19 | 通过 |
| 支付 | `/pay/{orderNo}` | desktop | `11-pay-desktop-1440x900.png` | 1440×900 | `95cc04bb4bd4b3d1b28b6cfc99ed1f32` | sprint3 20 | 通过 |
| 支付 | `/pay/{orderNo}` | mobile | `12-pay-mobile-390x844.png` | 390×844 | `0cbd64a544fc8d371864f3678b93a7a7` | sprint3 21 | 通过 |
| 秒杀 | `/seckill` | desktop | `13-seckill-desktop-1440x900.png` | 1440×900 | `e98d402c4940632064f3c34d528bc2c8` | sprint3 22 | 通过 |
| 秒杀 | `/seckill` | mobile | `14-seckill-mobile-390x844.png` | 390×844 | `8ae19acfdb1f582497ad80459ab4ef7d` | sprint3 23 | 通过 |
| Admin Dashboard | `/admin` | desktop | `15-admin-dashboard-desktop-1440x900.png` | 1440×900 | `56d12474cd13b42529368ad9581b7f6c` | sprint3 36 | 通过 |
| Admin Dashboard | `/admin` | mobile | `16-admin-dashboard-mobile-390x844.png` | 390×844 | `c232c15a32e08d32ed6e7780170bb80f` | sprint3 37 | 通过 |
| Admin 后台订单 | `/admin` 同页区块 | desktop | `17-admin-orders-desktop-1440x900.png` | 1440×900 | `509abfe9532d8eb7290a3ec076385568` | sprint3 38 | 通过 |
| Admin 后台订单 | `/admin` 同页区块 | mobile | `18-admin-orders-mobile-390x844.png` | 390×844 | `19a34d51a97f25276dbd6f5c8381a05c` | sprint3 39 | 通过 |
| Admin 后台商品 | `/admin` 同页区块 | desktop | `19-admin-products-desktop-1440x900.png` | 1440×900 | `99521650fe111a19de9a89d70d7fe668` | sprint3 40 | 通过 |
| Admin 后台商品 | `/admin` 同页区块 | mobile | `20-admin-products-mobile-390x844.png` | 390×844 | `0fce0e60f8ca285b27dc93a94e857ff1` | sprint3 41 | 通过 |

## 边界说明

- Admin 后台当前只有 `/admin` 单路由，同页包含 Dashboard、后台订单、后台商品三个区块。
- 未实现独立用户管理页面，不生成用户管理截图。
- 过程目录中 28-35 号 admin fallback 重复图不进入最终图库。
- PIL 核验 20/20 尺寸与文件名一致；MD5 20/20 唯一。
- 首页 mobile 使用 Playwright CLI 真实访问 `http://127.0.0.1:5173/`、`setViewportSize(390, 844)`、viewport screenshot，未使用 full-page。
