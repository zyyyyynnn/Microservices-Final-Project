<script setup lang="ts">
import { notifyError } from '../utils/notify';
import { computed, onMounted, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { mallApi } from '../api/mall';
import PageState from '../components/PageState.vue';
import ProductCard from '../components/ProductCard.vue';
import type { UnknownRecord } from '../api/types';
import { field } from '../utils/format';
import { resolveProductImage } from '../catalog/productAssets';
import { demoSpuIdBySkuId } from '../catalog/catalogLookup';
import ProductImage from '../components/ProductImage.vue';
import PriceText from '../components/PriceText.vue';
import {
  Lock, CircleCheckFilled, User, ShoppingCart, Document, Wallet, Check,
  ArrowRight, Search, Monitor, MagicStick, Basketball, Apple,
} from '@element-plus/icons-vue';

const router = useRouter();
const loading = ref(false);
const error = ref('');
const categories = ref<UnknownRecord[]>([]);
const products = ref<UnknownRecord[]>([]);
const seckillProducts = ref<UnknownRecord[]>([]);
const seckillError = ref(false);
const searchKeyword = ref('');

const homeProductIds = [
  1001, 1007, 1009, 1011,
  1005, 1008, 1010, 1012,
  1002, 1003, 1004, 1006,
];

const displayProducts = computed(() => products.value.slice(0, 12));

const categoryCards = [
  { label: '精选推荐', keyword: '', icon: CircleCheckFilled },
  { label: '数码家电', keyword: 'iPhone', icon: Monitor },
  { label: '美妆护肤', keyword: '美妆', icon: MagicStick },
  { label: '台灯照明', keyword: '台灯', icon: Search },
  { label: '运动户外', keyword: '运动', icon: Basketball },
  { label: '食品生鲜', keyword: '食品', icon: Apple },
];

const flowSteps = [
  { label: '登录', sub: '账户登录', icon: User, to: '/login' },
  { label: '浏览', sub: '逛好物', icon: Search, to: '/search' },
  { label: '加购', sub: '加入购物车', icon: ShoppingCart, to: '/cart' },
  { label: '下单', sub: '提交订单', icon: Document, to: '/checkout' },
  { label: '支付', sub: '模拟支付', icon: Wallet, to: '/account' },
  { label: '完成', sub: '查看订单', icon: Check, to: '/account' },
];

const bottomPromises = [
  { title: '30天价保', sub: '买贵退差价', icon: Lock },
  { title: '破损包退', sub: '免费上门取件', icon: Document },
  { title: '闪电退款', sub: '极速到账', icon: Wallet },
  { title: '24小时客服', sub: '专业服务', icon: User },
];

async function loadHome() {
  loading.value = true;
  error.value = '';
  products.value = [];
  seckillProducts.value = [];
  seckillError.value = false;
  try {
    const [categoryResult, ...productResults] = await Promise.allSettled([
      mallApi.categories(),
      ...homeProductIds.map((id) => mallApi.product(id)),
    ]);
    categories.value = categoryResult.status === 'fulfilled' ? categoryResult.value || [] : [];
    products.value = productResults
      .filter((result): result is PromiseFulfilledResult<UnknownRecord> => result.status === 'fulfilled')
      .map((result) => result.value);

    try {
      const seckillResult = await mallApi.seckillActivities();
      const activities = Array.isArray(seckillResult) ? seckillResult : [];
      seckillProducts.value = activities.filter((activity: UnknownRecord) => {
        const name = String(field(activity, ['title', 'name'], ''));
        return !/JMeter|压测|测试|乱码|test/i.test(name);
      });
      seckillError.value = false;
    } catch {
      seckillProducts.value = [];
      seckillError.value = true;
    }

    if (categoryResult.status === 'rejected' || products.value.length === 0) {
      error.value = '数据暂时无法加载，请稍后重试。';
      notifyError('首页数据加载失败，请稍后重试。');
    }
  } catch (err) {
    error.value = '数据暂时无法加载，请稍后重试。';
  } finally {
    loading.value = false;
  }
}

function selectCategory(card: { label: string; keyword: string }) {
  if (card.keyword) {
    router.push({ path: '/search', query: { keyword: card.keyword } });
  } else {
    router.push('/search');
  }
}

function goSearch() {
  const keyword = searchKeyword.value.trim();
  if (keyword) {
    router.push({ path: '/search', query: { keyword } });
  } else {
    router.push('/search');
  }
}

function getSeckillImage(activity: UnknownRecord) {
  const directImage = field<string>(activity, ['mainImage', 'image'], '');
  if (directImage) return directImage;

  const spuId = Number(field(activity, ['spuId'], 0)) || demoSpuIdBySkuId(field(activity, ['skuId'], 0));
  if (spuId) return resolveProductImage({ spuId });

  return '';
}

onMounted(loadHome);
</script>

<template>
  <div class="home-wrapper">
    <PageState
      :loading="loading"
      :error="error"
      :empty="!loading && !error && displayProducts.length === 0"
      empty-title="暂无推荐商品"
      empty-description="当前暂未找到可展示内容，请稍后重试。"
      @retry="loadHome"
    />

    <div v-if="!loading" class="home-grid">
      <!-- 品牌头：紧凑品牌名 + 搜索入口 -->
      <section class="brand-head">
        <div class="brand-head-text">
          <h1 class="brand-head-title">MallCloud 微商城</h1>
          <p class="brand-head-sub">精选好物 · 品质保障 · 极速送达</p>
        </div>
        <div class="brand-head-search">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索商品，如 iPhone、美妆、台灯"
            size="large"
            @keyup.enter="goSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="goSearch">搜索</el-button>
            </template>
          </el-input>
        </div>
      </section>

      <!-- 交易链路线条：横向步骤条 -->
      <section class="flow-line-section">
        <div class="flow-line-track">
          <template v-for="(step, index) in flowSteps" :key="index">
            <RouterLink :to="step.to" class="flow-node">
              <div class="flow-node-icon">
                <el-icon><component :is="step.icon" /></el-icon>
              </div>
              <div class="flow-node-text">
                <strong>{{ step.label }}</strong>
                <span>{{ step.sub }}</span>
              </div>
            </RouterLink>
            <div v-if="index < flowSteps.length - 1" class="flow-connector" aria-hidden="true">
              <el-icon><ArrowRight /></el-icon>
            </div>
          </template>
        </div>
      </section>

      <!-- 品类卡片网格 -->
      <section class="category-section">
        <div class="section-heading">
          <strong>热门频道</strong>
          <span>点击直达分类检索</span>
        </div>
        <div class="category-card-grid">
          <button
            v-for="card in categoryCards"
            :key="card.label"
            type="button"
            class="category-card"
            @click="selectCategory(card)"
          >
            <el-icon class="category-card-icon" :size="24"><component :is="card.icon" /></el-icon>
            <strong>{{ card.label }}</strong>
          </button>
        </div>
      </section>

      <!-- 推荐商品 -->
      <el-card class="panel recommend-section" shadow="never">
        <template #header>
          <div class="panel-title-row">
            <div class="panel-title-group">
              <span class="panel-title-main">为你推荐</span>
              <span class="panel-title-sub">精选好物</span>
            </div>
            <RouterLink to="/search" class="more-link">更多 <el-icon><ArrowRight /></el-icon></RouterLink>
          </div>
        </template>

        <div class="product-grid">
          <ProductCard
            v-for="product in displayProducts"
            :key="String(field(product, ['spuId', 'id']))"
            :product="product"
          />
        </div>
      </el-card>

      <!-- 限时秒杀 -->
      <el-card class="panel seckill-section" shadow="never">
        <template #header>
          <div class="panel-title-row">
            <div class="panel-title-group">
              <span class="panel-title-main">限时秒杀</span>
              <span class="panel-title-sub">每日优选好货</span>
            </div>
            <RouterLink to="/seckill" class="more-link">更多秒杀 <el-icon><ArrowRight /></el-icon></RouterLink>
          </div>
        </template>

        <div class="sk-grid">
          <div v-if="seckillError" class="sk-empty">
            <p>秒杀活动暂不可用</p>
          </div>
          <div v-else-if="!seckillProducts || seckillProducts.length === 0" class="sk-empty">
            <p>暂无秒杀活动</p>
          </div>
          <div v-else v-for="product in seckillProducts" :key="String(field(product, ['id', 'activityId', 'spuId']))" class="sk-card">
            <div class="sk-image">
              <ProductImage :src="getSeckillImage(product)" :alt="String(field(product, ['title', 'name']))" />
            </div>
            <div class="sk-info">
              <h3 class="sk-name">{{ field(product, ['title', 'name'], '—') }}</h3>
              <div class="sk-prices">
                <PriceText :value="field(product, ['seckillPrice', 'price'])" size="lg" class="sk-price" />
                <PriceText :value="field(product, ['price', 'oldPrice', 'originalPrice'])" size="sm" original class="sk-old" />
              </div>
              <button class="sk-btn" @click="router.push('/seckill')">立即抢购</button>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 服务承诺 -->
      <div class="bottom-promises">
        <div v-for="promise in bottomPromises" :key="promise.title" class="bp-item">
          <el-icon class="bp-icon" color="var(--color-brand)" :size="28"><component :is="promise.icon" /></el-icon>
          <div class="bp-text">
            <strong>{{ promise.title }}</strong>
            <span>{{ promise.sub }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-grid {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xl);
  padding-top: var(--spacing-xl);
}

/* 品牌头 */
.brand-head {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(320px, 540px);
  gap: var(--spacing-xl);
  align-items: center;
  padding: var(--spacing-xl) var(--spacing-xl);
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.brand-head-title {
  margin: 0;
  font-size: var(--font-2xl);
  font-weight: var(--weight-bold);
  color: var(--color-text-primary);
  letter-spacing: -0.01em;
}

.brand-head-sub {
  margin: var(--spacing-xs) 0 0;
  font-size: var(--font-sm);
  color: var(--color-text-secondary);
}

.brand-head-search :deep(.el-input-group__append) {
  background: var(--color-brand);
  border-color: var(--color-brand);
}

.brand-head-search :deep(.el-input-group__append .el-button) {
  color: var(--color-text-inverse);
}

/* 交易链路线条 */
.flow-line-section {
  padding: var(--spacing-lg) var(--spacing-xl);
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.flow-line-track {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
}

.flow-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-md);
  text-decoration: none;
  transition: background var(--transition-fast);
  min-width: 80px;
}

.flow-node:hover {
  background: var(--color-bg-subtle);
}

.flow-node-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--color-brand-soft);
  color: var(--color-brand);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-xl);
}

.flow-node-text {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1px;
}

.flow-node-text strong {
  font-size: var(--font-sm);
  color: var(--color-text-primary);
  font-weight: var(--weight-medium);
}

.flow-node-text span {
  font-size: var(--font-xs);
  color: var(--color-text-tertiary);
}

.flow-connector {
  color: var(--color-border);
  display: flex;
  align-items: center;
  margin-bottom: var(--spacing-lg);
}

/* 品类卡片网格 */
.category-section {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.category-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: var(--spacing-md);
}

.category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-lg) var(--spacing-md);
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast), transform var(--transition-base);
}

.category-card:hover {
  border-color: var(--color-brand);
  box-shadow: var(--shadow-sm);
  transform: translateY(-2px);
}

.category-card-icon {
  color: var(--color-brand);
}

.category-card strong {
  font-size: var(--font-base);
  color: var(--color-text-primary);
  font-weight: var(--weight-medium);
}

.more-link {
  font-size: var(--font-sm);
  color: var(--color-text-secondary);
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  transition: color var(--transition-fast);
}

.more-link:hover {
  color: var(--color-brand);
}

/* 推荐商品网格 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  justify-content: start;
  gap: var(--spacing-lg);
}

/* 秒杀区 */
.sk-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-lg);
}

.sk-card {
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  display: flex;
  padding: var(--spacing-md);
  gap: var(--spacing-md);
  transition: border-color var(--transition-fast);
}

.sk-card:hover {
  border-color: var(--color-brand);
}

.sk-image {
  width: 120px;
  height: 120px;
  flex-shrink: 0;
  background: var(--color-bg-subtle);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.sk-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
}

.sk-name {
  font-size: var(--font-base);
  margin: 0 0 var(--spacing-sm);
  color: var(--color-text-primary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.sk-prices {
  display: flex;
  align-items: baseline;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
}

.sk-price {
  color: var(--color-price);
}

.sk-old {
  color: var(--color-text-tertiary);
}

.sk-btn {
  background: var(--color-danger-soft);
  color: var(--color-price);
  border: 1px solid var(--color-error-border);
  padding: var(--spacing-xs) var(--spacing-md);
  border-radius: var(--radius-md);
  font-size: var(--font-sm);
  font-weight: var(--weight-bold);
  cursor: pointer;
  align-self: flex-start;
  transition: background var(--transition-fast), color var(--transition-fast);
}

.sk-btn:hover {
  background: var(--color-price);
  color: var(--color-text-inverse);
}

.sk-empty {
  grid-column: 1 / -1;
  padding: var(--spacing-xl);
  text-align: center;
  background: var(--color-bg-subtle);
  border-radius: var(--radius-md);
  color: var(--color-text-tertiary);
  font-size: var(--font-sm);
}

/* 服务承诺 */
.bottom-promises {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: var(--spacing-lg);
  padding: var(--spacing-xl);
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.bp-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.bp-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.bp-text strong {
  font-size: var(--font-base);
  color: var(--color-text-primary);
  font-weight: var(--weight-medium);
}

.bp-text span {
  font-size: var(--font-sm);
  color: var(--color-text-tertiary);
}

/* 响应式 */
@media (max-width: 1024px) {
  .brand-head {
    grid-template-columns: 1fr;
    gap: var(--spacing-lg);
  }

  .sk-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .flow-line-track {
    justify-content: flex-start;
    overflow-x: auto;
    padding-bottom: var(--spacing-xs);
  }

  .flow-connector {
    margin-bottom: var(--spacing-md);
  }

  .category-card-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
