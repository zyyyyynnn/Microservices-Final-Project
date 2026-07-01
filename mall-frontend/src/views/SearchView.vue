<script setup lang="ts">
import { notifyError } from '../utils/notify';
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { mallApi } from '../api/mall';
import PageState from '../components/PageState.vue';
import ProductCard from '../components/ProductCard.vue';
import type { UnknownRecord } from '../api/types';
import { asList, field } from '../utils/format';
import { Search } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const error = ref('');
const keyword = ref(String(route.query.keyword || ''));
const searchInput = ref(String(route.query.keyword || ''));
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
    error.value = '数据暂时无法加载，请稍后重试。';
    notifyError('搜索数据加载失败，请稍后重试。');
  } finally {
    loading.value = false;
  }
}

function useHotWord(word: string) {
  keyword.value = word;
  searchInput.value = word;
  brand.value = '';
  pageNum.value = 1;
  search();
}

function submitSearch() {
  const value = searchInput.value.trim();
  keyword.value = value;
  brand.value = '';
  pageNum.value = 1;
  search();
}

watch(() => route.query.keyword, (newVal) => {
  if (newVal !== undefined) {
    keyword.value = String(newVal);
    searchInput.value = String(newVal);
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
      <div class="search-head-text">
        <h1 class="search-title">
          <span v-if="brand">「{{ brand }}」专区</span>
          <span v-else-if="keyword">「{{ keyword }}」的搜索结果</span>
          <span v-else>全部商品</span>
        </h1>
        <p class="search-sub">输入关键字或选择热门搜索词，快速定位商品</p>
      </div>
      <div class="search-head-input">
        <el-input
          v-model="searchInput"
          placeholder="搜索商品，如 iPhone、美妆、台灯"
          size="large"
          @keyup.enter="submitSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="submitSearch">搜索</el-button>
          </template>
        </el-input>
      </div>
    </div>

    <div class="search-hot-row" v-if="hotWords.length">
      <span class="hot-label">热门搜索</span>
      <div class="tag-row">
        <button v-for="word in hotWords" :key="word" class="hot-chip" @click="useHotWord(word)">{{ word }}</button>
      </div>
    </div>

    <PageState
      :loading="loading"
      :error="error"
      :empty="!loading && !error && results.length === 0"
      empty-title="暂无搜索结果"
      empty-description="当前暂未找到可展示内容，请稍后重试或返回首页浏览。"
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
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(320px, 540px);
  gap: var(--spacing-xl);
  align-items: center;
  padding: var(--spacing-xl);
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.search-title {
  margin: 0;
  font-size: var(--font-2xl);
  font-weight: var(--weight-bold);
  color: var(--color-text-primary);
  letter-spacing: -0.01em;
}

.search-sub {
  margin: var(--spacing-xs) 0 0;
  font-size: var(--font-sm);
  color: var(--color-text-secondary);
}

.search-head-input :deep(.el-input-group__append) {
  background: var(--color-brand);
  border-color: var(--color-brand);
}

.search-head-input :deep(.el-input-group__append .el-button) {
  color: var(--color-text-inverse);
}

.search-hot-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  flex-wrap: wrap;
  padding: 0 var(--spacing-xs);
}

.hot-label {
  font-size: var(--font-sm);
  color: var(--color-text-tertiary);
  font-weight: var(--weight-medium);
  white-space: nowrap;
}

.hot-chip {
  cursor: pointer;
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border);
  background: var(--color-bg-surface);
  padding: var(--spacing-xs) var(--spacing-md);
  font-size: var(--font-xs);
  color: var(--color-text-secondary);
  outline: none;
  transition: border-color var(--transition-fast), color var(--transition-fast), background var(--transition-fast);
}
.hot-chip:hover, .hot-chip:focus {
  border-color: var(--color-brand);
  color: var(--color-brand);
  background: var(--color-surface-hover);
}

@media (max-width: 1024px) {
  .search-header {
    grid-template-columns: 1fr;
    gap: var(--spacing-lg);
  }
}
</style>
