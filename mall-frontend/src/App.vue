<script setup lang="ts">
import { computed } from 'vue';
import { RouterLink, RouterView, useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from './stores/auth';

const auth = useAuthStore();
const router = useRouter();
const route = useRoute();

const navItems = [
  { label: '首页', path: '/' },
  { label: '分类', path: '/search' },
  { label: '品牌', path: '/search?keyword=Apple' },
  { label: '秒杀', path: '/seckill' },
  { label: '购物车', path: '/cart' },
  { label: '我的订单', path: '/account' },
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

function isActive(path: string) {
  if (path === '/') return route.path === '/';
  if (path === '/search') return route.path === '/search' && route.query.keyword !== 'Apple';
  if (path === '/search?keyword=Apple') return route.path === '/search' && route.query.keyword === 'Apple';
  return route.path.startsWith(path);
}
</script>

<template>
  <el-container class="app-shell">
    <el-header class="app-header">
      <div class="header-left">
        <RouterLink class="brand" to="/" aria-label="MallCloud 首页">
          <svg class="brand-logo" width="40" height="40" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="1" y="1" width="38" height="38" rx="13" fill="#EAF4FF" stroke="#BFD8FF" stroke-width="2" />
            <path d="M12.2 23.4C9.9 23.2 8.2 21.4 8.2 19.1C8.2 16.7 10.1 14.8 12.5 14.8H13.3C14.2 11.9 16.9 9.8 20.2 9.8C23.9 9.8 26.9 12.4 27.4 15.9H28.1C30.5 15.9 32.4 17.8 32.4 20.2C32.4 22.6 30.5 24.5 28.1 24.5H12.8" stroke="#1B61C9" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round" />
            <path d="M14.3 19.8H25.8L24.8 29.2C24.7 30.1 24 30.8 23.1 30.8H17C16.1 30.8 15.4 30.1 15.3 29.2L14.3 19.8Z" fill="#1B61C9" />
            <path d="M17.4 19.6V18.5C17.4 17.1 18.5 16 19.9 16C21.3 16 22.4 17.1 22.4 18.5V19.6" stroke="white" stroke-width="1.7" stroke-linecap="round" />
            <path d="M17.8 25.2L19.4 26.8L22.6 23.5" stroke="white" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
          <span class="brand-copy">
            <strong class="brand-text">MallCloud</strong>
            <span>微服务商城</span>
          </span>
        </RouterLink>

        <nav class="app-nav" aria-label="主导航">
          <RouterLink
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            custom
            v-slot="{ navigate, href }"
          >
            <a 
              :href="href" 
              @click="navigate" 
              class="nav-link" 
              :class="{ 'router-link-active': isActive(item.path) }"
            >
              {{ item.label }}
            </a>
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
