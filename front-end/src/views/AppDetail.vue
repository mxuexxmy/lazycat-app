<template>
  <div class="app-detail" v-if="app">
    <n-space vertical size="large">
      <n-card class="detail-card">
        <n-space vertical size="large">
          <!-- 头部信息 -->
          <n-space align="center" :size="24">
            <n-image
                :src="getAppIcon(app)"
                :alt="app.name"
                class="app-icon"
                :fallback-src="defaultIcon"
                preview-disabled
            />
            <n-space vertical :size="12">
              <n-h2 class="app-title">{{ app.name }}</n-h2>
              <n-space>
                <n-tag
                    v-for="cat in app.category"
                    :key="cat"
                    size="medium"
                    round
                    :bordered="false"
                    type="success"
                >{{ cat }}
                </n-tag>
              </n-space>
            </n-space>
          </n-space>

          <n-divider/>

          <!-- 应用简介 -->
          <n-space vertical :size="12">
            <n-h3 class="section-title">应用简介</n-h3>
            <n-p class="app-brief">{{ app.brief }}</n-p>
          </n-space>

          <!-- 详细描述 -->
          <n-space vertical :size="12">
            <n-h3 class="section-title">详细描述</n-h3>
            <n-p class="app-description" style="white-space: pre-line">{{ app.description }}</n-p>
          </n-space>

          <!-- 应用截图 -->
          <n-space vertical :size="12">
            <n-h3 class="section-title">应用截图</n-h3>
            <n-tabs type="line" animated>
              <n-tab-pane v-if="app.pcScreenshotPaths?.length" name="pc" tab="PC端">
                <n-grid :cols="3" :x-gap="24" :y-gap="24">
                  <n-grid-item v-for="(screenshot, index) in app.pcScreenshotPaths" :key="index">
                    <n-card class="screenshot-card">
                      <n-image
                          :src="getScreenshotUrl(app.pkgId, screenshot)"
                          :alt="`PC Screenshot ${index + 1}`"
                          object-fit="cover"
                          class="screenshot-image"
                          :preview-src="getScreenshotUrl(app.pkgId, screenshot)"
                          :preview-disabled="false"
                          show-toolbar-tooltip
                      />
                    </n-card>
                  </n-grid-item>
                </n-grid>
              </n-tab-pane>
              <n-tab-pane v-if="app.mobileScreenshotPaths?.length" name="mobile" tab="移动端">
                <n-grid :cols="3" :x-gap="24" :y-gap="24">
                  <n-grid-item v-for="(screenshot, index) in app.mobileScreenshotPaths" :key="index">
                    <n-card class="screenshot-card">
                      <n-image
                          :src="getScreenshotUrl(app.pkgId, screenshot)"
                          :alt="`Mobile Screenshot ${index + 1}`"
                          object-fit="cover"
                          class="screenshot-image"
                          :preview-src="getScreenshotUrl(app.pkgId, screenshot)"
                          :preview-disabled="false"
                          show-toolbar-tooltip
                      />
                    </n-card>
                  </n-grid-item>
                </n-grid>
              </n-tab-pane>
              <n-empty v-if="!app.pcScreenshotPaths?.length && !app.mobileScreenshotPaths?.length"
                       description="暂无截图"/>
            </n-tabs>
          </n-space>

          <!-- 应用信息 -->
          <n-space vertical :size="12">
            <n-h3 class="section-title">应用信息</n-h3>
            <n-descriptions bordered :column="2">
              <n-descriptions-item label="版本">{{ app.version }}</n-descriptions-item>
              <n-descriptions-item label="作者">{{ app.author }}</n-descriptions-item>
              <n-descriptions-item label="贡献者">{{ app.creator }}</n-descriptions-item>
              <n-descriptions-item label="更新时间">{{ formatDate(app.appUpdateTime) }}</n-descriptions-item>
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
    </n-space>
  </div>
  <n-spin v-else size="large"/>
</template>

<script setup lang="ts">
import {ref, onMounted} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {
  NTabs,
  NTabPane,
  NEmpty,
  NImage,
  NSpace,
  NCard,
  NDivider,
  NH2,
  NH3,
  NP,
  NGrid,
  NGridItem,
  NDescriptions,
  NDescriptionsItem
} from 'naive-ui'

interface AppDetail {
  packageName: string
  name: string
  description: string
  brief: string
  category: string[]
  iconPath: string
  version: string
  author: string
  creator: string
  updateDate: string
  supportPC: boolean
  supportMobile: boolean
  source: string
  pcScreenshotPaths?: string[]
  mobileScreenshotPaths?: string[]
}

const route = useRoute()
const app = ref<AppDetail | null>(null)
const defaultIcon = 'https://dl.lazycatmicroserver.com/appstore/metarepo/default-icon.png'

const getAppIcon = (app: AppDetail) => {
  if (!app.iconPath) {
    return `https://dl.lazycatmicroserver.com` + app.iconPath
  }
  return `https://dl.lazycatmicroserver.com/appstore/metarepo/apps/${app.packageName}/icon.png`
}

const getScreenshotUrl = (pkgId: string, screenshot: string) => {
  const screenshotPath = screenshot.replace('/public', '/screenshots')
  return `https://dl.lazycatmicroserver.com/appstore/metarepo/fallback/apps/${pkgId}${screenshotPath}`
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleString('zh-CN')
}

const fetchAppDetail = async (pkgId: string) => {
  try {
    const response = await fetch(`/api/app/${pkgId}`)
    const data = await response.json()
    app.value = data
  } catch (error) {
    console.error('Failed to fetch app detail:', error)
  }
}

onMounted(async () => {
  const pkgId = route.params.pkgId as string
  await fetchAppDetail(pkgId)
})
</script>

<style scoped>
.app-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px;
  position: relative;
}

.detail-card {
  margin-top: 24px;
}

.app-icon {
  width: 120px;
  height: 120px;
  border-radius: 24px;
  background-color: #f9f9f9;
  padding: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.app-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.app-brief {
  color: #666;
  font-size: 16px;
  line-height: 1.6;
}

.app-description {
  color: #666;
  font-size: 14px;
  line-height: 1.8;
}

.screenshot-card {
  background-color: #f9f9f9;
  transition: all 0.3s ease;
}

.screenshot-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.screenshot-image {
  width: 100%;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
}
</style> 