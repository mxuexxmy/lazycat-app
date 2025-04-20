<template>
  <n-card :title="title || `${category}分类热门应用`" :bordered="false">
    <template #header-extra>
      <n-button text size="small" @click="refresh">
        <template #icon>
          <n-icon><refresh-outline /></n-icon>
        </template>
        刷新
      </n-button>
    </template>
    <n-spin :show="loading">
      <div class="popular-apps" v-if="apps.length > 0">
        <div v-for="(app, index) in apps" :key="app.pkgId" class="app-item" @click="goToAppDetail(app.pkgId)">
          <div class="app-rank" :class="getRankClass(index)">{{ index + 1 }}</div>
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
            <div class="app-name">{{ app.name }}</div>
            <div class="app-brief">{{ app.brief }}</div>
            <div class="app-meta">
              <div class="app-download">
                <n-icon><download-outline /></n-icon>
                {{ formatDownloadCount(app.downloads) }}
              </div>
              <div class="app-platform">
                <n-tag size="small" :bordered="false" type="success" v-if="app.supportPC">PC端</n-tag>
                <n-tag size="small" :bordered="false" type="info" v-if="app.supportMobile">移动端</n-tag>
              </div>
            </div>
          </div>
        </div>
      </div>
      <n-empty v-else description="暂无热门应用" />
    </n-spin>
  </n-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { App } from '@/types'
import {
  NCard,
  NImage,
  NTag,
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
  category: string
  title?: string
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

const getRankClass = (index: number) => {
  const classes = ['rank-first', 'rank-second', 'rank-third']
  return index < 3 ? classes[index] : ''
}

const goToAppDetail = (pkgId: string) => {
  router.push({
    name: 'AppDetail',
    params: { pkgId }
  })
}

const fetchPopularApps = async () => {
  loading.value = true
  try {
    const response = await fetch(`/api/recommendations/category/${encodeURIComponent(props.category)}/popular`)
    const data = await response.json()
    apps.value = data
  } catch (error) {
    console.error('获取热门应用失败:', error)
  } finally {
    loading.value = false
  }
}

const refresh = () => {
  fetchPopularApps()
}

onMounted(() => {
  fetchPopularApps()
})
</script>

<style scoped>
.popular-apps {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.app-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
}

.app-item:hover {
  background: #f9f9f9;
  transform: translateX(4px);
}

.app-rank {
  width: 24px;
  height: 24px;
  border-radius: 12px;
  background: #f4f4f5;
  color: #909399;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 500;
}

.rank-first {
  background: #f0f9eb;
  color: #67c23a;
}

.rank-second {
  background: #ecf5ff;
  color: #409eff;
}

.rank-third {
  background: #fdf6ec;
  color: #e6a23c;
}

.app-icon {
  width: 48px;
  height: 48px;
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
}

.app-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.app-brief {
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
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

@media screen and (max-width: 768px) {
  .app-item {
    padding: 8px;
    gap: 12px;
  }

  .app-icon {
    width: 40px;
    height: 40px;
  }

  .app-name {
    font-size: 13px;
  }
}
</style> 