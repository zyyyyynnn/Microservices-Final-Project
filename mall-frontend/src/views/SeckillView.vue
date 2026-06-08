<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { mallApi } from '../api/mall';
import type { UnknownRecord } from '../api/types';

const loading = ref(false);
const activities = ref<UnknownRecord[]>([]);
const detail = ref<UnknownRecord | null>(null);
const result = ref<UnknownRecord | null>(null);
const form = reactive({
  activityId: 1,
  skuId: 9001,
  quantity: 1,
  requestId: '',
});

function display(value: unknown) {
  return JSON.stringify(value, null, 2);
}

async function loadActivities() {
  loading.value = true;
  try {
    activities.value = await mallApi.seckillActivities();
  } finally {
    loading.value = false;
  }
}

async function loadDetail() {
  detail.value = await mallApi.seckillActivity(form.activityId);
}

async function createSeckill() {
  const response = await mallApi.createSeckill(form.activityId, form.skuId, form.quantity);
  result.value = response;
  const requestId = String(response.requestId || response.id || '');
  if (requestId) form.requestId = requestId;
  ElMessage.success('秒杀请求已提交');
}

async function queryResult() {
  result.value = await mallApi.seckillResult(form.requestId);
}

onMounted(loadActivities);
</script>

<template>
  <section class="page-grid two">
    <el-card class="panel">
      <template #header>
        <div class="panel-title">秒杀活动</div>
      </template>
      <el-button type="primary" :loading="loading" @click="loadActivities">刷新活动</el-button>
      <el-table v-if="activities.length" :data="activities" class="stable-table">
        <el-table-column prop="id" label="活动 ID" width="110" />
        <el-table-column prop="skuId" label="SKU" width="110" />
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="status" label="状态" width="100" />
      </el-table>
      <el-empty v-else description="暂无活动或服务未启动" />
    </el-card>

    <el-card class="panel">
      <template #header>
        <div class="panel-title">发起与查询</div>
      </template>
      <el-form class="inline-form" label-position="top">
        <el-form-item label="活动 ID">
          <el-input-number v-model="form.activityId" :min="1" />
        </el-form-item>
        <el-form-item label="SKU">
          <el-input-number v-model="form.skuId" :min="1" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="form.quantity" :min="1" />
        </el-form-item>
        <el-button plain @click="loadDetail">活动详情</el-button>
        <el-button type="primary" @click="createSeckill">发起秒杀</el-button>
      </el-form>
      <el-form class="inline-form" label-position="top">
        <el-form-item label="requestId">
          <el-input v-model="form.requestId" />
        </el-form-item>
        <el-button plain :disabled="!form.requestId" @click="queryResult">查询结果</el-button>
      </el-form>
      <pre class="json-box compact">{{ display(detail) }}</pre>
      <pre class="json-box compact">{{ display(result) }}</pre>
    </el-card>
  </section>
</template>
