<script setup lang="ts">
import { notifyError } from '../utils/notify';
import { computed, onMounted, ref } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import { mallApi } from '../api/mall';
import PageState from '../components/PageState.vue';
import type { UnknownRecord } from '../api/types';
import { asList, field, money, orderStatusMap, statusText } from '../utils/format';
import ProductImage from '../components/ProductImage.vue';
import { demoSkuById } from '../catalog/catalogLookup';

const route = useRoute();
const loading = ref(false);
const error = ref('');
const order = ref<UnknownRecord | null>(null);
const orderNo = computed(() => String(route.params.orderNo || ''));
const items = computed(() => asList(field(order.value, ['items', 'orderItems'], [])));
const status = computed(() => statusText(field(order.value, ['status'], null), orderStatusMap));

function orderSkuMeta(row: UnknownRecord) {
  return demoSkuById(field(row, ['skuId'], 0));
}

function orderItemName(row: UnknownRecord) {
  return field(row, ['skuName', 'productName', 'name'], '') || orderSkuMeta(row)?.name || '商品信息不完整';
}

function orderItemSpec(row: UnknownRecord) {
  return field(row, ['spec', 'skuSpec'], '') || orderSkuMeta(row)?.spec || `SKU ${field(row, ['skuId'], '-')}`;
}

function orderItemImage(row: UnknownRecord) {
  return field(row, ['skuImage', 'image', 'mainImage'], '') || orderSkuMeta(row)?.image || '';
}

function orderItemPrice(row: UnknownRecord) {
  const price = Number(field(row, ['price', 'salePrice'], 0));
  return price > 0 ? money(price) : '—';
}

function orderItemSubtotal(row: UnknownRecord) {
  const subtotal = Number(field(row, ['subtotal', 'totalAmount', 'payAmount'], 0));
  const price = Number(field(row, ['price', 'salePrice'], 0));
  const quantity = Number(field(row, ['quantity', 'count'], 0));
  const amount = subtotal || price * quantity;
  return amount > 0 ? money(amount) : '—';
}

function formatAddress(value: unknown) {
  if (!value) return '—';

  if (typeof value === 'string') {
    try {
      const parsed = JSON.parse(value) as UnknownRecord;
      return formatAddress(parsed);
    } catch {
      return value;
    }
  }

  if (typeof value === 'object') {
    const record = value as UnknownRecord;
    const receiver = field(record, ['receiver', 'receiverName', 'name'], '');
    const phone = field(record, ['phone', 'mobile'], '');
    const province = field(record, ['province'], '');
    const city = field(record, ['city'], '');
    const district = field(record, ['district'], '');
    const detail = field(record, ['detail', 'detailAddress', 'address'], '');
    const text = [receiver, phone, `${province}${city}${district}${detail}`]
      .map((item) => String(item || '').trim())
      .filter(Boolean)
      .join('，');
    return text || '—';
  }

  return '—';
}

async function load() {
  loading.value = true;
  error.value = '';
  try {
    order.value = await mallApi.order(orderNo.value);
  } catch (err) {
    order.value = null;
    notifyError(err instanceof Error ? err.message : '订单详情加载失败');
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <section class="cart-layout">
    <el-card class="panel wide-panel">
      <template #header>
        <div class="panel-title-row">
          <div class="panel-title-group">
            <span class="panel-title-main">订单详情</span>
            <span class="panel-title-sub">查看订单状态与商品明细</span>
          </div>
        </div>
      </template>
      <PageState :loading="loading" :error="''" @retry="load" />
      <div v-if="order && !loading" class="order-header">
        <div>
          <span class="hint">订单号</span>
          <h1>{{ orderNo }}</h1>
        </div>
        <el-tag effect="plain">{{ status }}</el-tag>
      </div>
      <el-descriptions v-if="order && !loading" border :column="2" class="mb">
        <el-descriptions-item label="实付金额">{{ money(field(order, ['payAmount', 'totalAmount'], 0)) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ field(order, ['gmtCreate', 'createTime'], '—') }}</el-descriptions-item>
        <el-descriptions-item label="收货信息">{{ formatAddress(field(order, ['addressJson', 'address'], '')) }}</el-descriptions-item>
        <el-descriptions-item label="支付截止">{{ field(order, ['payDeadline'], '—') }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="items.length" class="table-scroll">
        <el-table :data="items" class="stable-table">
          <el-table-column label="商品" min-width="280">
            <template #default="{ row }">
              <div class="line-item">
                <div class="thumb">
                  <ProductImage :src="orderItemImage(row)" :alt="orderItemName(row)" />
                </div>
                <div class="item-info">
                  <strong>{{ orderItemName(row) }}</strong>
                  <span>{{ orderItemSpec(row) }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120">
            <template #default="{ row }">
              <span class="price-cell">{{ orderItemPrice(row) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="100" align="center">
            <template #default="{ row }">
              {{ field(row, ['quantity', 'count'], 0) || '—' }}
            </template>
          </el-table-column>
          <el-table-column label="小计" width="120">
            <template #default="{ row }">
              <span class="subtotal-cell">{{ orderItemSubtotal(row) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <el-empty v-if="order && !items.length" description="暂无订单商品信息" />
    </el-card>

    <el-card class="panel summary-panel">
      <template #header>
        <div class="panel-title-row">
          <div class="panel-title-group">
            <span class="panel-title-main">后续操作</span>
            <span class="panel-title-sub">继续支付或返回</span>
          </div>
        </div>
      </template>
      <RouterLink :to="`/pay/${orderNo}`">
        <el-button type="primary" class="full-button" :disabled="!order">去支付</el-button>
      </RouterLink>
      <RouterLink to="/cart">
        <el-button plain class="full-button mt">返回购物车</el-button>
      </RouterLink>
    </el-card>
  </section>
</template>
