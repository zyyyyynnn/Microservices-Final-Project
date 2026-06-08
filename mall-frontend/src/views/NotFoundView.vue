<script setup lang="ts">
import { RouterLink } from 'vue-router';
</script>

<template>
  <section class="not-found">
    <h1>404</h1>
    <p>页面不存在或已被移除。</p>
    <RouterLink to="/">
      <el-button type="primary">返回首页</el-button>
    </RouterLink>
  </section>
</template>
