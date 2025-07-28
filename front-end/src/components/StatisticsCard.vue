<template>
  <div class="statistics-card">
    <n-card :bordered="false">
      <div class="statistics-content">
        <div class="stat-item">
          <n-icon size="24" class="stat-icon"><apps /></n-icon>
          <n-tooltip trigger="hover">
            <template #trigger>
              <div class="stat-value">{{ totalApps }}</div>
            </template>
            应用
          </n-tooltip>
        </div>
        <div class="stat-item">
          <n-icon size="24" class="stat-icon"><people /></n-icon>
          <n-tooltip trigger="hover">
            <template #trigger>
              <div class="stat-value">{{ totalDevelopers }}</div>
            </template>
            开发者
          </n-tooltip>
        </div>
      </div>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { NCard, NIcon, NTooltip } from 'naive-ui'
import { ref, onMounted } from 'vue'
import { Apps, People } from '@vicons/ionicons5'

const totalApps = ref(0)
const totalDevelopers = ref(0)

const fetchStatistics = async () => {
  try {
    const response = await fetch('/api/app/statistics')
    const data = await response.json()
    totalApps.value = data.totalApps
    totalDevelopers.value = data.totalDevelopers
  } catch (error) {
    console.error('获取统计信息失败:', error)
  }
}

onMounted(() => {
  fetchStatistics()
})
</script>

<style scoped>
.statistics-card {
  margin-bottom: 16px;
}

.statistics-content {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 4px 0;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.stat-icon {
  color: #18a058;
  font-size: 20px;
}

.stat-value {
  font-size: 18px;
  font-weight: bold;
  color: #18a058;
  cursor: help;
}

@media screen and (max-width: 768px) {
  .statistics-content {
    flex-direction: row;
    gap: 8px;
    padding: 4px;
  }
  
  .stat-item {
    width: auto;
  }

  .stat-value {
    font-size: 16px;
  }
}
</style> 