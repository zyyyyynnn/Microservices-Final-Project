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
      <div>
        <el-tag effect="plain">Gateway 统一入口</el-tag>
        <h1>MallCloud 微商城</h1>
        <p>从商品浏览到购物车、下单、支付消息和订单查询的课程演示链路。</p>
        <div class="hero-actions">
          <RouterLink to="/products/1001">
            <el-button type="primary">查看演示商品</el-button>
          </RouterLink>
          <RouterLink to="/cart">
            <el-button plain>进入购物车</el-button>
          </RouterLink>
        </div>
      </div>
      <div class="search-panel">
        <label for="home-search">搜索商品</label>
        <div class="search-row">
          <el-input id="home-search" v-model="keyword" placeholder="输入关键字" @keyup.enter="goSearch" />
          <el-button type="primary" @click="goSearch">搜索</el-button>
        </div>
        <span>当前搜索由 `/api/v1/search/products` 提供，未启动搜索服务时会显示错误状态。</span>
      </div>
    </div>

    <PageState :loading="loading" :error="error" @retry="loadHome" />

    <div class="page-grid two" v-if="!loading">
      <el-card class="panel">
        <template #header>
          <div class="panel-title">类目入口</div>
        </template>
        <div v-if="categoryCount" class="category-grid">
          <RouterLink
            v-for="category in categories"
            :key="String(field(category, ['id', 'categoryId', 'name']))"
            class="category-item"
            :to="{ path: '/search', query: { categoryId: field(category, ['id', 'categoryId']) } }"
          >
            <strong>{{ field(category, ['name'], '未命名类目') }}</strong>
            <span>进入商品搜索</span>
          </RouterLink>
        </div>
        <el-empty v-else description="类目服务未返回数据" />
      </el-card>

      <el-card class="panel">
        <template #header>
          <div class="panel-title">商品推荐</div>
        </template>
        <div class="product-grid" v-if="products.length">
          <ProductCard v-for="product in products" :key="String(field(product, ['spuId', 'id']))" :product="product" />
        </div>
        <el-empty v-else description="商品详情接口暂不可用" />
      </el-card>
    </div>
  </section>
</template>
