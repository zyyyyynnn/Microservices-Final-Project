<script setup lang="ts">
import { computed } from 'vue';
import type { CartItem } from '../api/types';
import { money } from '../utils/format';

const props = defineProps<{
  items: CartItem[];
}>();

const totalQuantity = computed(() => props.items.reduce((sum, item) => sum + Number(item.quantity || 0), 0));
const totalAmount = computed(() => props.items.reduce((sum, item) => {
  const subtotal = Number(item.subtotal ?? Number(item.price || 0) * Number(item.quantity || 0));
  return sum + subtotal;
}, 0));
</script>

<template>
  <div class="summary-box">
    <div>
      <span>商品数量</span>
      <strong>{{ totalQuantity }}</strong>
    </div>
    <div>
      <span>商品金额</span>
      <strong>{{ money(totalAmount) }}</strong>
    </div>
    <div>
      <span>运费</span>
      <strong>{{ money(0) }}</strong>
    </div>
    <div class="summary-total">
      <span>应付合计</span>
      <strong>{{ money(totalAmount) }}</strong>
    </div>
  </div>
</template>
