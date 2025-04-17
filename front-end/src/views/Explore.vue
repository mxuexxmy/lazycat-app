<template>
  <div class="explore-view">
    <!-- 顶部搜索栏 -->
    <div class="search-bar">
      <n-input
        v-model:value="searchQuery"
        placeholder="搜索懒猫应用"
        clearable
        class="search-input"
      >
        <template #suffix>
          <n-button type="primary" ghost @click="handleSearch">
            搜索
          </n-button>
        </template>
      </n-input>
    </div>

    <div class="main-content">
      <!-- 左侧功能列表 -->
      <div class="function-list">
        <n-menu
          v-model:value="selectedKey"
          :options="menuOptions"
          :indent="18"
          class="custom-menu"
        />
      </div>

      <!-- 右侧内容区 -->
      <div class="content-area">
        <n-card v-if="selectedCategory">
          <template #header>
            <div class="category-header">
              <h2>{{ selectedCategory.title }}</h2>
              <p v-if="selectedCategory.description">{{ selectedCategory.description }}</p>
            </div>
          </template>
          
          <!-- 应用列表 -->
          <n-grid :cols="3" :x-gap="16" :y-gap="16" responsive="screen">
            <n-grid-item v-for="app in categoryApps" :key="app.pkgId">
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
        </n-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  NInput,
  NButton,
  NMenu,
  NCard,
  NGrid,
  NGridItem,
  NSpace,
  NTag,
  NImage
} from 'naive-ui'
import type { MenuOption } from 'naive-ui'
import { 
  GameControllerOutline,
  CodeSlashOutline,
  BookOutline,
  SearchOutline,
  TrophyOutline,
  RocketOutline,
  TimeOutline,
  RefreshOutline
} from '@vicons/ionicons5'
import { h } from 'vue'
import type { Component } from 'vue'

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

const router = useRouter()
const searchQuery = ref('')
const selectedKey = ref<string | null>(null)
const categoryApps = ref<AppInfo[]>([])
const defaultIcon = '/path/to/default-icon.png'

// 渲染图标
const renderIcon = (icon: Component) => {
  return () => h('div', { class: 'menu-icon' }, h(icon))
}

// 菜单配置
const menuOptions: MenuOption[] = [
  {
    label: '排行榜',
    key: 'rankings',
    icon: renderIcon(TrophyOutline),
    children: [
      {
        label: '最受欢迎',
        key: 'most-popular',
        icon: renderIcon(RocketOutline)
      },
      {
        label: '月度新品推荐',
        key: 'monthly-new',
        icon: renderIcon(TimeOutline)
      },
      {
        label: '最新上架',
        key: 'latest',
        icon: renderIcon(RocketOutline)
      },
      {
        label: '最近更新',
        key: 'recently-updated',
        icon: renderIcon(RefreshOutline)
      }
    ]
  },
  {
    label: '开始探索',
    key: 'explore',
    icon: renderIcon(SearchOutline)
  },
  {
    label: '游戏',
    key: 'games',
    icon: renderIcon(GameControllerOutline)
  },
  {
    label: '开发工具',
    key: 'dev-tools',
    icon: renderIcon(CodeSlashOutline)
  },
  {
    label: '阅读学习',
    key: 'education',
    icon: renderIcon(BookOutline)
  }
]

// 获取当前选中的分类信息
const selectedCategory = computed(() => {
  if (!selectedKey.value) return null
  const findCategory = (options: MenuOption[]): MenuOption | null => {
    for (const option of options) {
      if (option.key === selectedKey.value) return option
      if (option.children) {
        const found = findCategory(option.children)
        if (found) return found
      }
    }
    return null
  }
  return findCategory(menuOptions)
})

// 处理搜索
const handleSearch = () => {
  if (!searchQuery.value.trim()) return
  // TODO: 实现搜索功能
  console.log('Searching for:', searchQuery.value)
}

// 处理菜单选择
const handleMenuSelect = async (key: string) => {
  selectedKey.value = key
  await fetchCategoryApps(key)
}

// 获取应用图标URL
const getAppIcon = (app: AppInfo) => {
  if (!app.iconPath) return defaultIcon
  return `https://dl.lazycatmicroserver.com/appstore/metarepo/apps/${app.pkgId}/${app.iconPath}`
}

// 获取分类应用列表
const fetchCategoryApps = async (categoryKey: string) => {
  try {
    const url = `https://appstore.api.lazycat.cloud/api/app/list?category=${categoryKey}`
    const response = await fetch(url)
    const result = await response.json()
    if (result.success && Array.isArray(result.data)) {
      categoryApps.value = result.data
    }
  } catch (error) {
    console.error('Failed to fetch category apps:', error)
  }
}

// 处理应用点击
const handleAppClick = (app: AppInfo) => {
  router.push({
    name: 'AppDetail',
    params: { pkgId: app.pkgId }
  })
}
</script>

<style scoped>
.explore-view {
  height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 24px;
}

.search-bar {
  margin-bottom: 24px;
}

.search-input {
  max-width: 600px;
  margin: 0 auto;
}

.main-content {
  display: flex;
  gap: 24px;
  flex: 1;
  min-height: 0;
}

.function-list {
  width: 200px;
  flex-shrink: 0;
}

.content-area {
  flex: 1;
  overflow: auto;
}

.custom-menu {
  background: transparent;
}

.custom-menu :deep(.n-menu-item) {
  height: 50px;
  margin: 4px 0;
}

.custom-menu :deep(.n-menu-item-content) {
  border-radius: 8px;
}

.custom-menu :deep(.n-menu-item-content--selected) {
  background-color: rgba(31, 143, 255, 0.1);
  border: 1px solid #1f8fff;
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

.menu-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.category-header {
  margin-bottom: 24px;
}

.category-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 500;
}

.category-header p {
  margin: 8px 0 0;
  color: #666;
}

@media screen and (max-width: 1200px) {
  .n-grid {
    --n-cols: 2 !important;
  }
}

@media screen and (max-width: 768px) {
  .explore-view {
    padding: 16px;
  }
  
  .main-content {
    gap: 16px;
  }

  .function-list {
    width: 160px;
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
  
  .main-content {
    flex-direction: column;
  }

  .function-list {
    width: 100%;
  }
}
</style> 