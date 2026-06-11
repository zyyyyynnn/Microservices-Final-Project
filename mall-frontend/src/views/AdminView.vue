<script setup lang="ts">
import { notifyError } from '../utils/notify';
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { mallApi } from '../api/mall';
import PageState from '../components/PageState.vue';
import type { UnknownRecord } from '../api/types';
import { asList, field, money, orderStatusMap, productStatusMap, statusText } from '../utils/format';
import ProductImage from '../components/ProductImage.vue';
import { resolveProductImage } from '../catalog/productAssets';

const error = ref('');
const dashboardLoading = ref(false);
const ordersLoading = ref(false);
const productsLoading = ref(false);
const dashboardError = ref('');
const ordersError = ref('');
const productsError = ref('');
const dashboard = ref<UnknownRecord | null>(null);
const ordersRaw = ref<UnknownRecord | null>(null);
const productsRaw = ref<UnknownRecord | null>(null);
const shipOrderNo = ref('');
const shipping = ref(false);

const orders = computed(() => asList(ordersRaw.value));
const products = computed(() => asList(productsRaw.value));
const loading = computed(() => dashboardLoading.value || ordersLoading.value || productsLoading.value);

function moneyText(value: unknown) {
  const amount = Number(value || 0);
  return amount > 0 ? money(amount) : '—';
}

function adminProductImage(row: UnknownRecord) {
  return String(
    field(row, ['mainImage', 'image', 'spuImage'], '')
    || resolveProductImage({ spuId: Number(field(row, ['spuId'], 0)) })
    || ''
  );
}

function adminProductName(row: UnknownRecord) {
  return String(field(row, ['name', 'spuName', 'title'], '商品信息不完整'));
}

async function loadAdmin() {
  error.value = '';
  dashboardError.value = '';
  ordersError.value = '';
  productsError.value = '';
  dashboardLoading.value = true;
  ordersLoading.value = true;
  productsLoading.value = true;

  const dashboardTask = mallApi.adminDashboard()
    .then((value) => {
      dashboard.value = value;
    })
    .catch(() => {
      dashboard.value = null;
      dashboardError.value = '看板数据暂时无法加载';
    })
    .finally(() => {
      dashboardLoading.value = false;
    });

  const ordersTask = mallApi.adminOrders()
    .then((value) => {
      ordersRaw.value = value;
    })
    .catch(() => {
      ordersRaw.value = null;
      ordersError.value = '订单数据暂时无法加载';
    })
    .finally(() => {
      ordersLoading.value = false;
    });

  const productsTask = mallApi.adminProducts()
    .then((value) => {
      productsRaw.value = value;
    })
    .catch(() => {
      productsRaw.value = null;
      productsError.value = '商品数据暂时无法加载';
    })
    .finally(() => {
      productsLoading.value = false;
    });

  await Promise.allSettled([dashboardTask, ordersTask, productsTask]);

  if (dashboardError.value || ordersError.value || productsError.value) {
    notifyError('部分后台数据暂时无法加载，请稍后重试。');
  }
}

async function ship() {
  if (!shipOrderNo.value) return;
  shipping.value = true;
  try {
    await mallApi.shipOrder(shipOrderNo.value);
    ElMessage.success('发货请求已提交');
    await loadAdmin();
  } catch (err) {
    notifyError(err instanceof Error ? err.message : '发货失败');
  } finally {
    shipping.value = false;
  }
}

onMounted(loadAdmin);
</script>

<template>
  <section class="commerce-layout">
    <PageState :loading="loading" :error="''" @retry="loadAdmin" />
    <el-alert v-if="dashboardError" :title="dashboardError" type="warning" :closable="false" />
    <div class="stats-grid">
      <el-card class="metric-card">
        <span>订单数</span>
        <strong>{{ field(dashboard, ['orderCount', 'orders'], orders.length) }}</strong>
      </el-card>
      <el-card class="metric-card">
        <span>商品数</span>
        <strong>{{ field(dashboard, ['productCount', 'products'], products.length) }}</strong>
      </el-card>
      <el-card class="metric-card">
        <span>销售额</span>
        <strong>{{ moneyText(field(dashboard, ['salesAmount', 'totalSales'], 0)) }}</strong>
      </el-card>
    </div>

    <div class="page-grid two">
      <el-card class="panel wide-panel">
        <template #header>
          <div class="panel-title">后台订单</div>
        </template>
        <PageState
          :loading="ordersLoading"
          :error="''"
          :empty="!ordersLoading && !ordersError && orders.length === 0"
          empty-title="订单列表为空"
          empty-description="当前暂无订单记录。"
          @retry="loadAdmin"
        />
        <div v-if="!ordersError && orders.length" class="table-scroll">
        <el-table :data="orders" class="stable-table">
          <el-table-column label="订单号" min-width="180">
            <template #default="{ row }">{{ field(row, ['orderNo'], '—') }}</template>
          </el-table-column>
          <el-table-column label="用户" width="100">
            <template #default="{ row }">{{ field(row, ['userId', 'username'], '—') }}</template>
          </el-table-column>
          <el-table-column label="金额" width="120">
            <template #default="{ row }">{{ moneyText(field(row, ['payAmount', 'totalAmount'], 0)) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="{ row }">{{ statusText(field(row, ['status'], null), orderStatusMap, '—') }}</template>
          </el-table-column>
          <el-table-column label="创建时间" min-width="170">
            <template #default="{ row }">{{ field(row, ['gmtCreate', 'createTime'], '—') }}</template>
          </el-table-column>
        </el-table>
        </div>
        <div class="shipping-block">
          <div class="shipping-title">订单发货操作</div>
          <div class="ship-query">
            <label class="ship-query-label">发货订单号</label>
            <div class="ship-query-row">
              <el-input v-model="shipOrderNo" />
              <el-button type="primary" :loading="shipping" :disabled="!shipOrderNo || shipping" @click="ship">
                确认发货
              </el-button>
            </div>
          </div>
        </div>
      </el-card>

      <el-card class="panel">
        <template #header>
          <div class="panel-title">后台商品</div>
        </template>
        <PageState
          :loading="productsLoading"
          :error="''"
          :empty="!productsLoading && !productsError && products.length === 0"
          empty-title="商品列表为空"
          empty-description="当前暂无商品记录。"
          @retry="loadAdmin"
        />
        <div v-if="!productsError && products.length" class="table-scroll">
        <el-table :data="products" class="stable-table">
          <el-table-column label="商品" min-width="220">
            <template #default="{ row }">
              <div class="line-item">
                <div class="thumb">
                  <ProductImage :src="adminProductImage(row)" :alt="adminProductName(row)" />
                </div>
                <div class="item-info">
                  <strong>{{ adminProductName(row) }}</strong>
                  <span>SPU {{ field(row, ['spuId'], '—') }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="brand" label="品牌" width="100" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">{{ statusText(row.status, productStatusMap) }}</template>
          </el-table-column>
          <el-table-column prop="sales" label="销量" width="90" />
        </el-table>
        </div>
      </el-card>
    </div>
  </section>
</template>

<style scoped>
.metric-card {
  text-align: center;
  border-top: 4px solid var(--color-brand);
}
.metric-card span {
  display: block;
  font-size: var(--font-sm);
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-sm);
}
.metric-card strong {
  font-size: 36px;
  color: var(--color-text-primary);
  line-height: 1;
}
.shipping-block {
  margin-top: var(--spacing-lg);
  padding: var(--spacing-md);
  background: var(--color-surface-hover);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
}
.shipping-title {
  font-size: var(--font-sm);
  font-weight: var(--weight-bold);
  margin-bottom: var(--spacing-sm);
}
</style>
