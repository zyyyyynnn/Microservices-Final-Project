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
      <span v-else>商品</span>
    </div>
    <div class="product-body">
      <div class="product-meta">
        <el-tag size="small" effect="plain">{{ status }}</el-tag>
        <span>销量 {{ field(product, ['sales'], 0) }}</span>
      </div>
      <h2>{{ productName(product) }}</h2>
      <p>{{ field(product, ['description', 'brand'], '课程演示商品') }}</p>
      <div class="product-bottom">
        <strong>{{ money(price) }}</strong>
        <span v-if="stock !== null">库存 {{ stock }}</span>
        <span v-else>库存待联调</span>
      </div>
      <RouterLink :to="`/products/${spuId || 1001}`">
        <el-button type="primary">查看详情</el-button>
      </RouterLink>
    </div>
  </article>
</template>
