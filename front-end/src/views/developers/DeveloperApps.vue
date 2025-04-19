<template>
  <div class="developer-apps">
    <div class="developer-header-fixed">
      <div class="content-wrapper">
        <n-card>
          <template #header>
            <div class="developer-header">
              <div class="developer-info">
                <div class="avatar-wrapper" @click="handleBackClick">
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
                <div class="info-content">
                  <h1 class="developer-name">{{ developer.name }}</h1>
                  <div class="developer-stats">
                    <span>应用数: {{ developer.appCount }}</span>
                    <n-divider vertical />
                    <span>总下载: {{ formatDownloads(developer.totalDownloads) }}</span>
                    <n-divider vertical />
                    <span>最近更新: {{ formatDate(developer.lastUpdateDate) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </template>

          <div class="search-bar">
            <n-input
              v-model:value="searchQuery"
              placeholder="搜索应用..."
              clearable
            >
              <template #prefix>
                <n-icon><search /></n-icon>
              </template>
            </n-input>
          </div>
        </n-card>
      </div>
    </div>

    <div class="apps-content">
      <div class="content-wrapper">
        <n-card>
          <n-spin :show="loading">
            <n-empty v-if="!loading && (!filteredApps || filteredApps.length === 0)" description="暂无应用" />
            <div v-else class="app-grid">
              <n-card
                v-for="app in filteredApps"
                :key="app.pkgId"
                class="app-card"
                hoverable
                @click="handleAppClick(app)"
              >
                <template #cover>
                  <n-image
                    :src="getAppIcon(app)"
                    :fallback-src="defaultIcon"
                    preview-disabled
                    object-fit="contain"
                    class="app-icon"
                  />
                </template>
                <template #header>
                  <div class="app-name" :title="app.name">{{ app.name }}</div>
                </template>
                <template #header-extra>
                  <n-tag size="small" type="info">{{ formatDownloads(app.downloads || 0) }}</n-tag>
                </template>
                <div class="app-description">{{ app.description || '暂无描述' }}</div>
              </n-card>
            </div>
          </n-spin>
        </n-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NCard, NSpin, NEmpty, NInput, NIcon, NDivider, NTag, NImage } from 'naive-ui'
import { Search } from '@vicons/ionicons5'

interface App {
  pkgId: string
  name: string
  description?: string
  icon: string
  downloads?: number
  updateDate: string
}

interface Developer {
  creatorId: number
  name: string
  avatar?: string
  appCount: number
  totalDownloads: number
  lastUpdateDate: string
}

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const searchQuery = ref('')
const defaultIcon = '/path/to/default-icon.png'
const developer = ref<Developer>({
  creatorId: 0,
  name: '',
  appCount: 0,
  totalDownloads: 0,
  lastUpdateDate: ''
})
const apps = ref<App[]>([])

// 头像背景色列表
const avatarColors = [
  '#1677ff', '#13c2c2', '#52c41a', '#faad14', '#eb2f96',
  '#722ed1', '#2f54eb', '#fa8c16', '#fadb14', '#a0d911'
]

const getAvatarColor = (name: string) => {
  if (!name) return avatarColors[0]
  const charSum = name.split('').reduce((sum, char) => sum + char.charCodeAt(0), 0)
  return avatarColors[charSum % avatarColors.length]
}

const getFirstChar = (name: string) => {
  if (!name) return '?'
  return name.charAt(0)
}

const formatDownloads = (downloads: number) => {
  if (downloads >= 10000) {
    return `${(downloads / 10000).toFixed(1)}万`
  }
  return downloads.toString()
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '未知'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const filteredApps = computed(() => {
  if (!searchQuery.value) return apps.value
  const query = searchQuery.value.toLowerCase()
  return apps.value.filter(app => 
    app.name.toLowerCase().includes(query) ||
    app.description?.toLowerCase().includes(query)
  )
})

const handleAppClick = (app: App) => {
  router.push({
    name: 'AppDetail',
    params: { pkgId: app.pkgId }
  })
}

const handleBackClick = () => {
  router.back()
}

const getAppIcon = (app: App) => {
  if (!app.icon) return defaultIcon
  return `https://dl.lazycatmicroserver.com/appstore/metarepo/apps/${app.pkgId}/icon.png`
}

const fetchDeveloperInfo = async () => {
  try {
    loading.value = true
    const creatorId = route.params.creatorId
    const [userResponse, appsResponse] = await Promise.all([
      fetch(`/api/users/${creatorId}/info`),
      fetch(`/api/apps/developers/${creatorId}/apps`)
    ])
    
    const userResult = await userResponse.json()
    const appsResult = await appsResponse.json()
    
    if (userResult.id) {
      developer.value = {
        creatorId: userResult.id,
        name: userResult.nickname || userResult.username || '未知开发者',
        avatar: userResult.avatar,
        appCount: 0,
        totalDownloads: 0,
        lastUpdateDate: ''
      }
    }

    if (Array.isArray(appsResult)) {
      apps.value = appsResult.map(app => ({
        pkgId: app.pkgId,
        name: app.name || '',
        description: app.description || app.brief || '',
        icon: app.iconPath ? `/api${app.iconPath}` : '',
        downloads: app.downloadCount || 0,
        updateDate: app.updateDate || app.lastUpdated || ''
      }))

      // 更新开发者统计信息
      if (developer.value) {
        developer.value.appCount = apps.value.length
        developer.value.totalDownloads = apps.value.reduce((sum, app) => sum + (app.downloads || 0), 0)
        developer.value.lastUpdateDate = apps.value.length > 0 ? 
          apps.value.reduce((latest, app) => 
            app.updateDate > latest ? app.updateDate : latest, 
            apps.value[0].updateDate
          ) : ''
      }
    }
  } catch (error) {
    console.error('Failed to fetch developer data:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDeveloperInfo()
})
</script>

<style scoped>
.developer-apps {
  width: 100%;
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  width: 100%;
  box-sizing: border-box;
}

.developer-header-fixed {
  position: fixed;
  top: 64px;
  left: 0;
  right: 0;
  z-index: 100;
  background-color: #f5f6fb;
  padding: 24px 0;
}

.apps-content {
  margin-top: 220px;
  padding: 24px 0;
}

.developer-header {
  padding: 16px 0;
}

.developer-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar-wrapper {
  width: 64px;
  height: 64px;
  flex-shrink: 0;
  cursor: pointer;
  transition: transform 0.2s;
}

.avatar-wrapper:hover {
  transform: scale(1.05);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  font-weight: bold;
}

.info-content {
  flex: 1;
  min-width: 0;
}

.developer-name {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: bold;
}

.developer-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
}

.search-bar {
  margin-bottom: 24px;
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.app-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.app-card:hover {
  transform: translateY(-4px);
}

.app-icon {
  width: 100%;
  height: 160px;
  object-fit: contain;
  background-color: #f9f9f9;
  padding: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.app-name {
  font-size: 16px;
  font-weight: bold;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.app-description {
  font-size: 14px;
  color: #666;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .developer-info {
    flex-direction: column;
    align-items: flex-start;
    text-align: center;
  }

  .avatar-wrapper {
    margin: 0 auto;
  }

  .developer-name {
    font-size: 20px;
    text-align: center;
  }

  .developer-stats {
    justify-content: center;
    flex-wrap: wrap;
  }

  .app-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  }

  .app-icon {
    height: 120px;
    padding: 16px;
  }
}
</style> 