# 商品详情页库存字段映射修复报告

## 1. 问题
商品详情页显示“库存不足或字段未返回”，导致购物链路 UI 截图中断。

## 2. API 证据
- Newman 中 product/detail/cart/order/pay/stock 均通过。
- product-1001-response-20260611.json (API 返回的 `product.skus[].stock` 确实为 0，因为设计上库存属于独立服务)
- stock-9001-response-20260611.json (API 返回独立且有效的真实库存数据：`{"code":200,"data":{"skuId":9001,"total":86,"locked":16,"available":70}}`)

## 3. 根因
`ProductDetailView.vue` 前端组件直接读取了商品聚合接口中的 `product.skus[].stock` 作为库存。而根据微服务划分，实际可售库存（available）由独立的 `mall-inventory` 服务管理，因此前端读取的商品静态 stock 永远为 0，进而导致所有商品的“加入购物车”及“立即购买”按钮被逻辑禁用。

## 4. 修复
1. `mall-frontend/src/api/mall.ts`:
   - 补充增加了库存查询方法 `inventoryStock(skuId)`，调用真实的 `/api/v1/inventory/stock/{skuId}` 接口。
2. `mall-frontend/src/views/ProductDetailView.vue`:
   - 增加对 `selectedSkuId` 的 `watch` 监听；
   - 在选中或切换 SKU 时，动态异步调用 `mallApi.inventoryStock` 并提取真实的 `available` 字段作为远程实时库存（`remoteStock`）；
   - 更新按钮的禁用条件逻辑，改为依赖 `remoteStock` 评估是否允许加入购物车。

## 5. 验证
| 步骤 | 结果 |
|---|---|
| /products/1001 | 成功打开并默认选中第一个 SKU |
| 库存显示 | 成功拉取到真实库存数据并正常显示 (available > 0) |
| 加入购物车 | 按钮高亮可点击，点击后提示“已加入购物车” |
| /cart | 购物车内正常拉取并展示刚才加入的商品信息 |
| /checkout | 结算页非空，正确显示了待支付的商品列表、金额以及收货地址 |

## 6. 结论
通过
