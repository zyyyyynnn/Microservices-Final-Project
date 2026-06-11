<script setup lang="ts">
import { notifyError } from '../utils/notify';
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { mallApi } from '../api/mall';
import PageState from '../components/PageState.vue';
import type { UnknownRecord } from '../api/types';
import { field, money, productImage, productName, productStatusMap, skuList, statusText, formatSkuSpec } from '../utils/format';
import { onlineProductImage } from '../catalog/productAssets';
import ProductImage from '../components/ProductImage.vue';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const error = ref('');
const product = ref<UnknownRecord | null>(null);
const selectedSkuId = ref<number | null>(null);
const quantity = ref(1);
const adding = ref(false);

const remoteStock = ref<number | null>(null);
const fetchingStock = ref(false);

const skus = computed(() => skuList(product.value));
const selectedSku = computed(() => skus.value.find((sku) => Number(field(sku, ['skuId'])) === selectedSkuId.value) || skus.value[0]);
const stock = computed(() => remoteStock.value !== null ? remoteStock.value : Number(field(selectedSku.value, ['stock'], 0)));
const disabled = computed(() => !selectedSku.value || stock.value <= 0 || adding.value || fetchingStock.value);

watch(selectedSkuId, async (newId) => {
  if (!newId) {
    remoteStock.value = null;
    return;
  }
  fetchingStock.value = true;
  try {
    const data = await mallApi.inventoryStock(newId);
    remoteStock.value = Number(field(data, ['available'], 0));
  } catch (err) {
    remoteStock.value = 0;
  } finally {
    fetchingStock.value = false;
  }
});
const image = computed(() => onlineProductImage(product.value) || productImage(product.value));

async function loadProduct() {
  loading.value = true;
  error.value = '';
  try {
    const id = Number(route.params.id);
    if (!id) {
      throw new Error('商品 ID 缺失');
    }
    const data = await mallApi.product(id);
    product.value = data;
    const dataSkus = skuList(data);
    selectedSkuId.value = dataSkus.length ? Number(field(dataSkus[0], ['skuId'], null)) : null;
  } catch (err) {
    product.value = null;
    notifyError(err instanceof Error ? err.message : '商品详情加载失败');
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
    <PageState :loading="loading" :error="''" @retry="loadProduct" />

    <div v-if="product && !loading" class="detail-layout">
      <div class="detail-media">
        <ProductImage :src="image" :alt="productName(product)" />
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
            @click="selectedSkuId = Number(field(sku, ['skuId']))"
          >
            <strong>{{ formatSkuSpec(sku) }}</strong>
            <span>SKU {{ field(sku, ['skuId']) }} <template v-if="Number(field(sku, ['skuId'])) === selectedSkuId">/ 库存 {{ remoteStock !== null ? remoteStock : '...' }}</template></span>
          </button>
        </div>

        <div class="purchase-row">
          <el-input-number v-model="quantity" :min="1" :max="Math.max(stock, 1)" aria-label="购买数量" />
          <el-button type="primary" :loading="adding" :disabled="disabled" @click="addCart">加入购物车</el-button>
          <el-button plain :disabled="disabled" @click="checkoutNow">立即结算</el-button>
        </div>
        <el-alert
          v-if="skus.length === 0"
          title="SKU 数据待联调，加入购物车和立即结算已禁用。"
          type="warning"
          :closable="false"
          class="mt"
        />
        <el-alert
          v-else-if="fetchingStock"
          title="正在获取实时库存..."
          type="info"
          :closable="false"
          class="mt"
        />
        <el-alert
          v-else-if="stock <= 0"
          title="当前 SKU 库存不足，购买操作已禁用。"
          type="warning"
          :closable="false"
          class="mt"
        />
      </el-card>
    </div>
  </section>
</template>
