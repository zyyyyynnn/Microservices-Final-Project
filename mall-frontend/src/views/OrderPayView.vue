<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { mallApi } from '../api/mall';
import type { UnknownRecord } from '../api/types';

const route = useRoute();
const loading = ref(false);
const order = ref<unknown>(null);
const pay = ref<unknown>(null);
const record = ref<UnknownRecord | null>(null);

const form = reactive({
  orderNo: String(route.query.orderNo || ''),
  skuId: 9001,
  quantity: 1,
  addressId: 1,
});

function display(value: unknown) {
  return JSON.stringify(value, null, 2);
}

async function queryOrder() {
  if (!form.orderNo) return;
  loading.value = true;
  try {
    order.value = await mallApi.order(form.orderNo);
  } finally {
    loading.value = false;
  }
}

async function directCreateOrder() {
  const result = await mallApi.createOrder({
    addressId: form.addressId,
    items: [{ skuId: form.skuId, quantity: form.quantity }],
    remark: 'frontend-direct-order',
  });
  form.orderNo = result.orderNo;
  order.value = result;
  ElMessage.success(`订单已创建：${result.orderNo}`);
}

async function createPay() {
  pay.value = await mallApi.createPay(form.orderNo);
  ElMessage.success('支付记录已创建');
}

async function notifyPay() {
  pay.value = await mallApi.notifyPay(form.orderNo);
  ElMessage.success(`通知结果：${pay.value}`);
}

async function queryPayRecord() {
  record.value = await mallApi.payRecord(form.orderNo);
}

onMounted(queryOrder);
</script>

<template>
  <section class="page-grid two">
    <el-card class="panel">
      <template #header>
        <div class="panel-title">订单</div>
      </template>
      <el-form class="inline-form" label-position="top">
        <el-form-item label="订单号">
          <el-input v-model="form.orderNo" placeholder="SO..." />
        </el-form-item>
        <el-button plain :loading="loading" :disabled="!form.orderNo" @click="queryOrder">查询订单</el-button>
      </el-form>
      <el-divider />
      <el-form class="inline-form" label-position="top">
        <el-form-item label="SKU">
          <el-input-number v-model="form.skuId" :min="1" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="form.quantity" :min="1" />
        </el-form-item>
        <el-form-item label="地址 ID">
          <el-input-number v-model="form.addressId" :min="1" />
        </el-form-item>
        <el-button type="primary" @click="directCreateOrder">直接创建订单</el-button>
      </el-form>
      <pre class="json-box">{{ display(order) }}</pre>
    </el-card>

    <el-card class="panel">
      <template #header>
        <div class="panel-title">模拟支付</div>
      </template>
      <div class="button-row">
        <el-button type="primary" :disabled="!form.orderNo" @click="createPay">创建支付记录</el-button>
        <el-button plain :disabled="!form.orderNo" @click="notifyPay">发送成功通知</el-button>
        <el-button plain :disabled="!form.orderNo" @click="queryPayRecord">查询支付记录</el-button>
      </div>
      <p class="hint">通知使用 out_trade_no / trade_no / trade_status=TRADE_SUCCESS，经 Gateway 进入 mall-pay。</p>
      <pre class="json-box compact">{{ display(pay) }}</pre>
      <pre class="json-box compact">{{ display(record) }}</pre>
    </el-card>
  </section>
</template>
