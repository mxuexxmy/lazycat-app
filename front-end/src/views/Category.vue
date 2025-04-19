<template>
  <div class="category-view">
    <n-space vertical size="large">
      <div class="category-header">
        <h1>{{ categoryName }}</h1>
        <div class="search-section">
          <n-input-group>
            <n-input
              v-model:value="searchQuery"
              placeholder="搜索应用"
              class="search-input"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <n-icon :component="SearchOutline" />
              </template>
            </n-input>
            <n-button type="primary" @click="handleSearch">
              搜索
            </n-button>
          </n-input-group>
        </div>
      </div>

      <div class="app-grid">
        <n-empty v-if="filteredApps.length === 0" description="暂无应用" />
        <n-grid v-else :cols="4" :x-gap="16" :y-gap="16" responsive="screen">
          <n-grid-item v-for="app in filteredApps" :key="app.pkgId">
            <n-card class="app-card" hoverable @click="handleAppClick(app)">
              <div class="app-content">
                <div class="app-icon">
                  <n-image
                    :src="getAppIcon(app)"
                    :fallback-src="defaultIcon"
                    preview-disabled
                    object-fit="contain"
                  />
                </div>
                <div class="app-info">
                  <div class="app-title">{{ app.name }}</div>
                  <div class="app-desc">{{ app.brief || app.description }}</div>
                  <div class="app-tags">
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
                  </div>
                </div>
              </div>
            </n-card>
          </n-grid-item>
        </n-grid>
      </div>
    </n-space>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NSpace,
  NGrid,
  NGridItem,
  NCard,
  NImage,
  NTag,
  NInput,
  NInputGroup,
  NButton,
  NIcon,
  NEmpty
} from 'naive-ui'
import { SearchOutline } from '@vicons/ionicons5'
import type { App } from '@/types'

const __name = 'Category'

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
  keywords: string;
  packageName: string;
  source: string;
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
const searchQuery = ref('')

// Filter apps based on search query
const filteredApps = computed(() => {
  if (!searchQuery.value) return apps.value
  const query = searchQuery.value.toLowerCase()
  return apps.value.filter(app => 
    app.name?.toLowerCase().includes(query) || 
    app.brief?.toLowerCase().includes(query) ||
    app.keywords?.toLowerCase().includes(query) ||
    app.packageName?.toLowerCase().includes(query) ||
    app.source?.toLowerCase().includes(query)
  )
})

// Handle search
const handleSearch = () => {
  // 搜索逻辑已通过 computed 属性实现
  // 这里可以添加额外的搜索相关操作
}

// Get app icon URL
const getAppIcon = (app: AppInfo) => {
  if (!app.iconPath) return defaultIcon
  return `https://dl.lazycatmicroserver.com/appstore/metarepo/apps/${app.pkgId}/icon.png`
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
    const url = `/api/apps/category/${route.params.id}`
    const response = await fetch(url)
    const result = await response.json()
    if (Array.isArray(result)) {
      apps.value = result
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
  max-width: 1440px;
  margin: 0 auto;
  padding: 24px;
  min-height: 100vh;
  background: #f5f5f5;
  padding-top: 120px; /* 为固定头部留出空间 */
}

.category-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  text-align: center;
  background: #fff;
  padding: 24px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.category-header h1 {
  font-size: 28px;
  font-weight: 500;
  color: #333;
  margin: 0 0 24px;
}

.search-section {
  max-width: 600px;
  margin: 0 auto;
}

.search-input {
  font-size: 14px;
}

.app-grid {
  margin-top: 24px;
}

:deep(.n-grid) {
  --n-cols: 4 !important;
  gap: 24px !important;
}

.app-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.app-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.app-content {
  display: flex;
  flex-direction: column;
  padding: 16px;
  height: 100%;
}

.app-icon {
  width: 100%;
  height: 160px;
  padding: 24px;
  background: #f9f9f9;
  display: flex;
  justify-content: center;
  align-items: center;
  border-bottom: 1px solid #f0f0f0;
}

.app-icon :deep(img) {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.app-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  padding-top: 16px;
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
  flex: 1;
  line-height: 1.5;
}

.app-tags {
  margin-top: 12px;
}

:deep(.n-tag) {
  transition: all 0.3s ease;
}

@media screen and (max-width: 1440px) {
  :deep(.n-grid) {
    --n-cols: 3 !important;
  }
}

@media screen and (max-width: 1200px) {
  :deep(.n-grid) {
    --n-cols: 2 !important;
  }

  .category-view {
    padding: 16px;
  }
}

@media screen and (max-width: 768px) {
  .category-view {
    padding: 0;
    padding-top: 100px; /* 移动端头部高度较小 */
  }

  .category-header {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 100;
    padding: 16px;
    border-bottom: 1px solid #f0f0f0;
    background: #fff;
  }

  .category-header h1 {
    font-size: 18px;
    margin: 0 0 16px;
  }

  .search-section {
    padding: 0;
  }

  .app-grid {
    margin-top: 0;
  }

  :deep(.n-grid) {
    --n-cols: 1 !important;
    display: block !important;
  }

  :deep(.n-grid-item) {
    padding-top: 0 !important;
    padding-left: 0 !important;
    width: 100% !important;
    max-width: 100% !important;
  }

  :deep(.n-card) {
    border-radius: 0;
    margin-bottom: 8px;
    background: #fff;
    border-bottom: 1px solid #f0f0f0;
  }

  .app-content {
    gap: 12px;
  }

  .app-icon {
    width: 56px;
    height: 56px;
    padding: 4px;
    background: #f9f9f9;
    border-radius: 12px;
  }

  .app-info {
    padding: 4px 0;
  }

  .app-title {
    font-size: 15px;
    margin-bottom: 4px;
  }

  .app-desc {
    font-size: 13px;
    margin: 4px 0;
    color: #666;
    -webkit-line-clamp: 2;
  }

  .app-tags {
    margin-top: 8px;
  }

  :deep(.n-tag) {
    font-size: 12px !important;
  }
}

@media screen and (max-width: 480px) {
  .category-header h1 {
    font-size: 16px;
  }

  .app-content {
    gap: 8px;
  }

  .app-icon {
    width: 48px;
    height: 48px;
    border-radius: 8px;
  }

  .app-title {
    font-size: 14px;
  }

  .app-desc {
    font-size: 12px;
    -webkit-line-clamp: 2;
    margin: 2px 0;
  }

  .app-tags {
    margin-top: 6px;
  }
}
</style> 