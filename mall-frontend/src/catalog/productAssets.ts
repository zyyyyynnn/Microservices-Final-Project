import type { UnknownRecord } from '../api/types';




export const productImagesBySpuId: Record<number, string> = {
  1001: '/products/1001-iphone-15-pro.svg',
  1002: '/products/1002-iphone-15-pink.svg',
  1003: '/products/1003-mi-14-pro.svg',
  1004: '/products/1004-huawei-mate-60.svg',
  1005: '/products/1005-macbook-air.svg',
  1006: '/products/1006-logitech-mouse.svg',
  1007: '/products/1007-ysl-lipstick.svg',
  1008: '/products/1008-loreal-serum.svg',
  1009: '/products/1009-coffee-gift-box.svg',
  1010: '/products/1010-nuts-gift-box.svg',
  1011: '/products/1011-nike-running-shoes.svg',
  1012: '/products/1012-desk-lamp.svg',
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
