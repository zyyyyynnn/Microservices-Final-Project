import type { UnknownRecord } from '../api/types';

type CatalogProduct = UnknownRecord & {
  spuId: number;
  name: string;
  brand: string;
  description: string;
  mainImage: string;
  sales: number;
  status: number;
  skus: UnknownRecord[];
};

// [FALLBACK_ASSET]: Use unsplash as fallback
export const onlineImageSources = [
  'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9', // iPhone
  'https://images.unsplash.com/photo-1517336714731-489689fd1ca8', // MacBook
  'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5', // iPad/Phone
  'https://images.unsplash.com/photo-1598327105666-5b89351aff97', // Xiaomi 14
  'https://images.unsplash.com/photo-1601784551446-20c9e07cdbdb', // Huawei Mate
  'https://images.unsplash.com/photo-1542393545-10f5cde2c227', // Apple Watch / iPad
];

const imageParams = '?auto=format&fit=crop&w=900&q=80';

export const productImagesBySpuId: Record<number, string> = {
  1001: `https://images.unsplash.com/photo-1511707171634-5f897ff02aa9${imageParams}`,
  1002: `https://images.unsplash.com/photo-1517336714731-489689fd1ca8${imageParams}`,
  1003: `https://images.unsplash.com/photo-1598327105666-5b89351aff97${imageParams}`,
  1004: `https://images.unsplash.com/photo-1601784551446-20c9e07cdbdb${imageParams}`,
  1005: `https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5${imageParams}`,
  1006: `https://images.unsplash.com/photo-1542393545-10f5cde2c227${imageParams}`,
};

export const heroImage = new URL('../assets/hero/mallcloud-hero.png', import.meta.url).href;

export const seedCatalogProducts: CatalogProduct[] = [
  {
    spuId: 1001,
    name: 'iPhone 15 Pro 256GB',
    description: '支持移动联通电信5G',
    brand: 'Apple',
    mainImage: productImagesBySpuId[1001],
    status: 1,
    sales: 234,
    skus: [{ skuId: 9001, spec: '钛金属 / 256G', price: 7999, stock: 128 }],
  },
  {
    spuId: 1002,
    name: 'Apple MacBook Air 15.3英寸',
    description: 'M3芯片 8G 256G',
    brand: 'Apple',
    mainImage: productImagesBySpuId[1002],
    status: 1,
    sales: 567,
    skus: [{ skuId: 9003, spec: '午夜色 / 256G', price: 7999, stock: 96 }],
  },
  {
    spuId: 1003,
    name: '小米 14 Pro 16+512 黑色',
    description: '骁龙 8 Gen3，徕卡光学',
    brand: '小米',
    mainImage: productImagesBySpuId[1003],
    status: 1,
    sales: 890,
    skus: [{ skuId: 9004, spec: '黑色 / 512G', price: 5499, stock: 140 }],
  },
  {
    spuId: 1004,
    name: '华为 Mate 60 Pro 12+512',
    description: '麒麟 9000S，卫星通话',
    brand: '华为',
    mainImage: productImagesBySpuId[1004],
    status: 1,
    sales: 1500,
    skus: [{ skuId: 9006, spec: '雅川青 / 512G', price: 6999, stock: 88 }],
  },
  {
    spuId: 1005,
    name: 'Apple iPad Pro 11英寸',
    description: 'M4芯片 256G WLAN版',
    brand: 'Apple',
    mainImage: productImagesBySpuId[1005],
    status: 1,
    sales: 345,
    skus: [{ skuId: 9007, spec: '深空灰色 / 256G', price: 8999, stock: 64 }],
  },
  {
    spuId: 1006,
    name: 'Apple Watch Ultra 2',
    description: 'GPS+蜂窝 钛金属表壳',
    brand: 'Apple',
    mainImage: productImagesBySpuId[1006],
    status: 1,
    sales: 120,
    skus: [{ skuId: 9008, spec: '原色 / 海洋表带', price: 6499, stock: 45 }],
  },
];

export const seckillProducts = [
  { spuId: 2001, name: '小米真无线降噪耳机', price: 199, oldPrice: 399, mainImage: 'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?auto=format&fit=crop&w=600&q=80' },
  { spuId: 2002, name: 'YSL小金条口红', price: 129, oldPrice: 310, mainImage: 'https://images.unsplash.com/photo-1586495777744-4413f21062fa?auto=format&fit=crop&w=600&q=80' },
  { spuId: 2003, name: '维达超韧抽纸 3层', price: 39.9, oldPrice: 79.9, mainImage: 'https://images.unsplash.com/photo-1584473457406-6240486414e9?auto=format&fit=crop&w=600&q=80' },
  { spuId: 2004, name: '飞利浦电动牙刷', price: 269, oldPrice: 599, mainImage: 'https://images.unsplash.com/photo-1606220838315-056192d5e927?auto=format&fit=crop&w=600&q=80' },
];

function readNumber(value: unknown, keys: string[]) {
  if (!value || typeof value !== 'object') return 0;
  const record = value as UnknownRecord;
  for (const key of keys) {
    const item = Number(record[key]);
    if (Number.isFinite(item) && item > 0) return item;
  }
  return 0;
}

function readString(value: unknown, keys: string[]) {
  if (!value || typeof value !== 'object') return '';
  const record = value as UnknownRecord;
  for (const key of keys) {
    const item = String(record[key] || '');
    if (item) return item;
  }
  return '';
}

export function onlineProductImage(product: unknown) {
  const image = readString(product, ['mainImage', 'image', 'skuImage']);
  const spuId = readNumber(product, ['spuId', 'id']);
  if (image && !image.includes('picsum.photos')) return image;
  return productImagesBySpuId[spuId] || '';
}

/**
 * 统一商品图片解析器（真正的唯一入口）
 * 规则：
 * 1. 优先使用 mainImage / image / skuImage 字段
 * 2. 如果图片是 picsum.photos 或为空，且存在明确 spuId，则使用 productImagesBySpuId[spuId]
 * 3. 只读取明确的 spuId 字段，不允许用 id/skuId 兜底为 spuId
 * 4. 如果没有明确 spuId，就返回原图或空字符串，不强行映射
 * 5. 没有图时由 ProductImage 组件展示 placeholder
 */
export function resolveProductImage(product: unknown): string {
  const image = readString(product, ['mainImage', 'image', 'skuImage']);
  const spuId = readNumber(product, ['spuId']);

  if (image && !image.includes('picsum.photos')) {
    return image;
  }

  if (spuId && productImagesBySpuId[spuId]) {
    return productImagesBySpuId[spuId];
  }

  return image || '';
}
