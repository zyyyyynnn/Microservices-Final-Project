<script setup lang="ts">
export interface SkuOption {
  value: string;
  label: string;
  disabled?: boolean;
}

const props = withDefaults(defineProps<{
  options: SkuOption[];
  modelValue: string | null;
  disabled?: boolean;
}>(), {
  disabled: false,
});

const emit = defineEmits<{
  'update:modelValue': [value: string | null];
}>();

function select(option: SkuOption) {
  if (props.disabled || option.disabled) return;
  emit('update:modelValue', option.value);
}
</script>

<template>
  <div class="sku-selector" :class="{ 'is-disabled': disabled }" role="radiogroup" aria-label="规格选择">
    <button
      v-for="option in options"
      :key="option.value"
      type="button"
      class="sku-chip"
      :class="{
        'is-active': modelValue === option.value,
        'is-disabled': disabled || option.disabled,
      }"
      :disabled="disabled || option.disabled"
      :aria-pressed="modelValue === option.value"
      @click="select(option)"
    >
      {{ option.label }}
    </button>
  </div>
</template>

<style scoped>
.sku-selector {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.sku-chip {
  min-width: 140px;
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-bg-surface);
  color: var(--color-text-primary);
  font-size: var(--font-sm);
  font-weight: var(--weight-normal);
  cursor: pointer;
  transition: border-color var(--transition-fast), color var(--transition-fast);
}

.sku-chip:not(.is-disabled):hover {
  border-color: var(--color-brand);
  color: var(--color-brand);
}

.sku-chip.is-active {
  border-color: var(--color-brand);
  color: var(--color-brand);
  font-weight: var(--weight-bold);
}

.sku-chip.is-disabled {
  background: var(--color-bg-subtle);
  color: var(--color-text-tertiary);
  border-color: var(--color-border);
  cursor: not-allowed;
  outline: 1px dashed var(--color-border);
  outline-offset: -3px;
}
</style>
