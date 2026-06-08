<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { mallApi } from '../api/mall';
import PageState from '../components/PageState.vue';
import type { UnknownRecord } from '../api/types';
import { field, money, productImage, productName, productStatusMap, statusText } from '../utils/format';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const error = ref('');
const product = ref<UnknownRecord | null>(null);
const selectedSkuId = ref<number | null>(null);
const quantity = ref(1);
const adding = ref(false);

const skus = computed(() => {
  const items = product.value?.skus;
  return Array.isArray(items) ? (items as UnknownRecord[]) : [];
});
const selectedSku = computed(() => skus.value.find((sku) => Number(field(sku, ['skuId'])) === selectedSkuId.value) || skus.value[0]);
const stock = computed(() => Number(field(selectedSku.value, ['stock'], 0)));
const disabled = computed(() => !selectedSku.value || stock.value <= 0 || adding.value);

async function loadProduct() {
  loading.value = true;
  error.value = '';
  try {
    const data = await mallApi.product(Number(route.params.id || 1001));
    product.value = data;
    const dataSkus = Array.isArray(data.skus) ? (data.skus as UnknownRecord[]) : [];
    selectedSkuId.value = Number(field(dataSkus[0], ['skuId'], null));
  } catch (err) {
    product.value = null;
    error.value = err instanceof Error ? err.message : '商品详情加载失败';
  } finally {
    loading.value = false;
  }
}

async function addCart() {
  if (!selectedSku.value) return;
  adding.value = true;
  try {
    await mallApi.addCart({ skuId: Number(field(selectedSku.value, ['skuId'])), quantity: quantity.value });
    ElMessage.success('已加入购物车');
  } finally {
    adding.value = false;
  }
}

function checkoutNow() {
  if (!selectedSku.value) return;
  router.push({
    path: '/checkout',
    query: {
      skuId: String(field(selectedSku.value, ['skuId'])),
      quantity: String(quantity.value),
    },
  });
}

onMounted(loadProduct);
</script>

<template>
  <section>
    <PageState :loading="loading" :error="error" @retry="loadProduct" />

    <div v-if="product && !loading" class="detail-layout">
      <div class="detail-media">
        <img v-if="productImage(product)" :src="productImage(product)" :alt="productName(product)" />
        <span v-else>商品图片待联调</span>
      </div>
      <el-card class="panel detail-main">
        <el-tag effect="plain">
          {{ statusText(field(product, ['status'], 1), productStatusMap) }}
        </el-tag>
        <h1>{{ productName(product) }}</h1>
        <p>{{ field(product, ['description'], '商品描述由商品服务返回，当前为空。') }}</p>
        <div class="price-line">{{ money(field(selectedSku, ['price'], 0)) }}</div>

        <div class="sku-list">
          <button
            v-for="sku in skus"
            :key="String(field(sku, ['skuId']))"
            class="sku-option"
            :class="{ active: Number(field(sku, ['skuId'])) === selectedSkuId }"
            :disabled="Number(field(sku, ['stock'], 0)) <= 0"
            @click="selectedSkuId = Number(field(sku, ['skuId']))"
          >
            <strong>{{ field(sku, ['spec'], '默认规格') }}</strong>
            <span>SKU {{ field(sku, ['skuId']) }} / 库存 {{ field(sku, ['stock'], 0) }}</span>
          </button>
        </div>

        <div class="purchase-row">
          <el-input-number v-model="quantity" :min="1" :max="Math.max(stock, 1)" aria-label="购买数量" />
          <el-button type="primary" :loading="adding" :disabled="disabled" @click="addCart">加入购物车</el-button>
          <el-button plain :disabled="disabled" @click="checkoutNow">立即结算</el-button>
        </div>
        <el-alert
          v-if="stock <= 0"
          title="当前 SKU 库存不足或库存字段未返回，购买操作已禁用。"
          type="warning"
          :closable="false"
          class="mt"
        />
      </el-card>
    </div>
  </section>
</template>
