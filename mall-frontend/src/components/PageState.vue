<script setup lang="ts">
defineProps<{
  loading?: boolean;
  error?: string;
  empty?: boolean;
  emptyTitle?: string;
  emptyDescription?: string;
}>();

defineEmits<{
  retry: [];
}>();
</script>

<template>
  <div v-if="loading" class="state-box" aria-busy="true">
    <div class="state-lines">
      <span />
      <span />
      <span />
    </div>
    <p>正在加载数据</p>
  </div>
  <el-alert
    v-else-if="error"
    :title="error"
    type="error"
    :closable="false"
    show-icon
  >
    <template #default>
      <el-button size="small" plain @click="$emit('retry')">重试</el-button>
    </template>
  </el-alert>
  <el-empty
    v-else-if="empty"
    :description="emptyDescription || '暂无数据'"
  >
    <template #description>
      <strong>{{ emptyTitle || '暂无数据' }}</strong>
      <span>{{ emptyDescription || '当前没有可展示的内容' }}</span>
    </template>
  </el-empty>
</template>
