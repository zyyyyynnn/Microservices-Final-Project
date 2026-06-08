<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import { mallApi } from '../api/mall';
import PageState from '../components/PageState.vue';
import type { UnknownRecord } from '../api/types';
import { asList, field, money, orderStatusMap, statusText } from '../utils/format';

const route = useRoute();
const loading = ref(false);
const error = ref('');
const order = ref<UnknownRecord | null>(null);
const orderNo = computed(() => String(route.params.orderNo || ''));
const items = computed(() => asList(field(order.value, ['items', 'orderItems'], [])));
const status = computed(() => statusText(field(order.value, ['status'], null), orderStatusMap));

async function load() {
  loading.value = true;
  error.value = '';
  try {
    order.value = await mallApi.order(orderNo.value);
  } catch (err) {
    order.value = null;
    error.value = err instanceof Error ? err.message : '订单详情加载失败';
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <section class="page-grid two">
    <el-card class="panel wide-panel">
      <template #header>
        <div class="panel-title">订单详情</div>
      </template>
      <PageState :loading="loading" :error="error" @retry="load" />
      <div v-if="order && !loading" class="order-header">
        <div>
          <span class="hint">订单号</span>
          <h1>{{ orderNo }}</h1>
        </div>
        <el-tag effect="plain">{{ status }}</el-tag>
      </div>
      <el-descriptions v-if="order && !loading" border :column="2" class="mb">
        <el-descriptions-item label="实付金额">{{ money(field(order, ['payAmount', 'totalAmount'], 0)) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ field(order, ['gmtCreate', 'createTime'], '待联调') }}</el-descriptions-item>
        <el-descriptions-item label="收货信息">{{ field(order, ['addressJson'], '待联调') }}</el-descriptions-item>
        <el-descriptions-item label="支付截止">{{ field(order, ['payDeadline'], '待联调') }}</el-descriptions-item>
      </el-descriptions>
      <el-table v-if="items.length" :data="items" class="stable-table">
        <el-table-column prop="skuName" label="商品" min-width="220" />
        <el-table-column prop="skuId" label="SKU" width="120" />
        <el-table-column label="单价" width="120">
          <template #default="{ row }">{{ money(row.price) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column label="小计" width="120">
          <template #default="{ row }">{{ money(row.subtotal) }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-if="order && !items.length" description="订单项字段未返回，待后端联调确认" />
    </el-card>

    <el-card class="panel">
      <template #header>
        <div class="panel-title">后续操作</div>
      </template>
      <RouterLink :to="`/pay/${orderNo}`">
        <el-button type="primary" class="full-button" :disabled="!order">去支付</el-button>
      </RouterLink>
      <RouterLink to="/cart">
        <el-button plain class="full-button mt">返回购物车</el-button>
      </RouterLink>
      <el-alert class="mt" title="取消、退款、确认收货接口未在当前外部 Controller 中确认，本页不展示伪操作。" type="info" :closable="false" />
    </el-card>
  </section>
</template>
