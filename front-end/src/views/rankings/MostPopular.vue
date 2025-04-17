<template>
  <div class="most-popular">
    <n-card title="最受欢迎应用">
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
                  <n-tag type="success">下载量: {{ app.downloads }}</n-tag>
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

const getAppIcon = (app: AppInfo) => {
  if (!app.iconPath) return defaultIcon
  return `https://dl.lazycatmicroserver.com/appstore/metarepo/apps/${app.pkgId}/${app.iconPath}`
}

const fetchMostPopularApps = async () => {
  loading.value = true
  try {
    const response = await fetch('https://appstore.api.lazycat.cloud/api/app/most-popular')
    const result = await response.json()
    if (result.success && Array.isArray(result.data)) {
      apps.value = result.data
    }
  } catch (error) {
    console.error('Failed to fetch most popular apps:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchMostPopularApps()
})
</script>

<style scoped>
.most-popular {
  padding: 16px;
}
</style> 