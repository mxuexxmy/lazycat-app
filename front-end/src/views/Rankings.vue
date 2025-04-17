<template>
  <div class="rankings-container">
    <!-- 桌面端菜单 -->
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

    <router-view />
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
  router.push({ name: key })
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
  padding: 16px;
}

/* 移动端样式 */
.mobile-menu {
  margin-bottom: 16px;
  background-color: #fff;
  border-bottom: 1px solid #eee;
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
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  font-size: 14px;
}

.mobile-tab.active {
  background-color: #f0faf5;
  color: #18a058;
}

.mobile-tab:active {
  opacity: 0.8;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .rankings-container {
    padding: 0;
  }
  
  :deep(.n-card) {
    border-radius: 0;
  }
}

@media screen and (max-width: 480px) {
  .mobile-tab {
    padding: 8px;
    font-size: 12px;
  }
}
</style> 