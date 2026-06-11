<script setup lang="ts">
import { computed } from 'vue';
import { RouterLink } from 'vue-router';
import type { UnknownRecord } from '../api/types';
import { field, firstSku, money, productImage, productName, statusText, productStatusMap } from '../utils/format';
import { onlineProductImage } from '../catalog/productAssets';
import ProductImage from './ProductImage.vue';

const props = defineProps<{
  product: UnknownRecord;
}>();

const sku = computed(() => firstSku(props.product));
const spuId = computed(() => field<number>(props.product, ['spuId', 'id'], 0));
const price = computed(() => field(sku.value || props.product, ['price', 'minPrice'], 0));
const stock = computed(() => field(sku.value || props.product, ['stock', 'available'], null));
const status = computed(() => statusText(field(props.product, ['status'], 1), productStatusMap));
const image = computed(() => onlineProductImage(props.product) || productImage(props.product));
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
      <p>{{ field(product, ['description', 'brand'], '精选商品') }}</p>
      <div class="product-bottom">
        <strong>{{ money(price) }}</strong>
        <span v-if="stock !== null">库存 {{ stock }}</span>
        <span v-else>库存待联调</span>
      </div>
      <RouterLink :to="`/products/${spuId || 1001}`" class="product-card-link">
        <el-button type="primary" round class="full-button">查看详情</el-button>
      </RouterLink>
    </div>
  </article>
</template>
