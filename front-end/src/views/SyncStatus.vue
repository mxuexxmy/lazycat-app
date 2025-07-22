<template>
  <div class="sync-status-container">
    <h2>同步信息</h2>
    <div class="progress-bar-wrapper">
      <div class="progress-bar">
        <div class="progress-bar-inner" :style="{ width: syncProgress + '%' }"></div>
      </div>
      <span>{{ syncProgress }}%</span>
    </div>
    <div class="sync-details">
      <p>应用同步：{{ status.appCount }} / {{ status.totalApps }}</p>
      <p>分类同步：{{ status.categoryCount }} / {{ status.totalCategories }}</p>
      <p>同步状态：<span :style="{ color: status.isInitialSyncComplete ? 'green' : 'orange' }">{{ status.isInitialSyncComplete ? '已完成' : '同步中' }}</span></p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const syncProgress = ref(0)
const status = ref({
  appCount: 0,
  totalApps: 0,
  categoryCount: 0,
  totalCategories: 0,
  isInitialSyncComplete: false
})
let timer: number | null = null

const checkSyncStatus = async () => {
  try {
    const response = await fetch('/api/apps/status')
    const data = await response.json()
    status.value = data
    const totalItems = data.totalApps + data.totalCategories
    const completedItems = data.appCount + data.categoryCount
    syncProgress.value = totalItems > 0 ? Math.round((completedItems / totalItems) * 100) : 0
    if (!data.isInitialSyncComplete) {
      timer = window.setTimeout(checkSyncStatus, 5000)
    } else {
      timer = window.setTimeout(checkSyncStatus, 60000)
    }
  } catch (error) {
    console.error('检查同步状态失败:', error)
    timer = window.setTimeout(checkSyncStatus, 5000)
  }
}

onMounted(() => {
  checkSyncStatus()
})
onUnmounted(() => {
  if (timer) clearTimeout(timer)
})
</script>

<style scoped>
.sync-status-container {
  max-width: 400px;
  margin: 40px auto;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.progress-bar-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.progress-bar {
  flex: 1;
  height: 16px;
  background: #eee;
  border-radius: 8px;
  overflow: hidden;
}
.progress-bar-inner {
  height: 100%;
  background: #42b983;
  transition: width 0.3s;
}
.sync-details {
  font-size: 15px;
  color: #333;
}
</style> 