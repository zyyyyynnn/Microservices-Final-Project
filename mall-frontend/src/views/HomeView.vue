<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { mallApi } from '../api/mall';
import type { UnknownRecord } from '../api/types';

const loading = ref(false);
const categories = ref<UnknownRecord[]>([]);
const product = ref<UnknownRecord | null>(null);
const searchResult = ref<UnknownRecord | null>(null);
const hotWords = ref<string[]>([]);
const loadError = ref('');

const query = reactive({
  spuId: 1001,
  skuId: 9001,
  quantity: 1,
  keyword: 'iPhone',
});

function display(value: unknown) {
  return JSON.stringify(value, null, 2);
}

async function loadBasics() {
  loading.value = true;
  loadError.value = '';
  try {
    const [categoryData, productData, words] = await Promise.allSettled([
      mallApi.categories(),
      mallApi.product(query.spuId),
      mallApi.hotWords(),
    ]);
    categories.value = categoryData.status === 'fulfilled' ? categoryData.value || [] : [];
    product.value = productData.status === 'fulfilled' ? productData.value : null;
    hotWords.value = words.status === 'fulfilled' ? words.value || [] : [];
    if (categoryData.status === 'rejected' || productData.status === 'rejected') {
      loadError.value = 'Gateway 或后端服务暂不可用，已保留页面可操作状态。';
    }
  } finally {
    loading.value = false;
  }
}

async function search() {
  searchResult.value = await mallApi.searchProducts(query.keyword);
}

async function addCart() {
  await mallApi.addCart({ skuId: query.skuId, quantity: query.quantity });
  ElMessage.success('已加入购物车');
}

onMounted(loadBasics);
</script>

<template>
  <section class="page-grid two">
    <el-card class="panel">
      <template #header>
        <div class="panel-title">公共商品入口</div>
      </template>
      <el-form class="inline-form" label-position="top" @submit.prevent="loadBasics">
        <el-form-item label="SPU">
          <el-input-number v-model="query.spuId" :min="1" />
        </el-form-item>
        <el-form-item label="SKU">
          <el-input-number v-model="query.skuId" :min="1" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="query.quantity" :min="1" />
        </el-form-item>
        <el-button type="primary" :loading="loading" @click="loadBasics">加载商品</el-button>
        <el-button plain @click="addCart">加入购物车</el-button>
      </el-form>
      <el-alert v-if="loadError" class="mt" :title="loadError" type="error" :closable="false" />

      <el-empty v-if="!loading && !product" description="未加载商品详情" />
      <pre v-else class="json-box">{{ display(product) }}</pre>
    </el-card>

    <el-card class="panel">
      <template #header>
        <div class="panel-title">类目与搜索</div>
      </template>
      <div class="search-row">
        <el-input v-model="query.keyword" placeholder="搜索关键字" />
        <el-button type="primary" @click="search">搜索</el-button>
      </div>
      <div class="tag-row">
        <el-tag v-for="word in hotWords" :key="word" effect="plain">{{ word }}</el-tag>
        <el-tag v-if="!hotWords.length" type="info" effect="plain">热词接口未返回数据</el-tag>
      </div>
      <el-divider />
      <div class="split-block">
        <div>
          <h2 class="section-title">类目树</h2>
          <pre class="json-box compact">{{ display(categories) }}</pre>
        </div>
        <div>
          <h2 class="section-title">搜索结果</h2>
          <pre class="json-box compact">{{ display(searchResult) }}</pre>
        </div>
      </div>
    </el-card>
  </section>
</template>
