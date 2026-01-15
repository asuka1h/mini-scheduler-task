export interface WorkerNode {
  id: string
  host: string
  port: number
  totalCpu: number
  totalMemory: number
  usedCpu: number
  usedMemory: number
  lastHeartbeat: string
  online: boolean
  runningTasks: string[]
}

export interface Task {
  id: string
  command: string
  cpuRequired: number
  memRequired: number
  status: 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED'
  workerId?: string
  createdAt: string
  startedAt?: string
  finishedAt?: string
  errorMessage?: string
}

export interface TaskSubmitRequest {
  command: string
  cpuRequired: number
  memRequired: number
}

export interface ClusterStatus {
  workers: WorkerNode[]
  timestamp: number
}

export interface TaskLogMessage {
  taskId: string
  log: string
  timestamp: number
}
