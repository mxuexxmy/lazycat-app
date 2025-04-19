<template>
  <div class="most-popular">
    <n-card>
      <template #header>
        <div class="header">
          <h2 class="section-title">最受欢迎</h2>
          <p class="section-desc">按下载量排序的热门应用</p>
        </div>
      </template>
      <n-spin :show="loading">
        <n-empty 
          v-if="!loading && (!apps || apps.length === 0)" 
          description="暂无数据"
        />
        <n-list v-else>
          <n-list-item 
            v-for="(app, index) in apps" 
            :key="app.pkgId" 
            class="app-item"
            @click="handleAppClick(app)"
          >
            <n-thing>
              <template #header>
                <n-space align="center">
                  <div class="rank-badge" v-if="index < 3">{{ index + 1 }}</div>
                  <n-avatar
                    :src="getAppIcon(app)"
                    :fallback-src="defaultIcon"
                    round
                    :size="48"
                    object-fit="cover"
                    class="app-icon"
                  />
                  <span class="app-name">{{ app.name }}</span>
                  <n-tag type="success" size="small" round>
                    <template #icon>
                      <n-icon><download-outline /></n-icon>
                    </template>
                    {{ formatDownloads(app.downloads) }}
                  </n-tag>
                </n-space>
              </template>
              <template #description>
                <div class="app-desc">{{ app.brief || app.description }}</div>
              </template>
              <template #footer>
                <n-space wrap :size="4">
                  <n-tag 
                    v-for="cat in app.category" 
                    :key="cat"
                    size="small"
                    round
                    :bordered="false"
                    type="warning"
                  >
                    {{ cat }}
                  </n-tag>
                  <n-tag 
                    size="small"
                    round
                    :bordered="false"
                    type="info"
                  >
                    v{{ app.version }}
                  </n-tag>
                  <n-tag 
                    v-if="app.supportPC && app.supportMobile"
                    size="small"
                    round
                    :bordered="false"
                    type="info"
                  >
                    全平台
                  </n-tag>
                  <n-tag 
                    v-else-if="app.supportPC"
                    size="small"
                    round
                    :bordered="false"
                    type="info"
                  >
                    PC端
                  </n-tag>
                  <n-tag 
                    v-else-if="app.supportMobile"
                    size="small"
                    round
                    :bordered="false"
                    type="info"
                  >
                    移动端
                  </n-tag>
                </n-space>
              </template>
            </n-thing>
          </n-list-item>
        </n-list>
      </n-spin>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  NCard, 
  NList,
  NListItem,
  NThing,
  NTag, 
  NEmpty, 
  NSpin, 
  NSpace, 
  NAvatar,
  NIcon
} from 'naive-ui'
import { DownloadOutline } from '@vicons/ionicons5'

interface AppInfo {
  name: string
  pkgId: string
  description: string
  brief: string
  category: string[]
  iconPath: string
  version: string
  downloads: number
  supportPC: boolean
  supportMobile: boolean
}

const loading = ref(false)
const apps = ref<AppInfo[]>([])
const defaultIcon = '/path/to/default-icon.png'
const router = useRouter()

const getAppIcon = (app: AppInfo) => {
  if (!app.iconPath) return defaultIcon
  return `https://dl.lazycatmicroserver.com/appstore/metarepo/apps/${app.pkgId}/icon.png`
}

const formatDownloads = (downloads: number) => {
  if (downloads >= 10000) {
    return `${(downloads / 10000).toFixed(1)}万`
  }
  return downloads.toString()
}

const fetchMostPopularApps = async () => {
  loading.value = true
  try {
    const response = await fetch('/api/apps/popular')
    const result = await response.json()
    if (Array.isArray(result)) {
      apps.value = result.map(app => ({
        pkgId: app.pkgId,
        name: app.name,
        description: app.description,
        brief: app.brief,
        category: app.category || [],
        iconPath: app.iconPath,
        version: app.version,
        downloads: app.downloadCount || 0,
        supportPC: app.supportPC,
        supportMobile: app.supportMobile
      }))
    }
  } catch (error) {
    console.error('Failed to fetch most popular apps:', error)
  } finally {
    loading.value = false
  }
}

const handleAppClick = (app: AppInfo) => {
  router.push({
    name: 'AppDetail',
    params: { pkgId: app.pkgId }
  })
}

onMounted(() => {
  fetchMostPopularApps()
})
</script>

<style scoped>
.most-popular {
  padding: 16px;
}

.header {
  margin-bottom: 8px;
}

.section-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.section-desc {
  margin: 8px 0 0;
  font-size: 14px;
  color: #666;
}

.app-item {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
}

.app-item:hover {
  background-color: rgba(0, 0, 0, 0.02);
}

.app-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.app-desc {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin: 8px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.app-icon {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  background: #fff;
  padding: 4px;
}

.rank-badge {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
  color: #fff;
  background: linear-gradient(135deg, #ff9a9e 0%, #fad0c4 99%, #fad0c4 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.app-item:nth-child(1) .rank-badge {
  background: linear-gradient(120deg, #f6d365 0%, #fda085 100%);
}

.app-item:nth-child(2) .rank-badge {
  background: linear-gradient(120deg, #89f7fe 0%, #66a6ff 100%);
}

.app-item:nth-child(3) .rank-badge {
  background: linear-gradient(120deg, #84fab0 0%, #8fd3f4 100%);
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .most-popular {
    padding: 12px;
  }

  .section-title {
    font-size: 18px;
  }

  .section-desc {
    font-size: 13px;
  }

  .app-name {
    font-size: 15px;
  }

  .app-desc {
    font-size: 13px;
  }
}

@media screen and (max-width: 480px) {
  .most-popular {
    padding: 8px;
  }

  .rank-badge {
    width: 20px;
    height: 20px;
    font-size: 12px;
  }
}
</style> 