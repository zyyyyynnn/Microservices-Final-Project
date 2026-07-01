import type { UnknownRecord } from '../api/types';




export const productImagesBySpuId: Record<number, string> = {
  1001: 'https://images.unsplash.com/photo-1592286927505-1def25115558?w=600&h=600&fit=crop',
  1002: 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=600&h=600&fit=crop',
  1003: 'https://images.unsplash.com/photo-1598327105666-5b8934aff731?w=600&h=600&fit=crop',
  1004: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600&h=600&fit=crop',
  1005: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600&h=600&fit=crop',
  1006: 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=600&h=600&fit=crop',
  1007: 'https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=600&h=600&fit=crop',
  1008: 'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=600&h=600&fit=crop',
  1009: 'https://images.unsplash.com/photo-1559056199-641a0ac8b55e?w=600&h=600&fit=crop',
  1010: 'https://images.unsplash.com/photo-1599639668273-001de803c5d3?w=600&h=600&fit=crop',
  1011: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&h=600&fit=crop',
  1012: 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=600&h=600&fit=crop',
};



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
