import type { UnknownRecord } from '../api/types';



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
