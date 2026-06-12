<script setup lang="ts">
import { notifyError } from '../utils/notify';
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../stores/auth';

const auth = useAuthStore();
const route = useRoute();
const router = useRouter();
const loading = ref(false);

const loginForm = reactive({
  username: String(route.query.username || 'zhangsan'),
  password: '123456',
});

async function submitLogin() {
  if (!loginForm.username || !loginForm.password) {
    notifyError('请输入用户名和密码');
    return;
  }
  loading.value = true;
  try {
    await auth.login(loginForm.username, loginForm.password);
    ElMessage.success('登录成功');
    router.push(String(route.query.redirect || '/'));
  } catch (err) {
    const rawMsg = err instanceof Error ? err.message : '登录失败';
    const lowercaseMsg = rawMsg.toLowerCase();
    const techKeywords = ['500', '400', '403', '404', 'network error', 'exception', 'nullpointer', 'failed', 'refused', 'timeout', 'mall-', 'service'];
    let displayMsg = rawMsg;
    if (techKeywords.some(kw => lowercaseMsg.includes(kw))) {
      displayMsg = '数据暂时无法加载，请稍后重试';
    }
    notifyError(displayMsg);
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <section class="auth-layout">
    <el-card class="panel auth-card">
      <template #header>
        <div class="panel-title">登录 MallCloud</div>
      </template>

      <el-form label-position="top" @submit.prevent="submitLogin">
        <el-form-item label="用户名">
          <el-input v-model="loginForm.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" type="password" autocomplete="current-password" show-password />
        </el-form-item>
        <div class="auth-actions">
          <el-button type="primary" native-type="submit" :loading="loading" :disabled="loading">
            登录
          </el-button>
          <el-button plain @click="router.push('/register')">注册新用户</el-button>
        </div>
      </el-form>
      <p class="hint">演示账号：zhangsan / merchant01 / admin，统一密码 123456。</p>
    </el-card>
  </section>
</template>

<style scoped>
.auth-layout {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 120px);
  background: linear-gradient(135deg, var(--color-brand-soft) 0%, var(--color-bg-page) 100%);
  padding: var(--spacing-xl) var(--page-gutter);
  width: 100%;
}
.auth-card {
  width: 100%;
  max-width: 420px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  background: var(--color-bg-surface);
}
.auth-card :deep(.el-card__header) {
  padding: var(--spacing-lg) var(--spacing-xl);
  border-bottom: 1px solid var(--color-border);
  background: var(--color-bg-subtle);
}
.panel-title {
  font-size: var(--font-lg);
  font-weight: var(--weight-bold);
  text-align: center;
  color: var(--color-text-primary);
}
.auth-actions {
  display: flex;
  gap: var(--spacing-md);
  margin-top: var(--spacing-lg);
}
.auth-actions button {
  flex: 1;
}
.hint {
  font-size: var(--font-xs);
  color: var(--color-text-tertiary);
  margin-top: var(--spacing-lg);
  text-align: center;
}
</style>
