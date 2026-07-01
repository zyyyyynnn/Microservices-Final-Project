<script setup lang="ts">
import { ref, watch } from 'vue';

const props = defineProps<{
  orderId: string;
  loading?: boolean;
}>();

const emit = defineEmits<{
  submit: [orderId: string];
}>();

const localOrderId = ref(props.orderId);

watch(() => props.orderId, (val) => {
  localOrderId.value = val;
});

function onSubmit() {
  const trimmed = localOrderId.value.trim();
  if (!trimmed || props.loading) return;
  emit('submit', trimmed);
}
</script>

<template>
  <div class="ship-form">
    <label class="ship-form-label">发货订单号</label>
    <div class="ship-form-row">
      <el-input
        v-model="localOrderId"
        :disabled="loading"
        @keyup.enter="onSubmit"
      />
      <el-button
        type="primary"
        :loading="loading"
        :disabled="!localOrderId.trim() || loading"
        @click="onSubmit"
      >
        确认发货
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.ship-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}
.ship-form-label {
  font-size: var(--font-sm);
  font-weight: var(--weight-medium);
  color: var(--color-text-secondary);
}
.ship-form-row {
  display: flex;
  gap: var(--spacing-sm);
  align-items: center;
}
.ship-form-row .el-input {
  flex: 1;
}
@media (max-width: 768px) {
  .ship-form-row {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
