<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { mallApi } from '../api/mall';
import PageState from '../components/PageState.vue';
import type { UnknownRecord } from '../api/types';
import { field, money, payStatusMap, statusText } from '../utils/format';

const route = useRoute();
const orderNo = String(route.params.orderNo || '');
const loading = ref(false);
const error = ref('');
const payRecord = ref<UnknownRecord | null>(null);
const payCreate = ref<unknown>(null);
const notifyResult = ref('');
const submitting = ref(false);

async function loadRecord() {
  loading.value = true;
  error.value = '';
  try {
    payRecord.value = await mallApi.payRecord(orderNo);
  } catch (err) {
    payRecord.value = null;
    error.value = err instanceof Error ? err.message : '支付记录加载失败';
  } finally {
    loading.value = false;
  }
}

async function createPay() {
  submitting.value = true;
  try {
    payCreate.value = await mallApi.createPay(orderNo);
    ElMessage.success('支付记录已创建');
    await loadRecord();
  } catch (err) {
    error.value = err instanceof Error ? err.message : '创建支付记录失败';
  } finally {
    submitting.value = false;
  }
}

async function notifyPay() {
  submitting.value = true;
  try {
    notifyResult.value = await mallApi.notifyPay(orderNo);
    ElMessage.success(`通知结果：${notifyResult.value}`);
    await loadRecord();
  } catch (err) {
    error.value = err instanceof Error ? err.message : '支付通知失败';
  } finally {
    submitting.value = false;
  }
}

onMounted(loadRecord);
</script>

<template>
  <section class="page-grid two">
    <el-card class="panel">
      <template #header>
        <div class="panel-title">模拟支付</div>
      </template>
      <PageState :loading="loading" :error="error" @retry="loadRecord" />
      <el-descriptions border :column="1">
        <el-descriptions-item label="订单号">{{ orderNo }}</el-descriptions-item>
        <el-descriptions-item label="支付单号">{{ field(payRecord, ['payNo'], field(payCreate, ['payNo'], '待创建')) }}</el-descriptions-item>
        <el-descriptions-item label="支付渠道">{{ field(payRecord, ['payChannel'], 'ALIPAY') }}</el-descriptions-item>
        <el-descriptions-item label="支付金额">{{ money(field(payRecord, ['payAmount'], 0)) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          {{ statusText(field(payRecord, ['status'], 0), payStatusMap) }}
        </el-descriptions-item>
      </el-descriptions>
      <div class="button-row mt">
        <el-button type="primary" :loading="submitting" :disabled="submitting" @click="createPay">创建支付记录</el-button>
        <el-button plain :loading="submitting" :disabled="submitting" @click="notifyPay">发送成功通知</el-button>
      </div>
      <el-alert
        v-if="notifyResult"
        class="mt"
        :title="`支付通知返回：${notifyResult}`"
        type="success"
        :closable="false"
      />
    </el-card>

    <el-card class="panel">
      <template #header>
        <div class="panel-title">支付说明</div>
      </template>
      <ol class="step-list">
        <li>创建本地模拟支付记录。</li>
        <li>发送 `TRADE_SUCCESS` 通知到 Gateway 白名单接口。</li>
        <li>mall-pay 投递 PAY_RESULT，mall-message 更新订单和库存。</li>
        <li>回到订单详情查询最终状态。</li>
      </ol>
      <RouterLink :to="`/orders/${orderNo}`">
        <el-button type="primary" class="full-button">查询订单状态</el-button>
      </RouterLink>
    </el-card>
  </section>
</template>
