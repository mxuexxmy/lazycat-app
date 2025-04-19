<template>
  <div class="rankings-container">
    <n-card title="开发者排行榜" class="developer-ranking">
      <n-spin :show="loading">
        <n-empty v-if="!loading && (!developers || developers.length === 0)" description="暂无数据" />
        <div v-else class="ranking-list">
          <n-list>
            <n-list-item v-for="(developer, index) in sortedDevelopers" :key="developer.creatorId">
              <div class="developer-item" @click="handleDeveloperClick(developer)">
                <div class="rank" :class="getRankClass(index + 1)">
                  {{ index + 1 }}
                </div>
                <div class="avatar-wrapper">
                  <img
                    v-if="developer.avatar"
                    :src="developer.avatar"
                    :alt="developer.name"
                    class="avatar-img"
                  />
                  <div v-else class="avatar-placeholder" :style="{ background: getAvatarColor(developer.name) }">
                    {{ getFirstChar(developer.name) }}
                  </div>
                </div>
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NCard, NSpin, NEmpty, NList, NListItem, NDivider, NTag } from 'naive-ui'

interface App {
  pkgId: string
  name: string
  updateDate: string
  downloads?: number
}

interface Developer {
  creatorId: number
  name: string
  avatar?: string
  apps: App[]
  appCount: number
  totalDownloads: number
  lastUpdateDate: string
}

interface DeveloperRankingResponse {
  success: boolean
  data: Array<{
    id: number
    nickName: string
    avatar?: string
    apps: Array<{
      pkgId: string
      name: string
      updateDate: string
    }>
    appCount: number
    totalDownloads: number
    lastUpdateDate: string
  }>
}

const router = useRouter()
const loading = ref(true)
const developers = ref<Developer[]>([])

// 头像背景色列表
const avatarColors = [
  '#1677ff', '#13c2c2', '#52c41a', '#faad14', '#eb2f96',
  '#722ed1', '#2f54eb', '#fa8c16', '#fadb14', '#a0d911'
]

const getAvatarColor = (name: string | undefined) => {
  if (!name) return avatarColors[0] // 默认返回第一个颜色
  // 使用名字的 charCode 来选择颜色
  const charSum = name.split('').reduce((sum, char) => sum + char.charCodeAt(0), 0)
  return avatarColors[charSum % avatarColors.length]
}

const getFirstChar = (name: string | undefined) => {
  if (!name) return '?' // 返回问号作为默认字符
  return name.charAt(0)
}

const sortedDevelopers = computed(() => {
  if (!developers.value) return []
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

const handleDeveloperClick = (developer: Developer) => {
  if (!developer || typeof developer.creatorId !== 'number') {
    console.error('Invalid developer data:', developer)
    return
  }
  router.push({
    name: 'DeveloperApps',
    params: { creatorId: developer.creatorId.toString() }
  })
}

const fetchAllApps = async () => {
  try {
    loading.value = true
    const response = await fetch('/api/apps/developers/ranking')
    const result = await response.json() as DeveloperRankingResponse
    
    if (result.success && result.data) {
      developers.value = result.data.map(developer => ({
        creatorId: developer.id,
        name: developer.nickName || '未知开发者',
        avatar: developer.avatar,
        apps: developer.apps || [],
        appCount: developer.appCount || 0,
        totalDownloads: developer.totalDownloads || 0,
        lastUpdateDate: developer.lastUpdateDate || ''
      }))
    } else {
      developers.value = []
    }
  } catch (error) {
    console.error('Failed to fetch developer ranking:', error)
    developers.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchAllApps()
})
</script>

<style scoped>
.rankings-container {
  width: 100%;
}

.developer-ranking {
  width: 100%;
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

.avatar-wrapper {
  position: relative;
  width: 40px;
  height: 40px;
  cursor: pointer;
  perspective: 1000px;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
  transition: all 0.6s ease-in-out;
  transform-style: preserve-3d;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  transition: all 0.6s ease-in-out;
  transform-style: preserve-3d;
}

.avatar-wrapper:hover .avatar-img,
.avatar-wrapper:hover .avatar-placeholder {
  transform: rotateY(360deg);
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

  .developer-name {
    font-size: 14px;
  }

  .developer-stats {
    font-size: 12px;
    gap: 8px;
  }
}
</style> 