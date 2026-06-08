<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { mallApi } from '../api/mall';
import PageState from '../components/PageState.vue';
import ProductCard from '../components/ProductCard.vue';
import type { UnknownRecord } from '../api/types';
import { asList, field } from '../utils/format';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const error = ref('');
const keyword = ref(String(route.query.keyword || 'iPhone'));
const pageNum = ref(1);
const pageSize = ref(8);
const response = ref<UnknownRecord | null>(null);
const hotWords = ref<string[]>([]);

const results = computed(() => asList(response.value));
const total = computed(() => Number(field(response.value, ['total'], results.value.length)));

async function loadHotWords() {
  try {
    hotWords.value = await mallApi.hotWords();
  } catch {
    hotWords.value = [];
  }
}

async function search() {
  loading.value = true;
  error.value = '';
  try {
    router.replace({ path: '/search', query: { keyword: keyword.value, pageNum: pageNum.value } });
    response.value = await mallApi.searchProducts(keyword.value, pageNum.value, pageSize.value);
  } catch (err) {
    response.value = null;
    error.value = err instanceof Error ? err.message : '搜索服务暂不可用';
  } finally {
    loading.value = false;
  }
}

function useHotWord(word: string) {
  keyword.value = word;
  pageNum.value = 1;
  search();
}

onMounted(() => {
  loadHotWords();
  search();
});
</script>

<template>
  <section class="commerce-layout">
    <el-card class="panel">
      <template #header>
        <div class="panel-title">商品搜索</div>
      </template>
      <div class="search-row">
        <el-input v-model="keyword" placeholder="输入商品关键字" @keyup.enter="search" />
        <el-button type="primary" :loading="loading" @click="search">搜索</el-button>
      </div>
      <div class="tag-row">
        <el-tag v-for="word in hotWords" :key="word" effect="plain" @click="useHotWord(word)">{{ word }}</el-tag>
        <el-tag v-if="!hotWords.length" type="info" effect="plain">热词受后端限制 / 待联调</el-tag>
      </div>
    </el-card>

    <PageState
      :loading="loading"
      :error="error"
      :empty="!loading && !error && results.length === 0"
      empty-title="暂无搜索结果"
      empty-description="请调整关键字，或确认 mall-search 与 Elasticsearch 已启动。"
      @retry="search"
    />

    <div v-if="results.length" class="product-grid">
      <ProductCard v-for="item in results" :key="String(field(item, ['spuId', 'id', 'skuId']))" :product="item" />
    </div>

    <div v-if="!loading && !error" class="pager-row">
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="search"
      />
    </div>
  </section>
</template>
