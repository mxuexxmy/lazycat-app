<template>
  <div class="search">
    <n-space vertical>
      <n-h1>搜索应用</n-h1>
      
      <div class="search-section">
        <n-input
          v-model:value="searchQuery"
          placeholder="输入应用名称、包名、关键词、源码地址、描述等..."
          @keyup.enter="handleSearch"
          clearable
        >
          <template #prefix>
            <n-icon><search /></n-icon>
          </template>
        </n-input>
        <div class="search-tips">
          支持搜索：应用名称、包名、关键词、源码地址、应用描述、简介、分类
        </div>
      </div>

      <n-divider />

      <div class="app-grid">
        <n-grid 
          :cols="4" 
          :x-gap="16" 
          :y-gap="16" 
          :item-responsive="true"
          responsive="screen"
          :cols-xs="1"
          :cols-s="1"
          :cols-m="2"
          :cols-l="4"
          v-if="searchResults.length > 0"
        >
          <n-grid-item v-for="app in searchResults" :key="app.pkgId">
            <n-card hoverable @click="goToAppDetail(app.pkgId)" class="app-card">
              <div class="app-content">
                <div class="app-icon">
                  <n-image
                    :src="getAppIcon(app)"
                    :fallback-src="defaultIcon"
                    preview-disabled
                    object-fit="cover"
                    class="icon-image"
                    :intersection-observer-options="{
                      root: null,
                      rootMargin: '0px',
                      threshold: 0.1
                    }"
                    lazy
                  />
                </div>
                <div class="app-info">
                  <div class="app-title">{{ app.name }}</div>
                  <div class="app-desc">{{ app.brief }}</div>
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

        <n-empty v-else-if="!loading" description="没有找到相关应用" />
        <n-spin v-else size="large" />
      </div>
    </n-space>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, defineComponent } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@vicons/ionicons5'
import type { App } from '../types'
import {
  NSpace,
  NGrid,
  NGridItem,
  NCard,
  NImage,
  NTag,
  NEmpty,
  NSpin,
  NDivider
} from 'naive-ui'

const __name = 'Search'
const defaultIcon = 'https://dl.lazycatmicroserver.com/appstore/metarepo/default-icon.png'

const route = useRoute()
const router = useRouter()
const searchQuery = ref(route.query.q as string || '')
const loading = ref(false)
const searchResults = ref<App[]>([])

const getAppIcon = (app: App) => {
  if (!app.iconPath) return 'https://dl.lazycatmicroserver.com/appstore/metarepo/default-icon.png'
  return `https://dl.lazycatmicroserver.com/appstore/metarepo/apps/${app.pkgId}/icon.png`
}

const handleSearch = async () => {
  if (!searchQuery.value.trim()) {
    searchResults.value = []
    return
  }
  
  loading.value = true
  try {
    const response = await fetch(`/api/apps/search/all?keyword=${encodeURIComponent(searchQuery.value)}`)
    const data = await response.json()
    searchResults.value = data
  } catch (error) {
    console.error('搜索失败:', error)
    searchResults.value = []
  } finally {
    loading.value = false
  }
  
  // 更新URL，但不触发新的搜索
  router.push({ 
    name: 'Search', 
    query: { q: searchQuery.value }
  })
}

const goToAppDetail = (pkgId: string) => {
  router.push({
    name: 'AppDetail',
    params: { pkgId }
  })
}

// Watch for route query changes
watch(() => route.query.q, (newQuery) => {
  if (newQuery) {
    searchQuery.value = newQuery as string
    handleSearch()
  }
}, { immediate: true })
</script>

<style scoped>
.search {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.search-section {
  max-width: 800px;
  margin: 0 auto;
}

.search-tips {
  margin-top: 8px;
  font-size: 13px;
  color: #666;
  text-align: left;
}

.app-grid {
  margin-top: 24px;
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
  padding: 16px;
  height: 100%;
}

.app-icon {
  flex-shrink: 0;
  background: #f9f9f9;
  border-radius: 16px;
  overflow: hidden;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.icon-image {
  width: 100%;
  height: 100%;
  transition: transform 0.3s ease;
}

.icon-image :deep(img) {
  object-fit: contain !important;
  padding: 8px;
  width: 100%;
  height: 100%;
  transition: transform 0.3s ease;
}

.app-card:hover .icon-image :deep(img) {
  transform: scale(1.05);
}

.app-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  padding-left: 16px;
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

@media screen and (min-width: 769px) {
  .app-content {
    flex-direction: column;
  }

  .app-icon {
    width: 100%;
    height: 160px;
    padding: 16px;
  }

  .app-info {
    padding: 16px 0 0;
  }
}

@media screen and (max-width: 768px) {
  .search {
    padding: 16px;
  }

  .search-section {
    padding: 0;
  }

  :deep(.n-grid) {
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
    padding: 8px;
    border-radius: 12px;
  }

  .icon-image :deep(img) {
    padding: 4px;
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
  .search {
    padding: 12px;
  }

  .app-content {
    gap: 8px;
  }

  .app-icon {
    width: 48px;
    height: 48px;
    padding: 6px;
    border-radius: 10px;
  }

  .icon-image :deep(img) {
    padding: 3px;
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

  :deep(.n-h1) {
    font-size: 24px !important;
    margin-top: 8px !important;
    margin-bottom: 16px !important;
  }
}
</style> 