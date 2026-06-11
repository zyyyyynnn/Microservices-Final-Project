<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();
</script>

<template>
  <section class="commerce-layout" style="padding-top: 10vh; display: flex; justify-content: center;">
    <el-empty description="404 - 页面不存在或已被移除">
      <div class="button-row mt" style="justify-content: center;">
        <el-button type="primary" round @click="router.push('/')">返回首页</el-button>
        <el-button plain round @click="router.back()">返回上一页</el-button>
      </div>
    </el-empty>
  </section>
</template>
