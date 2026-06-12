<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(defineProps<{
  value?: number | string | null;
  size?: 'sm' | 'md' | 'lg' | 'xl';
  original?: boolean;
}>(), {
  value: null,
  size: 'md',
  original: false,
});

const formattedValue = computed(() => {
  if (props.value === null || props.value === undefined || props.value === '') {
    return '—';
  }
  const amount = Number(props.value);
  if (isNaN(amount)) return '—';
  return amount.toFixed(2);
});
</script>

<template>
  <span
    class="price-text"
    :class="[
      `size-${size}`,
      { 'is-original': original }
    ]"
  >
    <span class="price-symbol" v-if="formattedValue !== '—'">¥</span>
    <span class="price-amount">{{ formattedValue }}</span>
  </span>
</template>

<style scoped>
.price-text {
  font-family: "JetBrains Mono", Consolas, monospace;
  font-weight: var(--weight-bold, 700);
  color: var(--color-price);
  display: inline-flex;
  align-items: baseline;
}
.is-original {
  text-decoration: line-through;
  color: var(--color-price-muted);
  font-weight: var(--weight-normal, 400);
}
.size-sm {
  font-size: var(--font-sm);
}
.size-md {
  font-size: var(--font-base);
}
.size-lg {
  font-size: var(--font-lg);
}
.size-xl {
  font-size: var(--font-xl);
}
.price-symbol {
  font-size: 0.85em;
  margin-right: 1px;
}
</style>
