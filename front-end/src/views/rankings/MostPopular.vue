<template>
  <div class="most-popular">
    <n-card title="最受欢迎应用">
      <n-spin :show="loading">
        <n-empty v-if="!loading && (!apps || apps.length === 0)" description="暂无数据" />
        <n-list v-else>
          <n-list-item v-for="app in apps" :key="app.pkgId" class="app-item" @click="handleAppClick(app)">
            <n-thing>
              <template #header>
                <n-space align="center">
                  <n-avatar
                    :src="getAppIcon(app)"
                    :fallback-src="defaultIcon"
                    size="small"
                    object-fit="contain"
                  />
                  <span class="app-name">{{ app.name }}</span>
                  <n-tag type="success">下载量: {{ app.downloads }}</n-tag>
                </n-space>
              </template>
              <template #description>
                <div class="app-desc">{{ app.brief || app.description }}</div>
              </template>
              <template #footer>
                <n-space>
                  <n-tag>版本 {{ app.version }}</n-tag>
                  <n-tag v-for="cat in app.category" :key="cat">{{ cat }}</n-tag>
                  <n-tag v-if="app.supportPC && app.supportMobile">全平台</n-tag>
                  <n-tag v-else-if="app.supportPC">PC端</n-tag>
                  <n-tag v-else-if="app.supportMobile">移动端</n-tag>
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
import { NCard, NList, NListItem, NThing, NTag, NEmpty, NSpin, NSpace, NAvatar } from 'naive-ui'

interface AppInfo {
  name: string;
  pkgId: string;
  description: string;
  brief: string;
  category: string[];
  iconPath: string;
  version: string;
  downloads: number;
  supportPC: boolean;
  supportMobile: boolean;
}

const loading = ref(false)
const apps = ref<AppInfo[]>([])
const defaultIcon = '/path/to/default-icon.png'
const router = useRouter()

const getAppIcon = (app: AppInfo) => {
  if (!app.iconPath) return defaultIcon
  return `https://dl.lazycatmicroserver.com/appstore/metarepo/apps/${app.pkgId}/icon.png`
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
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.app-item {
  cursor: pointer;
  transition: all 0.3s ease;
  border-bottom: 1px solid #f0f0f0;
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
}

:deep(.n-card) {
  border-radius: 8px;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .most-popular {
    border-radius: 0;
    box-shadow: none;
  }

  :deep(.n-card) {
    border-radius: 0;
  }

  .app-name {
    font-size: 15px;
  }

  .app-desc {
    font-size: 13px;
    margin: 4px 0;
  }

  :deep(.n-tag) {
    font-size: 12px;
  }
}

@media screen and (max-width: 480px) {
  .app-name {
    font-size: 14px;
  }

  .app-desc {
    font-size: 12px;
  }
}
</style> 