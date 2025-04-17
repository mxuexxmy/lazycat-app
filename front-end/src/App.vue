<template>
  <n-config-provider :locale="naiveConfig.locale" :date-locale="naiveConfig.dateLocale">
    <n-layout>
      <n-layout-header bordered>
        <router-link :to="{ name: 'Home' }" class="site-title" replace>
          <n-space justify="center" align="center" class="header-content">
            <n-h2>懒猫应用</n-h2>
          </n-space>
        </router-link>
      </n-layout-header>
      <n-layout>
        <n-layout-content style="background: #f5f6fb; padding: 80px 24px 24px;">
          <router-view />
        </n-layout-content>
      </n-layout>
    </n-layout>
  </n-config-provider>
</template>

<script setup lang="ts">
import { inject, h } from 'vue'
import { useAppStore } from './stores/app'
import {
  NConfigProvider,
  NLayout,
  NLayoutHeader,
  NLayoutContent,
  NSpace,
  NH2,
  type NLocale,
  type NDateLocale
} from 'naive-ui'

interface NaiveConfig {
  locale: NLocale
  dateLocale: NDateLocale
}

const appStore = useAppStore()
const naiveConfig = inject('naiveConfig') as NaiveConfig

// Fetch categories on component mount
appStore.fetchCategories()
</script>

<style>
.n-layout {
  height: 100vh;
}

.n-layout-header {
  background: #fff;
  padding: 0;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;
}

.n-layout-content {
  min-height: calc(100vh - 60px);
}

.site-title {
  text-decoration: none;
  color: #333;
  transition: color 0.3s ease;
  display: block;
  width: 100%;
  cursor: pointer;
}

.site-title:hover {
  color: #18a058;
}

.header-content {
  padding: 0 20px;
  height: 60px;
}

.site-title .n-h2 {
  margin: 0;
}

@media screen and (max-width: 768px) {
  .site-title .n-h2 {
    font-size: 1.25em;
  }
}
</style> 