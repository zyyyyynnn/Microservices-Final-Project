<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { mallApi } from '../api/mall';
import { useAuthStore } from '../stores/auth';
import PageState from '../components/PageState.vue';
import type { Address, UserInfo } from '../api/types';

const auth = useAuthStore();
const loading = ref(false);
const error = ref('');
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
  error.value = '';
  try {
    await auth.reloadUser();
    Object.assign(profile, auth.user || {});
    addresses.value = await mallApi.listAddresses();
  } catch (err) {
    error.value = err instanceof Error ? err.message : '账户信息加载失败';
  } finally {
    loading.value = false;
  }
}

async function saveProfile() {
  try {
    await mallApi.updateUser(profile);
    await load();
    ElMessage.success('资料已更新');
  } catch (err) {
    error.value = err instanceof Error ? err.message : '资料保存失败';
  }
}

async function addAddress() {
  try {
    await mallApi.addAddress(addressForm);
    await load();
    ElMessage.success('地址已新增');
  } catch (err) {
    error.value = err instanceof Error ? err.message : '地址新增失败';
  }
}

onMounted(load);
</script>

<template>
  <section class="page-grid two">
    <el-card class="panel">
      <template #header>
        <div class="panel-title">账户资料</div>
      </template>
      <PageState :loading="loading" :error="error" @retry="load" />
      <el-form v-if="!loading" label-position="top" :disabled="loading">
        <el-form-item label="昵称">
          <el-input v-model="profile.nickname" placeholder="填写昵称" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="profile.email" placeholder="name@example.com" />
        </el-form-item>
        <el-form-item label="头像 URL">
          <el-input v-model="profile.avatar" placeholder="https://..." />
        </el-form-item>
        <el-button type="primary" :loading="loading" @click="saveProfile">保存资料</el-button>
      </el-form>
      <el-descriptions v-if="auth.user" border :column="1" class="mt">
        <el-descriptions-item label="用户 ID">{{ auth.user.id || auth.user.userId || '待联调' }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ auth.user.username || '待联调' }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ auth.user.role || auth.user.roles || '待联调' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="panel">
      <template #header>
        <div class="panel-title">收货地址</div>
      </template>
      <div v-if="addresses.length" class="table-scroll">
      <el-table :data="addresses" class="stable-table">
        <el-table-column prop="receiver" label="收件人" min-width="100" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column label="地区" min-width="160">
          <template #default="{ row }">{{ row.province }} {{ row.city }} {{ row.district }}</template>
        </el-table-column>
        <el-table-column prop="detail" label="详细地址" min-width="220" />
        <el-table-column label="操作" width="150">
          <template #default>
            <el-button text disabled>编辑待联调</el-button>
            <el-button text type="danger" disabled>删除待联调</el-button>
          </template>
        </el-table-column>
      </el-table>
      </div>
      <el-empty v-else description="暂无地址，新增后可用于订单确认" />

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
