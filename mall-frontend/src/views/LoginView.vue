<script setup lang="ts">
import { notifyError } from '../utils/notify';
import { reactive, ref } from 'vue';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../stores/auth';

const auth = useAuthStore();
const route = useRoute();
const router = useRouter();
const loading = ref(false);
const error = ref('');

const loginForm = reactive({
  username: String(route.query.username || 'zhangsan'),
  password: '123456',
});

async function submitLogin() {
  error.value = '';
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
    notifyError(err instanceof Error ? err.message : '登录失败');
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
      <el-alert v-if="error" :title="error" type="error" :closable="false" class="mb" />

      <el-form label-position="top" @submit.prevent="submitLogin">
        <el-form-item label="用户名">
          <el-input v-model="loginForm.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" type="password" autocomplete="current-password" show-password />
        </el-form-item>
        <div style="display: flex; gap: var(--spacing-md);">
          <el-button type="primary" native-type="submit" :loading="loading" :disabled="loading" style="flex: 1;">
            登录
          </el-button>
          <el-button plain @click="router.push('/register')" style="flex: 1;">注册新用户</el-button>
        </div>
      </el-form>
      <p class="hint">演示账号：zhangsan / merchant01 / admin，统一密码 123456。</p>
    </el-card>
  </section>
</template>
