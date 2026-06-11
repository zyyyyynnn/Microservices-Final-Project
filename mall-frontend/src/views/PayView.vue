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
const order = ref<UnknownRecord | null>(null);
const payCreate = ref<UnknownRecord | null>(null);
const notifyResult = ref('');
const submitting = ref(false);

async function loadRecord() {
  loading.value = true;
  error.value = '';
  try {
    const [recordResult, orderResult] = await Promise.allSettled([
      mallApi.payRecord(orderNo),
      mallApi.order(orderNo),
    ]);

    payRecord.value = recordResult.status === 'fulfilled' ? recordResult.value : null;
    order.value = orderResult.status === 'fulfilled' ? orderResult.value : null;

    if (recordResult.status === 'rejected' && orderResult.status === 'rejected') {
      throw recordResult.reason;
    }
  } catch (err) {
    payRecord.value = null;
    order.value = null;
    notifyError(err instanceof Error ? err.message : '支付信息加载失败');
  } finally {
    loading.value = false;
  }
}

async function createPay() {
  submitting.value = true;
  try {
    payCreate.value = (await mallApi.createPay(orderNo)) as UnknownRecord;
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
    ElMessage.success('支付状态已更新');
    await loadRecord();
  } catch (err) {
    notifyError(err instanceof Error ? err.message : '支付通知失败');
  } finally {
    submitting.value = false;
  }
}

function payAmountText() {
  const amount = Number(
    field(payRecord.value, ['payAmount'], 0)
    || field(order.value, ['payAmount', 'totalAmount'], 0)
    || field(payCreate.value, ['payAmount'], 0)
  );
  return amount > 0 ? money(amount) : '—';
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
          <span class="pay-amount">{{ payAmountText() }}</span>
        </div>
        
        <el-descriptions border :column="1" class="pay-info-card mt">
          <el-descriptions-item label="订单号">{{ orderNo }}</el-descriptions-item>
          <el-descriptions-item label="支付单号">{{ field(payRecord, ['payNo'], field(payCreate, ['payNo'], '—')) }}</el-descriptions-item>
          <el-descriptions-item label="支付渠道">{{ field(payRecord, ['payChannel'], field(payCreate, ['payChannel'], 'ALIPAY')) }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="field(payRecord, ['status']) === 0 ? 'warning' : 'info'" effect="plain">
              {{ statusText(field(payRecord, ['status'], 0), payStatusMap) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单金额">{{ payAmountText() }}</el-descriptions-item>
        </el-descriptions>

        <div class="pay-actions mt">
          <el-button type="primary" size="large" class="full-button" :loading="submitting" @click="createPay" v-if="!field(payRecord, ['payNo']) && !field(payCreate, ['payNo'])">
            创建支付记录
          </el-button>
          <el-button type="success" size="large" class="full-button" :loading="submitting" @click="notifyPay" v-else>
            模拟支付完成
          </el-button>
        </div>

        <p class="pay-safe-note">支付完成后可返回订单详情查看状态。</p>
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
.pay-safe-note {
  font-size: var(--font-xs);
  color: var(--color-text-tertiary);
  text-align: center;
  margin-top: var(--spacing-lg);
}
</style>
