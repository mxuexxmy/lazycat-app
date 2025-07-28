<template>
  <div class="explore-recent-updates">
    <n-space vertical size="large">
      <!-- Categories Filter -->
      <n-card title="分类筛选">
        <n-space>
          <n-select
            v-model:value="selectedCategory"
            :options="categoryOptions"
            placeholder="选择应用分类"
            clearable
          />
        </n-space>
      </n-card>

      <!-- Recent Updates List -->
      <n-card title="最近更新">
        <n-spin :show="loading">
          <n-empty v-if="!loading && (!recentUpdates || recentUpdates.length === 0)" description="暂无更新" />
          <n-list v-else>
            <n-list-item v-for="app in recentUpdates" :key="app.pkgId">
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
    </n-space>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useAppStore } from '@/stores/app'
import {
  NSpace,
  NCard,
  NSelect,
  NList,
  NListItem,
  NThing,
  NTag,
  NEmpty,
  NSpin,
  NAvatar
} from 'naive-ui'
import { formatDistanceToNow } from 'date-fns'
import { zhCN } from 'date-fns/locale'
import { useRoute } from 'vue-router'

interface Category {
  id: number;
  name: string;
  icon: string;
}

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
const selectedCategory = ref<number | null>(null)
const recentUpdates = ref<AppInfo[]>([])
const categories = ref<Category[]>([])
const defaultIcon = '/path/to/default-icon.png'
const categoryName = ref('')
const route = useRoute()

// Convert categories to select options
const categoryOptions = computed(() => {
  return categories.value.map(category => ({
    label: category.name,
    value: category.id
  }))
})

// Get app icon URL
const getAppIcon = (app: AppInfo) => {
  if (!app.iconPath) return defaultIcon
  return `https://dl.lazycatmicroserver.com/appstore/metarepo/apps/${app.pkgId}/${app.iconPath}`
}

// Format update time
const formatUpdateTime = (time: string) => {
  return formatDistanceToNow(new Date(time), {
    addSuffix: true,
    locale: zhCN
  })
}

// Get tag type based on update time
const getUpdateTagType = (time: string) => {
  const days = (Date.now() - new Date(time).getTime()) / (1000 * 60 * 60 * 24)
  if (days <= 1) return 'success'
  if (days <= 7) return 'info'
  return 'default'
}

// Fetch categories
const fetchCategories = async () => {
  try {
    const response = await fetch('https://dl.lazycatmicroserver.com/appstore/metarepo/zh/categories.json')
    categories.value = await response.json()
  } catch (error) {
    console.error('Failed to fetch categories:', error)
  }
}

// Fetch recent updates
const fetchRecentUpdates = async () => {
  loading.value = true
  try {
    const url = `https://appstore.api.lazycat.cloud/api/app/list${selectedCategory.value ? `?categories=${selectedCategory.value}` : ''}`
    const response = await fetch(url)
    const result = await response.json()
    if (result.success && Array.isArray(result.data)) {
      recentUpdates.value = result.data
    }
  } catch (error) {
    console.error('Failed to fetch recent updates:', error)
  } finally {
    loading.value = false
  }
}

// Watch category changes
watch(selectedCategory, () => {
  fetchRecentUpdates()
})

const fetchCategoryName = async () => {
  try {
    const response = await fetch('/api/categories')
    const result = await response.json()
    if (result.success) {
      const category = result.data.find((cat: Category) => cat.id === Number(route.params.id))
      if (category) {
        categoryName.value = category.name
      }
    }
  } catch (error) {
    console.error('Failed to fetch category name:', error)
  }
}

onMounted(async () => {
  await fetchCategories()
  await fetchRecentUpdates()
  await fetchCategoryName()
})
</script>

<style scoped>
.explore-recent-updates {
  max-width: 1000px;
  margin: 0 auto;
}

.n-avatar {
  background-color: #fff;
}
</style> 