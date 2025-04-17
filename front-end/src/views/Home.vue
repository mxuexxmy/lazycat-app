<template>
  <div class="home">
    <!-- Search Section -->
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

    <!-- Main Content -->
    <div class="main-content">
      <transition name="fade" mode="out-in">
        <!-- Initial Grid Layout -->
        <div v-if="!selectedFunction" class="grid-layout">
          <div v-for="category in functions" :key="category.id" class="category-section">
            <h2 class="category-title">
              <n-icon size="24" :color="category.iconColor">
                <component :is="category.icon" />
              </n-icon>
              <span>{{ category.title }}</span>
            </h2>
            <n-grid :cols="4" :x-gap="16" :y-gap="16">
              <n-grid-item v-for="func in category.children" :key="func.id">
                <n-card hoverable class="function-card" @click="handleFunctionClick(func)">
                  <n-space align="center">
                    <div class="function-icon">
                      <n-icon size="24" :color="func.iconColor">
                        <component :is="func.icon" />
                      </n-icon>
                    </div>
                    <div class="function-info">
                      <div class="function-title">{{ func.title }}</div>
                      <div class="function-desc">{{ func.description }}</div>
                    </div>
                  </n-space>
                </n-card>
              </n-grid-item>
            </n-grid>
          </div>
        </div>

        <!-- Selected Function Layout -->
        <div v-else class="function-detail-layout">
          <n-grid :cols="5" :x-gap="16" class="desktop-grid">
            <!-- Left Side: Function List -->
            <n-grid-item span="1">
              <n-space vertical>
                <div v-for="category in functions" :key="category.id">
                  <div class="category-header">
                    <n-icon size="20" :color="category.iconColor">
                      <component :is="category.icon" />
                    </n-icon>
                    <span class="category-name">{{ category.title }}</span>
                  </div>
                  <n-card 
                    v-for="func in category.children" 
                    :key="func.id"
                    hoverable 
                    class="function-list-card"
                    :class="{ active: selectedFunction.id === func.id }"
                    @click="handleFunctionClick(func)"
                  >
                    <n-space align="center">
                      <div class="function-icon" :class="{ active: selectedFunction.id === func.id }">
                        <n-icon size="20">
                          <component :is="func.icon" />
                        </n-icon>
                      </div>
                      <span class="function-name">{{ func.title }}</span>
                    </n-space>
                  </n-card>
                </div>
              </n-space>
            </n-grid-item>

            <!-- Right Side: Function Content -->
            <n-grid-item span="4">
              <n-card class="function-content">
                <template #header>
                  <n-space align="center">
                    <div class="function-icon large">
                      <n-icon size="24">
                        <component :is="selectedFunction.icon" />
                      </n-icon>
                    </div>
                    <span class="content-title">{{ selectedFunction.title }}</span>
                  </n-space>
                </template>
                <div class="content-area">
                  <component :is="selectedFunction.component" />
                </div>
              </n-card>
            </n-grid-item>
          </n-grid>

          <!-- Mobile Layout -->
          <div class="mobile-menu">
            <n-scrollbar x-scrollable>
              <div class="mobile-tabs">
                <div
                  v-for="func in functions[0].children"
                  :key="func.id"
                  class="mobile-tab"
                  :class="{ active: selectedFunction?.id === func.id }"
                  @click="handleFunctionClick(func)"
                >
                  <n-icon size="20" :color="func.iconColor">
                    <component :is="func.icon" />
                  </n-icon>
                  <span>{{ func.title }}</span>
                </div>
              </div>
            </n-scrollbar>
          </div>

          <div class="mobile-content">
            <n-card v-if="selectedFunction">
              <div class="content-area">
                <component :is="selectedFunction.component" />
              </div>
            </n-card>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { Component } from 'vue'
import { useRouter, useRoute, RouteLocationRaw } from 'vue-router'
import {
  SearchOutline,
  TrophyOutline,
  TimeOutline,
  RocketOutline,
  RefreshOutline,
  AnalyticsOutline,
  StatsChartOutline,
  CompassOutline,
  GameControllerOutline,
  CodeSlashOutline,
  BookOutline,
  BrushOutline,
  FilmOutline,
  HeartOutline,
  BulbOutline,
  StarOutline
} from '@vicons/ionicons5'
import { useAppStore } from '@/stores/app'

interface Function {
  id: string;
  title: string;
  description?: string;
  icon: Component;
  iconColor: string;
  children?: Function[];
  component?: Component;
  route?: RouteLocationRaw;
}

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const searchQuery = ref('')
const selectedFunction = ref<Function | null>(null)

const functions: Function[] = [
  {
    id: 'rankings',
    title: '排行榜',
    icon: TrophyOutline,
    iconColor: '#ffb800',
    children: [
      {
        id: 'most-popular',
        title: '最受欢迎',
        description: '最受用户欢迎的应用',
        icon: TrophyOutline,
        iconColor: '#ffb800',
        route: { name: 'MostPopular' }
      },
      {
        id: 'monthly-new',
        title: '月度新品推荐',
        description: '本月新上架的优质应用',
        icon: RocketOutline,
        iconColor: '#13c2c2',
        route: { name: 'MonthlyNew' }
      },
      {
        id: 'latest-release',
        title: '最新上架',
        description: '最新发布的应用',
        icon: TimeOutline,
        iconColor: '#722ed1',
        route: { name: 'LatestRelease' }
      },
      {
        id: 'recent-updates',
        title: '最近更新',
        description: '近期更新的应用',
        icon: RefreshOutline,
        iconColor: '#2f54eb',
        route: { name: 'RecentUpdates' }
      }
    ]
  },
  {
    id: 'explore',
    title: '开始探索',
    icon: CompassOutline,
    iconColor: '#13c2c2',
    children: [
      {
        id: 'games',
        title: '游戏',
        description: '精选游戏应用',
        icon: GameControllerOutline,
        iconColor: '#ff4d4f',
        route: { name: 'Category', params: { id: 2 } }
      },
      {
        id: 'dev-tools',
        title: '开发工具',
        description: '提升开发效率的工具',
        icon: CodeSlashOutline,
        iconColor: '#1890ff',
        route: { name: 'Category', params: { id: 4 } }
      },
      {
        id: 'reading',
        title: '阅读学习',
        description: '知识获取与学习工具',
        icon: BookOutline,
        iconColor: '#722ed1',
        route: { name: 'Category', params: { id: 5 } }
      },
      {
        id: 'design',
        title: '图形设计',
        description: '设计创作工具',
        icon: BrushOutline,
        iconColor: '#13c2c2',
        route: { name: 'Category', params: { id: 7 } }
      },
      {
        id: 'entertainment',
        title: '影音娱乐',
        description: '视频音乐播放工具',
        icon: FilmOutline,
        iconColor: '#eb2f96',
        route: { name: 'Category', params: { id: 14 } }
      },
      {
        id: 'life',
        title: '生活',
        description: '提升生活品质的应用',
        icon: HeartOutline,
        iconColor: '#f5222d',
        route: { name: 'Category', params: { id: 23 } }
      },
      {
        id: 'productivity',
        title: '效率工具',
        description: '提升工作效率的应用',
        icon: BulbOutline,
        iconColor: '#faad14',
        route: { name: 'Category', params: { id: 24 } }
      },
      {
        id: 'official',
        title: '官方应用',
        description: '官方出品的应用',
        icon: StarOutline,
        iconColor: '#52c41a',
        route: { name: 'Category', params: { id: 26 } }
      }
    ]
  },
  {
    id: 'statistics',
    title: '统计分析',
    icon: StatsChartOutline,
    iconColor: '#1890ff',
    children: [
      {
        id: 'trend-analysis',
        title: '趋势分析',
        description: '应用发布和更新趋势',
        icon: AnalyticsOutline,
        iconColor: '#52c41a',
        route: { name: 'TrendAnalysis' }
      }
    ]
  }
]

// Find the selected function based on current route
const findFunctionByRoute = (routeName: string | null | undefined): Function | null => {
  if (!routeName) return null
  for (const category of functions) {
    const found = category.children?.find(func => func.route?.name === routeName)
    if (found) return found
  }
  return null
}

// Watch route changes
watch(
  () => route.name,
  (newRouteName) => {
    selectedFunction.value = findFunctionByRoute(newRouteName as string)
  },
  { immediate: true }
)

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({ name: 'Search', query: { q: searchQuery.value } })
  }
}

const handleFunctionClick = (func: Function) => {
  if (func.route) {
    router.push(func.route)
  }
}
</script>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px;
}

.search-section {
  margin: 16px auto 32px;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
}

.search-input {
  width: 500px;
  max-width: calc(100% - 88px);
}

:deep(.n-input-group) {
  width: 600px;
  max-width: 100%;
  margin: 0 auto;
}

.main-content {
  min-height: 400px;
}

.grid-layout {
  animation: fadeIn 0.3s ease;
}

.function-detail-layout {
  animation: fadeIn 0.3s ease;
}

.mobile-menu {
  display: none;
}

.mobile-content {
  display: none;
}

.function-card {
  cursor: pointer;
  transition: all 0.3s ease;
  height: 100%;
}

.function-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
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

.function-info {
  margin-left: 12px;
  flex: 1;
  min-width: 0;
}

.function-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.function-desc {
  font-size: 14px;
  color: #666;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
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

.function-name {
  font-size: 14px;
  font-weight: 500;
}

.function-content {
  min-height: 600px;
}

.content-title {
  font-size: 18px;
  font-weight: 500;
}

.content-area {
  padding: 16px 0;
}

.function-icon.large {
  width: 48px;
  height: 48px;
}

/* Mobile Styles */
@media screen and (max-width: 768px) {
  .home {
    padding: 12px;
  }

  .search-section {
    margin: 12px auto 24px;
  }

  .desktop-grid {
    display: none !important;
  }

  .mobile-menu {
    display: block;
    margin-bottom: 16px;
  }

  .mobile-content {
    display: block;
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

  .grid-layout {
    .n-grid {
      --n-cols: 2 !important;
      gap: 12px !important;
    }
  }

  .function-card {
    margin-bottom: 0;
  }

  .function-icon {
    width: 32px;
    height: 32px;
  }

  .function-info {
    margin-left: 8px;
  }

  .category-section {
    margin-bottom: 24px;
  }

  .category-title {
    font-size: 18px;
    margin-bottom: 12px;
  }
}

@media screen and (max-width: 480px) {
  .grid-layout {
    .n-grid {
      --n-cols: 1 !important;
    }
  }

  .function-card {
    max-width: 100%;
  }

  .search-section {
    margin: 8px auto 16px;
  }

  :deep(.n-input-group) {
    max-width: calc(100% - 16px);
  }

  .category-title {
    font-size: 16px;
  }

  .function-title {
    font-size: 14px;
  }

  .function-desc {
    font-size: 12px;
  }
}

/* Animation Styles */
.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.fade-enter-to,
.fade-leave-from {
  opacity: 1;
  transform: translateY(0);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.category-section {
  margin-bottom: 48px;
}

.category-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  font-size: 24px;
  font-weight: 500;
  color: #333;
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
</style> 