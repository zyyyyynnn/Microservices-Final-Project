<script setup lang="ts">
import { computed } from 'vue';
import { RouterLink, RouterView, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from './stores/auth';

const auth = useAuthStore();
const router = useRouter();

const navItems = [
  { label: '公共浏览', path: '/' },
  { label: '账户', path: '/account', auth: true },
  { label: '购物车', path: '/cart', auth: true },
  { label: '订单支付', path: '/orders', auth: true },
  { label: '秒杀', path: '/seckill', auth: true },
  { label: '后台', path: '/admin', auth: true },
  { label: '技术演示', path: '/tech' },
];

const userLabel = computed(() => {
  if (!auth.isAuthenticated) return '未登录';
  return auth.user?.username || auth.user?.nickname || `用户 ${auth.user?.id || ''}`;
});

async function logout() {
  await auth.logout();
  ElMessage.success('已退出登录');
  router.push('/login');
}
</script>

<template>
  <el-container class="app-shell">
    <el-header class="app-header">
      <RouterLink class="brand" to="/" aria-label="MallCloud 首页">
        <span class="brand-mark">MC</span>
        <span>
          <strong>MallCloud</strong>
          <small>微服务演示台</small>
        </span>
      </RouterLink>

      <nav class="app-nav" aria-label="主导航">
        <RouterLink
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="nav-link"
        >
          {{ item.label }}
        </RouterLink>
      </nav>

      <div class="session-bar">
        <el-tag :type="auth.isAuthenticated ? 'success' : 'info'" effect="plain">
          {{ userLabel }}
        </el-tag>
        <RouterLink v-if="!auth.isAuthenticated" to="/login">
          <el-button type="primary">登录</el-button>
        </RouterLink>
        <el-button v-else plain @click="logout">退出</el-button>
      </div>
    </el-header>

    <el-main class="app-main">
      <RouterView />
    </el-main>
  </el-container>
</template>
