<template>
  <div class="category-view">
    <n-space vertical size="large">
      <div class="category-header">
        <h1>{{ categoryName }}</h1>
      </div>

      <div class="app-grid">
        <n-grid :cols="4" :x-gap="16" :y-gap="16" responsive="screen">
          <n-grid-item v-for="app in apps" :key="app.pkgId">
            <n-card class="app-card" hoverable @click="handleAppClick(app)">
              <template #cover>
                <div class="app-icon">
                  <n-image
                    :src="getAppIcon(app)"
                    :fallback-src="defaultIcon"
                    preview-disabled
                    object-fit="contain"
                  />
                </div>
              </template>
              <template #header>
                <div class="app-title">{{ app.name }}</div>
              </template>
              <div class="app-desc">{{ app.brief || app.description }}</div>
              <template #footer>
                <n-space vertical size="small">
                  <n-space wrap :size="4">
                    <n-tag v-for="cat in app.category" :key="cat" size="small" round>
                      {{ cat }}
                    </n-tag>
                  </n-space>
                  <n-space :size="4">
                    <n-tag size="small" :bordered="false" type="success" v-if="app.supportPC">PC端</n-tag>
                    <n-tag size="small" :bordered="false" type="info" v-if="app.supportMobile">移动端</n-tag>
                  </n-space>
                </n-space>
              </template>
            </n-card>
          </n-grid-item>
        </n-grid>
      </div>
    </n-space>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NSpace,
  NGrid,
  NGridItem,
  NCard,
  NImage,
  NTag
} from 'naive-ui'

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

interface Category {
  id: number;
  name: string;
  description: string;
}

const route = useRoute()
const router = useRouter()
const apps = ref<AppInfo[]>([])
const categoryName = ref('')
const defaultIcon = '/path/to/default-icon.png'

// Get app icon URL
const getAppIcon = (app: AppInfo) => {
  if (!app.iconPath) return defaultIcon
  return `https://dl.lazycatmicroserver.com/appstore/metarepo/apps/${app.pkgId}/${app.iconPath}`
}

// Handle app card click
const handleAppClick = (app: AppInfo) => {
  router.push({
    name: 'AppDetail',
    params: { pkgId: app.pkgId }
  })
}

// Fetch category name
const fetchCategoryName = async () => {
  try {
    const response = await fetch('https://dl.lazycatmicroserver.com/appstore/metarepo/zh/categories.json')
    const categories = await response.json()
    const category = categories.find((cat: Category) => cat.id === Number(route.params.id))
    if (category) {
      categoryName.value = category.name
    }
  } catch (error) {
    console.error('Failed to fetch category name:', error)
  }
}

// Fetch apps in category
const fetchApps = async () => {
  try {
    const url = `https://appstore.api.lazycat.cloud/api/app/list?categories=${route.params.id}`
    const response = await fetch(url)
    const result = await response.json()
    if (result.success && Array.isArray(result.data)) {
      apps.value = result.data
    }
  } catch (error) {
    console.error('Failed to fetch apps:', error)
  }
}

onMounted(async () => {
  await Promise.all([fetchCategoryName(), fetchApps()])
})
</script>

<style scoped>
.category-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.category-header {
  text-align: center;
}

.category-header h1 {
  font-size: 28px;
  font-weight: 500;
  color: #333;
  margin: 0;
}

.app-grid {
  margin-top: 24px;
}

.app-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.app-icon {
  padding: 20px;
  background: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 160px;
}

.app-icon :deep(img) {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.app-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-desc {
  font-size: 14px;
  color: #666;
  margin: 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

@media screen and (max-width: 1200px) {
  .n-grid {
    --n-cols: 3 !important;
  }
}

@media screen and (max-width: 768px) {
  .n-grid {
    --n-cols: 2 !important;
  }

  .category-view {
    padding: 16px;
  }

  .category-header h1 {
    font-size: 24px;
  }

  .app-icon {
    height: 120px;
    padding: 16px;
  }
}

@media screen and (max-width: 480px) {
  .n-grid {
    --n-cols: 1 !important;
  }
}
</style> 