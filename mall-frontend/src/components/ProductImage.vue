<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import placeholderSrc from '../assets/placeholder.svg';

const props = withDefaults(defineProps<{
  src?: string;
  alt?: string;
}>(), {
  src: '',
  alt: '商品图片',
});

const isError = ref(false);

const hasErrorOrNoSrc = computed(() => isError.value || !props.src);

watch(() => props.src, () => {
  isError.value = false;
});

function handleError() {
  isError.value = true;
}
</script>

<template>
  <div class="product-image-container">
    <div v-if="hasErrorOrNoSrc" class="image-fallback">
      <img
        :src="placeholderSrc"
        :alt="alt"
        class="fallback-svg"
      />
      <span class="fallback-text">暂无图片</span>
    </div>
    <img
      v-else
      :src="src"
      :alt="alt"
      @error="handleError"
      loading="lazy"
      decoding="async"
      class="product-image-el"
    />
  </div>
</template>

<style scoped>
.product-image-container {
  width: 100%;
  height: 100%;
  aspect-ratio: 1 / 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--color-bg-subtle);
  overflow: hidden;
  border-radius: inherit;
}

.image-fallback {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
  width: 100%;
  height: 100%;
  padding: var(--spacing-sm);
  box-sizing: border-box;
}

.fallback-svg {
  width: 32%;
  max-width: 64px;
  height: auto;
  opacity: 0.55;
  margin-bottom: var(--spacing-xs);
}

.fallback-text {
  font-size: var(--font-xs);
  color: var(--color-text-light);
  font-weight: var(--weight-medium, 500);
}

.product-image-el {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: transform var(--transition-base);
}
</style>
