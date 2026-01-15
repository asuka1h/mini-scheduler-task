<template>
  <el-dialog
    v-model="dialogVisible"
    :title="`任务日志 - ${task.id.substring(0, 8)}`"
    width="80%"
    :before-close="handleClose"
    @closed="handleClosed"
  >
    <div class="log-container">
      <div class="log-header">
        <el-tag :type="getStatusTagType(task.status)" size="small">
          {{ task.status }}
        </el-tag>
        <el-text type="info" size="small">
          命令: {{ task.command }}
        </el-text>
      </div>
      
      <el-scrollbar 
        ref="scrollbarRef" 
        class="log-scrollbar"
        :always="true"
      >
        <div 
          ref="logContentRef" 
          class="log-content"
        >
          <div 
            v-for="(log, index) in logs" 
            :key="index"
            class="log-line"
          >
            <span class="log-timestamp">{{ formatTimestamp(log.timestamp) }}</span>
            <span class="log-text">{{ log.log }}</span>
          </div>
          <div v-if="logs.length === 0" class="log-empty">
            暂无日志输出...
          </div>
        </div>
      </el-scrollbar>
    </div>
    
    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onUnmounted } from 'vue'
import { wsService } from '../services/websocket'
import type { Task, TaskLogMessage } from '../types'

const props = defineProps<{
  task: Task
  visible: boolean
}>()

const emit = defineEmits<{
  close: []
}>()

const dialogVisible = ref(props.visible)
const logs = ref<TaskLogMessage[]>([])
const scrollbarRef = ref()
const logContentRef = ref<HTMLElement>()
const autoScroll = ref(true)

const getStatusTagType = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'warning',
    RUNNING: 'primary',
    SUCCESS: 'success',
    FAILED: 'danger'
  }
  return map[status] || 'info'
}

const formatTimestamp = (timestamp: number) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString()
}

const scrollToBottom = () => {
  if (!autoScroll.value || !scrollbarRef.value || !logContentRef.value) return
  
  nextTick(() => {
    const scrollbar = scrollbarRef.value
    if (scrollbar && scrollbar.wrapRef) {
      scrollbar.wrapRef.scrollTop = scrollbar.wrapRef.scrollHeight
    }
  })
}

const handleClose = () => {
  dialogVisible.value = false
  emit('close')
}

let scrollCleanup: (() => void) | null = null

const handleClosed = () => {
  // 清理日志订阅
  wsService.unsubscribeFromTaskLogs(props.task.id)
  logs.value = []
  // 清理滚动监听
  if (scrollCleanup) {
    scrollCleanup()
    scrollCleanup = null
  }
}

watch(() => props.visible, (newVal) => {
  dialogVisible.value = newVal
  if (newVal && props.task.status === 'RUNNING') {
    // 订阅任务日志
    wsService.subscribeToTaskLogs(props.task.id, (logMessage: TaskLogMessage) => {
      logs.value.push(logMessage)
      scrollToBottom()
    })
    
    // 对话框打开后，设置滚动监听
    nextTick(() => {
      if (scrollbarRef.value && scrollbarRef.value.wrapRef) {
        const wrap = scrollbarRef.value.wrapRef
        const handleScroll = () => {
          const isAtBottom = wrap.scrollHeight - wrap.scrollTop - wrap.clientHeight < 50
          autoScroll.value = isAtBottom
        }
        wrap.addEventListener('scroll', handleScroll)
        
        // 保存清理函数
        scrollCleanup = () => {
          wrap.removeEventListener('scroll', handleScroll)
        }
      }
    })
  }
})

watch(logs, () => {
  if (autoScroll.value) {
    scrollToBottom()
  }
}, { deep: true })

onUnmounted(() => {
  // 组件卸载时清理
  if (scrollCleanup) {
    scrollCleanup()
  }
  wsService.unsubscribeFromTaskLogs(props.task.id)
})
</script>

<style scoped>
.log-container {
  height: 500px;
  display: flex;
  flex-direction: column;
}

.log-header {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 10px;
}

.log-scrollbar {
  flex: 1;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background: #1e1e1e;
}

.log-content {
  padding: 10px;
  font-family: 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
}

.log-line {
  display: flex;
  margin-bottom: 4px;
  color: #d4d4d4;
}

.log-timestamp {
  color: #858585;
  margin-right: 10px;
  flex-shrink: 0;
}

.log-text {
  flex: 1;
  word-break: break-all;
}

.log-empty {
  color: #858585;
  text-align: center;
  padding: 20px;
}
</style>
