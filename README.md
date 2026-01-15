# Mini-Scheduler - 轻量级分布式任务调度系统

## 项目简介

一个轻量级分布式任务调度系统，支持任务提交、智能调度（Bin Packing算法）、实时状态追踪和可视化监控。

## 技术栈

### 后端
- Java 17 + Spring Boot 3.2.0
- WebSocket (STOMP协议，实时通信)
- REST API
- Maven

### 前端
- Vue 3 (Composition API)
- TypeScript
- Element Plus (UI组件库)
- Vite (构建工具)
- WebSocket (实时数据推送)

## 项目结构

```
mini-scheduler-task/
├── backend/                    # Java后端（Master + Worker）
│   ├── src/main/java/
│   │   ├── com/minischeduler/
│   │   │   ├── controller/    # REST控制器
│   │   │   ├── service/        # 业务逻辑（调度算法）
│   │   │   ├── model/         # 数据模型
│   │   │   ├── config/        # 配置类
│   │   │   └── worker/        # Worker节点实现
│   │   └── resources/
│   │       └── application.yml
│   └── pom.xml
├── frontend/                   # Vue3前端
│   ├── src/
│   │   ├── components/        # Vue组件
│   │   ├── api/               # API客户端
│   │   ├── services/          # WebSocket服务
│   │   └── types/             # TypeScript类型定义
│   ├── package.json
│   └── vite.config.ts
├── start.sh                   # 一键启动脚本
└── README.md
```

## 快速开始

### 方式一：使用启动脚本（推荐）

```bash
./start.sh
```

脚本会自动启动：
- Master节点（端口8080）
- Worker节点（端口8081）
- 前端开发服务器（端口3000）

### 方式二：手动启动

#### 1. 启动Master节点

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=master
```

Master节点将在 `http://localhost:8080` 启动

#### 2. 启动Worker节点（新终端）

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=worker
```

Worker节点将在 `http://localhost:8081` 启动，并自动向Master注册

#### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端将在 `http://localhost:3000` 启动

## 功能特性

### 后端核心功能

1. **节点注册**：Worker启动后自动向Master注册，汇报CPU核数和内存大小
2. **任务提交**：通过REST API提交任务（command, cpu_required, mem_required）
3. **智能调度（Bin Packing）**：
   - Master根据Worker剩余资源进行分配
   - 支持多任务在同一Worker上运行（资源允许的情况下）
   - 例如：Worker A有4核/8G，可以同时运行两个各需2核/4G的任务
4. **任务状态追踪**：实时查询任务状态（PENDING, RUNNING, SUCCESS, FAILED）
5. **心跳机制**：Worker每3秒发送心跳，Master检测离线节点（超过5秒未收到心跳）
6. **实时日志流**：Worker执行任务时，实时将stdout日志发送到Master，通过WebSocket推送给前端

### 前端核心功能

1. **集群资源热力图（Cluster Heatmap）**：
   - 可视化展示所有Worker节点
   - 每个节点卡片动态显示CPU/内存实时占用率（进度条）
   - 数据通过WebSocket实时更新（每秒推送）
   - 离线节点自动变灰显示"OFFLINE"状态

2. **任务日志流（Live Log Stream）**：
   - 点击运行中的任务，弹出模态框
   - 实时展示任务的stdout日志
   - **自动滚动到底部**：新日志到达时自动滚动，用户手动滚动到顶部时暂停自动滚动
   - 使用WebSocket接收实时日志数据

3. **任务提交表单**：
   - 输入执行命令、CPU需求、内存需求
   - 表单验证和错误提示

4. **任务列表**：
   - 时间线展示所有任务
   - 任务状态标签（颜色区分）
   - 快速查看运行中任务的日志

## API接口

### Master节点API

- `POST /api/workers/register` - Worker注册
- `POST /api/workers/{workerId}/heartbeat` - Worker心跳
- `GET /api/workers` - 获取所有Worker节点
- `GET /api/workers/{workerId}/tasks` - 获取分配给Worker的任务
- `POST /api/tasks` - 提交任务
- `GET /api/tasks` - 获取所有任务
- `GET /api/tasks/{taskId}` - 获取任务详情
- `POST /api/tasks/{taskId}/complete` - 任务完成通知
- `POST /api/tasks/{taskId}/logs` - 接收任务日志

### WebSocket端点

- `/ws` - WebSocket连接端点
- `/topic/cluster-status` - 集群状态推送（每秒）
- `/topic/task-logs/{taskId}` - 任务日志推送（实时）

## AI辅助设计说明

本项目前端部分使用了AI辅助设计，特别是以下关键功能：

### 1. 日志自动滚动到底部

**需求描述**：
- 当新日志到达时，自动滚动到日志容器底部
- 如果用户手动滚动到顶部查看历史日志，则暂停自动滚动
- 当用户再次滚动到底部时，恢复自动滚动

**实现方案**：
- 使用Vue的`watch`监听日志数组变化
- 使用`nextTick`确保DOM更新后再滚动
- 监听滚动事件，检测是否接近底部（50px阈值）
- 使用`autoScroll`标志控制是否自动滚动

**关键代码**（`TaskLogModal.vue`）：
```typescript
const scrollToBottom = () => {
  if (!autoScroll.value || !scrollbarRef.value || !logContentRef.value) return
  
  nextTick(() => {
    const scrollbar = scrollbarRef.value
    if (scrollbar && scrollbar.wrapRef) {
      scrollbar.wrapRef.scrollTop = scrollbar.wrapRef.scrollHeight
    }
  })
}

// 监听滚动，智能控制自动滚动
const handleScroll = () => {
  const isAtBottom = wrap.scrollHeight - wrap.scrollTop - wrap.clientHeight < 50
  autoScroll.value = isAtBottom
}
```

### 2. 实时数据更新

使用WebSocket + STOMP协议实现实时数据推送：
- 集群状态每秒推送一次
- 任务日志实时推送（Worker执行时）
- 自动重连机制

### 3. UI设计

使用Element Plus组件库，实现：
- 响应式布局（Grid系统）
- 美观的卡片式设计
- 进度条可视化资源占用
- 状态标签和图标
- 深色主题的日志显示区域

## 测试示例

### 提交测试任务

```bash
# 提交一个简单的echo任务
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "command": "echo Hello World && sleep 5 && echo Task completed",
    "cpuRequired": 1,
    "memRequired": 100
  }'
```

### 提交长时间运行的任务（用于测试日志流）

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "command": "for i in {1..10}; do echo \"Log line $i\"; sleep 1; done",
    "cpuRequired": 1,
    "memRequired": 100
  }'
```

## 注意事项

1. **Java版本**：需要Java 17或更高版本
2. **Node.js版本**：需要Node.js 18或更高版本
3. **端口占用**：确保8080、8081、3000端口未被占用
4. **Worker执行环境**：Worker节点会在本地执行shell命令，请确保命令安全

## 开发说明

### 后端开发

- 使用Spring Boot的Profile机制区分Master和Worker
- Master和Worker可以运行在同一台机器上（不同端口）
- 调度算法在`SchedulerService`中实现，使用Bin Packing策略

### 前端开发

- 使用Vue 3 Composition API
- TypeScript提供类型安全
- WebSocket服务封装在`services/websocket.ts`
- 组件化设计，易于维护和扩展

## 未来改进

- [ ] 支持多Worker节点
- [ ] 任务优先级队列
- [ ] 任务依赖关系
- [ ] 资源预留机制
- [ ] 任务重试机制
- [ ] 更详细的监控指标
- [ ] 任务历史记录持久化
