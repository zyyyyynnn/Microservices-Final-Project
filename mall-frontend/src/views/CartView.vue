<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { mallApi } from '../api/mall';
import type { Cart, CartItem } from '../api/types';

const router = useRouter();
const loading = ref(false);
const cart = ref<Cart>({ items: [], totalAmount: 0, totalQuantity: 0 });
const addressId = ref(1);
const creating = ref(false);

const selectedItems = computed(() => cart.value.items.filter((item) => item.selected !== false));

async function load() {
  loading.value = true;
  try {
    cart.value = await mallApi.cart();
  } finally {
    loading.value = false;
  }
}

async function updateQuantity(item: CartItem) {
  await mallApi.updateCartQuantity(item.skuId, item.quantity);
  await load();
}

async function updateSelected(item: CartItem) {
  await mallApi.updateCartSelected(item.skuId, item.selected !== false);
  await load();
}

async function removeItem(item: CartItem) {
  await mallApi.deleteCartItem(item.skuId);
  await load();
  ElMessage.success('已删除');
}

async function createOrder() {
  creating.value = true;
  try {
    const result = await mallApi.createOrder({
      addressId: addressId.value,
      items: selectedItems.value.map((item) => ({ skuId: item.skuId, quantity: item.quantity })),
      remark: 'frontend-demo',
    });
    ElMessage.success(`订单已创建：${result.orderNo}`);
    router.push({ path: '/orders', query: { orderNo: result.orderNo } });
  } finally {
    creating.value = false;
  }
}

onMounted(load);
</script>

<template>
  <section class="page-grid">
    <el-card class="panel">
      <template #header>
        <div class="panel-title">购物车</div>
      </template>
      <el-table v-loading="loading" :data="cart.items" class="stable-table">
        <el-table-column prop="skuId" label="SKU" width="120" />
        <el-table-column prop="skuName" label="商品" min-width="180" />
        <el-table-column prop="price" label="单价" width="120" />
        <el-table-column label="数量" width="170">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="1" size="small" @change="updateQuantity(row)" />
          </template>
        </el-table-column>
        <el-table-column label="选中" width="100">
          <template #default="{ row }">
            <el-switch v-model="row.selected" @change="updateSelected(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="subtotal" label="小计" width="120" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button text type="danger" @click="removeItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !cart.items.length" description="购物车为空" />

      <div class="checkout-bar">
        <span>选中 {{ selectedItems.length }} 件，合计 {{ cart.totalAmount || 0 }}</span>
        <el-input-number v-model="addressId" :min="1" aria-label="收货地址 ID" />
        <el-button
          type="primary"
          :disabled="!selectedItems.length || creating"
          :loading="creating"
          @click="createOrder"
        >
          创建订单
        </el-button>
      </div>
    </el-card>
  </section>
</template>
