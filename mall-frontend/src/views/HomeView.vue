<script setup lang="ts">
import { notifyError } from '../utils/notify';
import { computed, onMounted, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { mallApi } from '../api/mall';
import PageState from '../components/PageState.vue';
import type { UnknownRecord } from '../api/types';
import { field } from '../utils/format';
import { heroImage, resolveProductImage } from '../catalog/productAssets';
import ProductImage from '../components/ProductImage.vue';
import { Lock, Van, CircleCheckFilled, User, ShoppingCart, Document, Wallet, Check, ArrowRight } from '@element-plus/icons-vue';

const router = useRouter();
const loading = ref(false);
const error = ref('');
const categories = ref<UnknownRecord[]>([]);
const products = ref<UnknownRecord[]>([]);
const seckillProducts = ref<UnknownRecord[]>([]);
const seckillError = ref(false);

const homeProductIds = [1001, 1002, 1003, 1004, 1005, 1006];

const displayProducts = computed(() => products.value.slice(0, 6));

const categoryTabs = [
  { label: '精选推荐', keyword: '' },
  { label: '数码家电', keyword: 'iPhone' },
  { label: '美妆护肤', keyword: '美妆' },
  { label: '家居生活', keyword: '家居' },
  { label: '运动户外', keyword: '运动' },
  { label: '食品生鲜', keyword: '食品' },
  { label: '母婴玩具', keyword: '母婴' },
];

const flowSteps = [
  { label: '登录', sub: '账户登录', icon: User },
  { label: '购物车', sub: '加入商品', icon: ShoppingCart },
  { label: '下单', sub: '提交订单', icon: Document },
  { label: '支付', sub: '支付完成', icon: Wallet },
  { label: '订单状态', sub: '查看跟踪', icon: Check },
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

    // 加载秒杀活动
    try {
      const seckillResult = await mallApi.seckillActivities();
      const activities = Array.isArray(seckillResult) ? seckillResult : [];
      // 过滤掉明显的测试/乱码活动
      seckillProducts.value = activities.filter((activity: UnknownRecord) => {
        const name = String(field(activity, ['title', 'name'], ''));
        return !/JMeter|压测|测试|乱码|test/i.test(name);
      });
      seckillError.value = false;
    } catch {
      // 秒杀接口失败
      seckillProducts.value = [];
      seckillError.value = true;
    }

    if (categoryResult.status === 'rejected' || products.value.length === 0) {
      error.value = '商品服务暂不可用，无法加载首页商品';
      notifyError('Gateway 或商品服务暂不可用，首页已进入错误状态。');
    }
  } catch (err) {
    error.value = '商品服务暂不可用，无法加载首页商品';
  } finally {
    loading.value = false;
  }
}

function selectCategory(tab: { label: string; keyword: string }) {
  if (tab.keyword) {
    router.push({ path: '/search', query: { keyword: tab.keyword } });
  } else {
    router.push('/search');
  }
}

function getPrice(product: UnknownRecord) {
  const skus = product.skus as UnknownRecord[] | undefined;
  return Number(field(skus?.[0], ['price'], 0)).toFixed(2);
}
function getImage(product: UnknownRecord) {
  return resolveProductImage(product);
}
function getSeckillImage(activity: UnknownRecord) {
  // 秒杀活动可能包含 spuId 字段，优先使用；其次尝试从 skuId 反推（需后端配合）
  const spuId = field<number>(activity, ['spuId'], 0);
  if (spuId) {
    return resolveProductImage({ spuId });
  }
  // 兜底：若无 spuId，返回空字符串让 ProductImage 组件显示 placeholder
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
      empty-description="请确认商品服务已启动并存在演示商品。"
      @retry="loadHome"
    />

    <div v-if="!loading" class="home-grid">
      <!-- Top Section: Hero & Flow -->
      <div class="hero-section">
        <div class="hero-banner" :style="{ backgroundImage: `url(${heroImage})`, backgroundSize: 'cover', backgroundPosition: 'center right', backgroundRepeat: 'no-repeat' }">
          <div class="hero-content">
            <h1 class="hero-title">MallCloud<br/>让购物更简单</h1>
            <p class="hero-subtitle">精选好物 · 品质保障 · 极速送达</p>

            <div class="hero-guarantees">
              <div class="guarantee-item">
                <el-icon color="#1b61c9" :size="24"><Lock /></el-icon>
                <div class="g-text">
                  <strong>正品保障</strong>
                  <span>品牌直供</span>
                </div>
              </div>
              <div class="guarantee-item">
                <el-icon color="#1b61c9" :size="24"><Van /></el-icon>
                <div class="g-text">
                  <strong>极速配送</strong>
                  <span>211限时达</span>
                </div>
              </div>
              <div class="guarantee-item">
                <el-icon color="#1b61c9" :size="24"><CircleCheckFilled /></el-icon>
                <div class="g-text">
                  <strong>无忧售后</strong>
                  <span>7天无理由</span>
                </div>
              </div>
            </div>

            <RouterLink to="/search" class="hero-action">
              <el-button type="primary" size="large" class="hero-btn">立即选购 <el-icon class="el-icon--right"><ArrowRight /></el-icon></el-button>
            </RouterLink>
          </div>
          
        </div>

        <el-card class="panel flow-panel" shadow="never">
          <template #header>
            <div class="panel-title-row">
              <div class="panel-title-group">
                <span class="panel-title-main">交易链路</span>
                <span class="panel-title-tag">(演示流程)</span>
              </div>
              <span class="panel-title-sub">完整电商交易闭环体验</span>
            </div>
          </template>

          <div class="flow-track">
            <template v-for="(step, index) in flowSteps" :key="index">
              <div class="flow-node">
                <div class="node-icon">
                  <el-icon><component :is="step.icon" /></el-icon>
                </div>
                <strong>{{ step.label }}</strong>
                <span>{{ step.sub }}</span>
              </div>
              <div v-if="index < flowSteps.length - 1" class="flow-line">
                <el-icon><ArrowRight /></el-icon>
              </div>
            </template>
          </div>

        </el-card>
      </div>

      <!-- Recommendation Section -->
      <el-card class="panel recommend-section" shadow="never">
        <template #header>
          <div class="panel-title-row">
            <div class="panel-title-group">
              <span class="panel-title-main">热门频道</span>
              <span class="panel-title-sub">点击跳转搜索页</span>
            </div>
          </div>
        </template>
        <div class="category-tabs">
          <button
            v-for="tab in categoryTabs"
            :key="tab.label"
            :class="['tab-btn']"
            @click="selectCategory(tab)"
          >
            {{ tab.label }}
          </button>
          <RouterLink to="/search" class="more-link ml-auto">更多 <el-icon><ArrowRight /></el-icon></RouterLink>
        </div>

        <div class="product-grid">
          <RouterLink
            v-for="product in displayProducts"
            :key="String(field(product, ['spuId', 'id']))"
            :to="`/products/${field(product, ['spuId', 'id'])}`"
            class="product-card"
          >
            <div class="p-image">
              <ProductImage :src="getImage(product)" :alt="String(product.name)" />
            </div>
            <div class="p-info">
              <h3 class="p-name">{{ product.name }}</h3>
              <p class="p-desc">{{ product.description }}</p>
              <div class="p-bottom">
                <div class="p-price">
                  <span class="currency">¥</span>
                  <span class="amount">{{ getPrice(product) }}</span>
                </div>
                <button class="cart-btn" @click.prevent="router.push(`/products/${field(product, ['spuId', 'id'])}`)">
                  <el-icon><ShoppingCart /></el-icon>
                </button>
              </div>
            </div>
          </RouterLink>
        </div>
      </el-card>

      <!-- Flash Sale Section -->
      <el-card class="panel seckill-section" shadow="never">
        <template #header>
          <div class="panel-title-row">
            <div class="panel-title-group">
              <span class="panel-title-main">限时秒杀</span>
              <span class="panel-title-sub">每日优选好货</span>
            </div>
            <RouterLink to="/seckill" class="more-link panel-title-link">更多秒杀 <el-icon><ArrowRight /></el-icon></RouterLink>
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
              <ProductImage :src="getSeckillImage(product)" :alt="field(product, ['title', 'name'])" />
            </div>
            <div class="sk-info">
              <h3 class="sk-name">{{ field(product, ['title', 'name']) }}</h3>
              <div class="sk-prices">
                <strong class="sk-price">¥{{ field(product, ['seckillPrice', 'price'], 0) }}</strong>
                <span class="sk-old">¥{{ field(product, ['price', 'oldPrice', 'originalPrice'], 0) }}</span>
              </div>
              <button class="sk-btn" @click="router.push('/seckill')">立即抢购</button>
            </div>
          </div>
        </div>
      </el-card>


      <!-- Bottom Promises -->
      <div class="bottom-promises">
        <div v-for="promise in bottomPromises" :key="promise.title" class="bp-item">
          <el-icon class="bp-icon" color="#1b61c9" :size="32"><component :is="promise.icon" /></el-icon>
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
.home-wrapper {
}

.home-grid {
  display: flex;
  flex-direction: column;
  gap: 40px;
  padding-top: var(--spacing-xl);
}

/* Hero Section */
.hero-section {
  display: grid;
  grid-template-columns: minmax(540px, 1.8fr) minmax(460px, 1.2fr);
  gap: var(--spacing-lg);
}

.hero-banner {
  display: flex;
  align-items: center;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  min-height: 310px;
  padding: 48px 44px;
}
.hero-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  width: 38%;
  max-width: 400px;
  min-width: 0;
}
@media (max-width: 768px) {
  .hero-banner {
    min-height: 420px;
    background-position: center bottom !important;
    padding: 32px 24px;
  }
}

.hero-title {
  max-width: 400px;
  font-size: 36px;
  font-weight: 800;
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-sm);
  letter-spacing: -0.02em;
  line-height: 1.14;
  white-space: normal;
}

.hero-subtitle {
  max-width: 380px;
  font-size: 18px;
  color: var(--color-text-muted);
  margin-bottom: 22px;
}

.hero-guarantees {
  display: flex;
  gap: var(--spacing-lg);
  margin-bottom: 22px;
  white-space: nowrap;
}

.guarantee-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: 14px;
}

.g-text {
  display: flex;
  flex-direction: column;
}

.g-text strong {
  color: var(--color-text-secondary);
}

.g-text span {
  color: var(--color-text-light);
  font-size: 12px;
}

.hero-btn {
  font-size: 16px;
  padding: 12px 32px;
  border-radius: 30px;
  height: auto;
  text-decoration: none;
}

/* Flow Panel */
.flow-panel {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.flow-track {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 10px;
}

.flow-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.node-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--color-brand-light);
  color: var(--color-brand);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.flow-node strong {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.flow-node span {
  font-size: 11px;
  color: var(--color-text-light);
}

.flow-line {
  color: var(--color-border);
  margin-bottom: 16px;
}

.flow-footer {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  background: var(--color-surface-hover);
  padding: 12px;
  border-radius: var(--radius-md);
  text-decoration: none;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.flow-footer .arrow {
  margin-left: auto;
  color: var(--color-text-light);
}

/* Sections Common */
.section-header {
  margin-bottom: 16px;
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0;
}

.more-link {
  font-size: 14px;
  color: var(--color-text-muted);
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 4px;
}
.more-link:hover {
  color: var(--color-brand);
}

/* Recommend Section */
.category-tabs {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: 24px;
}

.tab-btn {
  background: none;
  border: none;
  font-size: 15px;
  color: var(--color-text-muted);
  padding: 6px 16px;
  border-radius: 20px;
  cursor: pointer;
}

.tab-btn:hover {
  color: var(--color-brand);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 240px));
  justify-content: start;
  gap: 20px;
}

.product-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  overflow: hidden;
  text-decoration: none;
  display: flex;
  flex-direction: column;
  border: 1px solid transparent;
}

.product-card:hover {
  box-shadow: 0 8px 24px rgba(0,0,0,0.05);
  border-color: var(--color-brand-light);
}

.p-image {
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  padding: 16px;
}

.p-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.p-info {
  padding: 0 16px 16px 16px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.p-name {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0 0 4px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-weight: 500;
  height: 40px;
}

.p-desc {
  font-size: 12px;
  color: var(--color-text-light);
  margin: 0 0 auto 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.p-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
}

.p-price {
  color: var(--color-price);
  font-weight: 700;
}

.currency {
  font-size: 12px;
}

.amount {
  font-size: 20px;
}

.cart-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: var(--color-brand-light);
  color: var(--color-brand);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.cart-btn:hover {
  background: var(--color-brand);
  color: white;
}

/* Seckill Section */

.sk-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.sk-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  display: flex;
  padding: 16px;
  gap: 16px;
}

.sk-card:hover {
  border-color: var(--color-error-border);
  box-shadow: 0 4px 12px var(--color-error-shadow);
}

.sk-image {
  width: 120px;
  height: 120px;
  flex-shrink: 0;
  background: var(--color-surface);
  border-radius: var(--radius-md);
}

.sk-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.sk-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.sk-name {
  font-size: 15px;
  margin: 0 0 8px 0;
  color: var(--color-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.sk-prices {
  margin-bottom: 12px;
}

.sk-price {
  color: var(--color-price);
  font-size: 20px;
  font-weight: bold;
  margin-right: 8px;
}

.sk-old {
  color: var(--color-text-light);
  text-decoration: line-through;
  font-size: 12px;
}

.sk-btn {
  background: var(--color-error-bg);
  color: var(--color-price);
  border: 1px solid var(--color-error-border);
  padding: 6px 16px;
  border-radius: 20px;
  font-weight: bold;
  cursor: pointer;
  align-self: flex-start;
}

.sk-btn:hover {
  background: var(--color-price);
  color: white;
}


/* Bottom Promises */
.bottom-promises {
  display: flex;
  justify-content: space-between;
  padding: 40px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  margin-top: 20px;
}

.bp-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.bp-text {
  display: flex;
  flex-direction: column;
}

.bp-text strong {
  font-size: 18px;
  color: var(--color-text-secondary);
}

.bp-text span {
  font-size: 14px;
  color: var(--color-text-light);
}

/* Responsive adjustments */
@media (max-width: 1200px) {
  .sk-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 900px) {
  .hero-section {
    grid-template-columns: 1fr;
  }
}

.panel-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.panel-title-group {
  display: flex;
  align-items: center;
  gap: 8px;
}
.panel-title-main {
  font-size: 20px;
  font-weight: bold;
  color: var(--color-text-primary);
}
.panel-title-tag {
  font-size: 12px;
  color: var(--color-brand);
  background: var(--color-brand-light);
  padding: 2px 8px;
  border-radius: 12px;
}
.panel-title-sub {
  font-size: 13px;
  color: var(--color-text-light);
  font-weight: normal;
}
.panel-title-link {
  font-size: 14px;
  font-weight: normal;
  margin-left: auto;
}

.ml-auto {
  margin-left: auto;
}
.sk-empty {
  grid-column: 1 / -1;
  padding: 40px;
  text-align: center;
  background: var(--color-surface-hover);
  border-radius: 8px;
}


</style>
