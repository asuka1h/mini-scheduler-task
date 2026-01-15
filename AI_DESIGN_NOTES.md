# AI辅助设计说明文档

本文档记录了在开发Mini-Scheduler项目过程中，如何使用AI辅助完成关键功能的设计和实现。

## 1. 日志自动滚动到底部功能

### 需求描述
在任务日志模态框中，需要实现以下功能：
- 当新日志到达时，自动滚动到容器底部
- 如果用户手动滚动到顶部查看历史日志，则暂停自动滚动
- 当用户再次滚动到底部时，恢复自动滚动

### AI提示词示例
```
我需要实现一个日志查看器组件，要求：
1. 实时接收WebSocket推送的日志数据
2. 新日志到达时自动滚动到底部
3. 如果用户手动向上滚动查看历史，则停止自动滚动
4. 用户滚动回底部时，恢复自动滚动
5. 使用Vue 3 Composition API和Element Plus的el-scrollbar组件
```

### 实现方案

#### 关键代码（TaskLogModal.vue）

```typescript
// 自动滚动标志
const autoScroll = ref(true)

// 滚动到底部的函数
const scrollToBottom = () => {
  if (!autoScroll.value || !scrollbarRef.value || !logContentRef.value) return
  
  nextTick(() => {
    const scrollbar = scrollbarRef.value
    if (scrollbar && scrollbar.wrapRef) {
      scrollbar.wrapRef.scrollTop = scrollbar.wrapRef.scrollHeight
    }
  })
}

// 监听滚动事件，智能控制自动滚动
watch(() => props.visible, (newVal) => {
  if (newVal) {
    nextTick(() => {
      if (scrollbarRef.value && scrollbarRef.value.wrapRef) {
        const wrap = scrollbarRef.value.wrapRef
        const handleScroll = () => {
          // 检测是否接近底部（50px阈值）
          const isAtBottom = wrap.scrollHeight - wrap.scrollTop - wrap.clientHeight < 50
          autoScroll.value = isAtBottom
        }
        wrap.addEventListener('scroll', handleScroll)
      }
    })
  }
})

// 监听日志数组变化，自动滚动
watch(logs, () => {
  if (autoScroll.value) {
    scrollToBottom()
  }
}, { deep: true })
```

### 优化点

1. **使用nextTick确保DOM更新**：在DOM更新后再执行滚动操作
2. **50px阈值**：允许一定的误差范围，提升用户体验
3. **事件清理**：组件卸载时正确清理事件监听器，避免内存泄漏
4. **条件检查**：在滚动前检查autoScroll标志和相关引用

### 潜在问题和解决方案

**问题**：如果日志更新非常频繁，可能导致滚动性能问题

**解决方案**：
- 使用`nextTick`批量处理DOM更新
- 可以考虑使用`requestAnimationFrame`优化滚动性能
- 如果日志量很大，可以考虑虚拟滚动

## 2. WebSocket实时数据推送

### 需求描述
- 集群状态每秒推送一次
- 任务日志实时推送（Worker执行时）
- 支持自动重连
- 支持多个任务日志订阅

### AI提示词示例
```
我需要实现一个WebSocket服务，要求：
1. 使用STOMP协议连接Spring Boot的WebSocket服务器
2. 支持订阅多个主题（集群状态、任务日志）
3. 自动重连机制
4. 连接状态管理
5. 使用TypeScript和Vue 3
```

### 实现方案

使用`@stomp/stompjs`和`sockjs-client`实现：

```typescript
export class WebSocketService {
  private client: Client | null = null
  private taskLogCallbacks: Map<string, (log: TaskLogMessage) => void> = new Map()
  private taskLogSubscriptions: Map<string, any> = new Map()

  connect() {
    const socket = new SockJS('http://localhost:8080/ws')
    this.client = new Client({
      webSocketFactory: () => socket as any,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        this.subscribeToClusterStatus()
        // 重新订阅所有任务日志（重连后）
        this.taskLogCallbacks.forEach((callback, taskId) => {
          this.subscribeToTaskLogs(taskId, callback)
        })
      }
    })
    this.client.activate()
  }
}
```

### 关键设计点

1. **订阅管理**：使用Map存储订阅和回调，支持动态订阅/取消订阅
2. **重连恢复**：重连后自动恢复所有订阅
3. **心跳机制**：保持连接活跃

## 3. 集群资源热力图设计

### 需求描述
- 展示所有Worker节点
- 实时显示CPU/内存占用率
- 离线节点视觉区分
- 响应式布局

### AI提示词示例
```
我需要设计一个集群资源热力图组件，要求：
1. 使用卡片式布局展示Worker节点
2. 每个卡片显示CPU和内存的实时占用率（进度条）
3. 离线节点要视觉上区分（变灰）
4. 使用Element Plus组件库
5. 响应式设计，支持不同屏幕尺寸
6. 数据通过WebSocket实时更新
```

### 实现方案

使用Element Plus的`el-progress`和`el-card`组件：

```vue
<el-card :class="['worker-card', { 'offline': !worker.online }]">
  <el-progress 
    :percentage="cpuPercentage" 
    :color="getProgressColor(cpuPercentage)"
  />
  <el-progress 
    :percentage="memoryPercentage" 
    :color="getProgressColor(memoryPercentage)"
  />
</el-card>
```

### 设计亮点

1. **颜色编码**：根据占用率使用不同颜色（绿色<50%，黄色50-80%，红色>80%）
2. **离线状态**：使用CSS filter和opacity实现视觉区分
3. **响应式**：使用Element Plus的Grid系统实现响应式布局

## 4. Bin Packing调度算法

### 需求描述
实现资源分配算法，能够：
- 根据Worker剩余资源分配任务
- 支持多任务在同一Worker上运行
- 优先选择资源最匹配的Worker

### AI提示词示例
```
我需要实现一个Bin Packing调度算法，要求：
1. 根据任务的CPU和内存需求分配Worker
2. 支持多个任务在同一个Worker上运行（如果资源允许）
3. 优先选择资源最匹配的Worker（最小浪费）
4. 使用Java实现
```

### 实现方案

```java
public WorkerNode findBestWorker(int cpuRequired, int memRequired) {
    return workers.values().stream()
            .filter(WorkerNode::isOnline)
            .filter(w -> w.canAllocate(cpuRequired, memRequired))
            .min(Comparator
                    .comparing((WorkerNode w) -> w.getAvailableCpu() - cpuRequired)
                    .thenComparing(w -> w.getAvailableMemory() - memRequired))
            .orElse(null);
}
```

### 算法特点

1. **贪心策略**：选择剩余资源最接近任务需求的Worker
2. **多任务支持**：通过`canAllocate`方法检查资源是否足够
3. **在线检查**：只考虑在线的Worker节点

## 总结

通过AI辅助设计，我们实现了：
1. ✅ 智能的日志自动滚动功能（用户体验优化）
2. ✅ 可靠的WebSocket实时通信（自动重连、订阅管理）
3. ✅ 美观的可视化界面（响应式设计、状态感知）
4. ✅ 高效的调度算法（Bin Packing资源分配）

### AI辅助的优势

1. **快速原型**：能够快速生成符合需求的代码框架
2. **最佳实践**：AI能够提供行业标准的实现方案
3. **问题预防**：AI能够识别潜在问题并提供解决方案
4. **代码优化**：AI能够提供性能优化建议

### 注意事项

1. **代码审查**：AI生成的代码需要人工审查和测试
2. **业务逻辑**：复杂的业务逻辑需要人工设计和验证
3. **安全性**：涉及安全的代码需要特别审查
4. **性能测试**：需要实际测试验证性能表现
