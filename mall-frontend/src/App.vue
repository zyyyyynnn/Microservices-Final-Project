<script setup lang="ts">
import { ref } from 'vue';
import { RouterLink, RouterView, useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from './stores/auth';
import { Search, ShoppingCart, UserFilled } from '@element-plus/icons-vue';

const auth = useAuthStore();
const router = useRouter();
const route = useRoute();

const navItems = [
  { label: '首页', path: '/' },
  { label: '分类', path: '/search' },
  { label: '品牌', path: '/search?keyword=Apple' },
  { label: '秒杀', path: '/seckill' },
];

const searchKeyword = ref('');

function goSearch() {
  if (searchKeyword.value) {
    router.push({ path: '/search', query: { keyword: searchKeyword.value } });
  } else {
    router.push('/search');
  }
}

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
      <div class="header-main">
        <RouterLink class="brand" to="/" aria-label="MallCloud 首页">
          <svg class="brand-logo" width="48" height="48" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12.5 29C10.0147 29 8 26.9853 8 24.5C8 22.0147 10.0147 20 12.5 20C13.0697 20 13.6152 20.1066 14.1197 20.3015C15.0113 16.1438 18.6836 13 23 13C27.9706 13 32 17.0294 32 22C32 22.0844 31.9988 21.849 31.9965 21.9324C32.32 21.9772 32.656 22 33 22C36.3137 22 39 24.6863 39 28C39 31.3137 36.3137 34 33 34H12.5C10.0147 34 8 31.9853 8 29.5C8 27.0147 10.0147 25 12.5 25Z" fill="var(--color-brand)"/>
            <path d="M21 21H27L25.8 30H22.2L21 21Z" fill="var(--color-surface)"/>
            <path d="M22 21V19C22 17.8954 22.8954 17 24 17C25.1046 17 26 17.8954 26 19V21" stroke="var(--color-surface)" stroke-width="2" stroke-linecap="round"/>
            <path d="M22 26L23.5 27.5L25.5 24.5" stroke="var(--color-brand)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span class="brand-copy">
            <strong class="brand-text">MallCloud</strong>
            <span>云原生微服务电商平台</span>
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

        <div class="header-search">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索商品、品牌、分类"
            class="search-input"
            @keyup.enter="goSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button type="primary" class="search-btn" @click="goSearch">搜索</el-button>
            </template>
          </el-input>
        </div>

        <div class="header-actions">
          <RouterLink to="/cart" class="action-icon">
            <el-icon :size="20"><ShoppingCart /></el-icon>
            <span>购物车</span>
          </RouterLink>

          <el-dropdown class="user-dropdown" trigger="click">
            <span class="el-dropdown-link user-profile">
              <div class="avatar">
                <el-icon><UserFilled /></el-icon>
              </div>
              <span class="greeting" v-if="auth.isAuthenticated">你好, {{ auth.user?.username }}</span>
              <span class="greeting" v-else>你好, 请登录</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="!auth.isAuthenticated" @click="router.push('/login')">登录</el-dropdown-item>
                <el-dropdown-item @click="router.push('/account')">我的订单</el-dropdown-item>
                <el-dropdown-item @click="router.push('/admin')">后台管理</el-dropdown-item>
                <el-dropdown-item @click="router.push('/tech')">技术演示</el-dropdown-item>
                <el-dropdown-item v-if="auth.isAuthenticated" divided @click="logout">退出</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>

    <el-main class="app-main">
      <RouterView />
    </el-main>
  </el-container>
</template>

<style scoped>
.app-header {
  height: 80px;
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}
.header-main {
  width: min(1440px, 100%);
  padding: 0 var(--spacing-xl);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-xl);
}
.brand {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  text-decoration: none;
}
.brand-copy {
  display: flex;
  flex-direction: column;
}
.brand-text {
  font-size: 22px;
  font-weight: 800;
  color: var(--color-text-primary);
  letter-spacing: -0.02em;
}
.brand-copy span {
  font-size: 11px;
  color: var(--color-text-secondary);
}
.app-nav {
  display: flex;
  gap: var(--spacing-sm);
}
.nav-link {
  font-size: 15px;
  color: var(--color-text-primary);
  text-decoration: none;
  font-weight: 500;
  padding: 6px 12px;
  border-radius: 20px;
}
.nav-link:hover {
  color: var(--color-brand);
}
.nav-link.router-link-active {
  color: var(--color-brand);
  background: var(--color-brand-light);
}
.header-search {
  flex: 1;
  max-width: 400px;
}
.search-input :deep(.el-input-group__append) {
  background-color: var(--color-brand);
  color: white;
  border: none;
  border-top-right-radius: 20px;
  border-bottom-right-radius: 20px;
}
.search-input :deep(.el-input__wrapper) {
  border-top-left-radius: 20px;
  border-bottom-left-radius: 20px;
  box-shadow: 0 0 0 1px var(--color-brand) inset;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-xl);
}
.action-icon {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  color: var(--color-text-primary);
  text-decoration: none;
  font-size: 14px;
}
.action-icon:hover {
  color: var(--color-brand);
}
.user-profile {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  cursor: pointer;
}
.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-surface-hover);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-secondary);
}
.greeting {
  font-size: 14px;
  color: var(--color-text-primary);
}
</style>
