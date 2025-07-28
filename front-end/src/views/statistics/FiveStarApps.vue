<template>
  <div class="five-star-apps">
    <n-card title="五星应用排行榜" class="mb-4">
      <n-grid :cols="24" :x-gap="24" :y-gap="24" responsive="screen">
        <n-grid-item v-for="app in apps" :key="app.pkgId" :span="6">
          <n-card hoverable @click="goToApp(app.pkgId)">
            <template #cover>
              <n-image
                :src="getAppIcon(app)"
                :fallback-src="defaultIcon"
                :alt="app.name"
                object-fit="contain"
                height="200"
                preview-disabled
              />
            </template>
            
            <n-space vertical>
              <n-ellipsis :line-clamp="1">
                <n-text strong>{{ app.name }}</n-text>
              </n-ellipsis>
              
              <n-ellipsis :line-clamp="2">
                <n-text depth="3">{{ app.description }}</n-text>
              </n-ellipsis>
              
              <n-space justify="space-between" align="center">
                <n-tag type="success" size="small">
                  <template #icon>
                    <n-icon><Star /></n-icon>
                  </template>
                  {{ app.score.toFixed(1) }}
                </n-tag>
                
                <n-space>
                  <n-tag type="info" size="small">
                    <template #icon>
                      <n-icon><Download /></n-icon>
                    </template>
                    {{ formatDownloadCount(app.downloadCount) }}
                  </n-tag>
                  
                  <n-tag type="warning" size="small">
                    <template #icon>
                      <n-icon><Chatbubble /></n-icon>
                    </template>
                    {{ formatReviewCount(app.totalReviews) }}
                  </n-tag>
                </n-space>
              </n-space>
              
              <n-space>
                <n-tag v-for="cat in app.category" :key="cat" size="small" type="info">
                  {{ cat }}
                </n-tag>
              </n-space>
            </n-space>
          </n-card>
        </n-grid-item>
      </n-grid>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  NCard, 
  NGrid, 
  NGridItem, 
  NImage, 
  NSpace, 
  NText, 
  NEllipsis, 
  NTag,
  NIcon 
} from 'naive-ui'
import { Star, Download, Chatbubble } from '@vicons/ionicons5'
interface FiveStarApp {
  pkgId: string
  name: string
  iconPath: string
  description: string
  downloadCount: number
  score: number
  totalReviews: number
  category: string[]
  creator: string
}

const getFiveStarApps = async (num) => {
  try {
    const response = await fetch('/api/app/statistics/app/five-star?limit=' + num )
    const data = await response.json()
    apps.value = data
  } catch (error) {
    console.error('获取分类统计失败:', error)
  }
}

const router = useRouter()
const apps = ref<FiveStarApp[]>([])
const defaultIcon = '/path/to/default-icon.png'

const fetchFiveStarApps = async () => {
  try {
    await getFiveStarApps(20) // 获取前20个五星应用
  } catch (error) {
    console.error('获取五星应用失败:', error)
  }
}

const formatDownloadCount = (count: number | null) => {
  if (!count) return '0'
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + 'w'
  } else if (count >= 1000) {
    return (count / 1000).toFixed(1) + 'k'
  }
  return count.toString()
}

const formatReviewCount = (count: number | null) => {
  if (!count) return '0'
  if (count >= 1000) {
    return (count / 1000).toFixed(1) + 'k'
  }
  return count.toString()
}

const getAppIcon = (app: FiveStarApp) => {
  if (!app.iconPath) return defaultIcon
  return `https://dl.lazycatmicroserver.com/appstore/metarepo/apps/${app.pkgId}/icon.png`
}

const goToApp = (pkgId: string) => {
  router.push(`/app/${pkgId}`)
}


onMounted(() => {
  fetchFiveStarApps()
})
</script>

<style scoped>
.five-star-apps {
  padding: 20px;
}

.mb-4 {
  margin-bottom: 16px;
}

.n-card {
  cursor: pointer;
  transition: all 0.3s;
}

.n-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

:deep(.n-image) {
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f9f9f9;
  padding: 24px;
}

:deep(.n-image img) {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}
</style> 