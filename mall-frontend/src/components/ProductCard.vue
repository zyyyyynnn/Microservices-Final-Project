<script setup lang="ts">
import { computed } from 'vue';
import { RouterLink } from 'vue-router';
import type { UnknownRecord } from '../api/types';
import { field, firstSku, money, productImage, productName, statusText, productStatusMap } from '../utils/format';

const props = defineProps<{
  product: UnknownRecord;
}>();

const sku = computed(() => firstSku(props.product));
const spuId = computed(() => field<number>(props.product, ['spuId', 'id'], 0));
const price = computed(() => field(sku.value || props.product, ['price', 'minPrice'], 0));
const stock = computed(() => field(sku.value || props.product, ['stock', 'available'], null));
const status = computed(() => statusText(field(props.product, ['status'], 1), productStatusMap));
</script>

<template>
  <article class="product-card">
    <div class="product-image">
      <img v-if="productImage(product)" :src="productImage(product)" :alt="productName(product)" />
      <span v-else>No Image</span>
    </div>
    <div class="product-body">
      <div class="product-meta">
        <el-tag size="small" effect="plain" round>{{ status }}</el-tag>
        <span>Sales: {{ field(product, ['sales'], 0) }}</span>
      </div>
      <h2>{{ productName(product) }}</h2>
      <p>{{ field(product, ['description', 'brand'], 'Featured Product') }}</p>
      <div class="product-bottom">
        <strong>{{ money(price) }}</strong>
        <span v-if="stock !== null">Stock: {{ stock }}</span>
        <span v-else>Stock TBA</span>
      </div>
      <RouterLink :to="`/products/${spuId || 1001}`" style="margin-top: 12px; display: block; width: 100%;">
        <el-button type="primary" round style="width: 100%;">View Details</el-button>
      </RouterLink>
    </div>
  </article>
</template>
