<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../stores/auth';
import { mallApi } from '../api/mall';

const auth = useAuthStore();
const route = useRoute();
const router = useRouter();
const loading = ref(false);
const registerLoading = ref(false);

const loginForm = reactive({
  username: 'zhangsan',
  password: '123456',
});

const registerForm = reactive({
  username: '',
  phone: '',
  password: '123456',
});

async function submitLogin() {
  loading.value = true;
  try {
    await auth.login(loginForm.username, loginForm.password);
    ElMessage.success('登录成功');
    router.push(String(route.query.redirect || '/'));
  } finally {
    loading.value = false;
  }
}

async function submitRegister() {
  registerLoading.value = true;
  try {
    await mallApi.register(registerForm);
    ElMessage.success('注册成功，请登录');
    loginForm.username = registerForm.username;
  } finally {
    registerLoading.value = false;
  }
}
</script>

<template>
  <section class="page-grid two">
    <el-card class="panel">
      <template #header>
        <div class="panel-title">账号登录</div>
      </template>
      <el-form label-position="top" @submit.prevent="submitLogin">
        <el-form-item label="用户名">
          <el-input v-model="loginForm.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" type="password" autocomplete="current-password" show-password />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" :disabled="loading">
          登录并保存 Token
        </el-button>
      </el-form>
      <p class="hint">演示账号：zhangsan / merchant01 / admin，统一密码 123456。</p>
    </el-card>

    <el-card class="panel">
      <template #header>
        <div class="panel-title">公开注册</div>
      </template>
      <el-form label-position="top" @submit.prevent="submitRegister">
        <el-form-item label="用户名">
          <el-input v-model="registerForm.username" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="registerForm.phone" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="registerForm.password" type="password" show-password />
        </el-form-item>
        <el-button plain native-type="submit" :loading="registerLoading" :disabled="registerLoading">
          注册 USER
        </el-button>
      </el-form>
    </el-card>
  </section>
</template>
