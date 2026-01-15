<template>
  <el-card 
    :class="['worker-card', { 'offline': !worker.online }]"
    shadow="hover"
    :body-style="{ padding: '20px' }"
  >
    <div class="worker-header">
      <div>
        <h3>{{ worker.host }}:{{ worker.port }}</h3>
        <el-tag :type="worker.online ? 'success' : 'info'" size="small">
          {{ worker.online ? 'ONLINE' : 'OFFLINE' }}
        </el-tag>
      </div>
      <el-icon class="worker-icon" :class="{ 'offline-icon': !worker.online }">
        <Monitor />
      </el-icon>
    </div>

    <div class="resource-info">
      <div class="resource-item">
        <div class="resource-label">
          <span>CPU</span>
          <span class="resource-usage">
            {{ worker.usedCpu }} / {{ worker.totalCpu }} 核
          </span>
        </div>
        <el-progress 
          :percentage="cpuPercentage" 
          :color="getProgressColor(cpuPercentage)"
          :stroke-width="12"
        />
      </div>

      <div class="resource-item">
        <div class="resource-label">
          <span>内存</span>
          <span class="resource-usage">
            {{ worker.usedMemory }} / {{ worker.totalMemory }} MB
          </span>
        </div>
        <el-progress 
          :percentage="memoryPercentage" 
          :color="getProgressColor(memoryPercentage)"
          :stroke-width="12"
        />
      </div>
    </div>

    <div class="worker-footer">
      <el-text type="info" size="small">
        运行中任务: {{ worker.runningTasks.length }}
      </el-text>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Monitor } from '@element-plus/icons-vue'
import type { WorkerNode } from '../types'

const props = defineProps<{
  worker: WorkerNode
}>()

const emit = defineEmits<{
  viewTask: [task: any]
}>()

const cpuPercentage = computed(() => {
  if (props.worker.totalCpu === 0) return 0
  return Math.round((props.worker.usedCpu / props.worker.totalCpu) * 100)
})

const memoryPercentage = computed(() => {
  if (props.worker.totalMemory === 0) return 0
  return Math.round((props.worker.usedMemory / props.worker.totalMemory) * 100)
})

const getProgressColor = (percentage: number) => {
  if (percentage < 50) return '#67c23a'
  if (percentage < 80) return '#e6a23c'
  return '#f56c6c'
}
</script>

<style scoped>
.worker-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

.worker-card.offline {
  opacity: 0.6;
  filter: grayscale(0.5);
}

.worker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.worker-header h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
}

.worker-icon {
  font-size: 32px;
  color: #409eff;
}

.worker-icon.offline-icon {
  color: #909399;
}

.resource-info {
  margin-bottom: 15px;
}

.resource-item {
  margin-bottom: 15px;
}

.resource-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
}

.resource-usage {
  color: #909399;
  font-size: 12px;
}

.worker-footer {
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
}
</style>
