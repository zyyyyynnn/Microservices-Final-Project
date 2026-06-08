<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { mallApi } from '../api/mall';
import PageState from '../components/PageState.vue';
import type { UnknownRecord } from '../api/types';
import { field, money, seckillStatusMap, statusText } from '../utils/format';

const loading = ref(false);
const error = ref('');
const activities = ref<UnknownRecord[]>([]);
const selected = ref<UnknownRecord | null>(null);
const requestId = ref('');
const result = ref<UnknownRecord | null>(null);
const submitting = ref(false);
const quantity = ref(1);

const selectedActivityId = computed(() => Number(field(selected.value, ['id', 'activityId'], 0)));
const selectedSkuId = computed(() => Number(field(selected.value, ['skuId'], 0)));

async function loadActivities() {
  loading.value = true;
  error.value = '';
  try {
    activities.value = await mallApi.seckillActivities();
    selected.value = activities.value[0] || null;
  } catch (err) {
    activities.value = [];
    selected.value = null;
    error.value = err instanceof Error ? err.message : '秒杀服务暂不可用';
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
    error.value = err instanceof Error ? err.message : '活动详情加载失败';
  }
}

async function createSeckill() {
  if (!selectedActivityId.value || !selectedSkuId.value) return;
  submitting.value = true;
  try {
    const response = await mallApi.createSeckill(selectedActivityId.value, selectedSkuId.value, quantity.value);
    result.value = response;
    requestId.value = String(field(response, ['requestId'], ''));
    ElMessage.success('秒杀请求已提交');
  } catch (err) {
    error.value = err instanceof Error ? err.message : '秒杀请求失败';
  } finally {
    submitting.value = false;
  }
}

async function queryResult() {
  if (!requestId.value) return;
  try {
    result.value = await mallApi.seckillResult(requestId.value);
  } catch (err) {
    error.value = err instanceof Error ? err.message : '秒杀结果查询失败';
  }
}

onMounted(loadActivities);
</script>

<template>
  <section class="page-grid two">
    <el-card class="panel">
      <template #header>
        <div class="panel-title">秒杀活动</div>
      </template>
      <PageState
        :loading="loading"
        :error="error"
        :empty="!loading && !error && activities.length === 0"
        empty-title="暂无秒杀活动"
        empty-description="请确认 mall-seckill、Redis 和 Gateway 路由已启动。"
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
          <strong>{{ field(activity, ['title', 'name'], `活动 ${field(activity, ['id', 'activityId'])}`) }}</strong>
          <span>SKU {{ field(activity, ['skuId'], '待联调') }}</span>
          <span>秒杀价 {{ money(field(activity, ['seckillPrice', 'price'], 0)) }}</span>
          <el-tag size="small" effect="plain">{{ field(activity, ['status'], '状态待联调') }}</el-tag>
        </button>
      </div>
    </el-card>

    <el-card class="panel">
      <template #header>
        <div class="panel-title">活动详情与结果</div>
      </template>
      <el-descriptions v-if="selected" border :column="1">
        <el-descriptions-item label="活动 ID">{{ selectedActivityId || '待联调' }}</el-descriptions-item>
        <el-descriptions-item label="SKU">{{ selectedSkuId || '待联调' }}</el-descriptions-item>
        <el-descriptions-item label="活动库存">{{ field(selected, ['stock', 'available', 'totalStock'], '待联调') }}</el-descriptions-item>
        <el-descriptions-item label="限购">{{ field(selected, ['limitCount', 'userLimit'], '待联调') }}</el-descriptions-item>
        <el-descriptions-item label="时间">{{ field(selected, ['startTime'], '待联调') }} ~ {{ field(selected, ['endTime'], '待联调') }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="请选择秒杀活动" />
      <div class="button-row mt">
        <el-input-number v-model="quantity" :min="1" aria-label="秒杀数量" />
        <el-button type="primary" :loading="submitting" :disabled="!selected || submitting" @click="createSeckill">
          发起秒杀
        </el-button>
      </div>
      <el-divider />
      <el-form class="inline-form" label-position="top">
        <el-form-item label="requestId">
          <el-input v-model="requestId" placeholder="发起秒杀后返回" />
        </el-form-item>
        <el-button plain :disabled="!requestId" @click="queryResult">查询结果</el-button>
      </el-form>
      <el-descriptions v-if="result" border :column="1" class="mt">
        <el-descriptions-item label="结果状态">
          {{ statusText(field(result, ['status'], 0), seckillStatusMap, String(field(result, ['status'], '待确认'))) }}
        </el-descriptions-item>
        <el-descriptions-item label="订单号">{{ field(result, ['orderNo'], '待生成') }}</el-descriptions-item>
        <el-descriptions-item label="消息">{{ field(result, ['message'], '待返回') }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </section>
</template>
