<script setup lang="ts">
import { ref, computed } from 'vue';
import placeholderSrc from '../assets/placeholder.svg';

const props = withDefaults(defineProps<{
  src?: string;
  alt?: string;
}>(), {
  src: '',
  alt: '商品图片',
});

const isError = ref(false);

const imageSource = computed(() => {
  if (isError.value || !props.src) {
    return placeholderSrc;
  }
  return props.src;
});

function handleError() {
  isError.value = true;
}
</script>

<template>
  <div class="product-image-container">
    <img 
      :src="imageSource" 
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

.product-image-el {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: transform var(--transition-base);
}
</style>
