<template>
  <div class="rankings-container">
    <!-- 桌面端菜单 -->
    <div class="menu-wrapper">
      <div class="menu-content">
        <n-menu
          v-if="!isMobile"
          v-model:value="activeKey"
          mode="horizontal"
          :options="menuOptions"
          @update:value="handleMenuUpdate"
        />
        
        <!-- 移动端菜单 -->
        <div v-else class="mobile-menu">
          <n-scrollbar x-scrollable>
            <div class="mobile-tabs">
              <div
                v-for="option in menuOptions"
                :key="option.key"
                class="mobile-tab"
                :class="{ active: activeKey === option.key }"
                @click="handleMenuUpdate(option.key as string)"
              >
                <n-icon size="20">
                  <component :is="(option.icon as Function)()" />
                </n-icon>
                <span>{{ option.label }}</span>
              </div>
            </div>
          </n-scrollbar>
        </div>
      </div>
    </div>

    <div class="rankings-content">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, h, watch, onMounted, computed, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NMenu, NScrollbar, NIcon } from 'naive-ui'
import type { MenuOption } from 'naive-ui'
import {
  TrophyOutline,
  SparklesOutline,
  RocketOutline,
  ReloadOutline,
  PeopleOutline
} from '@vicons/ionicons5'
import type { Component } from 'vue'

const router = useRouter()
const route = useRoute()
const activeKey = ref<string | null>(null)

// 检测是否为移动端
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value <= 768)

// 监听窗口大小变化
const handleResize = () => {
  windowWidth.value = window.innerWidth
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

function renderIcon(icon: Component) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

const menuOptions: MenuOption[] = [
  {
    label: '最受欢迎',
    key: 'most-popular',
    icon: renderIcon(TrophyOutline)
  },
  {
    label: '月度新品推荐',
    key: 'monthly-new',
    icon: renderIcon(SparklesOutline)
  },
  {
    label: '最新上架',
    key: 'latest-release',
    icon: renderIcon(RocketOutline)
  },
  {
    label: '最近更新',
    key: 'recent-updates',
    icon: renderIcon(ReloadOutline)
  },
  {
    label: '开发者排行',
    key: 'developer-ranking',
    icon: renderIcon(PeopleOutline)
  }
]

const handleMenuUpdate = (key: string) => {
  router.push(`/rankings/${key}`)
}

// 根据当前路由设置活动菜单项
const updateActiveKey = () => {
  const currentRouteName = route.name as string
  if (currentRouteName) {
    activeKey.value = currentRouteName
  }
}

// 监听路由变化
watch(() => route.name, updateActiveKey)

// 组件挂载时设置初始活动菜单项
onMounted(() => {
  updateActiveKey()
  // 如果没有选中任何菜单项，默认跳转到最受欢迎
  if (!activeKey.value) {
    router.push({ name: 'most-popular' })
  }
})
</script>

<style scoped>
.rankings-container {
  min-height: 100vh;
  background: #f5f5f5;
}

.menu-wrapper {
  background: #fff;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 64px;
  z-index: 100;
  padding: 12px 0;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.menu-content {
  max-width: 1040px; /* 1440px - 400px */
  margin: 0 auto;
  padding: 0 24px;
}

.rankings-content {
  max-width: 1040px; /* 1440px - 400px */
  margin: 0 auto;
  padding: 24px;
}

/* 移动端样式 */
.mobile-menu {
  margin: 0 -24px;
  background-color: #fff;
  padding: 4px 0;
}

.mobile-tabs {
  display: flex;
  padding: 8px 16px;
  gap: 12px;
  min-width: min-content;
  position: relative;
}

.mobile-tab {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
  font-size: 14px;
  color: #666;
  position: relative;
  overflow: hidden;
  background: transparent;
}

.mobile-tab:hover {
  color: #18a058;
  background-color: rgba(24, 160, 88, 0.1);
  transform: translateY(-1px);
}

.mobile-tab.active {
  color: #18a058;
  background-color: rgba(24, 160, 88, 0.1);
  font-weight: 500;
}

.mobile-tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 16px;
  height: 3px;
  background: #18a058;
  border-radius: 4px;
}

.mobile-tab:active {
  transform: translateY(0);
}

:deep(.n-menu) {
  height: 48px;
  font-size: 14px;
  background: transparent;
}

:deep(.n-menu .n-menu-item) {
  height: 40px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  margin: 0 4px;
  border-radius: 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.n-menu .n-menu-item:hover) {
  color: #18a058 !important;
  background-color: rgba(24, 160, 88, 0.1) !important;
  transform: translateY(-1px);
}

:deep(.n-menu .n-menu-item.n-menu-item--selected) {
  background-color: rgba(24, 160, 88, 0.1) !important;
  color: #18a058 !important;
  font-weight: 500;
}

:deep(.n-menu .n-menu-item-content) {
  padding: 0 8px;
  line-height: 40px;
}

:deep(.n-menu .n-menu-item-content__icon) {
  margin-right: 8px;
  font-size: 20px;
  transition: transform 0.3s ease;
}

:deep(.n-menu .n-menu-item:hover .n-menu-item-content__icon) {
  transform: scale(1.1);
}

/* 移动端适配 */
@media screen and (max-width: 1200px) {
  .rankings-content {
    padding: 16px;
  }

  .menu-content {
    padding: 0 16px;
  }
}

@media screen and (max-width: 768px) {
  .rankings-container {
    background: #fff;
  }

  .menu-wrapper {
    padding: 8px 0;
    border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  }

  .rankings-content {
    padding: 0;
  }

  .menu-content {
    padding: 0;
  }
  
  :deep(.n-card) {
    border-radius: 0;
    margin-bottom: 8px;
  }

  .mobile-tab {
    padding: 8px 16px;
    font-size: 13px;
  }

  :deep(.n-menu .n-menu-item-content__icon) {
    font-size: 18px;
  }
}

@media screen and (max-width: 480px) {
  .menu-wrapper {
    padding: 4px 0;
  }

  .mobile-tab {
    padding: 8px 12px;
    font-size: 12px;
    border-radius: 10px;
  }

  .mobile-tab.active::after {
    width: 12px;
    height: 2px;
  }
}
</style> 