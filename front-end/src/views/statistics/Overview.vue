<template>
  <div class="trend-analysis">
    <n-card title="数据概览" :bordered="false">
      <n-grid :cols="4" :x-gap="16" :y-gap="16" responsive="screen">
        <n-grid-item v-for="(stat, index) in statistics" :key="index">
          <n-card hoverable>
            <template #header>
              <div class="stat-header">
                <n-icon :component="stat.icon" size="24" />
                <span>{{ stat.title }}</span>
              </div>
            </template>
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-desc">{{ stat.description }}</div>
          </n-card>
        </n-grid-item>
      </n-grid>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NCard, NGrid, NGridItem, NIcon } from 'naive-ui'
import { 
  AppsOutline,
  TrendingUpOutline,
  CodeSlashOutline
} from '@vicons/ionicons5'

const statistics = ref([
  {
    title: '总应用数',
    value: '0',
    description: '平台上的应用总数',
    icon: AppsOutline
  },
  {
    title: '总下载量',
    value: '0',
    description: '应用总下载次数',
    icon: TrendingUpOutline
  },
  {
    title: '开发者数',
    value: '0',
    description: '移植开发者总数',
    icon: CodeSlashOutline
  },
])

const fetchStatistics = async () => {
  try {
    const response = await fetch('/api/app/statistics/overview')
    const data = await response.json()
    statistics.value[0].value = data.totalApps
    statistics.value[1].value = data.totalDownloads
    statistics.value[2].value = data.developerCount || 0
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

onMounted(() => {
  fetchStatistics()
})
</script>

<style scoped>
.trend-analysis {
  padding: 16px;
  background-color: #f5f7fa;
}

.stat-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 8px 0;
}

.stat-desc {
  font-size: 14px;
  color: #909399;
}

@media screen and (max-width: 768px) {
  .trend-analysis {
    padding: 8px;
  }
}

@media screen and (max-width: 480px) {
  .trend-analysis {
    padding: 4px;
  }
}
</style> 