<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { mallApi } from '../api/mall';
import PageState from '../components/PageState.vue';
import type { UnknownRecord } from '../api/types';
import { asList, field, money, orderStatusMap, productStatusMap, statusText } from '../utils/format';

const loading = ref(false);
const error = ref('');
const dashboard = ref<UnknownRecord | null>(null);
const ordersRaw = ref<UnknownRecord | null>(null);
const productsRaw = ref<UnknownRecord | null>(null);
const shipOrderNo = ref('');
const shipping = ref(false);

const orders = computed(() => asList(ordersRaw.value));
const products = computed(() => asList(productsRaw.value));

async function loadAdmin() {
  loading.value = true;
  error.value = '';
  try {
    const [dash, orderList, productList] = await Promise.all([
      mallApi.adminDashboard(),
      mallApi.adminOrders(),
      mallApi.adminProducts(),
    ]);
    dashboard.value = dash;
    ordersRaw.value = orderList;
    productsRaw.value = productList;
  } catch (err) {
    error.value = err instanceof Error ? err.message : '后台聚合接口暂不可用';
  } finally {
    loading.value = false;
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
        <el-table v-if="orders.length" :data="orders" class="stable-table">
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
        <el-empty v-else description="订单列表为空或后台服务未返回数据" />
        <el-form class="inline-form mt" label-position="top">
          <el-form-item label="发货订单号">
            <el-input v-model="shipOrderNo" placeholder="SO..." />
          </el-form-item>
          <el-button type="primary" :loading="shipping" :disabled="!shipOrderNo || shipping" @click="ship">发货</el-button>
        </el-form>
      </el-card>

      <el-card class="panel">
        <template #header>
          <div class="panel-title">后台商品</div>
        </template>
        <el-table v-if="products.length" :data="products" class="stable-table">
          <el-table-column prop="spuId" label="SPU" width="100" />
          <el-table-column prop="name" label="商品" min-width="160" />
          <el-table-column prop="brand" label="品牌" width="100" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">{{ statusText(row.status, productStatusMap) }}</template>
          </el-table-column>
          <el-table-column prop="sales" label="销量" width="90" />
        </el-table>
        <el-empty v-else description="商品列表为空或后台服务未返回数据" />
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
