import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import { mallApi } from '../api/mall';
import type { UserInfo } from '../api/types';

const TOKEN_KEY = 'mallcloud_access_token';
const REFRESH_KEY = 'mallcloud_refresh_token';
const USER_KEY = 'mallcloud_user';

function readUser(): UserInfo | null {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as UserInfo;
  } catch {
    return null;
  }
}

function normalizeRoles(user: UserInfo | null): string[] {
  if (!user) return ['USER'];
  const roles = user.roles;
  if (Array.isArray(roles)) return roles;
  if (typeof roles === 'string') return roles.split(',').map(r => r.trim()).filter(Boolean);
  if (user.role) return [user.role];
  return ['USER'];
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '');
  const refreshToken = ref(localStorage.getItem(REFRESH_KEY) || '');
  const user = ref<UserInfo | null>(readUser());

  const isAuthenticated = computed(() => Boolean(token.value));
  const roles = computed(() => normalizeRoles(user.value));
  const isAdmin = computed(() => roles.value.includes('ADMIN'));
  const isMerchant = computed(() => roles.value.includes('MERCHANT'));
  const canAccessAdmin = computed(() => isAdmin.value || isMerchant.value);

  async function login(username: string, password: string) {
    const result = await mallApi.login({ username, password, loginType: 'PASSWORD' });
    token.value = result.accessToken;
    refreshToken.value = result.refreshToken;
    user.value = result.userInfo;
    localStorage.setItem(TOKEN_KEY, result.accessToken);
    localStorage.setItem(REFRESH_KEY, result.refreshToken);
    localStorage.setItem(USER_KEY, JSON.stringify(result.userInfo));
  }

  async function reloadUser() {
    const result = await mallApi.currentUser();
    user.value = result;
    localStorage.setItem(USER_KEY, JSON.stringify(result));
  }

  async function logout() {
    if (token.value) {
      try {
        await mallApi.logout();
      } catch {
        // 本地退出必须可完成，远端黑名单失败在接口错误提示中体现。
      }
    }
    token.value = '';
    refreshToken.value = '';
    user.value = null;
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_KEY);
    localStorage.removeItem(USER_KEY);
  }

  return {
    token,
    refreshToken,
    user,
    isAuthenticated,
    roles,
    isAdmin,
    isMerchant,
    canAccessAdmin,
    login,
    reloadUser,
    logout,
  };
});
