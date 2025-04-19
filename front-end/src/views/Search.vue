<template>
  <div class="search">
    <n-space vertical>
      <n-h1>搜索应用</n-h1>
      
      <n-input
        v-model:value="searchQuery"
        placeholder="输入应用名称或关键字..."
        @keyup.enter="handleSearch"
        clearable
      >
        <template #prefix>
          <n-icon><search /></n-icon>
        </template>
      </n-input>

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
import { ref, computed, watch, defineComponent } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '../stores/app'
import { Search } from '@vicons/ionicons5'
import type { App } from '../types'

const __name = 'Search'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const searchQuery = ref(route.query.q as string || '')
const loading = ref(false)

const searchResults = computed(() => {
  if (!searchQuery.value) return []
  return appStore.apps.filter(app => 
    app.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    app.brief.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    app.keywords.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

const getAppIcon = (app: App) => {
  return `https://dl.lazycatmicroserver.com/appstore/metarepo/apps/${app.pkgId}/${app.iconPath}`
}

const handleSearch = () => {
  router.push({ name: 'Search', query: { q: searchQuery.value } })
}

const goToAppDetail = (pkgId: string) => {
  router.push({ name: 'AppDetail', params: { pkgId } })
}

// Watch for route query changes
watch(() => route.query.q, (newQuery) => {
  if (newQuery) {
    searchQuery.value = newQuery as string
  }
})
</script>

<style scoped>
.search {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
</style> 