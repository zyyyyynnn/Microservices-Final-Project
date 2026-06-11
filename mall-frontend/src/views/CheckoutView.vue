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
      items.value = [{
        skuId: Number(route.query.skuId),
        skuName: '来自商品详情的直接结算商品',
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
  <section class="page-grid two">
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
            <el-radio :model-value="selectedAddressId" :label="address.id || 1" />
            <div class="address-info">
              <strong>{{ address.receiver }} {{ address.phone }}</strong>
              <span>{{ address.province }}{{ address.city }}{{ address.district }} {{ address.detail }}</span>
            </div>
          </div>
        </div>
        <el-alert v-if="!addresses.length" title="地址接口未返回数据，默认使用地址 ID 1 创建订单。" type="warning" :closable="false" />

        <h2 class="section-title">订单商品</h2>
        <div class="table-scroll">
          <el-table :data="items" class="stable-table">
            <el-table-column prop="skuName" label="商品" min-width="220" />
            <el-table-column prop="skuId" label="SKU" width="120" />
            <el-table-column label="单价" width="120">
              <template #default="{ row }">{{ row.price ? money(row.price) : '以订单服务为准' }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="100" />
          </el-table>
        </div>
        <el-form label-position="top" class="mt">
          <el-form-item label="订单备注">
            <el-input v-model="remark" maxlength="255" show-word-limit />
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-card class="panel">
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
