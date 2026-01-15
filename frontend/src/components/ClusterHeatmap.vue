<template>
  <div class="cluster-heatmap">
    <el-row :gutter="20">
      <el-col :span="18">
        <el-card class="workers-card">
          <template #header>
            <div class="card-header">
              <span>集群资源热力图</span>
              <el-tag :type="isConnected ? 'success' : 'danger'">
                {{ isConnected ? '已连接' : '未连接' }}
              </el-tag>
            </div>
          </template>
          
          <div v-if="workers.length === 0" class="empty-state">
            <el-empty description="暂无Worker节点" />
          </div>
          
          <el-row :gutter="20" v-else>
            <el-col 
              v-for="worker in workers" 
              :key="worker.id" 
              :xs="24" 
              :sm="12" 
              :md="8" 
              :lg="6"
            >
              <WorkerCard :worker="worker" @view-task="handleViewTask" />
            </el-col>
          </el-row>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <TaskSubmitForm @task-submitted="handleTaskSubmitted" />
        <TaskList :tasks="tasks" @view-log="handleViewTask" />
      </el-col>
    </el-row>

    <TaskLogModal 
      v-if="selectedTask" 
      :task="selectedTask"
      :visible="logModalVisible"
      @close="logModalVisible = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { wsService } from '../services/websocket'
import { taskApi } from '../api/client'
import type { WorkerNode, Task, ClusterStatus } from '../types'
import WorkerCard from './WorkerCard.vue'
import TaskSubmitForm from './TaskSubmitForm.vue'
import TaskList from './TaskList.vue'
import TaskLogModal from './TaskLogModal.vue'

const workers = ref<WorkerNode[]>([])
const tasks = ref<Task[]>([])
const isConnected = ref(false)
const selectedTask = ref<Task | null>(null)
const logModalVisible = ref(false)

const handleViewTask = (task: Task) => {
  selectedTask.value = task
  logModalVisible.value = true
}

const handleTaskSubmitted = async () => {
  await loadTasks()
}

const loadTasks = async () => {
  try {
    tasks.value = await taskApi.getAllTasks()
  } catch (error) {
    console.error('Failed to load tasks:', error)
  }
}

onMounted(() => {
  wsService.connect()
  isConnected.value = true
  
  wsService.onClusterStatus((status: ClusterStatus) => {
    workers.value = status.workers
  })
  
  loadTasks()
  // 定期刷新任务列表
  const interval = setInterval(loadTasks, 2000)
  
  onUnmounted(() => {
    clearInterval(interval)
  })
})

onUnmounted(() => {
  wsService.disconnect()
})
</script>

<style scoped>
.cluster-heatmap {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.workers-card {
  min-height: 500px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}
</style>
