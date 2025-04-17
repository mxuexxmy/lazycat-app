<template>
  <div class="statistics-container">
    <n-menu
      v-model:value="activeKey"
      mode="horizontal"
      :options="menuOptions"
      @update:value="handleMenuUpdate"
    />
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { ref, h } from 'vue'
import { useRouter } from 'vue-router'
import { NMenu } from 'naive-ui'
import type { MenuOption } from 'naive-ui'
import { AnalyticsOutline } from '@vicons/ionicons5'

const router = useRouter()
const activeKey = ref<string | null>(null)

const menuOptions: MenuOption[] = [
  {
    label: '趋势分析',
    key: 'trend-analysis',
    icon: () => h(AnalyticsOutline)
  }
]

const handleMenuUpdate = (key: string) => {
  router.push(`/statistics/${key}`)
}
</script>

<style scoped>
.statistics-container {
  padding: 16px;
}
</style> 