<template>
  <n-config-provider :locale="zhCN" :date-locale="dateZhCN">
    <n-layout>
      <n-layout-header bordered class="header">
        <div class="header-content">
          <router-link to="/" class="logo">
            <n-h2>懒猫应用</n-h2>
          </router-link>
          <div class="nav-section">
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
      <n-layout-content :style="{ background: '#f5f6fb', padding: '80px 24px 24px' }">
        <div class="back-button" v-if="showBackButton" @click="handleBack">
          <n-button circle>
            <template #icon>
              <n-icon><arrow-back /></n-icon>
            </template>
          </n-button>
        </div>
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-config-provider>
</template>

<script setup lang="ts">
import { inject, h, ref, computed } from 'vue'
import { useAppStore } from './stores/app'
import { useRouter, useRoute } from 'vue-router'
import {
  NConfigProvider,
  NLayout,
  NLayoutHeader,
  NLayoutContent,
  NSpace,
  NH2,
  type NLocale,
  type NDateLocale,
  NMenu,
  NButton,
  NIcon
} from 'naive-ui'
import { zhCN, dateZhCN } from 'naive-ui'
import { Search, ArrowBack } from '@vicons/ionicons5'

interface NaiveConfig {
  locale: NLocale
  dateLocale: NDateLocale
}

const appStore = useAppStore()
const naiveConfig = inject('naiveConfig') as NaiveConfig

const route = useRoute()
const router = useRouter()
const activeKey = ref<string | null>(null)

// Fetch categories on component mount
appStore.fetchCategories()

// 菜单选项
const menuOptions = [
  {
    label: '首页',
    key: 'home'
  },
  {
    label: '排行榜',
    key: 'rankings'
  },
  {
    label: '开发者',
    key: 'developers'
  },
  {
    label: '探索',
    key: 'explore'
  }
]

// 处理菜单点击
const handleMenuClick = (key: string) => {
  router.push({ name: key })
}

// 处理搜索点击
const handleSearch = () => {
  router.push({ name: 'Search' })
}

// 计算是否显示返回按钮
const showBackButton = computed(() => {
  const routesWithBack = [
    'AppDetail',
    'DeveloperApps',
    'Category',
    'Search',
    'DeveloperRanking',
    'MostPopular',
    'MonthlyNew',
    'LatestRelease',
    'RecentUpdates'
  ]
  return routesWithBack.includes(route.name as string)
})

const handleBack = () => {
  router.back()
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

@media screen and (max-width: 768px) {
  .header-content {
    padding: 0 16px;
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
  .nav-section :deep(.n-menu-item-content) {
    font-size: 14px;
    padding: 0 8px;
  }
}
</style> 