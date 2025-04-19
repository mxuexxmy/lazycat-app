<template>
  <div class="recent-updates">
    <n-card>
      <template #header>
        <div class="header">
          <h2 class="section-title">最近更新</h2>
          <p class="section-desc">应用的最新版本更新</p>
        </div>
      </template>
      <n-spin :show="loading">
        <n-empty 
          v-if="!loading && (!apps || apps.length === 0)" 
          description="暂无数据"
        />
        <n-list v-else>
          <n-list-item 
            v-for="app in apps" 
            :key="app.pkgId" 
            class="app-item"
            @click="handleAppClick(app)"
          >
            <n-thing>
              <template #header>
                <n-space align="center">
                  <n-avatar
                    :src="getAppIcon(app)"
                    :fallback-src="defaultIcon"
                    round
                    :size="48"
                    object-fit="cover"
                    class="app-icon"
                  />
                  <span class="app-name">{{ app.name }}</span>
                  <n-tag 
                    :type="getUpdateTagType(app.updateDate)"
                    size="small"
                    round
                  >
                    {{ formatUpdateTime(app.updateDate) }}
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
  NAvatar 
} from 'naive-ui'
import { formatDistanceToNow } from 'date-fns'
import { zhCN } from 'date-fns/locale'

interface AppInfo {
  name: string
  pkgId: string
  description: string
  brief: string
  category: string[]
  iconPath: string
  version: string
  updateDate: string
  supportPC: boolean
  supportMobile: boolean
}

const loading = ref(false)
const apps = ref<AppInfo[]>([])
const defaultIcon = 'https://dl.lazycatmicroserver.com/appstore/metarepo/default-icon.png'
const router = useRouter()

const getAppIcon = (app: AppInfo) => {
  if (!app.iconPath) return defaultIcon
  const iconPath = app.iconPath.startsWith('/') ? app.iconPath.slice(1) : app.iconPath
  return `https://dl.lazycatmicroserver.com/appstore/metarepo/apps/${app.pkgId}/icon.png`
}

const formatUpdateTime = (time: string) => {
  return formatDistanceToNow(new Date(time), {
    addSuffix: true,
    locale: zhCN
  })
}

const getUpdateTagType = (time: string) => {
  const days = (Date.now() - new Date(time).getTime()) / (1000 * 60 * 60 * 24)
  if (days <= 1) return 'success'
  if (days <= 7) return 'info'
  return 'default'
}

const fetchRecentUpdates = async () => {
  loading.value = true
  try {
    const response = await fetch('/api/apps/latest')
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
        updateDate: app.updateDate,
        supportPC: app.supportPC,
        supportMobile: app.supportMobile
      }))
    }
  } catch (error) {
    console.error('Failed to fetch recent updates:', error)
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
  fetchRecentUpdates()
})
</script>

<style scoped>
.recent-updates {
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

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .recent-updates {
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
  .recent-updates {
    padding: 8px;
  }
}
</style> 