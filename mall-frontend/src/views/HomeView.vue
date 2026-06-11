<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { mallApi } from '../api/mall';
import ProductCard from '../components/ProductCard.vue';
import PageState from '../components/PageState.vue';
import type { UnknownRecord } from '../api/types';
import { field } from '../utils/format';

const router = useRouter();
const loading = ref(false);
const error = ref('');
const categories = ref<UnknownRecord[]>([]);
const products = ref<UnknownRecord[]>([]);
const keyword = ref('iPhone');

const categoryCount = computed(() => categories.value.length);

async function loadHome() {
  loading.value = true;
  error.value = '';
  products.value = [];
  try {
    const [categoryResult, productResult] = await Promise.allSettled([
      mallApi.categories(),
      mallApi.product(1001),
    ]);
    categories.value = categoryResult.status === 'fulfilled' ? categoryResult.value || [] : [];
    if (productResult.status === 'fulfilled') {
      products.value = [productResult.value];
    }
    if (categoryResult.status === 'rejected' || productResult.status === 'rejected') {
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
    <div class="hero-band">
      <el-tag effect="plain" round class="hero-tag">企业级微商城</el-tag>
      <h1>MallCloud Microservices</h1>
      <p>基于现代微服务架构的高性能电商体验。探索从商品发现到订单结算的端到端完整旅程。</p>
      
      <div class="search-panel">
        <label for="home-search">寻找你心仪的商品</label>
        <div class="search-row">
          <el-input id="home-search" v-model="keyword" placeholder="例如：iPhone, MacBook" size="large" @keyup.enter="goSearch">
            <template #append>
              <el-button type="primary" @click="goSearch">搜索</el-button>
            </template>
          </el-input>
        </div>
        <span>由 Elasticsearch 提供强力搜索支持</span>
      </div>
    </div>

    <PageState :loading="loading" :error="error" @retry="loadHome" />

    <div class="page-grid two" v-if="!loading">
      <div class="panel">
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
      </div>

      <div class="panel">
        <h2 class="panel-title">精选推荐</h2>
        <div class="product-grid" v-if="products.length">
          <ProductCard v-for="product in products" :key="String(field(product, ['spuId', 'id']))" :product="product" />
        </div>
        <el-empty v-else description="商品服务暂时不可用" />
      </div>
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
