<n-card title="热门应用" class="mb-4">
  <n-spin :show="loading">
    <n-empty v-if="!loading && (!popularApps || popularApps.length === 0)" description="暂无数据" />
    <div v-else class="app-list">
      <n-list>
        <n-list-item v-for="(app, index) in popularApps" :key="app.pkgId">
          <div class="app-item" @click="handleAppClick(app)">
            <div class="rank" :class="getRankClass(index + 1)">
              {{ index + 1 }}
            </div>
            <div class="app-icon">
              <img :src="getAppIcon(app)" :alt="app.name" />
            </div>
            <div class="app-info">
              <div class="app-name">{{ app.name }}</div>
              <div class="app-stats">
                <span>下载量: {{ formatDownloads(app.downloadCount) }}</span>
                <n-divider vertical />
                <span>评分: {{ app.score || '暂无' }}</span>
                <n-divider vertical />
                <span>最近更新: {{ formatDate(app.updateDate) }}</span>
              </div>
            </div>
          </div>
        </n-list-item>
      </n-list>
    </div>
  </n-spin>
</n-card>

<script setup lang="ts">
import { ref } from 'vue'
import { NCard, NSpin, NEmpty, NList, NListItem, NDivider } from 'naive-ui'

interface App {
  pkgId: string
  name: string
  icon: string
  downloadCount: number
  score: number
  updateDate: string
}

interface PopularAppsResponse {
  success: boolean
  data: Array<{
    pkgId: string
    name: string
    icon: string
    downloadCount: number
    score: number
    updateDate: string
  }>
}

const popularApps = ref<App[]>([])
const loading = ref(true)

const fetchPopularApps = async () => {
  try {
    loading.value = true
    const response = await fetch('/api/apps/statistics/popular')
    const result = await response.json() as PopularAppsResponse
    
    if (result.success && result.data) {
      popularApps.value = result.data.map(app => ({
        pkgId: app.pkgId,
        name: app.name,
        icon: app.icon,
        downloadCount: app.downloadCount || 0,
        score: app.score,
        updateDate: app.updateDate || ''
      }))
    } else {
      popularApps.value = []
    }
  } catch (error) {
    console.error('Failed to fetch popular apps:', error)
    popularApps.value = []
  } finally {
    loading.value = false
  }
}
</script> 