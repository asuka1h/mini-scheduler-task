import axios from 'axios'
import type { Task, TaskSubmitRequest, WorkerNode } from '../types'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

export const taskApi = {
  submitTask: (request: TaskSubmitRequest): Promise<Task> => {
    return api.post('/tasks', request).then(res => res.data)
  },
  getAllTasks: (): Promise<Task[]> => {
    return api.get('/tasks').then(res => res.data)
  },
  getTask: (taskId: string): Promise<Task> => {
    return api.get(`/tasks/${taskId}`).then(res => res.data)
  }
}

export const workerApi = {
  getAllWorkers: (): Promise<WorkerNode[]> => {
    return api.get('/workers').then(res => res.data)
  }
}
