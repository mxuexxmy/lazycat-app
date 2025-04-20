<template>
  <div class="users-container">
    <n-card title="开发者统计" :bordered="false">
      <n-tabs type="line" animated>
        <n-tab-pane name="active" tab="活跃开发者">
          <n-data-table
            :columns="columns"
            :data="activeUsers"
            :loading="loading"
          />
        </n-tab-pane>
        <n-tab-pane name="growth" tab="开发者增长">
          <n-grid :cols="3" :x-gap="16" :y-gap="16" responsive="screen">
            <n-grid-item v-for="(stat, index) in growthStats" :key="index">
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
        </n-tab-pane>
      </n-tabs>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NCard, NGrid, NGridItem, NIcon, NTabs, NTabPane, NDataTable } from 'naive-ui'
import { 
  PeopleOutline,
  TrendingUpOutline,
  TimeOutline
} from '@vicons/ionicons5'
import type { DataTableColumns } from 'naive-ui'

interface User {
  username: string
  userId: string
  downloads: number
  lastActive: string
}

interface GrowthStat {
  title: string
  value: string
  description: string
  icon: typeof PeopleOutline | typeof TrendingUpOutline | typeof TimeOutline
}

const loading = ref(false)
const activeUsers = ref<User[]>([])
const growthStats = ref<GrowthStat[]>([
  {
    title: '今日新增',
    value: '0',
    description: '今日新增用户数',
    icon: TrendingUpOutline
  },
  {
    title: '本周新增',
    value: '0',
    description: '本周新增用户数',
    icon: TimeOutline
  },
  {
    title: '本月新增',
    value: '0',
    description: '本月新增用户数',
    icon: PeopleOutline
  }
])

const columns: DataTableColumns<User> = [
  {
    title: '用户名',
    key: 'nickname'
  },
  {
    title: '下载量',
    key: 'downloads',
    sorter: (a, b) => a.downloads - b.downloads
  },
  {
    title: '最后活跃',
    key: 'lastActive'
  }
]


const fetchActiveUsers = async () => {
  loading.value = true
  try {
    const response = await fetch('/api/apps/statistics/users/active')
    const data = await response.json()
    activeUsers.value = data
  } catch (error) {
    console.error('获取活跃用户失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchGrowthStats = async () => {
  try {
    const response = await fetch('/api/apps/statistics/users/growth')
    const data = await response.json()
    growthStats.value[0].value = data.daily
    growthStats.value[1].value = data.weekly
    growthStats.value[2].value = data.monthly
  } catch (error) {
    console.error('获取用户增长数据失败:', error)
  }
}

onMounted(() => {
  fetchActiveUsers()
  fetchGrowthStats()
})
</script>

<style scoped>
.users-container {
  padding: 16px;
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

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .users-container {
    padding: 12px;
  }

  :deep(.n-grid) {
    grid-template-columns: 1fr !important;
  }

  .stat-header {
    font-size: 15px;
  }

  .stat-value {
    font-size: 22px;
  }

  .stat-desc {
    font-size: 13px;
  }
}

@media screen and (max-width: 480px) {
  .users-container {
    padding: 8px;
  }

  .stat-header {
    font-size: 14px;
  }

  .stat-value {
    font-size: 20px;
  }

  .stat-desc {
    font-size: 12px;
  }
}
</style> 