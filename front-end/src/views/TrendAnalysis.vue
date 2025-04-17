<template>
  <div class="trend-analysis">
    <n-card title="应用发布趋势">
      <n-grid :cols="2" :x-gap="12" responsive="screen">
        <n-grid-item>
          <n-card title="每月新增应用数" :loading="loading">
            <div ref="monthlyAppsChart" class="chart-container">
              <n-empty v-if="!loading && !monthlyData.length" description="暂无数据" />
            </div>
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card title="应用类别分布" :loading="loading">
            <div ref="categoryChart" class="chart-container">
              <n-empty v-if="!loading && !categoryData.length" description="暂无数据" />
            </div>
          </n-card>
        </n-grid-item>
      </n-grid>
    </n-card>

    <n-card title="开发者活跃度" class="mt-4">
      <n-grid :cols="2" :x-gap="12" responsive="screen">
        <n-grid-item>
          <n-card title="每月活跃开发者" :loading="loading">
            <div ref="monthlyDevelopersChart" class="chart-container">
              <n-empty v-if="!loading && !developerData.length" description="暂无数据" />
            </div>
          </n-card>
        </n-grid-item>
        <n-grid-item>
          <n-card title="开发者贡献分布" :loading="loading">
            <div ref="developerDistChart" class="chart-container">
              <n-empty v-if="!loading && !developerDistData.length" description="暂无数据" />
            </div>
          </n-card>
        </n-grid-item>
      </n-grid>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { NCard, NGrid, NGridItem, NEmpty } from 'naive-ui'
import * as echarts from 'echarts'
import type { EChartsType } from 'echarts'

interface AppInfo {
  pkgId: string
  name: string
  updateDate: string
  category: string[]
  creatorId: number
}

interface DeveloperInfo {
  id: number
  name: string
  contributions: number
  lastActiveDate: string
}

const loading = ref(true)
const monthlyData = ref<{ date: string; count: number }[]>([])
const categoryData = ref<{ name: string; value: number }[]>([])
const developerData = ref<{ date: string; count: number }[]>([])
const developerDistData = ref<{ name: string; value: number }[]>([])

// Chart instances
let monthlyAppsChart: EChartsType | null = null
let categoryChart: EChartsType | null = null
let monthlyDevelopersChart: EChartsType | null = null
let developerDistChart: EChartsType | null = null

// Chart DOM refs
const monthlyAppsChartRef = ref<HTMLElement | null>(null)
const categoryChartRef = ref<HTMLElement | null>(null)
const monthlyDevelopersChartRef = ref<HTMLElement | null>(null)
const developerDistChartRef = ref<HTMLElement | null>(null)

// Fetch app list data
const fetchAppData = async () => {
  try {
    const response = await fetch('https://appstore.api.lazycat.cloud/api/app/list')
    const result = await response.json()
    if (result.success && Array.isArray(result.data)) {
      processAppData(result.data)
    }
  } catch (error) {
    console.error('Failed to fetch app data:', error)
  }
}

// Process app data
const processAppData = (apps: AppInfo[]) => {
  // Process monthly new apps
  const monthlyApps = new Map<string, number>()
  for (const app of apps) {
    const date = app.updateDate.substring(0, 7) // Get YYYY-MM
    monthlyApps.set(date, (monthlyApps.get(date) || 0) + 1)
  }
  monthlyData.value = Array.from(monthlyApps.entries())
    .map(([date, count]) => ({ date, count }))
    .sort((a, b) => a.date.localeCompare(b.date))

  // Process category distribution
  const categories = new Map<string, number>()
  for (const app of apps) {
    for (const cat of app.category) {
      categories.set(cat, (categories.get(cat) || 0) + 1)
    }
  }
  categoryData.value = Array.from(categories.entries())
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value)

  // Initialize charts after data processing
  initializeCharts()
}

// Initialize charts
const initializeCharts = () => {
  // Monthly new apps chart
  if (monthlyAppsChartRef.value) {
    monthlyAppsChart = echarts.init(monthlyAppsChartRef.value)
    monthlyAppsChart.setOption({
      title: { text: '每月新增应用数' },
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: monthlyData.value.map(item => item.date)
      },
      yAxis: { type: 'value' },
      series: [{
        name: '新增应用数',
        type: 'line',
        data: monthlyData.value.map(item => item.count),
        smooth: true,
        areaStyle: {}
      }]
    })
  }

  // Category distribution chart
  if (categoryChartRef.value) {
    categoryChart = echarts.init(categoryChartRef.value)
    categoryChart.setOption({
      title: { text: '应用类别分布' },
      tooltip: { trigger: 'item' },
      series: [{
        name: '应用数量',
        type: 'pie',
        radius: '60%',
        data: categoryData.value
      }]
    })
  }

  // Monthly active developers chart
  if (monthlyDevelopersChartRef.value) {
    monthlyDevelopersChart = echarts.init(monthlyDevelopersChartRef.value)
    monthlyDevelopersChart.setOption({
      title: { text: '每月活跃开发者' },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: [] },
      yAxis: { type: 'value' },
      series: [{
        name: '活跃开发者',
        type: 'bar',
        data: []
      }]
    })
  }

  // Developer distribution chart
  if (developerDistChartRef.value) {
    developerDistChart = echarts.init(developerDistChartRef.value)
    developerDistChart.setOption({
      title: { text: '开发者贡献分布' },
      tooltip: { trigger: 'item' },
      series: [{
        name: '贡献数量',
        type: 'pie',
        radius: ['40%', '70%'],
        data: []
      }]
    })
  }

  loading.value = false
}

// Handle window resize
const handleResize = () => {
  monthlyAppsChart?.resize()
  categoryChart?.resize()
  monthlyDevelopersChart?.resize()
  developerDistChart?.resize()
}

// Lifecycle hooks
onMounted(() => {
  fetchAppData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  monthlyAppsChart?.dispose()
  categoryChart?.dispose()
  monthlyDevelopersChart?.dispose()
  developerDistChart?.dispose()
})
</script>

<style scoped>
.trend-analysis {
  padding: 16px;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.mt-4 {
  margin-top: 16px;
}

@media screen and (max-width: 768px) {
  .trend-analysis {
    padding: 8px;
  }

  .chart-container {
    height: 250px;
  }

  :deep(.n-grid) {
    --n-cols: 1 !important;
  }

  :deep(.n-card) {
    margin-bottom: 8px;
  }
}
</style> 