export const demoSkuCatalog: Record<number, {
  spuId: number;
  skuId: number;
  name: string;
  spec: string;
  image: string;
}> = {
  9001: {
    spuId: 1001,
    skuId: 9001,
    name: 'iPhone 15 Pro 256G 钛原色',
    spec: '颜色：钛原色 / 版本：256G',
    image: '/products/1001-iphone-15-pro.svg',
  },
  9002: {
    spuId: 1002,
    skuId: 9002,
    name: 'iPhone 15 128G 粉色',
    spec: '颜色：粉色 / 版本：128G',
    image: '/products/1002-iphone-15-pink.svg',
  },
  9003: {
    spuId: 1003,
    skuId: 9003,
    name: '小米 14 Pro 16+512 黑色',
    spec: '颜色：黑色 / 版本：512G',
    image: '/products/1003-mi-14-pro.svg',
  },
  9004: {
    spuId: 1004,
    skuId: 9004,
    name: '华为 Mate 60 Pro 12+512',
    spec: '颜色：雅川青 / 版本：512G',
    image: '/products/1004-huawei-mate-60.svg',
  },
  9005: {
    spuId: 1005,
    skuId: 9005,
    name: 'MacBook Air 13 M3 8+256',
    spec: '颜色：午夜色 / 版本：256G',
    image: '/products/1005-macbook-air.svg',
  },
  9006: {
    spuId: 1006,
    skuId: 9006,
    name: '罗技 MX Master 3S 鼠标',
    spec: '颜色：黑色',
    image: '/products/1006-logitech-mouse.svg',
  },
  9007: {
    spuId: 1007,
    skuId: 9007,
    name: 'YSL 小金条口红 1966',
    spec: '色号：1966',
    image: '/products/1007-ysl-lipstick.svg',
  },
  9008: {
    spuId: 1008,
    skuId: 9008,
    name: '欧莱雅玻色因保湿精华',
    spec: '容量：50ml',
    image: '/products/1008-loreal-serum.svg',
  },
  9009: {
    spuId: 1009,
    skuId: 9009,
    name: '蓝山挂耳咖啡礼盒',
    spec: '口味：经典蓝山',
    image: '/products/1009-coffee-gift-box.svg',
  },
  9010: {
    spuId: 1010,
    skuId: 9010,
    name: '每日坚果礼盒 30 包',
    spec: '规格：30包/箱',
    image: '/products/1010-nuts-gift-box.svg',
  },
  9011: {
    spuId: 1011,
    skuId: 9011,
    name: 'Nike Pegasus 跑步鞋',
    spec: '尺码：42',
    image: '/products/1011-nike-running-shoes.svg',
  },
  9012: {
    spuId: 1012,
    skuId: 9012,
    name: '小米智能台灯',
    spec: '颜色：白色',
    image: '/products/1012-desk-lamp.svg',
  },
};

export function demoSkuById(skuId: unknown) {
  const id = Number(skuId);
  return Number.isFinite(id) ? demoSkuCatalog[id] : undefined;
}

export function demoSpuIdBySkuId(skuId: unknown) {
  return demoSkuById(skuId)?.spuId;
}
