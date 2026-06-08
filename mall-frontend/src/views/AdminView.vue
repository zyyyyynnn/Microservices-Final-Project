<script setup lang="ts">
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { mallApi } from '../api/mall';
import type { UnknownRecord } from '../api/types';

const dashboard = ref<UnknownRecord | null>(null);
const orders = ref<UnknownRecord | null>(null);
const products = ref<UnknownRecord | null>(null);
const shipOrderNo = ref('');

function display(value: unknown) {
  return JSON.stringify(value, null, 2);
}

async function loadDashboard() {
  dashboard.value = await mallApi.adminDashboard();
}

async function loadOrders() {
  orders.value = await mallApi.adminOrders();
}

async function loadProducts() {
  products.value = await mallApi.adminProducts();
}

async function ship() {
  await mallApi.shipOrder(shipOrderNo.value);
  ElMessage.success('发货请求已提交');
}
</script>

<template>
  <section class="page-grid two">
    <el-card class="panel">
      <template #header>
        <div class="panel-title">后台看板与订单</div>
      </template>
      <div class="button-row">
        <el-button type="primary" @click="loadDashboard">加载看板</el-button>
        <el-button plain @click="loadOrders">订单列表</el-button>
      </div>
      <el-form class="inline-form" label-position="top">
        <el-form-item label="发货订单号">
          <el-input v-model="shipOrderNo" placeholder="SO..." />
        </el-form-item>
        <el-button plain :disabled="!shipOrderNo" @click="ship">发货</el-button>
      </el-form>
      <pre class="json-box compact">{{ display(dashboard) }}</pre>
      <pre class="json-box compact">{{ display(orders) }}</pre>
    </el-card>

    <el-card class="panel">
      <template #header>
        <div class="panel-title">后台商品</div>
      </template>
      <el-button type="primary" @click="loadProducts">商品列表</el-button>
      <el-alert
        class="mt"
        title="商品新增、编辑、上下架接口未在 AdminController 中确认，本页不伪造这些写操作。"
        type="warning"
        :closable="false"
      />
      <pre class="json-box">{{ display(products) }}</pre>
    </el-card>
  </section>
</template>
