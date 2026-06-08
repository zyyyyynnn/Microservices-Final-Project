<script setup lang="ts">
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
    error.value = '请输入用户名和密码';
    return;
  }
  loading.value = true;
  try {
    await auth.login(loginForm.username, loginForm.password);
    ElMessage.success('登录成功');
    router.push(String(route.query.redirect || '/'));
  } catch (err) {
    error.value = err instanceof Error ? err.message : '登录失败';
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
      <el-descriptions border :column="1" class="mb">
        <el-descriptions-item label="Token 状态">
          <el-tag :type="auth.isAuthenticated ? 'success' : 'info'" effect="plain">
            {{ auth.isAuthenticated ? '已保存 Access Token' : '未登录' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="跳转来源">
          {{ route.query.redirect || '首页' }}
        </el-descriptions-item>
      </el-descriptions>

      <el-form label-position="top" @submit.prevent="submitLogin">
        <el-form-item label="用户名">
          <el-input v-model="loginForm.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" type="password" autocomplete="current-password" show-password />
        </el-form-item>
        <div class="button-row">
          <el-button type="primary" native-type="submit" :loading="loading" :disabled="loading">
            登录
          </el-button>
          <RouterLink to="/register">
            <el-button plain>注册新用户</el-button>
          </RouterLink>
        </div>
      </el-form>
      <p class="hint">演示账号：zhangsan / merchant01 / admin，统一密码 123456。</p>
    </el-card>
  </section>
</template>
