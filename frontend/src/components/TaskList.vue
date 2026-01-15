<template>
  <el-card class="task-list-card" shadow="hover">
    <template #header>
      <span>任务列表</span>
    </template>
    
    <el-scrollbar height="400px">
      <el-timeline>
        <el-timeline-item
          v-for="task in tasks"
          :key="task.id"
          :timestamp="formatTime(task.createdAt)"
          :type="getStatusType(task.status)"
          placement="top"
        >
          <el-card shadow="hover" class="task-item" @click="handleClick(task)">
            <div class="task-header">
              <el-tag :type="getStatusTagType(task.status)" size="small">
                {{ task.status }}
              </el-tag>
              <el-button 
                v-if="task.status === 'RUNNING'"
                type="primary" 
                size="small" 
                text
                @click.stop="handleViewLog(task)"
              >
                查看日志
              </el-button>
            </div>
            <div class="task-command">{{ task.command }}</div>
            <div class="task-resources">
              <el-text type="info" size="small">
                CPU: {{ task.cpuRequired }} | 内存: {{ task.memRequired }}MB
              </el-text>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-scrollbar>
  </el-card>
</template>

<script setup lang="ts">
import type { Task } from '../types'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  tasks: Task[]
}>()

const emit = defineEmits<{
  viewLog: [task: Task]
}>()

const formatTime = (time: string) => {
  return new Date(time).toLocaleTimeString()
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'warning',
    RUNNING: 'primary',
    SUCCESS: 'success',
    FAILED: 'danger'
  }
  return map[status] || 'info'
}

const getStatusTagType = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'warning',
    RUNNING: 'primary',
    SUCCESS: 'success',
    FAILED: 'danger'
  }
  return map[status] || 'info'
}

const handleClick = (task: Task) => {
  if (task.status === 'RUNNING') {
    handleViewLog(task)
  }
}

const handleViewLog = (task: Task) => {
  emit('viewLog', task)
}
</script>

<style scoped>
.task-list-card {
  margin-top: 20px;
}

.task-item {
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 10px;
}

.task-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.task-command {
  font-family: 'Monaco', 'Courier New', monospace;
  font-size: 12px;
  color: #606266;
  margin-bottom: 8px;
  word-break: break-all;
}

.task-resources {
  margin-top: 8px;
}
</style>
