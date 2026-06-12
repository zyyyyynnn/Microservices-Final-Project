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
  { label: '全部商品', path: '/search' },
  { label: 'Apple 专区', path: '/search?brand=Apple' },
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
  if (path === '/search') return route.path === '/search' && route.query.brand !== 'Apple';
  if (path === '/search?brand=Apple') return route.path === '/search' && route.query.brand === 'Apple';
  return route.path.startsWith(path);
}
</script>

<template>
  <el-container class="app-shell">
    <el-header class="app-header">
      <div class="header-main">
        <RouterLink class="brand" to="/" aria-label="MallCloud 首页">
          <svg class="brand-logo" style="flex-shrink: 0;" width="44" height="44" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="40" height="40" rx="10" fill="var(--color-brand)"/>
            <g fill="white">
              <rect x="10" y="20" width="20" height="10" rx="5" />
              <circle cx="15" cy="20" r="5" />
              <circle cx="23" cy="17" r="7" />
            </g>
            <path d="M15 24 Q20 28 25 24" stroke="var(--color-brand)" stroke-width="2.5" stroke-linecap="round" fill="none"/>
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
                <el-dropdown-item v-if="auth.isAuthenticated" @click="router.push('/account')">账户资料</el-dropdown-item>
                <el-dropdown-item v-if="auth.canAccessAdmin" @click="router.push('/admin')">后台管理</el-dropdown-item>
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
  background: var(--color-bg-surface);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}
.header-main {
  width: min(var(--layout-max-width), calc(100vw - var(--page-gutter) * 2));
  margin-inline: auto;
  display: flex;
  justify-content: space-between;
  gap: var(--spacing-xl);
  align-items: center;
}
.brand {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  text-decoration: none;
  flex-shrink: 0;
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
  gap: var(--spacing-xs);
  flex-shrink: 0;
}
.nav-link {
  font-size: 15px;
  color: var(--color-text-primary);
  text-decoration: none;
  font-weight: 500;
  padding: 6px 12px;
  border-radius: 20px;
  transition: color var(--transition-fast), background var(--transition-fast);
}
.nav-link:hover {
  color: var(--color-brand);
  background: var(--color-brand-soft);
}
.nav-link.router-link-active {
  color: var(--color-brand);
  background: var(--color-brand-soft);
  font-weight: 700;
}
.header-search {
  flex: 1;
  max-width: 400px;
  min-width: 150px;
}

.search-input :deep(.el-input-group__append) {
  background-color: var(--color-brand);
  color: white;
  border: 1px solid var(--color-brand);
  transition: background-color var(--transition-fast), border-color var(--transition-fast);
}
.search-input :deep(.el-input-group__append:hover) {
  background-color: var(--color-brand-hover);
  border-color: var(--color-brand-hover);
}
.search-input :deep(.el-input-group__append button.el-button) {
  color: white;
  border: none;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  flex-shrink: 0;
}
.action-icon {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  color: var(--color-text-primary);
  text-decoration: none;
  font-size: 14px;
  transition: color var(--transition-fast);
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
  background: var(--color-bg-subtle);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-secondary);
  transition: background var(--transition-fast), color var(--transition-fast);
}
.user-profile:hover .avatar {
  background: var(--color-brand-soft);
  color: var(--color-brand);
}
.greeting {
  font-size: 14px;
  color: var(--color-text-primary);
}

@media (max-width: 1024px) {
  .app-header {
    height: auto;
    padding: 14px 0;
  }

  .header-main {
    flex-wrap: wrap;
    row-gap: var(--spacing-md);
  }

  .brand {
    flex: 0 0 auto;
  }

  .app-nav {
    order: 3;
    width: 100%;
    overflow-x: auto;
    white-space: nowrap;
    padding: 4px 0;
    scrollbar-width: none;
  }
  .app-nav::-webkit-scrollbar {
    display: none;
  }

  .header-search {
    flex: 1 1 280px;
    max-width: none;
  }

  .header-actions {
    flex: 0 0 auto;
  }
}

@media (max-width: 768px) {
  .header-main {
    flex-direction: column;
    align-items: stretch;
    gap: var(--spacing-md);
  }
  .brand {
    align-self: center;
  }
  .header-search {
    width: 100%;
    margin: 4px 0;
  }
  .header-actions {
    justify-content: space-between;
    width: 100%;
  }
}
</style>