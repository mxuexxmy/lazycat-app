<template>
  <div class="explore-view">
    <!-- 顶部搜索栏 -->
    <div class="search-section">
      <n-input-group>
        <n-input
          v-model:value="searchQuery"
          placeholder="搜索懒猫应用"
          class="search-input"
          size="large"
        />
        <n-button type="primary" size="large" @click="handleSearch">
          搜索
        </n-button>
      </n-input-group>
    </div>

    <!-- 主要内容区 -->
    <div class="main-content">
      <!-- 桌面端布局 -->
      <n-grid :cols="5" :x-gap="16" class="desktop-grid">
        <!-- 左侧：功能列表 -->
        <n-grid-item span="1">
          <n-space vertical>
            <div v-for="category in menuOptions" :key="category.key">
              <div class="category-header">
                <n-icon size="20" :color="category.iconColor">
                  <component :is="category.icon" />
                </n-icon>
                <span class="category-name">{{ category.label }}</span>
              </div>
              <n-card 
                v-for="item in category.children" 
                :key="item.key"
                hoverable 
                class="function-list-card"
                :class="{ active: selectedKey === item.key }"
                @click="handleMenuSelect(item.key)"
              >
                <n-space align="center">
                  <div class="function-icon" :class="{ active: selectedKey === item.key }">
                    <n-icon size="20">
                      <component :is="item.icon" />
                    </n-icon>
                  </div>
                  <span class="function-name">{{ item.label }}</span>
                </n-space>
              </n-card>
            </div>
          </n-space>
        </n-grid-item>

        <!-- 右侧：内容区 -->
        <n-grid-item span="4">
          <n-card class="function-content">
            <template #header v-if="selectedCategory">
              <n-space align="center">
                <div class="function-icon large">
                  <n-icon size="24">
                    <component :is="selectedCategory.icon" />
                  </n-icon>
                </div>
                <span class="content-title">{{ selectedCategory.label }}</span>
              </n-space>
            </template>
            <!-- 应用列表 -->
            <n-grid :cols="3" :x-gap="16" :y-gap="16" responsive="screen" v-if="categoryApps.length">
              <n-grid-item v-for="app in categoryApps" :key="app.packageName">
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
            <n-empty v-else description="暂无应用" />
          </n-card>
        </n-grid-item>
      </n-grid>

      <!-- 移动端布局 -->
      <div class="mobile-layout">
        <div class="mobile-menu">
          <n-scrollbar x-scrollable>
            <div class="mobile-tabs">
              <div
                v-for="item in getAllMenuItems()"
                :key="item.key"
                class="mobile-tab"
                :class="{ active: selectedKey === item.key }"
                @click="handleMenuSelect(item.key)"
              >
                <n-icon size="20">
                  <component :is="item.icon" />
                </n-icon>
                <span>{{ item.label }}</span>
              </div>
            </div>
          </n-scrollbar>
        </div>

        <div class="mobile-content">
          <n-card v-if="selectedCategory">
            <template #header>
              <n-space align="center">
                <div class="function-icon">
                  <n-icon size="20">
                    <component :is="selectedCategory.icon" />
                  </n-icon>
                </div>
                <span class="content-title">{{ selectedCategory.label }}</span>
              </n-space>
            </template>
            <n-grid :cols="1" :y-gap="16">
              <n-grid-item v-for="app in categoryApps" :key="app.packageName">
                <n-card class="app-card" hoverable @click="handleAppClick(app)">
                  <n-space align="center">
                    <div class="app-icon small">
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
                      <n-space wrap :size="4" style="margin-top: 8px">
                        <n-tag size="small" :bordered="false" type="success" v-if="app.supportPC">PC端</n-tag>
                        <n-tag size="small" :bordered="false" type="info" v-if="app.supportMobile">移动端</n-tag>
                      </n-space>
                    </div>
                  </n-space>
                </n-card>
              </n-grid-item>
            </n-grid>
          </n-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  NInput,
  NInputGroup,
  NButton,
  NGrid,
  NGridItem,
  NCard,
  NSpace,
  NIcon,
  NTag,
  NImage,
  NEmpty,
  NScrollbar
} from 'naive-ui'
import type { Component } from 'vue'
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

interface AppInfo {
  name: string;
  packageName: string;
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
const menuOptions = [
  {
    label: '排行榜',
    key: 'rankings',
    icon: TrophyOutline,
    iconColor: '#ffb800',
    children: [
      {
        label: '最受欢迎',
        key: 'most-popular',
        icon: RocketOutline,
        iconColor: '#ffb800'
      },
      {
        label: '月度新品推荐',
        key: 'monthly-new',
        icon: TimeOutline,
        iconColor: '#13c2c2'
      },
      {
        label: '最新上架',
        key: 'latest',
        icon: RocketOutline,
        iconColor: '#722ed1'
      },
      {
        label: '最近更新',
        key: 'recently-updated',
        icon: RefreshOutline,
        iconColor: '#2f54eb'
      }
    ]
  },
  {
    label: '开始探索',
    key: 'explore',
    icon: SearchOutline,
    iconColor: '#13c2c2',
    children: [
      {
        label: '游戏',
        key: 'games',
        icon: GameControllerOutline,
        iconColor: '#ff4d4f'
      },
      {
        label: '开发工具',
        key: 'dev-tools',
        icon: CodeSlashOutline,
        iconColor: '#1890ff'
      },
      {
        label: '阅读学习',
        key: 'education',
        icon: BookOutline,
        iconColor: '#722ed1'
      }
    ]
  }
]

// 获取当前选中的分类信息
const selectedCategory = computed(() => {
  if (!selectedKey.value) return null
  for (const category of menuOptions) {
    if (category.children) {
      const found = category.children.find(item => item.key === selectedKey.value)
      if (found) return found
    }
  }
  return null
})

// 获取所有菜单项（用于移动端展示）
const getAllMenuItems = () => {
  const items = []
  for (const category of menuOptions) {
    if (category.children) {
      items.push(...category.children)
    }
  }
  return items
}

// 处理搜索
const handleSearch = () => {
  if (!searchQuery.value.trim()) return
  router.push({ name: 'Search', query: { q: searchQuery.value } })
}

// 处理菜单选择
const handleMenuSelect = async (key: string) => {
  selectedKey.value = key
  
  // 根据不同的key跳转到对应的路由
  const routeMap: Record<string, { name: string }> = {
    'games': { name: 'Games' },
    'dev-tools': { name: 'DevTools' },
    'education': { name: 'Education' },
    'most-popular': { name: 'MostPopular' },
    'monthly-new': { name: 'MonthlyNew' },
    'latest': { name: 'LatestRelease' },
    'recently-updated': { name: 'RecentUpdates' }
  }

  if (routeMap[key]) {
    router.push(routeMap[key])
  }
}

// 获取应用图标URL
const getAppIcon = (app: AppInfo) => {
  if (!app.iconPath) return defaultIcon
  return `https://dl.lazycatmicroserver.com/appstore/metarepo/apps/${app.packageName}/${app.iconPath}`
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

.search-section {
  margin-bottom: 24px;
}

.search-input {
  max-width: 600px;
  margin: 0 auto;
}

:deep(.n-input-group) {
  width: 600px;
  max-width: 100%;
  margin: 0 auto;
}

.main-content {
  flex: 1;
  min-height: 0;
}

.desktop-grid {
  height: 100%;
}

.mobile-layout {
  display: none;
}

.category-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.category-name {
  font-size: 16px;
}

.function-list-card {
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 12px;
}

.function-list-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.function-list-card.active {
  border-color: #18a058;
  background-color: #f0faf5;
}

.function-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #f5f5f5;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.function-icon.active {
  background: #18a058;
  color: white;
}

.function-icon.large {
  width: 48px;
  height: 48px;
}

.function-icon.small {
  width: 32px;
  height: 32px;
}

.function-name {
  font-size: 14px;
  font-weight: 500;
}

.function-content {
  height: 100%;
  overflow: auto;
}

.content-title {
  font-size: 18px;
  font-weight: 500;
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

.app-info {
  flex: 1;
  min-width: 0;
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

  .desktop-grid {
    display: none !important;
  }

  .mobile-layout {
    display: block;
  }

  .mobile-menu {
    margin-bottom: 16px;
  }

  .mobile-tabs {
    display: flex;
    padding: 8px;
    gap: 12px;
    min-width: min-content;
  }

  .mobile-tab {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    padding: 8px 16px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;
    white-space: nowrap;
  }

  .mobile-tab.active {
    background: #f0faf5;
    color: #18a058;
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

  .search-section {
    margin: 8px auto 16px;
  }

  :deep(.n-input-group) {
    max-width: calc(100% - 16px);
  }
}
</style> 