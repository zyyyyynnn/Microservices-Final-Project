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
        <div class="panel-title">1. API Gateway 与用户上下文</div>
      </template>
      <div class="panel-desc">通过 Gateway 鉴权，转发请求至下游服务。</div>
      <el-descriptions border :column="1" class="mt">
        <el-descriptions-item label="Token 状态">
          <el-tag :type="auth.isAuthenticated ? 'success' : 'danger'" effect="plain">
            {{ auth.isAuthenticated ? '已保存' : '未保存' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="当前用户">
          <span class="mono">{{ display(auth.user) }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <el-button class="mt" type="primary" :disabled="!auth.isAuthenticated" @click="checkGateway">
        调用 /api/v1/users/me 测试路由
      </el-button>
      <el-collapse class="mt" v-if="gatewayResult">
        <el-collapse-item title="查看原始响应">
          <pre class="json-box compact" style="margin:0">{{ display(gatewayResult) }}</pre>
        </el-collapse-item>
      </el-collapse>
    </el-card>

    <el-card class="panel">
      <template #header>
        <div class="panel-title">2. Elasticsearch 搜索链路</div>
      </template>
      <div class="panel-desc">通过网关路由到 mall-search 服务，执行商品搜索。</div>
      <el-button class="mt" type="primary" plain @click="checkSearch">验证搜索入口</el-button>
      <el-collapse class="mt" v-if="searchResult">
        <el-collapse-item title="查看原始响应">
          <pre class="json-box compact" style="margin:0">{{ display(searchResult) }}</pre>
        </el-collapse-item>
      </el-collapse>
    </el-card>

    <el-card class="panel">
      <template #header>
        <div class="panel-title">3. Seata 分布式事务</div>
      </template>
      <div class="panel-desc">
        订单创建失败回滚需通过后端故障注入和数据库状态验证，本页仅保留功能性文字说明入口，不作控制台展示。
      </div>
    </el-card>

    <el-card class="panel">
      <template #header>
        <div class="panel-title">4. RocketMQ 异步消息队列</div>
      </template>
      <div class="panel-desc">
        支付页的成功通知会触发 <code>PAY_RESULT</code> 主题消息。后续由 <code>mall-message</code> 等服务订阅并异步更新订单和库存状态。
      </div>
    </el-card>

    <el-card class="panel">
      <template #header>
        <div class="panel-title">5. Sentinel 流量控制与熔断</div>
      </template>
      <div class="panel-desc">
        创建订单和秒杀请求等高并发场景的核心限流资源。具体限流阈值效果可参考 JMeter 压测结果。
      </div>
    </el-card>

    <el-card class="panel">
      <template #header>
        <div class="panel-title">6. Nacos 服务注册与配置中心</div>
      </template>
      <div class="panel-desc">
        配置热更新支持。如需体验，请前往 Nacos 控制台修改参数并刷新应用查看效果。
      </div>
    </el-card>
  </section>
</template>

<style scoped>
.panel-desc {
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: var(--leading-normal);
}
</style>
