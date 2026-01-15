<template>
  <el-container class="app-container">
    <el-header class="app-header">
      <h1>🚀 Mini-Scheduler - 分布式任务调度系统</h1>
    </el-header>
    <el-main class="app-main">
      <ClusterHeatmap />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import ClusterHeatmap from './components/ClusterHeatmap.vue'
</script>

<style scoped>
.app-container {
  height: 100vh;
}

.app-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.app-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.app-main {
  padding: 20px;
  background: #f5f7fa;
}
</style>
