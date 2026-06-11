<script setup lang="ts">
import { computed } from 'vue';
import { RouterLink, RouterView, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from './stores/auth';

const auth = useAuthStore();
const router = useRouter();

const navItems = [
  { label: '首页', path: '/' },
  { label: '搜索', path: '/search' },
  { label: '秒杀', path: '/seckill' },
  { label: '购物车', path: '/cart' },
  { label: '我的', path: '/account' },
];

const userLabel = computed(() => {
  if (!auth.isAuthenticated) return '游客';
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
      <div class="header-left">
        <RouterLink class="brand" to="/" aria-label="MallCloud 首页">
          <svg class="brand-logo" width="36" height="36" viewBox="0 0 36 36" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="36" height="36" rx="12" fill="var(--color-brand)" />
            <path fill-rule="evenodd" clip-rule="evenodd" d="M12.5 14C12.5 12.6193 13.6193 11.5 15 11.5H21C22.3807 11.5 23.5 12.6193 23.5 14V16H25C26.1046 16 27 16.8954 27 18V25C27 26.1046 26.1046 27 25 27H11C9.89543 27 9 26.1046 9 25V18C9 16.8954 9.89543 16 11 16H12.5V14ZM14.5 16V14C14.5 13.7239 14.7239 13.5 15 13.5H21C21.2761 13.5 21.5 13.7239 21.5 14V16H14.5ZM11 18V25H25V18H11ZM18 20C17.4477 20 17 20.4477 17 21C17 21.5523 17.4477 22 18 22C18.5523 22 19 21.5523 19 21C19 20.4477 18.5523 20 18 20Z" fill="white"/>
          </svg>
          <span class="brand-text">MallCloud</span>
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
      </div>

      <div class="session-bar">
        <span class="user-status">{{ userLabel }}</span>
        
        <el-dropdown trigger="click">
          <span class="el-dropdown-link nav-link">
            演示工具 <i class="el-icon-arrow-down el-icon--right"></i>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/admin')">后台管理</el-dropdown-item>
              <el-dropdown-item @click="router.push('/tech')">技术演示</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <RouterLink v-if="!auth.isAuthenticated" to="/login">
          <el-button type="primary" round>登录</el-button>
        </RouterLink>
        <el-button v-else plain round @click="logout">退出</el-button>
      </div>
    </el-header>

    <el-main class="app-main">
      <RouterView />
    </el-main>
  </el-container>
</template>
