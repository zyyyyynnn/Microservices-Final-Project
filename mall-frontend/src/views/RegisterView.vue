<script setup lang="ts">
import { notifyError } from '../utils/notify';
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { mallApi } from '../api/mall';

const router = useRouter();
const loading = ref(false);
const form = reactive({
  username: '',
  phone: '',
  password: '123456',
});

function validate() {
  if (form.username.trim().length < 3) return '用户名至少 3 个字符';
  if (!/^1\d{10}$/.test(form.phone)) return '请输入 11 位手机号';
  if (form.password.length < 6) return '密码至少 6 位';
  return '';
}

async function submit() {
  const validationError = validate();
  if (validationError) {
    notifyError(validationError);
    return;
  }
  loading.value = true;
  try {
    await mallApi.register(form);
    ElMessage.success('注册成功，请登录');
    router.push({ path: '/login', query: { username: form.username } });
  } catch (err) {
    notifyError(err instanceof Error ? err.message : '注册失败');
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <section class="auth-layout">
    <el-card class="panel auth-card">
      <template #header>
        <div class="panel-title">注册 MallCloud 用户</div>
      </template>
      <el-form label-position="top" @submit.prevent="submit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" autocomplete="tel" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password autocomplete="new-password" />
        </el-form-item>
        <div class="auth-actions">
          <el-button type="primary" native-type="submit" :loading="loading" :disabled="loading">注册</el-button>
          <el-button plain @click="router.push('/login')">返回登录</el-button>
        </div>
      </el-form>
      <p class="hint">公开注册只创建 USER 角色，商家和管理员使用演示账号登录。</p>
    </el-card>
  </section>
</template>
