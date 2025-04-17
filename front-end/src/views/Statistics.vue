<template>
  <div class="statistics-container">
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

    <div class="statistics-content">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, h, onMounted, onUnmounted, watch } from 'vue'
import type { Component } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NMenu, NScrollbar, NIcon } from 'naive-ui'
import type { MenuOption } from 'naive-ui'
import { BarChartOutline } from '@vicons/ionicons5'

const router = useRouter()
const route = useRoute()
const activeKey = ref('trend-analysis')

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
    label: '趋势分析',
    key: 'trend-analysis',
    icon: renderIcon(BarChartOutline)
  }
]

const handleMenuUpdate = (key: string) => {
  router.push(`/statistics/${key}`)
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
  // 如果没有选中任何菜单项，默认跳转到趋势分析
  if (!activeKey.value) {
    router.push({ name: 'trend-analysis' })
  }
})
</script>

<style scoped>
.statistics-container {
  min-height: 100vh;
  background: #f5f5f5;
}

.menu-wrapper {
  background: #fff;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 64px;
  z-index: 100;
}

.menu-content {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 24px;
}

.statistics-content {
  max-width: 1440px;
  margin: 0 auto;
  padding: 24px;
}

/* 移动端样式 */
.mobile-menu {
  margin: 0 -24px;
  background-color: #fff;
}

.mobile-tabs {
  display: flex;
  padding: 8px 16px;
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

:deep(.n-menu) {
  height: 48px;
}

:deep(.n-menu .n-menu-item) {
  height: 48px;
  display: flex;
  align-items: center;
}

:deep(.n-menu .n-menu-item-content) {
  padding: 0 16px;
}

:deep(.n-menu .n-menu-item-content__icon) {
  margin-right: 6px;
}

/* 移动端适配 */
@media screen and (max-width: 1200px) {
  .statistics-content {
    padding: 16px;
  }

  .menu-content {
    padding: 0 16px;
  }
}

@media screen and (max-width: 768px) {
  .statistics-container {
    background: #fff;
  }

  .statistics-content {
    padding: 0;
  }

  .menu-content {
    padding: 0;
  }
  
  :deep(.n-card) {
    border-radius: 0;
    margin-bottom: 8px;
  }
}

@media screen and (max-width: 480px) {
  .mobile-tab {
    padding: 8px;
    font-size: 12px;
  }
}
</style> 