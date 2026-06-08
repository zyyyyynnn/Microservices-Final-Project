import type { UnknownRecord } from '../api/types';

export function asRecord(value: unknown): UnknownRecord {
  return value && typeof value === 'object' ? (value as UnknownRecord) : {};
}

export function asList(value: unknown): UnknownRecord[] {
  if (Array.isArray(value)) return value as UnknownRecord[];
  const record = asRecord(value);
  const candidates = [record.records, record.list, record.items, record.content, record.products, record.rows];
  for (const candidate of candidates) {
    if (Array.isArray(candidate)) return candidate as UnknownRecord[];
  }
  return [];
}

export function field<T = unknown>(value: unknown, keys: string[], fallback?: T): T {
  const record = asRecord(value);
  for (const key of keys) {
    const item = record[key];
    if (item !== undefined && item !== null && item !== '') {
      return item as T;
    }
  }
  return fallback as T;
}

export function money(value: unknown) {
  const amount = Number(value || 0);
  return `¥${amount.toFixed(2)}`;
}

export function statusText(status: unknown, map: Record<number, string>, fallback = '待确认') {
  const key = Number(status);
  return Number.isFinite(key) ? map[key] || fallback : fallback;
}

export const orderStatusMap: Record<number, string> = {
  0: '待支付',
  1: '已支付',
  2: '已发货',
  3: '已完成',
  4: '已取消',
  5: '已退款',
};

export const payStatusMap: Record<number, string> = {
  0: '待支付',
  1: '支付成功',
  2: '支付失败',
};

export const productStatusMap: Record<number, string> = {
  0: '下架',
  1: '在售',
};

export const seckillStatusMap: Record<number, string> = {
  0: '待处理',
  1: '成功',
  2: '失败',
};

export function productImage(value: unknown) {
  return String(field(value, ['mainImage', 'image', 'skuImage'], ''));
}

export function productName(value: unknown) {
  return String(field(value, ['name', 'title', 'spuName', 'skuName'], '未命名商品'));
}

export function firstSku(product: unknown) {
  const skus = field<UnknownRecord[]>(product, ['skus'], []);
  return Array.isArray(skus) && skus.length ? skus[0] : null;
}
