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
  'https://images.unsplash.com/photo-1620916566398-39f1143ab7be', // Beauty
  'https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a', // Nike Shoes
  'https://images.unsplash.com/photo-1584568694244-14fbdf83bd30', // Fridge
  'https://images.unsplash.com/photo-1518843875459-f738682238a6', // Coffee Machine
];

const imageParams = '?auto=format&fit=crop&w=900&q=80';

export const productImagesBySpuId: Record<number, string> = {
  1001: `https://images.unsplash.com/photo-1511707171634-5f897ff02aa9${imageParams}`,
  1002: `https://images.unsplash.com/photo-1517336714731-489689fd1ca8${imageParams}`,
  1003: `https://images.unsplash.com/photo-1620916566398-39f1143ab7be${imageParams}`,
  1004: `https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a${imageParams}`,
  1005: `https://images.unsplash.com/photo-1584568694244-14fbdf83bd30${imageParams}`,
  1006: `https://images.unsplash.com/photo-1518843875459-f738682238a6${imageParams}`,
};

export const heroImage = new URL('../assets/hero/mallcloud-hero.png', import.meta.url).href;

export const seedCatalogProducts: CatalogProduct[] = [
  {
    spuId: 1001,
    name: 'iPhone 15 128GB',
    description: '支持移动联通电信5G',
    brand: 'Apple',
    mainImage: productImagesBySpuId[1001],
    status: 1,
    sales: 234,
    skus: [{ skuId: 9001, spec: '默认 / 128G', price: 5999, stock: 128 }],
  },
  {
    spuId: 1002,
    name: 'Apple MacBook Air 15.3英寸',
    description: 'M2芯片 8G 256G',
    brand: 'Apple',
    mainImage: productImagesBySpuId[1002],
    status: 1,
    sales: 567,
    skus: [{ skuId: 9003, spec: '午夜色 / 256G', price: 7999, stock: 96 }],
  },
  {
    spuId: 1003,
    name: 'SK-II 神仙水护肤套装',
    description: '经典礼盒，深度保湿修护',
    brand: 'SK-II',
    mainImage: productImagesBySpuId[1003],
    status: 1,
    sales: 890,
    skus: [{ skuId: 9004, spec: '经典版', price: 1540, stock: 140 }],
  },
  {
    spuId: 1004,
    name: "Nike Air Force 1 '07",
    description: '经典纯白 男女子款',
    brand: 'Nike',
    mainImage: productImagesBySpuId[1004],
    status: 1,
    sales: 1500,
    skus: [{ skuId: 9006, spec: '纯白 / 42码', price: 699, stock: 88 }],
  },
  {
    spuId: 1005,
    name: '小米米家冰箱 十字对开门',
    description: '485L 超薄嵌入',
    brand: '小米',
    mainImage: productImagesBySpuId[1005],
    status: 1,
    sales: 345,
    skus: [{ skuId: 9007, spec: '银色 / 485L', price: 2699, stock: 64 }],
  },
  {
    spuId: 1006,
    name: '德龙 (DeLonghi) 半自动',
    description: '意式咖啡机，家用专业级',
    brand: 'DeLonghi',
    mainImage: productImagesBySpuId[1006],
    status: 1,
    sales: 120,
    skus: [{ skuId: 9008, spec: '银色款', price: 1899, stock: 45 }],
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
