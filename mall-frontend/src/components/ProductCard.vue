<script setup lang="ts">
import { computed } from 'vue';
import { RouterLink } from 'vue-router';
import type { UnknownRecord } from '../api/types';
import { field, firstSku, money, productName, statusText, productStatusMap } from '../utils/format';
import { resolveProductImage } from '../catalog/productAssets';
import ProductImage from './ProductImage.vue';

const props = defineProps<{
  product: UnknownRecord;
}>();

const sku = computed(() => firstSku(props.product));
const spuId = computed(() => field<number>(props.product, ['spuId', 'id'], 0));
const hasValidSpuId = computed(() => Number(spuId.value) > 0);
const price = computed(() => field(sku.value || props.product, ['price', 'minPrice'], 0));
const stock = computed(() => field(sku.value || props.product, ['stock', 'available'], null));
const status = computed(() => statusText(field(props.product, ['status'], 1), productStatusMap));
const image = computed(() => resolveProductImage(props.product));
</script>

<template>
  <article class="product-card">
    <div class="product-image">
      <ProductImage :src="image" :alt="productName(product)" />
    </div>
    <div class="product-body">
      <div class="product-meta">
        <el-tag size="small" effect="plain" round>{{ status }}</el-tag>
        <span>销量 {{ field(product, ['sales'], 0) }}</span>
      </div>
      <h2>{{ productName(product) }}</h2>
      <p>{{ field(product, ['description', 'brand'], '暂无商品描述') }}</p>
      <div class="product-bottom">
        <strong>{{ money(price) }}</strong>
        <span v-if="stock !== null">库存 {{ stock }}</span>
      </div>
      <RouterLink v-if="hasValidSpuId" :to="`/products/${spuId}`" class="product-card-link">
        <el-button type="primary" round class="full-button">查看详情</el-button>
      </RouterLink>
      <el-button v-else disabled round class="full-button">商品信息不完整</el-button>
    </div>
  </article>
</template>
