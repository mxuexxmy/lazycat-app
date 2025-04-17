<template>
  <n-card title="开发者排行榜" class="developer-ranking">
    <n-spin :show="loading">
      <n-empty v-if="!loading && (!developers || developers.length === 0)" description="暂无数据" />
      <div v-else class="ranking-list">
        <n-list>
          <n-list-item v-for="(developer, index) in sortedDevelopers" :key="developer.creatorId">
            <div class="developer-item">
              <div class="rank" :class="getRankClass(index + 1)">
                {{ index + 1 }}
              </div>
              <n-avatar
                round
                size="large"
                :style="{ background: getAvatarColor(developer.name) }"
              >
                {{ getFirstChar(developer.name) }}
              </n-avatar>
              <div class="developer-info">
                <div class="developer-name">{{ developer.name }}</div>
                <div class="developer-stats">
                  <span>应用数: {{ developer.appCount }}</span>
                  <n-divider vertical />
                  <span>总下载: {{ formatDownloads(developer.totalDownloads) }}</span>
                  <n-divider vertical />
                  <span>最近更新: {{ formatDate(developer.lastUpdateDate) }}</span>
                </div>
                <div class="app-list">
                  <n-tag
                    v-for="app in developer.apps.slice(0, 3)"
                    :key="app.pkgId"
                    size="small"
                    :bordered="false"
                    @click="handleAppClick(app)"
                  >
                    {{ app.name }}
                  </n-tag>
                  <n-tag v-if="developer.apps.length > 3" size="small" :bordered="false">
                    等 {{ developer.apps.length }} 个应用
                  </n-tag>
                </div>
              </div>
            </div>
          </n-list-item>
        </n-list>
      </div>
    </n-spin>
  </n-card>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NCard, NSpin, NEmpty, NList, NListItem, NAvatar, NDivider, NTag } from 'naive-ui'

interface AppData {
  pkgId: string
  name: string
  creator: string
  creatorId: number
  updateDate: string
}

interface DeveloperInfo {
  nickname: string
}

interface App {
  pkgId: string
  name: string
  updateDate: string
  downloads?: number
}

interface Developer {
  creatorId: number
  name: string
  nickname?: string
  apps: App[]
  appCount: number
  totalDownloads: number
  lastUpdateDate: string
}

const router = useRouter()
const loading = ref(true)
const developers = ref<Developer[]>([])

// 头像背景色列表
const avatarColors = [
  '#1677ff', '#13c2c2', '#52c41a', '#faad14', '#eb2f96',
  '#722ed1', '#2f54eb', '#fa8c16', '#fadb14', '#a0d911'
]

const getAvatarColor = (name: string) => {
  // 使用名字的 charCode 来选择颜色
  const charSum = name.split('').reduce((sum, char) => sum + char.charCodeAt(0), 0)
  return avatarColors[charSum % avatarColors.length]
}

const getFirstChar = (name: string) => {
  return name.charAt(0)
}

const sortedDevelopers = computed(() => {
  return [...developers.value].sort((a, b) => {
    // 首先按应用数量排序
    if (b.appCount !== a.appCount) {
      return b.appCount - a.appCount
    }
    // 其次按下载量排序
    return b.totalDownloads - a.totalDownloads
  })
})

const getRankClass = (rank: number) => {
  if (rank === 1) return 'rank-gold'
  if (rank === 2) return 'rank-silver'
  if (rank === 3) return 'rank-bronze'
  return ''
}

const formatDownloads = (downloads: number) => {
  if (downloads >= 10000) {
    return `${(downloads / 10000).toFixed(1)}万`
  }
  return downloads.toString()
}

const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const handleAppClick = (app: App) => {
  router.push({
    name: 'AppDetail',
    params: { pkgId: app.pkgId }
  })
}

const fetchDeveloperNickname = async (pkgId: string): Promise<string | null> => {
  try {
    const response = await fetch(`https://appstore.api.lazycat.cloud/api/app/developer/${pkgId}`)
    const result = await response.json()
    if (result.success && result.data?.nickname) {
      return result.data.nickname
    }
  } catch (error) {
    console.error('Failed to fetch developer nickname:', error)
  }
  return null
}

const fetchAllApps = async () => {
  try {
    loading.value = true
    const response = await fetch('https://appstore.api.lazycat.cloud/api/app/list')
    const result = await response.json()
    
    if (result.success && result.data) {
      // 按开发者ID分组应用
      const developerMap = new Map<number, Developer>()
      
      for (const app of result.data as AppData[]) {
        const creatorId = app.creatorId
        if (!developerMap.has(creatorId)) {
          developerMap.set(creatorId, {
            creatorId,
            name: '匿名开发者', // 默认为匿名开发者
            apps: [],
            appCount: 0,
            totalDownloads: 0,
            lastUpdateDate: app.updateDate
          })
        }
        
        const developer = developerMap.get(creatorId)
        if (developer) {
          developer.apps.push({
            pkgId: app.pkgId,
            name: app.name,
            updateDate: app.updateDate,
            downloads: 0
          })
          
          developer.appCount = developer.apps.length
          if (new Date(app.updateDate) > new Date(developer.lastUpdateDate)) {
            developer.lastUpdateDate = app.updateDate
          }
        }
      }
      
      // 获取开发者昵称
      const developerList = Array.from(developerMap.values())
      for (const developer of developerList) {
        if (developer.apps.length > 0) {
          const nickname = await fetchDeveloperNickname(developer.apps[0].pkgId)
          if (nickname) {
            developer.nickname = nickname
            developer.name = nickname
          }
        }
      }
      
      developers.value = developerList
    }
  } catch (error) {
    console.error('Failed to fetch apps:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchAllApps()
})
</script>

<style scoped>
.developer-ranking {
  padding: 16px;
}

.ranking-list {
  margin-top: 16px;
}

.developer-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px;
}

.rank {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-weight: bold;
  background-color: #f0f0f0;
  flex-shrink: 0;
}

.rank-gold {
  background-color: #ffd700;
  color: #fff;
}

.rank-silver {
  background-color: #c0c0c0;
  color: #fff;
}

.rank-bronze {
  background-color: #cd7f32;
  color: #fff;
}

.developer-info {
  flex: 1;
  min-width: 0;
}

.developer-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 4px;
}

.developer-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.app-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

:deep(.n-tag) {
  cursor: pointer;
}

:deep(.n-tag:hover) {
  background-color: #f0faf5;
}

:deep(.n-avatar) {
  color: #fff;
  font-size: 18px;
  flex-shrink: 0;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .developer-ranking {
    padding: 0;
  }

  .developer-item {
    padding: 12px;
    border-bottom: 1px solid #f0f0f0;
  }

  .developer-name {
    font-size: 15px;
  }

  .developer-stats {
    font-size: 13px;
    gap: 12px;
  }

  .developer-stats > span {
    display: flex;
    align-items: center;
  }

  :deep(.n-divider) {
    display: none;
  }

  .app-list {
    margin-top: 8px;
  }

  :deep(.n-tag) {
    font-size: 12px;
  }
}

@media screen and (max-width: 480px) {
  .developer-item {
    padding: 10px;
  }

  .rank {
    width: 28px;
    height: 28px;
    font-size: 14px;
  }

  :deep(.n-avatar) {
    width: 36px !important;
    height: 36px !important;
    font-size: 16px !important;
  }

  .developer-name {
    font-size: 14px;
  }

  .developer-stats {
    font-size: 12px;
    gap: 8px;
  }
}
</style> 