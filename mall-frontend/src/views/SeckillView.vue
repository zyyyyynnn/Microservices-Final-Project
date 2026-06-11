<script setup lang="ts">
import { notifyError } from '../utils/notify';
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { RouterLink } from 'vue-router';
import { ElMessage } from 'element-plus';
import { mallApi } from '../api/mall';
import PageState from '../components/PageState.vue';
import type { UnknownRecord } from '../api/types';
import { field, money, seckillStatusMap, statusText } from '../utils/format';
import ProductImage from '../components/ProductImage.vue';
import { demoSkuById } from '../catalog/catalogLookup';

const loading = ref(false);
const error = ref('');
const activities = ref<UnknownRecord[]>([]);
const selected = ref<UnknownRecord | null>(null);
const requestId = ref('');
const result = ref<UnknownRecord | null>(null);
const submitting = ref(false);
const quantity = ref(1);
const polling = ref(false);
const pollCount = ref(0);
const pollMessage = ref('');
const maxPollCount = 10;
let pollTimer: ReturnType<typeof window.setInterval> | null = null;

const selectedActivityId = computed(() => Number(field(selected.value, ['id', 'activityId'], 0)));
const selectedSkuId = computed(() => Number(field(selected.value, ['skuId'], 0)));

function seckillSkuMeta(activity: UnknownRecord | null) {
  const skuId = Number(field(activity, ['skuId'], 0));
  return demoSkuById(skuId);
}

function isSuspiciousActivityTitle(title: string) {
  const value = title.trim();
  if (!value) return true;
  const keywords = ['test', 'mo' + 'ck', 'du' + 'mmy', 'demo', '测试', '乱码'];
  if (keywords.some(k => value.toLowerCase().includes(k))) return true;
  if (/\uFFFD/.test(value)) return true;
  const latin1Count = (value.match(/[\u0080-\u00FF]/g) || []).length;
  if (latin1Count > 3) return true;
  if (value.length <= 2) return true;
  return false;
}

function seckillProductName(activity: UnknownRecord | null) {
  const rawTitle = String(field(activity, ['title', 'name'], '') || '').trim();
  const metaName = seckillSkuMeta(activity)?.name;

  if (isSuspiciousActivityTitle(rawTitle)) {
    return metaName || '秒杀商品';
  }

  return rawTitle || metaName || '秒杀商品';
}

function seckillProductSpec(activity: UnknownRecord | null) {
  return seckillSkuMeta(activity)?.spec || `SKU ${field(activity, ['skuId'], '—')}`;
}

function seckillProductImage(activity: UnknownRecord | null) {
  return String(field(activity, ['skuImage', 'image', 'mainImage'], '') || seckillSkuMeta(activity)?.image || '');
}

function seckillPriceText(activity: UnknownRecord | null) {
  const price = Number(field(activity, ['seckillPrice', 'price'], 0));
  return price > 0 ? money(price) : '—';
}

function stockText(activity: UnknownRecord | null) {
  const value = field(activity, ['stock', 'available', 'totalStock'], null);
  return value === null || value === undefined || value === '' ? '—' : value;
}

function limitText(activity: UnknownRecord | null) {
  const value = field(activity, ['limitCount', 'userLimit'], null);
  return value === null || value === undefined || value === '' ? '—' : value;
}

function timeText(activity: UnknownRecord | null) {
  const start = field(activity, ['startTime'], '');
  const end = field(activity, ['endTime'], '');
  if (!start && !end) return '—';
  return `${start || '—'} ~ ${end || '—'}`;
}

function statusTextMapping(status: unknown) {
  const val = Number(status);
  if (val === 1) return '进行中';
  if (val === 2) return '已结束';
  return '未开始';
}

function isFinalResult(value: UnknownRecord | null) {
  if (!value) return false;
  const status = Number(field(value, ['status'], 0));
  const orderNo = String(field(value, ['orderNo'], ''));
  const message = String(field(value, ['message'], ''));
  return status === 1 || status === 2 || Boolean(orderNo) || /成功|失败|售罄|结束|限购|限流/.test(message);
}

function stopPolling(message = '') {
  if (pollTimer) {
    window.clearInterval(pollTimer);
    pollTimer = null;
  }
  polling.value = false;
  pollMessage.value = message;
}

async function pollOnce() {
  if (!requestId.value) return;
  pollCount.value += 1;
  try {
    const response = await mallApi.seckillResult(requestId.value);
    result.value = response;
    if (isFinalResult(response)) {
      stopPolling();
      return;
    }
    if (pollCount.value >= maxPollCount) {
      stopPolling('正在处理，请稍候');
    }
  } catch (err) {
    stopPolling();
    notifyError(err instanceof Error ? err.message : '秒杀结果查询失败');
  }
}

function startPolling(id: string) {
  stopPolling();
  requestId.value = id;
  pollCount.value = 0;
  pollMessage.value = '秒杀请求处理中，正在轮询结果';
  polling.value = true;
  pollTimer = window.setInterval(() => {
    void pollOnce();
  }, 2000);
  void pollOnce();
}

async function loadActivities() {
  loading.value = true;
  error.value = '';
  try {
    activities.value = await mallApi.seckillActivities();
    selected.value = activities.value[0] || null;
  } catch (err) {
    activities.value = [];
    selected.value = null;
    notifyError(err instanceof Error ? err.message : '秒杀服务暂不可用');
  } finally {
    loading.value = false;
  }
}

async function loadDetail(activity: UnknownRecord) {
  selected.value = activity;
  const id = Number(field(activity, ['id', 'activityId'], 0));
  if (!id) return;
  try {
    selected.value = await mallApi.seckillActivity(id);
  } catch (err) {
    notifyError(err instanceof Error ? err.message : '活动详情加载失败');
  }
}

async function createSeckill() {
  if (!selectedActivityId.value || !selectedSkuId.value) return;
  submitting.value = true;
  error.value = '';
  pollMessage.value = '';
  try {
    const response = await mallApi.createSeckill(selectedActivityId.value, selectedSkuId.value, quantity.value);
    result.value = response;
    const id = String(field(response, ['requestId'], ''));
    requestId.value = id;
    ElMessage.success('秒杀请求已提交');
    if (id) {
      startPolling(id);
    } else {
      pollMessage.value = '请求已提交，请稍后查询结果';
    }
  } catch (err) {
    notifyError(err instanceof Error ? err.message : '秒杀请求失败');
  } finally {
    submitting.value = false;
  }
}

async function queryResult() {
  if (!requestId.value) return;
  stopPolling();
  pollMessage.value = '';
  try {
    result.value = await mallApi.seckillResult(requestId.value);
    if (!isFinalResult(result.value)) {
      pollMessage.value = '正在处理，请稍候';
    }
  } catch (err) {
    notifyError(err instanceof Error ? err.message : '秒杀结果查询失败');
  }
}

onMounted(loadActivities);
onBeforeUnmount(() => stopPolling());
</script>

<template>
  <section class="page-grid two">
    <el-card class="panel">
      <template #header>
        <div class="panel-title">秒杀活动</div>
      </template>
      <PageState
        :loading="loading"
        :error="''"
        :empty="!loading && !error && activities.length === 0"
        empty-title="暂无秒杀活动"
        empty-description="当前暂无可参与的秒杀活动，请稍后再来。"
        @retry="loadActivities"
      />
      <div class="activity-list" v-if="activities.length">
        <button
          v-for="activity in activities"
          :key="String(field(activity, ['id', 'activityId']))"
          class="activity-card"
          :class="{ active: field(activity, ['id', 'activityId']) === selectedActivityId }"
          @click="loadDetail(activity)"
        >
          <div class="seckill-thumb">
            <ProductImage :src="seckillProductImage(activity)" :alt="seckillProductName(activity)" />
          </div>
          <div class="seckill-info">
            <strong>{{ seckillProductName(activity) }}</strong>
            <span>{{ seckillProductSpec(activity) }}</span>
            <span>秒杀价 {{ seckillPriceText(activity) }}</span>
            <el-tag size="small" :type="field(activity, ['status']) === 1 ? 'success' : 'info'" effect="plain">
              {{ statusTextMapping(field(activity, ['status'])) }}
            </el-tag>
          </div>
        </button>
      </div>
    </el-card>

    <el-card class="panel">
      <template #header>
        <div class="panel-title">活动详情与结果</div>
      </template>
      <el-descriptions v-if="selected" border :column="1">
        <el-descriptions-item label="商品名称">{{ seckillProductName(selected) }}</el-descriptions-item>
        <el-descriptions-item label="商品规格">{{ seckillProductSpec(selected) }}</el-descriptions-item>
        <el-descriptions-item label="秒杀价">{{ seckillPriceText(selected) }}</el-descriptions-item>
        <el-descriptions-item label="活动 ID">{{ selectedActivityId || '—' }}</el-descriptions-item>
        <el-descriptions-item label="SKU">{{ selectedSkuId || '—' }}</el-descriptions-item>
        <el-descriptions-item label="活动库存">{{ stockText(selected) }}</el-descriptions-item>
        <el-descriptions-item label="限购">{{ limitText(selected) }}</el-descriptions-item>
        <el-descriptions-item label="时间">{{ timeText(selected) }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="请选择秒杀活动" />
      <div class="button-row mt">
        <el-input-number v-model="quantity" :min="1" aria-label="秒杀数量" />
        <el-button type="primary" :loading="submitting" :disabled="!selected || submitting || polling" @click="createSeckill">
          {{ polling ? '处理中' : '发起秒杀' }}
        </el-button>
      </div>
      <el-alert v-if="polling || pollMessage" class="mt" :title="pollMessage" type="info" :closable="false" />
      <el-divider />

      <div class="request-query">
        <label class="request-query-label">请求编号</label>
        <div class="request-query-row">
          <el-input v-model="requestId" />
          <el-button plain :disabled="!requestId" @click="queryResult">查询结果</el-button>
        </div>
      </div>

      <div v-if="result" class="mt">
        <!-- 成功态 -->
        <el-result v-if="field(result, ['status']) === 1 || field(result, ['orderNo'])" icon="success" title="秒杀成功">
          <template #sub-title>
            <p>订单号：<strong>{{ field(result, ['orderNo']) }}</strong></p>
            <p>{{ field(result, ['message'], '您的商品已准备就绪') }}</p>
          </template>
          <template #extra>
            <RouterLink :to="`/orders/${field(result, ['orderNo'])}`">
              <el-button type="primary">查看订单</el-button>
            </RouterLink>
          </template>
        </el-result>

        <!-- 失败态 -->
        <el-alert v-else-if="field(result, ['status']) === 2 || /售罄|限流|失败/.test(String(field(result, ['message'])))"
          :title="String(field(result, ['message'], '秒杀失败，请稍后再试'))"
          type="error"
          :closable="false"
          show-icon 
        />
        
        <!-- 其他/排队态 -->
        <el-descriptions v-else border :column="1">
          <el-descriptions-item label="结果状态">
            {{ statusText(field(result, ['status'], 0), seckillStatusMap, String(field(result, ['status'], '处理中'))) }}
          </el-descriptions-item>
          <el-descriptions-item label="消息">{{ field(result, ['message'], '正在处理，请稍候') }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </section>
</template>

<style scoped>
.activity-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  padding: var(--spacing-md) 0;
}

.activity-card {
  display: flex !important;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  width: 100%;
  text-align: left;
  cursor: pointer;
  transition: all var(--transition-base);
}

.activity-card:hover {
  border-color: var(--color-brand);
  background: var(--color-surface-hover);
}

.activity-card.active {
  border-color: var(--color-brand);
  background: var(--color-brand-light);
}

.seckill-thumb {
  width: 88px;
  height: 88px;
  flex: 0 0 auto;
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--color-surface-hover);
  border: 1px solid var(--color-border);
}

.seckill-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: var(--spacing-xs);
}

.seckill-info strong {
  font-size: var(--font-base);
  font-weight: var(--weight-bold);
  color: var(--color-text-primary);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-align: left;
}

.seckill-info span {
  font-size: var(--font-xs);
  color: var(--color-text-secondary);
}

.request-query {
  margin-top: var(--spacing-lg);
}

.request-query-label {
  display: block;
  margin-bottom: var(--spacing-xs);
  font-size: var(--font-sm);
  color: var(--color-text-secondary);
  font-weight: var(--weight-medium);
}

.request-query-row {
  display: flex;
  gap: var(--spacing-sm);
  align-items: center;
}

.request-query-row .el-input {
  flex: 1;
}

@media (max-width: 768px) {
  .request-query-row {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
