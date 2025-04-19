<template>
  <div class="rankings-container">
    <n-card title="应用代码仓库" class="repositories-card">
      <template #header-extra>
        <p class="description">这里展示了所有移植开源的应用程序，你可以查看和学习它们的源代码</p>
      </template>

      <n-spin :show="loading">
        <n-empty v-if="!loading && (!apps || apps.length === 0)" description="暂无开源应用" />
        
        <div v-else class="apps-grid">
          <n-card 
            v-for="app in apps" 
            :key="app.pkgId"
            class="app-card"
            :class="{ 'hoverable': true }"
            @click="handleAppClick(app)"
          >
            <div class="app-info">
              <div class="app-icon">
                <n-image
                  :src="getAppIcon(app)"
                  :fallback-src="defaultIcon"
                  preview-disabled
                  object-fit="contain"
                />
              </div>
              <div class="app-details">
                <n-ellipsis class="app-name">
                  {{ app.name }}
                </n-ellipsis>
                <div class="app-meta">
                  <n-tag size="small" :bordered="false">
                    <template #icon>
                      <n-icon><download-outline /></n-icon>
                    </template>
                    {{ formatDownloads(app.downloadCount) }}
                  </n-tag>
                  <n-tag size="small" type="success" :bordered="false">
                    <template #icon>
                      <n-icon><logo-github /></n-icon>
                    </template>
                    <a 
                      :href="app.source" 
                      target="_blank" 
                      rel="noopener noreferrer"
                      class="repo-link"
                      @click.stop
                    >
                      源码
                    </a>
                  </n-tag>
                </div>
              </div>
            </div>
            <n-ellipsis class="app-description" :line-clamp="2">
              {{ app.description }}
            </n-ellipsis>
          </n-card>
        </div>
      </n-spin>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NCard, NEmpty, NSpin, NImage, NEllipsis, NIcon, NTag } from 'naive-ui'
import {  DownloadOutline, LogoGithub } from '@vicons/ionicons5'

// Format downloads to display in 万 units
const formatDownloads = (count: number) => {
  if (count >= 10000) {
    return `${(count / 10000).toFixed(1)}万`
  }
  return count.toString()
}

// Get app icon URL
const getAppIcon = (app: AppData) => {
  if (!app.iconPath) return defaultIcon
  return `https://dl.lazycatmicroserver.com/appstore/metarepo/apps/${app.pkgId}/icon.png`
}

interface AppData {
  pkgId: string
  name: string
  description: string
  iconPath: string
  downloadCount: number
  source: string
}

const defaultIcon = '/app-icon.png'
const router = useRouter()
const loading = ref(true)
const apps = ref<AppData[]>([])

const fetchApps = async () => {
  try {
    const response = await fetch('/api/apps/repositories')
    const data = await response.json()
    apps.value = data
  } catch (error) {
    console.error('Failed to fetch apps:', error)
  } finally {
    loading.value = false
  }
}

const handleAppClick = (app: AppData) => {
  router.push({
    name: 'AppDetail',
    params: { pkgId: app.pkgId }
  })
}

onMounted(() => {
  fetchApps()
})
</script>

<style scoped>
.rankings-container {
  width: 100%;
}

.repositories-card {
  width: 100%;
}

.description {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.apps-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.app-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.app-card.hoverable:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.app-info {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}

.app-details {
  flex: 1;
  min-width: 0;
}

.app-name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 4px;
}

.app-meta {
  display: flex;
  gap: 8px;
}

.app-description {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.app-icon {
  width: 48px;
  height: 48px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f9f9f9;
  border-radius: 8px;
  overflow: hidden;
}

.app-icon :deep(img) {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .repositories-card {
    padding: 0;
  }

  .apps-grid {
    grid-template-columns: 1fr;
    gap: 12px;
    padding: 12px;
  }

  .app-card {
    padding: 12px;
  }

  .app-name {
    font-size: 15px;
  }

  .app-description {
    font-size: 13px;
  }

  :deep(.n-tag) {
    font-size: 12px;
  }
}

@media screen and (max-width: 480px) {
  .apps-grid {
    padding: 8px;
    gap: 8px;
  }

  .app-card {
    padding: 10px;
  }

  .app-name {
    font-size: 14px;
  }

  .app-description {
    font-size: 12px;
  }
}

.repo-link {
  color: inherit;
  text-decoration: none;
}

.repo-link:hover {
  text-decoration: underline;
}
</style> 