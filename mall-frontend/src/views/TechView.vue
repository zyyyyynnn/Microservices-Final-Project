<script setup lang="ts">
import { ref } from 'vue';
import { mallApi } from '../api/mall';
import { useAuthStore } from '../stores/auth';
import type { UnknownRecord } from '../api/types';

const auth = useAuthStore();
const gatewayResult = ref<unknown>(null);
const searchResult = ref<UnknownRecord | null>(null);

function display(value: unknown) {
  return JSON.stringify(value, null, 2);
}

async function checkGateway() {
  gatewayResult.value = await mallApi.currentUser();
}

async function checkSearch() {
  searchResult.value = await mallApi.searchProducts('iPhone');
}
</script>

<template>
  <section class="page-grid two">
    <el-card class="panel">
      <template #header>
        <div class="panel-title">Gateway 与用户上下文</div>
      </template>
      <el-descriptions border :column="1">
        <el-descriptions-item label="Token">
          <el-tag :type="auth.isAuthenticated ? 'success' : 'danger'" effect="plain">
            {{ auth.isAuthenticated ? '已保存' : '未保存' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户">
          <span class="mono">{{ display(auth.user) }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <el-button class="mt" type="primary" :disabled="!auth.isAuthenticated" @click="checkGateway">
        调用 /api/v1/users/me
      </el-button>
      <pre class="json-box compact">{{ display(gatewayResult) }}</pre>
    </el-card>

    <el-card class="panel">
      <template #header>
        <div class="panel-title">技术链路入口</div>
      </template>
      <el-timeline>
        <el-timeline-item timestamp="Seata">
          订单创建失败回滚需通过后端故障注入和数据库状态验证，本页只保留演示说明入口。
        </el-timeline-item>
        <el-timeline-item timestamp="RocketMQ">
          支付页的成功通知会触发 PAY_RESULT，后续由 mall-message 更新订单和库存。
        </el-timeline-item>
        <el-timeline-item timestamp="Sentinel">
          创建订单和秒杀请求是后续限流资源，阈值必须来自 JMeter 结果。
        </el-timeline-item>
        <el-timeline-item timestamp="Nacos">
          热更新仍需在 Nacos 控制台修改配置后截图验证。
        </el-timeline-item>
      </el-timeline>
      <el-button plain @click="checkSearch">验证搜索入口</el-button>
      <pre class="json-box compact">{{ display(searchResult) }}</pre>
    </el-card>
  </section>
</template>
