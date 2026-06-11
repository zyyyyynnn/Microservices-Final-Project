<script setup lang="ts">
import { notifyError } from '../utils/notify';
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
    notifyError(err instanceof Error ? err.message : '支付记录加载失败');
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
    notifyError(err instanceof Error ? err.message : '创建支付记录失败');
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
    notifyError(err instanceof Error ? err.message : '支付通知失败');
  } finally {
    submitting.value = false;
  }
}

onMounted(loadRecord);
</script>

<template>
  <section class="commerce-layout">
    <div class="panel wide-panel">
      <div class="panel-title">支付收银台</div>
      
      <PageState :loading="loading" :error="''" @retry="loadRecord" />
      
      <!-- 成功态 -->
      <div v-if="notifyResult === 'success' || field(payRecord, ['status']) === 1" class="pay-result">
        <el-result icon="success" title="支付成功" sub-title="感谢您的购买，订单已支付完成">
          <template #extra>
            <RouterLink :to="`/orders/${orderNo}`">
              <el-button type="primary">查看订单明细</el-button>
            </RouterLink>
            <RouterLink to="/">
              <el-button plain>返回首页</el-button>
            </RouterLink>
          </template>
        </el-result>
      </div>

      <!-- 支付态 -->
      <div v-else-if="!loading && !error" class="pay-process">
        <div class="pay-amount-box">
          <span class="pay-label">待支付金额</span>
          <span class="pay-amount">{{ money(field(payRecord, ['payAmount'], 0)) }}</span>
        </div>
        
        <el-descriptions border :column="1" class="mt">
          <el-descriptions-item label="订单号">{{ orderNo }}</el-descriptions-item>
          <el-descriptions-item label="支付单号">{{ field(payRecord, ['payNo'], field(payCreate, ['payNo'], '待创建')) }}</el-descriptions-item>
          <el-descriptions-item label="支付渠道">{{ field(payRecord, ['payChannel'], 'ALIPAY') }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="field(payRecord, ['status']) === 0 ? 'warning' : 'info'" effect="plain">
              {{ statusText(field(payRecord, ['status'], 0), payStatusMap) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div class="pay-actions mt">
          <el-button type="primary" size="large" class="full-button" :loading="submitting" @click="createPay" v-if="!field(payRecord, ['payNo']) && !field(payCreate, ['payNo'])">
            创建支付记录
          </el-button>
          <el-button type="success" size="large" class="full-button" :loading="submitting" @click="notifyPay" v-else>
            模拟支付完成
          </el-button>
        </div>

        <div class="pay-hint mt">
          <p>模拟说明：本系统为开源演示，点击上方按钮将模拟支付渠道回调，发送 TRADE_SUCCESS 通知。</p>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.pay-result {
  padding: var(--spacing-xl) 0;
}
.pay-process {
  max-width: 500px;
  margin: 0 auto;
  padding: var(--spacing-xl) 0;
}
.pay-amount-box {
  text-align: center;
  padding: var(--spacing-lg);
  background: var(--color-surface-hover);
  border-radius: var(--radius-md);
  margin-bottom: var(--spacing-lg);
}
.pay-label {
  display: block;
  font-size: var(--font-sm);
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-xs);
}
.pay-amount {
  font-size: 32px;
  font-weight: var(--weight-bold);
  color: var(--color-brand);
}
.pay-actions {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}
.pay-hint {
  font-size: var(--font-xs);
  color: var(--color-text-tertiary);
  text-align: center;
}
</style>
