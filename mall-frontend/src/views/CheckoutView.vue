<script setup lang="ts">
import { notifyError } from '../utils/notify';
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { mallApi } from '../api/mall';
import PageState from '../components/PageState.vue';
import OrderSummary from '../components/OrderSummary.vue';
import type { Address, CartItem } from '../api/types';
import { money } from '../utils/format';
import { demoSkuById } from '../catalog/catalogLookup';
import ProductImage from '../components/ProductImage.vue';

function checkoutSkuMeta(row: CartItem) {
  return demoSkuById(row.skuId);
}

function checkoutItemName(row: CartItem) {
  return row.skuName || checkoutSkuMeta(row)?.name || '商品信息不完整';
}

function checkoutItemSpec(row: CartItem) {
  return checkoutSkuMeta(row)?.spec || `SKU ${row.skuId}`;
}

function checkoutItemImage(row: CartItem) {
  return row.skuImage || checkoutSkuMeta(row)?.image || '';
}

function checkoutItemPrice(row: CartItem) {
  const price = Number(row.price ?? 0);
  return price > 0 ? money(price) : '—';
}

function checkoutItemSubtotal(row: CartItem) {
  const subtotal = Number(row.subtotal ?? Number(row.price || 0) * Number(row.quantity || 0));
  return subtotal > 0 ? money(subtotal) : '—';
}


const route = useRoute();
const router = useRouter();
const loading = ref(false);
const submitting = ref(false);
const error = ref('');
const addresses = ref<Address[]>([]);
const items = ref<CartItem[]>([]);
const selectedAddressId = ref<number | null>(null);
const remark = ref('frontend-checkout');

const directItem = computed(() => Number(route.query.skuId || 0) > 0);

function selectAddress(id: number) {
  selectedAddressId.value = id;
}

async function load() {
  loading.value = true;
  error.value = '';
  try {
    addresses.value = await mallApi.listAddresses();
    selectedAddressId.value = addresses.value[0]?.id || 1;
    if (directItem.value) {
      const skuId = Number(route.query.skuId);
      const meta = demoSkuById(skuId);
      items.value = [{
        skuId,
        skuName: meta?.name,
        skuImage: meta?.image,
        quantity: Number(route.query.quantity || 1),
        selected: true,
      }];
    } else {
      const cart = await mallApi.cart();
      items.value = cart.items.filter((item) => item.selected !== false);
    }
  } catch (err) {
    notifyError(err instanceof Error ? err.message : '订单确认信息加载失败');
  } finally {
    loading.value = false;
  }
}

async function submitOrder() {
  if (!selectedAddressId.value || items.value.length === 0) return;
  submitting.value = true;
  try {
    const result = await mallApi.createOrder({
      addressId: selectedAddressId.value,
      items: items.value.map((item) => ({ skuId: item.skuId, quantity: item.quantity })),
      remark: remark.value,
    });
    ElMessage.success(`订单已创建：${result.orderNo}`);
    router.push(`/orders/${result.orderNo}`);
  } catch (err) {
    notifyError(err instanceof Error ? err.message : '创建订单失败');
  } finally {
    submitting.value = false;
  }
}

onMounted(load);
</script>

<template>
  <section class="cart-layout">
    <el-card class="panel wide-panel">
      <template #header>
        <div class="panel-title">订单确认</div>
      </template>
      <PageState
        :loading="loading"
        :error="''"
        @retry="load"
      />
      <div v-if="!loading && !error && items.length === 0" class="empty-action">
        <el-empty description="暂无可结算商品，请先选购商品">
          <el-button type="primary" round @click="router.push('/')">去逛逛</el-button>
        </el-empty>
      </div>

      <div v-if="!loading && items.length" class="checkout-section">
        <h2 class="section-title">收货地址</h2>
        <div class="address-card-grid" v-if="addresses.length">
          <div
            v-for="address in addresses"
            :key="address.id"
            class="address-option"
            :class="{ active: address.id === selectedAddressId }"
            @click="selectAddress(address.id || 1)"
          >
            <el-radio :model-value="selectedAddressId" :label="address.id || 1"><span></span></el-radio>
            <div class="address-info">
              <strong>{{ address.receiver }} {{ address.phone }}</strong>
              <span>{{ address.province }}{{ address.city }}{{ address.district }} {{ address.detail }}</span>
            </div>
          </div>
        </div>
        <el-alert v-if="!addresses.length" title="暂无收货地址，请先在账户页维护地址；演示环境将使用默认地址提交。" type="warning" :closable="false" />

        <h2 class="section-title">订单商品</h2>
        <div class="table-scroll">
          <el-table :data="items" class="stable-table">
            <el-table-column label="商品" min-width="280">
              <template #default="{ row }">
                <div class="line-item">
                  <div class="thumb">
                    <ProductImage :src="checkoutItemImage(row)" :alt="checkoutItemName(row)" />
                  </div>
                  <div class="item-info">
                    <strong>{{ checkoutItemName(row) }}</strong>
                    <span>{{ checkoutItemSpec(row) }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="单价" width="120">
              <template #default="{ row }">
                <span class="price-cell">{{ checkoutItemPrice(row) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="100" align="center" />
            <el-table-column label="小计" width="120">
              <template #default="{ row }">
                <span class="subtotal-cell">{{ checkoutItemSubtotal(row) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-form label-position="top" class="mt">
          <el-form-item label="订单备注">
            <el-input v-model="remark" maxlength="255" show-word-limit />
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-card class="panel summary-panel">
      <template #header>
        <div class="panel-title">金额汇总</div>
      </template>
      <OrderSummary :items="items" />
      <el-button
        class="full-button"
        type="primary"
        :loading="submitting"
        :disabled="!items.length || submitting"
        @click="submitOrder"
      >
        提交订单
      </el-button>
      <p class="hint">价格和库存最终以 mall-order 调用商品、库存服务后的结果为准。</p>
    </el-card>
  </section>
</template>
