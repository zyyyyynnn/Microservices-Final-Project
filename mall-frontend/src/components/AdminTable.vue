<script setup lang="ts">
import type { UnknownRecord } from '../api/types';

export interface TableColumn {
  prop?: string;
  label: string;
  width?: number | string;
  minWidth?: number | string;
  slot?: string;
}

withDefaults(defineProps<{
  columns: TableColumn[];
  data: UnknownRecord[];
  loading?: boolean;
  emptyText?: string;
}>(), {
  loading: false,
  emptyText: '当前暂无记录。',
});
</script>

<template>
  <div class="admin-table" v-loading="loading">
    <el-table :data="data" :empty-text="emptyText">
      <el-table-column
        v-for="col in columns"
        :key="col.prop || col.label"
        :prop="col.prop"
        :label="col.label"
        :width="col.width"
        :min-width="col.minWidth"
      >
        <template v-if="col.slot" #default="scope">
          <slot :name="col.slot" v-bind="scope" />
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.admin-table {
  width: 100%;
}
</style>
