<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { mallApi } from '../api/mall';
import { useAuthStore } from '../stores/auth';
import type { Address, UserInfo } from '../api/types';

const auth = useAuthStore();
const loading = ref(false);
const addresses = ref<Address[]>([]);
const profile = reactive<Partial<UserInfo>>({
  nickname: '',
  email: '',
  avatar: '',
});
const addressForm = reactive<Address>({
  receiver: '张三',
  phone: '13800138001',
  province: '北京市',
  city: '北京市',
  district: '海淀区',
  detail: '中关村大街 1 号',
  isDefault: true,
});

async function load() {
  loading.value = true;
  try {
    await auth.reloadUser();
    Object.assign(profile, auth.user || {});
    addresses.value = await mallApi.listAddresses();
  } finally {
    loading.value = false;
  }
}

async function saveProfile() {
  await mallApi.updateUser(profile);
  await load();
  ElMessage.success('资料已更新');
}

async function addAddress() {
  await mallApi.addAddress(addressForm);
  await load();
  ElMessage.success('地址已新增');
}

onMounted(load);
</script>

<template>
  <section class="page-grid two">
    <el-card class="panel">
      <template #header>
        <div class="panel-title">当前用户</div>
      </template>
      <el-form label-position="top" :disabled="loading">
        <el-form-item label="昵称">
          <el-input v-model="profile.nickname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="profile.email" />
        </el-form-item>
        <el-form-item label="头像 URL">
          <el-input v-model="profile.avatar" />
        </el-form-item>
        <el-button type="primary" :loading="loading" @click="saveProfile">保存资料</el-button>
      </el-form>
    </el-card>

    <el-card class="panel">
      <template #header>
        <div class="panel-title">收货地址</div>
      </template>
      <el-table v-if="addresses.length" :data="addresses" class="stable-table">
        <el-table-column prop="receiver" label="收件人" min-width="100" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column prop="detail" label="地址" min-width="220" />
      </el-table>
      <el-empty v-else description="暂无地址" />

      <el-divider />
      <el-form class="compact-form" label-position="top" @submit.prevent="addAddress">
        <el-form-item label="收件人">
          <el-input v-model="addressForm.receiver" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="addressForm.phone" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="addressForm.detail" />
        </el-form-item>
        <el-button plain native-type="submit">新增地址</el-button>
      </el-form>
    </el-card>
  </section>
</template>
