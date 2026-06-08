<script setup lang="ts">
defineProps<{
  data: unknown;
  title?: string;
}>();
</script>

<template>
  <el-collapse class="debug-collapse">
    <el-collapse-item :title="title || '接口响应调试信息'" name="debug">
      <pre class="json-box compact">{{ JSON.stringify(data, null, 2) }}</pre>
    </el-collapse-item>
  </el-collapse>
</template>
