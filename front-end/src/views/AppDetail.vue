<template>
  <div class="app-detail" v-if="app">
    <n-card>
      <n-space vertical>
        <n-space align="center">
          <img :src="getAppIcon(app)" :alt="app.name" style="width: 100px; height: 100px; object-fit: cover">
          <n-space vertical>
            <n-h2>{{ app.name }}</n-h2>
            <n-space>
              <n-tag v-for="cat in app.category" :key="cat">{{ cat }}</n-tag>
            </n-space>
          </n-space>
        </n-space>

        <n-divider />

        <n-space vertical>
          <n-h3>应用简介</n-h3>
          <n-p>{{ app.brief }}</n-p>
        </n-space>

        <n-space vertical>
          <n-h3>详细描述</n-h3>
          <n-p style="white-space: pre-line">{{ app.description }}</n-p>
        </n-space>

        <n-space vertical>
          <n-h3>应用截图</n-h3>
          <n-grid :cols="4" :x-gap="12" :y-gap="8">
            <n-grid-item v-for="(screenshot, index) in app.pcScreenshotPaths" :key="index">
              <img :src="getScreenshotUrl(app.pkgId, screenshot)" :alt="`Screenshot ${index + 1}`" style="width: 100%; height: 200px; object-fit: cover">
            </n-grid-item>
          </n-grid>
        </n-space>

        <n-space vertical>
          <n-h3>应用信息</n-h3>
          <n-descriptions bordered>
            <n-descriptions-item label="版本">{{ app.version }}</n-descriptions-item>
            <n-descriptions-item label="作者">{{ app.author }}</n-descriptions-item>
            <n-descriptions-item label="贡献者">{{ app.creator }}</n-descriptions-item>
            <n-descriptions-item label="更新时间">{{ formatDate(app.updateDate) }}</n-descriptions-item>
            <n-descriptions-item label="支持平台">
              <n-space>
                <n-tag v-if="app.supportPC" type="success">PC</n-tag>
                <n-tag v-if="app.supportMobile" type="success">Mobile</n-tag>
              </n-space>
            </n-descriptions-item>
            <n-descriptions-item label="源代码">
              <a :href="app.source" target="_blank">{{ app.source }}</a>
            </n-descriptions-item>
          </n-descriptions>
        </n-space>
      </n-space>
    </n-card>
  </div>
  <n-spin v-else size="large" />
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '../stores/app'
import type { App } from '../types'

const route = useRoute()
const appStore = useAppStore()
const app = ref<App | null>(null)

const getAppIcon = (app: App) => {
  return `https://dl.lazycatmicroserver.com/appstore/metarepo/apps/${app.pkgId}/${app.iconPath}`
}

const getScreenshotUrl = (pkgId: string, screenshot: string) => {
  return `https://dl.lazycatmicroserver.com/appstore/metarepo/apps/${pkgId}/${screenshot}`
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(async () => {
  const pkgId = route.params.pkgId as string
  app.value = await appStore.fetchAppInfo(pkgId)
})
</script>

<style scoped>
.app-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
</style> 