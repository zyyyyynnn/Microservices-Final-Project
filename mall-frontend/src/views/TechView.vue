<script setup lang="ts">
import { RouterLink } from 'vue-router';
</script>

<template>
  <div class="page-header" style="text-align: center; margin-bottom: 30px;">
    <h2>技术验收索引页</h2>
    <p style="color: var(--color-text-secondary); margin-top: 10px;">提供微服务架构各链路与质量审计的入口指引</p>
  </div>
  <section class="page-grid two">
    <el-card class="panel">
      <template #header>
        <div class="panel-title">真实业务链路入口</div>
      </template>
      <div class="panel-desc">跳转至系统实际的业务交互页面：</div>
      <ul style="margin-top: 15px; line-height: 2; list-style-type: disc; padding-left: 20px;">
        <li><RouterLink to="/search">搜索 (Search)</RouterLink></li>
        <li><RouterLink to="/products/1001">商品详情 (Product Details)</RouterLink></li>
        <li><RouterLink to="/cart">购物车 (Cart)</RouterLink></li>
        <li><RouterLink to="/checkout">下单 (Checkout)</RouterLink></li>
        <li><RouterLink to="/seckill">秒杀 (Seckill)</RouterLink></li>
        <li><RouterLink to="/admin">后台 (Admin)</RouterLink></li>
      </ul>
    </el-card>

    <el-card class="panel">
      <template #header>
        <div class="panel-title">外部控制台入口</div>
      </template>
      <div class="panel-desc">微服务基础设施与中间件控制台：</div>
      <ul style="margin-top: 15px; line-height: 2; list-style-type: disc; padding-left: 20px;">
        <li><a href="http://localhost:8848/nacos" target="_blank">Nacos (8848)</a></li>
        <li><a href="http://localhost:8080" target="_blank">Sentinel (8080)</a></li>
        <li><a href="http://localhost:8081" target="_blank">RocketMQ Dashboard (8081)</a></li>
        <li><a href="http://localhost:9411" target="_blank">Zipkin (9411)</a></li>
        <li><a href="http://localhost:9200" target="_blank">Elasticsearch (9200)</a></li>
      </ul>
    </el-card>

    <el-card class="panel" style="grid-column: 1 / -1;">
      <template #header>
        <div class="panel-title">测试与文档证据入口</div>
      </template>
      <div class="panel-desc">项目产出的测试报告及代码质量审计证明文件路径：</div>
      <ul style="margin-top: 15px; line-height: 2; list-style-type: disc; padding-left: 20px;">
        <li><strong>JMeter 报告：</strong> <code>docs/test/jmeter/summary/seckill-ladder-20260610-234029.md</code> 等</li>
        <li><strong>full profile 启动报告：</strong> <code>docs/test/jmeter/summary/profile-resource-20260610.md</code></li>
        <li><strong>前端代码质量审计：</strong> <code>docs/test/frontend/frontend-code-quality-audit-20260611.md</code></li>
        <li><strong>后端代码质量审计：</strong> <code>docs/test/backend/backend-code-quality-audit-20260611.md</code></li>
      </ul>
    </el-card>
  </section>
</template>

<style scoped>
.panel-desc {
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: var(--leading-normal);
}
a {
  color: var(--color-brand);
  text-decoration: none;
}
a:hover {
  text-decoration: underline;
}
</style>
