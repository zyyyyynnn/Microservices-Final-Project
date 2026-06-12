<script setup lang="ts">
import { notifyError } from '../utils/notify';
import { computed, onMounted, ref, watch } from 'vue';
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
const keyword = ref(String(route.query.keyword || ''));
const brand = ref(String(route.query.brand || ''));
const pageNum = ref(1);
const pageSize = ref(8);
const response = ref<UnknownRecord | null>(null);
const hotWords = ref<string[]>([]);

const results = computed(() => asList(response.value));
const total = computed(() => Number(field(response.value, ['total'], results.value.length)));

// Build search query from keyword, brand
const searchQuery = computed(() => {
  const parts = [];
  if (keyword.value) parts.push(keyword.value);
  if (brand.value) parts.push(brand.value);
  // categoryId 不参与搜索，避免伪装成真实分类筛选
  return parts.join(' ');
});

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
    router.replace({
      path: '/search',
      query: {
        keyword: keyword.value,
        brand: brand.value || undefined,
        pageNum: pageNum.value,
      },
    });
    const query = searchQuery.value;
    response.value = await mallApi.searchProducts(query || ' ', pageNum.value, pageSize.value);
  } catch (err) {
    response.value = null;
    error.value = err instanceof Error ? err.message : '搜索服务暂不可用';
    notifyError(error.value);
  } finally {
    loading.value = false;
  }
}

function useHotWord(word: string) {
  keyword.value = word;
  brand.value = '';
  pageNum.value = 1;
  search();
}

watch(() => route.query.keyword, (newVal) => {
  if (newVal !== undefined) {
    keyword.value = String(newVal);
    pageNum.value = 1;
    search();
  }
});

watch(() => route.query.brand, (newVal) => {
  if (newVal !== undefined) {
    brand.value = String(newVal);
    pageNum.value = 1;
    search();
  }
});

onMounted(() => {
  loadHotWords();
  search();
});
</script>

<template>
  <section class="commerce-layout">
    <div class="search-header">
      <h1 class="search-title">
        <span v-if="brand">「{{ brand }}」专区</span>
        <span v-else-if="keyword">「{{ keyword }}」的搜索结果</span>
        <span v-else>全部商品</span>
      </h1>

      <div class="tag-row mt">
        <button v-for="word in hotWords" :key="word" class="hot-chip" @click="useHotWord(word)">{{ word }}</button>
        <span v-if="!hotWords.length" class="empty-hint">暂无热词推荐</span>
      </div>
    </div>

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

<style scoped>
.search-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-xl) 0;
  margin-bottom: var(--spacing-lg);
}
.large-search {
  width: 100%;
  max-width: 600px;
}
.hot-chip {
  cursor: pointer;
  border-radius: 16px;
  border: 1px solid var(--color-border);
  background: transparent;
  padding: 4px 12px;
  font-size: var(--font-xs);
  color: var(--color-text-secondary);
  outline: none;
}
.hot-chip:hover, .hot-chip:focus {
  border-color: var(--color-brand);
  color: var(--color-brand);
  background: var(--color-surface-hover);
}
.empty-hint {
  font-size: var(--font-xs);
  color: var(--color-text-tertiary);
}
</style>
