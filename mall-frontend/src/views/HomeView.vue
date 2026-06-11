<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { mallApi } from '../api/mall';
import PageState from '../components/PageState.vue';
import type { UnknownRecord } from '../api/types';
import { field } from '../utils/format';
import { heroImage, seedCatalogProducts, seckillProducts, onlineProductImage } from '../catalog/productAssets';
import ProductImage from '../components/ProductImage.vue';
import { Lock, Van, CircleCheckFilled, User, ShoppingCart, Document, Wallet, Check, ArrowRight } from '@element-plus/icons-vue';

const router = useRouter();
const loading = ref(false);
const error = ref('');
const categories = ref<UnknownRecord[]>([]);
const products = ref<UnknownRecord[]>([]);

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
  { label: '登录', sub: '账户登录', icon: User },
  { label: '购物�?, sub: '加入商品', icon: ShoppingCart },
  { label: '下单', sub: '提交订单', icon: Document },
  { label: '支付', sub: '支付完成', icon: Wallet },
  { label: '订单状�?, sub: '查看跟踪', icon: Check },
];

const dashboardItems = [
  { name: 'API Gateway', sub: '健康检�?, status: '运行�?, metricLabel: '状�?, metricValue: '正常', icon: '☁️', color: '#1b61c9' },
  { name: 'Nacos 注册中心', sub: '服务实例', status: '运行�?, metricLabel: '实例�?, metricValue: '32 �?, icon: '�?, color: '#1b61c9' },
  { name: 'Seata 分布式事�?, sub: '事务成功�?, status: '运行�?, metricLabel: '成功�?, metricValue: '99.96%', icon: '🔄', color: '#1b61c9' },
  { name: 'RocketMQ 消息队列', sub: '消息堆积', status: '运行�?, metricLabel: '堆积', metricValue: '0', icon: '🚀', color: '#e65100' },
  { name: 'Sentinel 流量防护', sub: 'QPS 限流', status: '运行�?, metricLabel: '状�?, metricValue: '正常', icon: '🛡�?, color: '#1b61c9' },
];

const bottomPromises = [
  { title: '30天价�?, sub: '买贵退差价', icon: Lock },
  { title: '破损包退', sub: '免费上门取件', icon: Document },
  { title: '闪电退�?, sub: '极速到�?, icon: Wallet },
  { title: '24小时客服', sub: '专业服务', icon: User },
];

const currentCategory = ref('精选推�?);
const categoryTabs = ['精选推�?, '数码家电', '美妆护肤', '家居生活', '运动户外', '食品生鲜', '母婴玩具'];

const timeRemaining = ref('02 : 18 : 36');

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
      error.value = 'Gateway 或商品服务暂不可用，首页已进入错误状态�?;
    }
  } finally {
    loading.value = false;
  }
}

function getPrice(product: UnknownRecord) {
  const skus = product.skus as UnknownRecord[] | undefined;
  return Number(field(skus?.[0], ['price'], 0)).toFixed(2);
}
function getImage(product: UnknownRecord) {
  return onlineProductImage(product);
}

onMounted(loadHome);
</script>

<template>
  <div class="home-wrapper">
    <PageState :loading="loading" :error="error" @retry="loadHome" />

    <div v-if="!loading" class="home-grid">
      <!-- Top Section: Hero & Flow -->
      <div class="hero-section">
        <div class="hero-banner">
          <div class="hero-content">
            <h1 class="hero-title">MallCloud 让购物更简�?/h1>
            <p class="hero-subtitle">精选好�?· 品质保障 · 极速送达</p>

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
                  <strong>极速配�?/strong>
                  <span>211限时�?/span>
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

          <div class="hero-image">
            <img :src="heroImage" alt="MallCloud Products" />
          </div>
        </div>

        <div class="flow-panel">
          <div class="panel-header">
            <div class="ph-left">
              <h3>交易链路</h3>
              <span class="ph-tag">(演示流程)</span>
            </div>
            <p class="ph-desc">完整电商交易闭环体验</p>
          </div>

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

          <RouterLink to="/tech" class="flow-footer">
            <el-icon color="#1b61c9"><CircleCheckFilled /></el-icon>
            <span>全链路微服务支撑，保障交易安全与高可�?/span>
            <el-icon class="arrow"><ArrowRight /></el-icon>
          </RouterLink>
        </div>
      </div>

      <!-- Recommendation Section -->
      <div class="recommend-section">
        <div class="section-header">
          <h2 class="section-title">为你推荐</h2>
        </div>
        <div class="category-tabs">
          <button
            v-for="cat in categoryTabs"
            :key="cat"
            :class="['tab-btn', { active: currentCategory === cat }]"
            @click="currentCategory = cat"
          >
            {{ cat }}
          </button>
          <RouterLink to="/search" class="more-link" style="margin-left: auto;">更多 <el-icon><ArrowRight /></el-icon></RouterLink>
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
      </div>

      <!-- Flash Sale Section -->
      <div class="seckill-section">
        <div class="seckill-header">
          <div class="sk-title">
            <h2>限时秒杀</h2>
            <span class="sk-badge">正在疯抢</span>
          </div>
          <div class="sk-timer">
            <span>距结�?/span>
            <div class="time-blocks">
              <span class="t-block">{{ timeRemaining.split(':')[0].trim() }}</span> :
              <span class="t-block">{{ timeRemaining.split(':')[1].trim() }}</span> :
              <span class="t-block">{{ timeRemaining.split(':')[2].trim() }}</span>
            </div>
          </div>
          <RouterLink to="/seckill" class="more-link" style="margin-left: auto;">更多秒杀 <el-icon><ArrowRight /></el-icon></RouterLink>
        </div>

        <div class="sk-grid">
          <div v-for="product in seckillProducts" :key="product.spuId" class="sk-card">
            <div class="sk-image">
              <ProductImage :src="product.mainImage" :alt="product.name" />
            </div>
            <div class="sk-info">
              <h3 class="sk-name">{{ product.name }}</h3>
              <div class="sk-prices">
                <strong class="sk-price">¥{{ product.price }}</strong>
                <span class="sk-old">¥{{ product.oldPrice }}</span>
              </div>
              <button class="sk-btn">立即抢购</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Tech Dashboard Section -->
      <div class="tech-section">
        <div class="tech-header">
          <h2>演示工具 <span>(微服务治理与中间�?</span></h2>
          <RouterLink to="/tech" class="more-link" style="margin-left: auto;">进入控制�?<el-icon><ArrowRight /></el-icon></RouterLink>
        </div>

        <div class="tech-grid">
          <div v-for="item in dashboardItems" :key="item.name" class="tech-card">
            <div class="tc-top">
              <div class="tc-icon" :style="{ color: item.color }">{{ item.icon }}</div>
              <div class="tc-info">
                <strong>{{ item.name }}</strong>
                <span>{{ item.sub }}</span>
              </div>
              <div class="tc-status">
                <span class="dot"></span> {{ item.status }}
              </div>
            </div>
            <div class="tc-bottom">
              <span class="tc-label">{{ item.metricLabel }}</span>
              <strong class="tc-value" :class="{ normal: item.metricValue === '正常' }">
                <el-icon v-if="item.metricValue === '正常'"><CircleCheckFilled /></el-icon>
                {{ item.metricValue }}
              </strong>
            </div>
          </div>
        </div>
      </div>

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
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 var(--spacing-xl);
  padding-bottom: 60px;
  background-color: #f7f8fa;
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
  grid-template-columns: 3fr 1fr;
  gap: var(--spacing-lg);
}

.hero-banner {
  background: linear-gradient(135deg, #eaf4ff 0%, #f7f8fa 100%);
  border-radius: var(--radius-xl);
  padding: 40px;
  display: flex;
  align-items: center;
  position: relative;
  overflow: hidden;
  min-height: 380px;
}

.hero-content {
  flex: 1;
  z-index: 2;
  max-width: 50%;
}

.hero-title {
  font-size: 42px;
  font-weight: 800;
  color: #1a1a1a;
  margin-bottom: var(--spacing-sm);
  letter-spacing: -0.02em;
  white-space: nowrap;
}

.hero-subtitle {
  font-size: 18px;
  color: #666;
  margin-bottom: 30px;
}

.hero-guarantees {
  display: flex;
  gap: var(--spacing-lg);
  margin-bottom: 30px;
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
  color: #333;
}

.g-text span {
  color: #999;
  font-size: 12px;
}

.hero-btn {
  font-size: 16px;
  padding: 12px 32px;
  border-radius: 30px;
  height: auto;
  text-decoration: none;
}

.hero-image {
  position: absolute;
  right: -40px;
  bottom: -20px;
  top: 0;
  width: 55%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-image img {
  width: 100%;
  height: 110%;
  object-fit: contain;
}

/* Flow Panel */
.flow-panel {
  background: white;
  border-radius: var(--radius-xl);
  padding: 24px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.panel-header {
  margin-bottom: var(--spacing-lg);
}

.ph-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.ph-left h3 {
  font-size: 18px;
  margin: 0;
  color: #333;
}

.ph-tag {
  background: var(--color-brand-light);
  color: var(--color-brand);
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 12px;
}

.ph-desc {
  font-size: 13px;
  color: #666;
  margin-top: 4px;
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
  background: #f0f6ff;
  color: var(--color-brand);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.flow-node strong {
  font-size: 13px;
  color: #333;
}

.flow-node span {
  font-size: 11px;
  color: #999;
}

.flow-line {
  color: #ccc;
  margin-bottom: 16px;
}

.flow-footer {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  background: #f7f8fa;
  padding: 12px;
  border-radius: var(--radius-md);
  text-decoration: none;
  font-size: 13px;
  color: #333;
}

.flow-footer .arrow {
  margin-left: auto;
  color: #999;
}

/* Sections Common */
.section-header {
  margin-bottom: 16px;
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}

.more-link {
  font-size: 14px;
  color: #666;
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
  color: #666;
  padding: 6px 16px;
  border-radius: 20px;
  cursor: pointer;
  transition: background-color, border-color, color, fill, stroke, opacity, box-shadow, transform 0.2s;
}

.tab-btn:hover {
  color: var(--color-brand);
}

.tab-btn.active {
  background: var(--color-brand);
  color: white;
  font-weight: 500;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 20px;
}

.product-card {
  background: white;
  border-radius: var(--radius-lg);
  overflow: hidden;
  text-decoration: none;
  display: flex;
  flex-direction: column;
  transition: background-color, border-color, color, fill, stroke, opacity, box-shadow, transform 0.2s;
  border: 1px solid transparent;
}

.product-card:hover {
  transform: translateY(-2px);
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
  color: #333;
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
  color: #999;
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
  color: #e62828;
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
  background: #f0f6ff;
  color: var(--color-brand);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background-color, border-color, color, fill, stroke, opacity, box-shadow, transform 0.2s;
}

.cart-btn:hover {
  background: var(--color-brand);
  color: white;
}

/* Seckill Section */
.seckill-section {
  background: white;
  border-radius: var(--radius-xl);
  padding: 24px;
}

.seckill-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
}

.sk-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.sk-title h2 {
  font-size: 24px;
  margin: 0;
  color: #333;
}

.sk-badge {
  background: linear-gradient(90deg, #ff4141, #ff6b6b);
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: bold;
}

.sk-timer {
  margin-left: 40px;
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: 14px;
  color: #333;
}

.time-blocks {
  display: flex;
  align-items: center;
  gap: 4px;
}

.t-block {
  background: #ff4141;
  color: white;
  padding: 4px 8px;
  border-radius: 6px;
  font-weight: bold;
}

.sk-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.sk-card {
  background: #fdfdfd;
  border: 1px solid #f0f0f0;
  border-radius: var(--radius-lg);
  display: flex;
  padding: 16px;
  gap: 16px;
  transition: background-color, border-color, color, fill, stroke, opacity, box-shadow, transform 0.2s;
}

.sk-card:hover {
  border-color: #ffd6d6;
  box-shadow: 0 4px 12px rgba(255,65,65,0.1);
}

.sk-image {
  width: 120px;
  height: 120px;
  flex-shrink: 0;
  background: white;
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
  color: #333;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.sk-prices {
  margin-bottom: 12px;
}

.sk-price {
  color: #ff4141;
  font-size: 20px;
  font-weight: bold;
  margin-right: 8px;
}

.sk-old {
  color: #999;
  text-decoration: line-through;
  font-size: 12px;
}

.sk-btn {
  background: #fff0f0;
  color: #ff4141;
  border: 1px solid #ffd6d6;
  padding: 6px 16px;
  border-radius: 20px;
  font-weight: bold;
  cursor: pointer;
  align-self: flex-start;
  transition: background-color, border-color, color, fill, stroke, opacity, box-shadow, transform 0.2s;
}

.sk-btn:hover {
  background: #ff4141;
  color: white;
}

/* Tech Section */
.tech-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
}

.tech-header h2 {
  font-size: 24px;
  margin: 0;
  color: #333;
}

.tech-header span {
  font-size: 16px;
  color: #999;
  font-weight: normal;
  margin-left: 8px;
}

.tech-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
}

.tech-card {
  background: white;
  border-radius: var(--radius-lg);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.02);
}

.tc-top {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
}

.tc-icon {
  font-size: 28px;
}

.tc-info {
  display: flex;
  flex-direction: column;
}

.tc-info strong {
  font-size: 15px;
  color: #333;
}

.tc-info span {
  font-size: 12px;
  color: #999;
}

.tc-status {
  position: absolute;
  top: -4px;
  right: -4px;
  font-size: 12px;
  color: #52c41a;
  display: flex;
  align-items: center;
  gap: 4px;
}

.dot {
  width: 6px;
  height: 6px;
  background: #52c41a;
  border-radius: 50%;
}

.tc-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px dashed var(--color-border);
}

.tc-label {
  font-size: 13px;
  color: #666;
}

.tc-value {
  font-size: 15px;
  color: #333;
  font-weight: bold;
}

.tc-value.normal {
  color: #52c41a;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* Bottom Promises */
.bottom-promises {
  display: flex;
  justify-content: space-between;
  padding: 40px;
  background: white;
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
  color: #333;
}

.bp-text span {
  font-size: 14px;
  color: #999;
}

/* Responsive adjustments */
@media (max-width: 1200px) {
  .product-grid {
    grid-template-columns: repeat(4, 1fr);
  }
  .sk-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 900px) {
  .hero-section {
    grid-template-columns: 1fr;
  }
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .tech-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
