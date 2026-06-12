<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  loading?: boolean;
  error?: string;
  empty?: boolean;
  emptyTitle?: string;
  emptyDescription?: string;
}>();

defineEmits<{
  retry: [];
}>();

const displayError = computed(() => {
  if (!props.error) return '';
  const lowercaseError = props.error.toLowerCase();
  const techKeywords = ['500', '400', '403', '404', 'network error', 'exception', 'nullpointer', 'failed', 'refused', 'timeout', 'mall-', 'service'];
  if (techKeywords.some(kw => lowercaseError.includes(kw))) {
    return '数据暂时无法加载，请稍后重试';
  }
  return props.error;
});
</script>

<template>
  <div v-if="loading" class="state-box" aria-busy="true">
    <div class="state-lines">
      <span />
      <span />
      <span />
    </div>
    <p class="state-text">正在加载数据</p>
  </div>
  <div v-else-if="error" class="error-container">
    <el-alert
      :title="displayError"
      type="error"
      :closable="false"
      show-icon
      class="error-alert"
    >
      <div class="error-actions">
        <el-button size="small" type="danger" plain @click="$emit('retry')">重新加载</el-button>
      </div>
    </el-alert>
  </div>
  <el-empty
    v-else-if="empty"
    class="empty-box"
  >
    <template #description>
      <div class="empty-info">
        <span class="empty-title">{{ emptyTitle || '暂无数据' }}</span>
        <span class="empty-desc">{{ emptyDescription || '当前没有可展示的内容' }}</span>
      </div>
    </template>
  </el-empty>
</template>

<style scoped>
.state-box {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  padding: var(--spacing-xl) 0;
  width: 100%;
}
.state-lines {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
  width: 100%;
}
.state-lines span {
  height: 16px;
  background: var(--color-bg-subtle);
  border-radius: var(--radius-sm);
  display: block;
}
.state-lines span:nth-child(1) {
  width: 40%;
}
.state-lines span:nth-child(2) {
  width: 80%;
}
.state-lines span:nth-child(3) {
  width: 60%;
}
.state-text {
  margin: 0;
  color: var(--color-text-tertiary);
  font-size: var(--font-sm);
}

.error-container {
  width: 100%;
  padding: var(--spacing-md) 0;
}
.error-alert {
  border: 1px solid var(--color-danger-soft);
  background-color: var(--color-danger-soft);
}
.error-actions {
  margin-top: var(--spacing-sm);
  display: flex;
  justify-content: flex-start;
}

.empty-box {
  padding: var(--spacing-xl) 0;
}
.empty-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-xs);
}
.empty-title {
  font-size: var(--font-base);
  font-weight: var(--weight-bold);
  color: var(--color-text-primary);
}
.empty-desc {
  font-size: var(--font-sm);
  color: var(--color-text-secondary);
}
</style>
