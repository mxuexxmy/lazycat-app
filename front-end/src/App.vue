<template>
  <n-config-provider :locale="zhCN" :date-locale="dateZhCN">
    <n-message-provider>
      <n-layout>
        <n-layout-header bordered class="header">
          <div class="header-content">
            <div class="left-section">
              <router-link to="/" class="logo">
                <n-h2>懒猫应用</n-h2>
              </router-link>
              <n-button 
                text 
                class="menu-toggle" 
                @click="showDrawer = true"
                size="large"
              >
                <template #icon>
                  <n-icon><menu-outline /></n-icon>
                </template>
                <span class="menu-text">菜单</span>
              </n-button>
            </div>
            <div class="nav-section desktop-only">
              <n-menu
                v-model:value="activeKey"
                mode="horizontal"
                :options="menuOptions"
                @update:value="handleMenuClick"
              />
            </div>
            <div class="right-section">
              <n-button text @click="handleSearch">
                <template #icon>
                  <n-icon><search /></n-icon>
                </template>
              </n-button>
            </div>
          </div>
        </n-layout-header>
        <n-drawer v-model:show="showDrawer" :width="280" placement="left">
          <n-drawer-content title="菜单">
            <n-menu
              v-model:value="activeKey"
              :indent="12"
              :options="menuOptions"
              @update:value="handleMobileMenuClick"
            />
          </n-drawer-content>
        </n-drawer>
        <n-layout-content :style="{ background: '#f5f6fb', padding: '80px 24px 24px' }">
          <div class="back-button" v-if="showBackButton" @click="handleBack">
            <n-button circle>
              <template #icon>
                <n-icon><arrow-back /></n-icon>
              </template>
            </n-button>
          </div>
          <router-view v-slot="{ Component }">
            <keep-alive :include="['Search', 'MostPopular', 'MonthlyNew', 'LatestRelease', 'RecentUpdates', 'DeveloperRanking']">
              <component 
                :is="Component" 
                :key="shouldRefresh ? route.fullPath : undefined"
              />
            </keep-alive>
          </router-view>
        </n-layout-content>
      </n-layout>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import { h, ref, computed, watch, nextTick } from 'vue'
import { useAppStore } from './stores/app'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  NConfigProvider,
  NLayout,
  NLayoutHeader,
  NLayoutContent,
  NH2,
  NMenu,
  NButton,
  NIcon,
  NDrawer,
  NDrawerContent,
  NMessageProvider
} from 'naive-ui'
import { zhCN, dateZhCN } from 'naive-ui'
import { Search, ArrowBack, MenuOutline, HomeOutline, TrophyOutline, CodeSlashOutline, RocketOutline, BookOutline, BuildOutline, RefreshOutline } from '@vicons/ionicons5'

const appStore = useAppStore()
const userStore = useUserStore()

const route = useRoute()
const router = useRouter()
const activeKey = ref<string | null>(null)
const shouldRefresh = ref(false)

// Fetch categories on component mount
appStore.fetchCategories()

// 菜单选项
const menuOptions = [
  {
    label: '首页',
    key: 'Home',
    icon: () => h(NIcon, null, { default: () => h(HomeOutline) })
  },
  {
    label: '开发者手册',
    key: 'DeveloperManual',
    icon: () => h(NIcon, null, { default: () => h(BookOutline) })
  },
  {
    label: '开发者中心',
    key: 'DeveloperCenter',
    icon: () => h(NIcon, null, { default: () => h(BuildOutline) })
  },
  {
    label: '同步信息',
    key: 'SyncStatus',
    icon: () => h(NIcon, null, { default: () => h(RefreshOutline) })
  }
]

// 需要刷新的路由配置
interface RouteRefreshConfig {
  [key: string]: string[];
}

const refreshRoutes: RouteRefreshConfig = {
  'Search': ['keyword'],
  'Category': ['id'],
  'DeveloperRanking': ['period'],
  'MostPopular': ['period'],
  'MonthlyNew': ['month'],
  'LatestRelease': ['page'],
  'RecentUpdates': ['page'],
  'DeveloperApps': ['developerId']
}

// 监听路由变化
watch(
  () => route.fullPath,
  () => {
    const routeName = route.name as string
    if (refreshRoutes[routeName]) {
      const params = refreshRoutes[routeName]
      const shouldForceRefresh = params.some((param: string) => 
        route.query[param] !== undefined && 
        route.query[param] !== router.currentRoute.value.query[param]
      )
      if (shouldForceRefresh) {
        shouldRefresh.value = true
        nextTick(() => {
          shouldRefresh.value = false
        })
      }
    }
  }
)

// 处理菜单点击
const handleMenuClick = (key: string) => {
  if (key === 'achievements') {
    router.push(`/achievements/${userStore.userId}`)
    return
  }
  // 如果是顶级菜单且有子菜单，不进行跳转
  const menuItem = menuOptions.find(item => item.key === key)
  
  if (menuItem) {
    if (key === 'Explore') {
      router.push({ name: 'Category', params: { id: '0' } })
    } else if (key === 'DeveloperManual') {
      window.open('https://developer.lazycat.cloud/', '_blank')
    } else if (key === 'DeveloperCenter') {
      window.open('https://developer.lazycat.cloud/manage', '_blank')
    } else if (!menuItem.children) {
      router.push({ name: key })
    }
  } else {
    // 处理子菜单项点击
    router.push({ name: key })
  }
}

// 处理搜索点击
const handleSearch = () => {
  router.push({ name: 'Search' })
}

// 计算是否显示返回按钮
const showBackButton = computed(() => {
  const routesWithBack = [
    // 开发者相关
    'DeveloperCenter',
    'AppRepositories',
    'DeveloperRanking',
    'DeveloperCommunity',
    'DeveloperApps',
    'GitHubAchievements',
    // 排行榜相关
    'MostPopular',
    'MonthlyNew',
    'LatestRelease',
    'RecentUpdates',
    // 开始探索相关
    'Games',
    'DevTools',
    'Education',
    'Category',
    // 统计分析相关
    'TrendAnalysis',
    'StatisticsOverview',
    "StatisticsApps",
    "StatisticsUsers",
    "FiveStarApps",
    // 其他
    'Search',
    'AppDetail',
    'Comments',
    'SyncStatus'
  ]
  return routesWithBack.includes(route.name as string)
})

const handleBack = () => {
  router.back()
}

const showDrawer = ref(false)

const handleMobileMenuClick = (key: string) => {
  handleMenuClick(key)
  showDrawer.value = false
}

</script>

<style>
.n-layout {
  height: 100vh;
}

.header {
  background: #fff;
  padding: 0;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;
  border-bottom: 1px solid #f0f0f0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.left-section {
  display: flex;
  align-items: center;
  gap: 8px;
}

.menu-toggle {
  display: none;
  font-size: 24px;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  margin-left: 12px;
  border-radius: 8px;
  color: #333;
  background-color: rgba(24, 160, 88, 0.1);
  transition: all 0.3s ease;
}

.menu-toggle:hover {
  background-color: rgba(24, 160, 88, 0.15);
  transform: translateY(-1px);
}

.menu-toggle:active {
  transform: translateY(0);
}

.menu-toggle .menu-text {
  font-size: 14px;
  font-weight: 500;
  margin-left: 4px;
  color: #18a058;
}

.menu-toggle :deep(.n-icon) {
  color: #18a058;
}

.logo {
  text-decoration: none;
  color: #333;
  transition: color 0.3s ease;
}

.logo:hover {
  color: #18a058;
}

.logo .n-h2 {
  margin: 0;
  font-size: 20px;
  line-height: 60px;
}

.nav-section {
  flex: 1;
  display: flex;
  justify-content: center;
  margin: 0 48px;
}

.nav-section :deep(.n-menu) {
  border-bottom: none;
}

.nav-section :deep(.n-menu-item) {
  margin: 0 16px;
}

.right-section {
  display: flex;
  align-items: center;
}

.right-section :deep(.n-button) {
  font-size: 20px;
}

.n-layout-content {
  min-height: calc(100vh - 60px);
}

.back-button {
  position: fixed;
  left: 24px;
  top: 90px;
  z-index: 1000;
  cursor: pointer;
}

.back-button :deep(.n-button) {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.back-button :deep(.n-button:hover) {
  background-color: #f5f5f5;
  transform: scale(1.05);
}

.back-button :deep(.n-icon) {
  font-size: 16px;
}

:deep(.n-drawer-content .n-menu-item-content-header) {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.n-drawer-content .n-menu-item-content-header .n-icon) {
  font-size: 18px;
}

@media screen and (max-width: 768px) {
  .header-content {
    padding: 0 16px;
  }

  .menu-toggle {
    display: flex;
    padding: 6px 10px;
  }

  .menu-toggle :deep(.n-icon) {
    font-size: 20px;
  }

  .desktop-only {
    display: none;
  }

  .logo .n-h2 {
    font-size: 18px;
  }

  .nav-section {
    margin: 0 16px;
  }

  .nav-section :deep(.n-menu-item) {
    margin: 0 8px;
  }

  .back-button {
    left: 16px;
    top: 80px;
  }
  
  .back-button :deep(.n-button) {
    width: 32px;
    height: 32px;
    font-size: 16px;
  }
}

@media screen and (max-width: 480px) {
  .header-content {
    padding: 0 12px;
  }

  .logo .n-h2 {
    font-size: 16px;
  }

  .right-section :deep(.n-button) {
    font-size: 18px;
  }

  .menu-toggle {
    padding: 6px;
    margin-left: 8px;
    border-radius: 6px;
  }

  .menu-toggle :deep(.n-icon) {
    font-size: 18px;
  }

  .menu-text {
    display: none;
  }
}
</style> 