<template>
  <div class="apps-container">
    <n-card title="应用统计" :bordered="false">
      <n-tabs type="line" animated>
        <n-tab-pane name="popular" tab="热门应用">
          <n-data-table
            :columns="columns"
            :data="popularApps"
            :loading="loading"
            :pagination="pagination"
          />
        </n-tab-pane>
        <n-tab-pane name="categories" tab="分类统计">
          <n-grid :cols="3" :x-gap="16" :y-gap="16" responsive="screen">
            <n-grid-item v-for="(category, index) in categories" :key="index">
              <n-card hoverable>
                <template #header>
                  <div class="category-header">
                    <n-icon :component="AppsOutline" size="24"  />
                    <span>{{ category.category }}</span>
                  </div>
                </template>
                <div class="category-value">{{ category.count }}</div>
                <div class="category-desc">个应用</div>
              </n-card>
            </n-grid-item>
          </n-grid>
        </n-tab-pane>
      </n-tabs>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NCard, NGrid, NGridItem, NIcon, NTabs, NTabPane, NDataTable } from 'naive-ui'
import { AppsOutline } from '@vicons/ionicons5'
import type { DataTableColumns } from 'naive-ui'

interface App {
  name: string
  pkgId: string
  downloads: number
  category: string[]
}

interface Category {
  category: string
  count: number
}

const loading = ref(false)
const popularApps = ref<App[]>([])
const categories = ref<Category[]>([])

const columns: DataTableColumns<App> = [
  {
    title: '应用名称',
    key: 'name'
  },
  {
    title: '下载量',
    key: 'downloads',
    sorter: (a, b) => a.downloads - b.downloads
  },
  {
    title: '分类',
    key: 'category',
    render(row) {
      return row.category.join(', ')
    }
  }
]

const pagination = {
  pageSize: 10
}

const fetchPopularApps = async () => {
  loading.value = true
  try {
    const response = await fetch('/api/apps/statistics/apps/popular')
    const data = await response.json()
    popularApps.value = data
  } catch (error) {
    console.error('获取热门应用失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const response = await fetch('/api/apps/statistics/apps/categories')
    const data = await response.json()
    categories.value = data
  } catch (error) {
    console.error('获取分类统计失败:', error)
  }
}

onMounted(() => {
  fetchPopularApps()
  fetchCategories()
})
</script>

<style scoped>
.apps-container {
  padding: 16px;
}

.category-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
}

.category-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 8px 0;
}

.category-desc {
  font-size: 14px;
  color: #909399;
}
</style> 