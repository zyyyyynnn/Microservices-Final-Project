<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { mallApi } from '../api/mall';
import ProductCard from '../components/ProductCard.vue';
import PageState from '../components/PageState.vue';
import type { UnknownRecord } from '../api/types';
import { field } from '../utils/format';
import { heroImage, seedCatalogProducts } from '../catalog/productAssets';

const router = useRouter();
const loading = ref(false);
const error = ref('');
const categories = ref<UnknownRecord[]>([]);
const products = ref<UnknownRecord[]>([]);
const keyword = ref('iPhone');

const categoryCount = computed(() => categories.value.length);
const displayProducts = computed(() => {
  const byId = new Map<number, UnknownRecord>();
  for (const item of seedCatalogProducts) byId.set(item.spuId, item);
  for (const item of products.value) {
    const id = Number(field(item, ['spuId', 'id'], 0));
    if (id) byId.set(id, item);
  }
  return Array.from(byId.values()).slice(0, 6);
});

const flowSteps = [
  { label: '登录', value: 'JWT 鉴权' },
  { label: '购物车', value: '勾选结算' },
  { label: '订单', value: '库存锁定' },
  { label: '支付', value: 'MQ 通知' },
  { label: '履约', value: '状态回写' },
];

const serviceItems = [
  { title: 'Gateway 统一入口', desc: '/api/v1/** 业务请求' },
  { title: '微服务链路完整', desc: '商品、购物车、订单、支付、秒杀' },
  { title: '真实状态反馈', desc: '不使用假成功或调试 JSON 占位' },
];

async function loadHome() {
  loading.value = true;
  error.value = '';
  products.value = [];
  try {
    const productIds = seedCatalogProducts.map((item) => item.spuId);
    const [categoryResult, ...productResults] = await Promise.allSettled([
      mallApi.categories(),
      ...productIds.map((id) => mallApi.product(id)),
    ]);
    categories.value = categoryResult.status === 'fulfilled' ? categoryResult.value || [] : [];
    products.value = productResults
      .filter((result): result is PromiseFulfilledResult<UnknownRecord> => result.status === 'fulfilled')
      .map((result) => result.value);
    if (categoryResult.status === 'rejected' || productResults.some((result) => result.status === 'rejected')) {
      error.value = 'Gateway 或商品服务暂不可用，首页已进入错误状态。';
    }
  } finally {
    loading.value = false;
  }
}

function goSearch() {
  router.push({ path: '/search', query: { keyword: keyword.value } });
}

onMounted(loadHome);
</script>

<template>
  <section class="commerce-layout">
    <div class="home-dashboard">
      <section class="commerce-hero">
        <div class="hero-copy">
          <el-tag effect="plain" round class="hero-tag">MallCloud 微商城</el-tag>
          <h1>把微服务链路做成真实电商首页</h1>
          <p>从商品发现、购物车、订单到模拟支付，围绕课程项目的真实后端能力组织可演示的用户任务。</p>

          <div class="hero-search">
            <label for="home-search">搜索商品</label>
            <div class="search-row">
              <el-input id="home-search" v-model="keyword" placeholder="试试 iPhone 或 MacBook" size="large" @keyup.enter="goSearch">
                <template #append>
                  <el-button type="primary" @click="goSearch">搜索</el-button>
                </template>
              </el-input>
            </div>
            <span>Elasticsearch 搜索入口</span>
          </div>

          <div class="hero-actions">
            <RouterLink to="/search">
              <el-button type="primary" size="large">立即选购</el-button>
            </RouterLink>
            <RouterLink to="/seckill">
              <el-button plain size="large">查看秒杀</el-button>
            </RouterLink>
          </div>
        </div>

        <div class="hero-visual" aria-label="精选商品展示">
          <img :src="heroImage" alt="MallCloud 精选数码商品" />
          <div class="hero-stat">
            <span>核心链路</span>
            <strong>5 步完成</strong>
          </div>
        </div>
      </section>

      <aside class="flow-panel">
        <div class="section-heading">
          <span>交易链路</span>
          <strong>真实服务流转</strong>
        </div>
        <div class="flow-steps">
          <div v-for="step in flowSteps" :key="step.label" class="flow-step">
            <span>{{ step.label }}</span>
            <strong>{{ step.value }}</strong>
          </div>
        </div>
        <RouterLink to="/tech" class="flow-link">查看技术演示</RouterLink>
      </aside>
    </div>

    <PageState :loading="loading" :error="error" @retry="loadHome" />

    <section v-if="!loading" class="section-block">
      <div class="section-heading">
        <span>种子商品</span>
        <strong>精选推荐</strong>
      </div>
      <div class="product-grid">
        <ProductCard
          v-for="product in displayProducts"
          :key="String(field(product, ['spuId', 'id']))"
          :product="product"
        />
      </div>
    </section>

    <div class="page-grid two" v-if="!loading">
      <section class="panel">
        <h2 class="panel-title">探索类目</h2>
        <div v-if="categoryCount" class="category-grid">
          <RouterLink
            v-for="category in categories"
            :key="String(field(category, ['id', 'categoryId', 'name']))"
            class="category-item"
            :to="{ path: '/search', query: { categoryId: field(category, ['id', 'categoryId']) } }"
          >
            <strong>{{ field(category, ['name'], '未命名类目') }}</strong>
            <span>浏览商品 &rarr;</span>
          </RouterLink>
        </div>
        <el-empty v-else description="暂无类目" />
      </section>

      <section class="panel promise-panel">
        <h2 class="panel-title">服务承诺</h2>
        <div class="promise-list">
          <div v-for="item in serviceItems" :key="item.title" class="promise-item">
            <strong>{{ item.title }}</strong>
            <span>{{ item.desc }}</span>
          </div>
        </div>
      </section>
    </div>
  </section>
</template>

<style scoped>
.hero-tag {
  margin-bottom: var(--spacing-sm);
  border-color: var(--color-brand);
  color: var(--color-brand);
}
</style>
