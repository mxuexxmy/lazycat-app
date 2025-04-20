<template>
  <n-card title="相似应用推荐" :bordered="false">
    <template #header-extra>
      <n-button text size="small" @click="refresh">
        <template #icon>
          <n-icon><refresh-outline /></n-icon>
        </template>
        换一批
      </n-button>
    </template>
    <n-spin :show="loading">
      <div class="similar-apps" v-if="apps.length > 0">
        <n-grid :cols="3" :x-gap="16" :y-gap="16" responsive="screen" :cols-xs="1" :cols-s="2" :cols-m="2" :cols-l="3">
          <n-grid-item v-for="app in apps" :key="app.pkgId">
            <n-card hoverable @click="goToAppDetail(app.pkgId)" class="app-card">
              <div class="app-content">
                <div class="app-icon">
                  <n-image
                    :src="getAppIcon(app)"
                    :fallback-src="defaultIcon"
                    preview-disabled
                    object-fit="cover"
                    class="icon-image"
                    lazy
                  />
                </div>
                <div class="app-info">
                  <div class="app-title">{{ app.name }}</div>
                  <div class="app-desc">{{ app.brief }}</div>
                  <div class="app-meta">
                    <div class="app-download">
                      <n-icon><download-outline /></n-icon>
                      {{ formatDownloadCount(app.downloadCount) }}
                    </div>
                    <div class="app-platform">
                      <n-tag size="small" :bordered="false" type="success" v-if="app.supportPC">PC端</n-tag>
                      <n-tag size="small" :bordered="false" type="info" v-if="app.supportMobile">移动端</n-tag>
                    </div>
                  </div>
                  <div class="app-tags">
                    <n-space :size="4">
                      <n-tag v-for="cat in app.category" :key="cat" size="small" round>
                        {{ cat }}
                      </n-tag>
                    </n-space>
                  </div>
                </div>
              </div>
            </n-card>
          </n-grid-item>
        </n-grid>
      </div>
      <n-empty v-else description="暂无相似应用" />
    </n-spin>
  </n-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { App } from '@/types'
import {
  NCard,
  NGrid,
  NGridItem,
  NImage,
  NTag,
  NSpace,
  NSpin,
  NEmpty,
  NButton,
  NIcon
} from 'naive-ui'
import {
  RefreshOutline,
  DownloadOutline
} from '@vicons/ionicons5'

const props = defineProps<{
  pkgId: string
}>()

const router = useRouter()
const loading = ref(false)
const apps = ref<App[]>([])
const defaultIcon = 'https://dl.lazycatmicroserver.com/appstore/metarepo/default-icon.png'

const getAppIcon = (app: App) => {
  if (!app.iconPath) return defaultIcon
  return `https://dl.lazycatmicroserver.com/appstore/metarepo/apps/${app.pkgId}/icon.png`
}

const formatDownloadCount = (count: number | null) => {
  if (!count) return '0'
  if (count >= 10000) {
    return `${(count / 10000).toFixed(1)}w`
  }
  if (count >= 1000) {
    return `${(count / 1000).toFixed(1)}k`
  }
  return count.toString()
}

const goToAppDetail = (pkgId: string) => {
  router.push({
    name: 'AppDetail',
    params: { pkgId }
  })
}

const fetchSimilarApps = async () => {
  loading.value = true
  try {
    const response = await fetch(`/api/recommendations/similar/${props.pkgId}`)
    const data = await response.json()
    apps.value = data
  } catch (error) {
    console.error('获取相似应用失败:', error)
  } finally {
    loading.value = false
  }
}

const refresh = () => {
  fetchSimilarApps()
}

onMounted(() => {
  fetchSimilarApps()
})
</script>

<style scoped>
.similar-apps {
  margin-top: 8px;
}

.app-card {
  height: 100%;
  transition: all 0.3s ease;
}

.app-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.app-content {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.app-icon {
  flex-shrink: 0;
  width: 56px;
  height: 56px;
  border-radius: 12px;
  overflow: hidden;
  background: #f9f9f9;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.icon-image {
  width: 100%;
  height: 100%;
}

.app-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.app-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-desc {
  font-size: 12px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
}

.app-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.app-download {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.app-download :deep(.n-icon) {
  font-size: 14px;
}

.app-platform {
  display: flex;
  gap: 4px;
}

.app-tags {
  margin-top: auto;
}

@media screen and (max-width: 768px) {
  .app-content {
    flex-direction: row;
    align-items: center;
  }

  .app-icon {
    width: 48px;
    height: 48px;
  }

  .app-info {
    gap: 4px;
  }

  .app-desc {
    -webkit-line-clamp: 1;
  }

  .app-meta {
    flex-direction: row;
  }
}
</style> 