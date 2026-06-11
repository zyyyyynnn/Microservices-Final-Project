<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { mallApi } from '../api/mall';
import PageState from '../components/PageState.vue';
import type { UnknownRecord } from '../api/types';
import { asList, field, money, orderStatusMap, productStatusMap, statusText } from '../utils/format';

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
      dashboardError.value = '看板接口暂不可用';
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
      ordersError.value = '订单列表暂不可用';
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
      productsError.value = '商品列表暂不可用';
    })
    .finally(() => {
      productsLoading.value = false;
    });

  await Promise.allSettled([dashboardTask, ordersTask, productsTask]);

  if (dashboardError.value || ordersError.value || productsError.value) {
    error.value = '部分后台接口暂不可用，请查看各区域提示。';
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
    error.value = err instanceof Error ? err.message : '发货失败';
  } finally {
    shipping.value = false;
  }
}

onMounted(loadAdmin);
</script>

<template>
  <section class="commerce-layout">
    <PageState :loading="loading" :error="error" @retry="loadAdmin" />
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
        <strong>{{ money(field(dashboard, ['salesAmount', 'totalSales'], 0)) }}</strong>
      </el-card>
    </div>

    <div class="page-grid two">
      <el-card class="panel wide-panel">
        <template #header>
          <div class="panel-title">后台订单</div>
        </template>
        <PageState
          :loading="ordersLoading"
          :error="ordersError"
          :empty="!ordersLoading && !ordersError && orders.length === 0"
          empty-title="订单列表为空"
          empty-description="后台订单接口当前未返回订单。"
          @retry="loadAdmin"
        />
        <div v-if="!ordersError && orders.length" class="table-scroll">
        <el-table :data="orders" class="stable-table">
          <el-table-column prop="orderNo" label="订单号" min-width="180" />
          <el-table-column prop="userId" label="用户" width="100" />
          <el-table-column label="金额" width="120">
            <template #default="{ row }">{{ money(row.payAmount) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="{ row }">{{ statusText(row.status, orderStatusMap) }}</template>
          </el-table-column>
          <el-table-column prop="gmtCreate" label="创建时间" min-width="170" />
        </el-table>
        </div>
        <div class="shipping-block">
          <div class="shipping-title">订单发货操作</div>
          <el-form class="inline-form" label-position="top">
            <el-form-item label="发货订单号">
              <el-input v-model="shipOrderNo" placeholder="输入 SO 开头的订单号" style="width: 200px" />
            </el-form-item>
            <el-button type="primary" :loading="shipping" :disabled="!shipOrderNo || shipping" @click="ship" style="margin-top: 14px">确认发货</el-button>
          </el-form>
        </div>
      </el-card>

      <el-card class="panel">
        <template #header>
          <div class="panel-title">后台商品</div>
        </template>
        <PageState
          :loading="productsLoading"
          :error="productsError"
          :empty="!productsLoading && !productsError && products.length === 0"
          empty-title="商品列表为空"
          empty-description="后台商品接口当前未返回商品。"
          @retry="loadAdmin"
        />
        <div v-if="!productsError && products.length" class="table-scroll">
        <el-table :data="products" class="stable-table">
          <el-table-column prop="spuId" label="SPU" width="100" />
          <el-table-column prop="name" label="商品" min-width="160" />
          <el-table-column prop="brand" label="品牌" width="100" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">{{ statusText(row.status, productStatusMap) }}</template>
          </el-table-column>
          <el-table-column prop="sales" label="销量" width="90" />
        </el-table>
        </div>
        <el-alert
          class="mt"
          title="新增、编辑、删除、上下架外部接口未在 AdminController 中确认，当前只展示状态说明，不伪造操作成功。"
          type="warning"
          :closable="false"
        />
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
