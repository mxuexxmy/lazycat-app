<template>
  <div class="recent-updates">
    <n-card title="最近更新">
      <n-spin :show="loading">
        <n-empty v-if="!loading && (!apps || apps.length === 0)" description="暂无数据" />
        <n-list v-else>
          <n-list-item v-for="app in apps" :key="app.pkgId">
            <n-thing>
              <template #header>
                <n-space align="center">
                  <n-avatar
                    :src="getAppIcon(app)"
                    :fallback-src="defaultIcon"
                    size="small"
                    object-fit="contain"
                  />
                  <span>{{ app.name }}</span>
                  <n-tag :type="getUpdateTagType(app.updateDate)">
                    {{ formatUpdateTime(app.updateDate) }}
                  </n-tag>
                </n-space>
              </template>
              <template #description>
                {{ app.brief || app.description }}
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
import { NCard, NList, NListItem, NThing, NTag, NEmpty, NSpin, NSpace, NAvatar } from 'naive-ui'
import { formatDistanceToNow } from 'date-fns'
import { zhCN } from 'date-fns/locale'

interface AppInfo {
  name: string;
  pkgId: string;
  description: string;
  brief: string;
  category: string[];
  iconPath: string;
  version: string;
  updateDate: string;
  supportPC: boolean;
  supportMobile: boolean;
}

const loading = ref(false)
const apps = ref<AppInfo[]>([])
const defaultIcon = 'https://dl.lazycatmicroserver.com/appstore/metarepo/default-icon.png'

const getAppIcon = (app: AppInfo) => {
  if (!app.iconPath) return defaultIcon
  const iconPath = app.iconPath.startsWith('/') ? app.iconPath.slice(1) : app.iconPath
  return `https://dl.lazycatmicroserver.com/appstore/metarepo/apps/${app.pkgId}/${iconPath}`
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
    const response = await fetch('https://appstore.api.lazycat.cloud/api/app/recent-updates')
    const result = await response.json()
    if (result.success && Array.isArray(result.data)) {
      apps.value = result.data
    }
  } catch (error) {
    console.error('Failed to fetch recent updates:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchRecentUpdates()
})
</script>

<style scoped>
.recent-updates {
  padding: 16px;
}
</style> 