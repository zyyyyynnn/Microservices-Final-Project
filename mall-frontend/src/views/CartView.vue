<script setup lang="ts">
import { notifyError } from '../utils/notify';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { mallApi } from '../api/mall';
import PageState from '../components/PageState.vue';
import OrderSummary from '../components/OrderSummary.vue';
import type { Cart, CartItem, UnknownRecord } from '../api/types';
import { money, field } from '../utils/format';
import ProductImage from '../components/ProductImage.vue';
import { demoSkuById } from '../catalog/catalogLookup';

function cartSkuMeta(row: UnknownRecord) {
  return demoSkuById(field(row, ['skuId']));
}

function cartItemName(row: UnknownRecord) {
  return field(row, ['skuName', 'productName', 'name'], '') || cartSkuMeta(row)?.name || '商品信息不完整';
}

function cartItemSpec(row: UnknownRecord) {
  return field(row, ['spec', 'skuSpec'], '') || cartSkuMeta(row)?.spec || `SKU ${field(row, ['skuId'], '-')}`;
}

function cartItemImage(row: UnknownRecord) {
  return field(row, ['skuImage', 'image', 'mainImage'], '') || cartSkuMeta(row)?.image || '';
}

const router = useRouter();
const loading = ref(false);
const error = ref('');
const cart = ref<Cart>({ items: [], totalAmount: 0, totalQuantity: 0 });
const submittingSku = ref<number | null>(null);

const selectedItems = computed(() => cart.value.items.filter((item) => item.selected !== false));

async function load() {
  loading.value = true;
  error.value = '';
  try {
    cart.value = await mallApi.cart();
  } catch (err) {
    notifyError(err instanceof Error ? err.message : '购物车加载失败');
  } finally {
    loading.value = false;
  }
}

async function updateQuantity(item: CartItem) {
  submittingSku.value = item.skuId;
  try {
    await mallApi.updateCartQuantity(item.skuId, item.quantity);
    await load();
    ElMessage.success('数量已更新');
  } catch (err) {
    notifyError(err instanceof Error ? err.message : '数量更新失败');
  } finally {
    submittingSku.value = null;
  }
}

async function updateSelected(item: CartItem) {
  submittingSku.value = item.skuId;
  try {
    await mallApi.updateCartSelected(item.skuId, item.selected !== false);
    await load();
  } catch (err) {
    notifyError(err instanceof Error ? err.message : '选中状态更新失败');
  } finally {
    submittingSku.value = null;
  }
}

async function removeItem(item: CartItem) {
  submittingSku.value = item.skuId;
  try {
    await mallApi.deleteCartItem(item.skuId);
    await load();
    ElMessage.success('已删除商品');
  } catch (err) {
    notifyError(err instanceof Error ? err.message : '删除失败');
  } finally {
    submittingSku.value = null;
  }
}

function goCheckout() {
  router.push('/checkout');
}

onMounted(load);
</script>

<template>
  <section class="cart-layout">
    <el-card class="panel wide-panel">
      <template #header>
        <div class="panel-title">购物车</div>
      </template>
      <PageState
        :loading="loading"
        :error="''"
        @retry="load"
      />
      <div v-if="!loading && !error && cart.items.length === 0" class="empty-action">
        <el-empty description="购物车空空如也">
          <el-button type="primary" round @click="router.push('/')">去逛逛</el-button>
        </el-empty>
      </div>
      <div v-if="cart.items.length" class="table-scroll">
        <el-table :data="cart.items" class="stable-table">
          <el-table-column label="选择" width="70" align="center">
            <template #default="{ row }">
              <el-checkbox
                v-model="row.selected"
                :disabled="submittingSku === row.skuId"
                @change="updateSelected(row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="商品" min-width="260">
            <template #default="{ row }">
              <div class="line-item">
                <div class="thumb">
                  <ProductImage :src="cartItemImage(row)" :alt="cartItemName(row)" />
                </div>
                <div class="item-info">
                  <strong>{{ cartItemName(row) }}</strong>
                  <span>{{ cartItemSpec(row) }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120">
            <template #default="{ row }">
              <span class="price-cell">{{ money(row.price) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="150" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="row.quantity"
                :min="1"
                size="small"
                :disabled="submittingSku === row.skuId"
                @change="updateQuantity(row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="120">
            <template #default="{ row }">
              <span class="subtotal-cell">{{ money(row.subtotal ?? Number(row.price || 0) * Number(row.quantity || 0)) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="90" align="center">
            <template #default="{ row }">
              <el-button text type="danger" :disabled="submittingSku === row.skuId" @click="removeItem(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <el-card class="panel summary-panel">
      <template #header>
        <div class="panel-title">结算摘要</div>
      </template>
      <OrderSummary :items="selectedItems" />
      <el-button
        class="full-button"
        type="primary"
        :disabled="selectedItems.length === 0"
        @click="goCheckout"
      >
        去结算
      </el-button>
    </el-card>
  </section>
</template>
