<template>
  <el-card class="submit-form-card" shadow="hover">
    <template #header>
      <span>提交任务</span>
    </template>
    
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="执行命令" prop="command">
        <el-input 
          v-model="form.command" 
          placeholder="例如: echo 'Hello World'"
          type="textarea"
          :rows="3"
        />
      </el-form-item>
      
      <el-form-item label="CPU需求" prop="cpuRequired">
        <el-input-number 
          v-model="form.cpuRequired" 
          :min="1" 
          :max="32"
          style="width: 100%"
        />
      </el-form-item>
      
      <el-form-item label="内存需求(MB)" prop="memRequired">
        <el-input-number 
          v-model="form.memRequired" 
          :min="1" 
          :max="16384"
          style="width: 100%"
        />
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="submitTask" :loading="submitting" style="width: 100%">
          提交任务
        </el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { taskApi } from '../api/client'
import type { TaskSubmitRequest } from '../types'

const emit = defineEmits<{
  taskSubmitted: []
}>()

const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive<TaskSubmitRequest>({
  command: '',
  cpuRequired: 1,
  memRequired: 100
})

const rules: FormRules = {
  command: [
    { required: true, message: '请输入执行命令', trigger: 'blur' }
  ],
  cpuRequired: [
    { required: true, message: '请输入CPU需求', trigger: 'blur' }
  ],
  memRequired: [
    { required: true, message: '请输入内存需求', trigger: 'blur' }
  ]
}

const submitTask = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await taskApi.submitTask(form)
        ElMessage.success('任务提交成功')
        emit('taskSubmitted')
        // 重置表单
        form.command = ''
        form.cpuRequired = 1
        form.memRequired = 100
      } catch (error: any) {
        ElMessage.error('任务提交失败: ' + (error.message || '未知错误'))
      } finally {
        submitting.value = false
      }
    }
  })
}
</script>

<style scoped>
.submit-form-card {
  margin-bottom: 20px;
}
</style>
