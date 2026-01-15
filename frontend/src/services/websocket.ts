import { Client, IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import type { ClusterStatus, TaskLogMessage } from '../types'

export class WebSocketService {
  private client: Client | null = null
  private clusterStatusCallback?: (status: ClusterStatus) => void
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
        console.log('WebSocket connected')
        this.subscribeToClusterStatus()
        // 重新订阅所有任务日志
        this.taskLogCallbacks.forEach((callback, taskId) => {
          this.subscribeToTaskLogs(taskId, callback)
        })
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame)
      },
      onWebSocketClose: () => {
        console.log('WebSocket closed')
      }
    })

    this.client.activate()
  }

  private subscribeToClusterStatus() {
    if (!this.client) return

    this.client.subscribe('/topic/cluster-status', (message: IMessage) => {
      const status: ClusterStatus = JSON.parse(message.body)
      if (this.clusterStatusCallback) {
        this.clusterStatusCallback(status)
      }
    })
  }

  subscribeToTaskLogs(taskId: string, callback: (log: TaskLogMessage) => void) {
    if (!this.client || !this.client.connected) {
      console.error('WebSocket not connected')
      this.taskLogCallbacks.set(taskId, callback)
      return
    }

    this.taskLogCallbacks.set(taskId, callback)

    const subscription = this.client.subscribe(`/topic/task-logs/${taskId}`, (message: IMessage) => {
      const logMessage: TaskLogMessage = JSON.parse(message.body)
      callback(logMessage)
    })
    
    this.taskLogSubscriptions.set(taskId, subscription)
  }

  unsubscribeFromTaskLogs(taskId: string) {
    this.taskLogCallbacks.delete(taskId)
    const subscription = this.taskLogSubscriptions.get(taskId)
    if (subscription) {
      subscription.unsubscribe()
      this.taskLogSubscriptions.delete(taskId)
    }
  }

  onClusterStatus(callback: (status: ClusterStatus) => void) {
    this.clusterStatusCallback = callback
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate()
      this.client = null
    }
    this.taskLogCallbacks.clear()
    this.taskLogSubscriptions.clear()
  }
}

export const wsService = new WebSocketService()
