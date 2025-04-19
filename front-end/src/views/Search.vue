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
          支持搜索：应用名称、包名、关键词、源码地址、应用描述、简介
        </div>
      </div>

      <n-divider />

      <n-grid :cols="4" :x-gap="12" :y-gap="8" v-if="searchResults.length > 0">
        <n-grid-item v-for="app in searchResults" :key="app.pkgId">
          <n-card hoverable @click="goToAppDetail(app.pkgId)">
            <template #cover>
              <img :src="getAppIcon(app)" :alt="app.name" style="width: 100%; height: 200px; object-fit: cover">
            </template>
            <n-h3>{{ app.name }}</n-h3>
            <n-p>{{ app.brief }}</n-p>
            <n-space>
              <n-tag v-for="cat in app.category" :key="cat">{{ cat }}</n-tag>
            </n-space>
          </n-card>
        </n-grid-item>
      </n-grid>

      <n-empty v-else-if="!loading" description="没有找到相关应用" />
      <n-spin v-else size="large" />
    </n-space>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, defineComponent } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@vicons/ionicons5'
import type { App } from '../types'

const __name = 'Search'

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
</style> 