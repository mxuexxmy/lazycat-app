<template>
  <div class="trend-analysis">
    <n-space vertical size="large">
      <!-- 应用发布趋势 -->
      <n-card title="应用发布趋势">
        <n-space vertical :size="16">
          <n-card title="每月新增应用数">
            <div ref="monthlyChartRef" class="chart-container">
              <n-spin v-if="loading" />
              <n-empty v-else-if="!monthlyData.length" description="暂无数据" />
            </div>
          </n-card>
          <n-card title="应用类别分布">
            <div ref="categoryChartRef" class="chart-container">
              <n-spin v-if="loading" />
              <n-empty v-else-if="!categoryData.length" description="暂无数据" />
            </div>
          </n-card>
        </n-space>
      </n-card>

      <!-- 开发者活跃度 -->
      <n-card title="开发者活跃度">
        <n-space vertical :size="16">
          <n-card title="开发者提交应用">
            <div ref="developerActivityChartRef" class="chart-container">
              <n-spin v-if="loading" />
              <n-empty v-else-if="!developerData.length" description="暂无数据" />
            </div>
          </n-card>
          <n-card title="应用更新频率">
            <div ref="updateFrequencyChartRef" class="chart-container">
              <n-spin v-if="loading" />
              <n-empty v-else-if="!updateData.length" description="暂无数据" />
            </div>
          </n-card>
        </n-space>
      </n-card>
    </n-space>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { NCard, NGrid, NGridItem, NSpace, NSpin, NEmpty } from 'naive-ui'
import * as echarts from 'echarts'

interface AppInfo {
  name: string
  pkgId: string
  category: string[]
  creator: string
  creatorId: number
  updateDate: string
  updateId: number
}

interface DeveloperInfo {
  nickname: string
}

interface DeveloperData {
  name: string
  value: number
  updates: number
}

const loading = ref(true)
const monthlyData = ref<{ date: string; count: number }[]>([])
const categoryData = ref<{ name: string; value: number }[]>([])
const developerData = ref<DeveloperData[]>([])
const updateData = ref<{ date: string; count: number }[]>([])

const monthlyChartRef = ref<HTMLElement | null>(null)
const categoryChartRef = ref<HTMLElement | null>(null)
const developerActivityChartRef = ref<HTMLElement | null>(null)
const updateFrequencyChartRef = ref<HTMLElement | null>(null)

let monthlyChart: echarts.ECharts | null = null
let categoryChart: echarts.ECharts | null = null
let developerActivityChart: echarts.ECharts | null = null
let updateFrequencyChart: echarts.ECharts | null = null

// 获取应用列表数据
const fetchAppData = async () => {
  try {
    const response = await fetch('https://appstore.api.lazycat.cloud/api/app/list')
    const result = await response.json()
    if (result.success && Array.isArray(result.data)) {
      processAppData(result.data)
    }
  } catch (error) {
    console.error('Failed to fetch app data:', error)
  } finally {
    loading.value = false
  }
}

// 获取开发者昵称
const fetchDeveloperNickname = async (pkgId: string): Promise<string> => {
  try {
    const response = await fetch(`https://appstore.api.lazycat.cloud/api/app/developer/${pkgId}`)
    const result = await response.json()
    if (result.success && result.data?.nickname) {
      return result.data.nickname
    }
  } catch (error) {
    console.error('Failed to fetch developer nickname:', error)
  }
  return ''
}

// 处理应用数据
const processAppData = async (apps: AppInfo[]) => {
  // 处理月度新增应用数据
  const monthlyApps = new Map<string, number>()
  for (const app of apps) {
    const date = app.updateDate.substring(0, 7) // 获取 YYYY-MM
    monthlyApps.set(date, (monthlyApps.get(date) || 0) + 1)
  }
  monthlyData.value = Array.from(monthlyApps.entries())
    .map(([date, count]) => ({ date, count }))
    .sort((a, b) => a.date.localeCompare(b.date))

  // 处理应用类别分布
  const categories = new Map<string, number>()
  for (const app of apps) {
    for (const cat of app.category) {
      categories.set(cat, (categories.get(cat) || 0) + 1)
    }
  }
  categoryData.value = Array.from(categories.entries())
    .map(([name, value]) => ({ name, value }))
    .sort((a, b) => b.value - a.value)

  // 处理开发者数据
  const developers = new Map<number, { 
    count: number; 
    pkgId: string; 
    creator: string;
    updates: number;
    lastUpdate: string;
  }>()
  
  for (const app of apps) {
    const dev = developers.get(app.creatorId) || { 
      count: 0, 
      pkgId: app.pkgId, 
      creator: app.creator,
      updates: 0,
      lastUpdate: app.updateDate
    }
    dev.count++
    dev.updates += app.updateId || 0
    if (app.updateDate > dev.lastUpdate) {
      dev.lastUpdate = app.updateDate
    }
    developers.set(app.creatorId, dev)
  }

  // 获取开发者昵称并更新数据
  const topDevelopers = Array.from(developers.entries())
    .sort(([, a], [, b]) => {
      // 首先按更新次数排序
      if (b.updates !== a.updates) {
        return b.updates - a.updates
      }
      // 其次按应用数量排序
      if (b.count !== a.count) {
        return b.count - a.count
      }
      // 最后按最近更新时间排序
      return new Date(b.lastUpdate).getTime() - new Date(a.lastUpdate).getTime()
    })
    .slice(0, 10)

  const developerPromises = topDevelopers.map(async ([, dev]) => {
    const nickname = await fetchDeveloperNickname(dev.pkgId)
    return {
      name: nickname || dev.creator || '未知开发者',
      value: dev.count,
      updates: dev.updates
    }
  })

  developerData.value = await Promise.all(developerPromises)

  // 处理更新频率数据
  updateData.value = developerData.value.map(dev => ({
    date: dev.name,
    count: dev.updates
  }))

  // 初始化图表
  initCharts()
}

const initCharts = () => {
  // 月度新增应用图表
  if (monthlyChartRef.value) {
    monthlyChart = echarts.init(monthlyChartRef.value)
    const isMobile = window.innerWidth <= 768
    monthlyChart.setOption({
      grid: {
        top: '10%',
        left: '8%',
        right: '4%',
        bottom: '12%',
        containLabel: true
      },
      tooltip: {
        trigger: 'axis',
        formatter: '{b}: {c} 个应用'
      },
      xAxis: {
        type: 'category',
        data: monthlyData.value.map(item => item.date),
        axisLabel: {
          interval: isMobile ? 2 : 0,
          rotate: isMobile ? 45 : 0
        }
      },
      yAxis: {
        type: 'value',
        name: '应用数量',
        nameTextStyle: {
          padding: [0, 0, 0, isMobile ? 20 : 0]
        }
      },
      series: [{
        name: '新增应用数',
        type: 'line',
        data: monthlyData.value.map(item => item.count),
        smooth: true,
        areaStyle: {
          opacity: 0.3
        }
      }]
    })
  }

  // 应用类别分布图表
  if (categoryChartRef.value) {
    categoryChart = echarts.init(categoryChartRef.value)
    const isMobile = window.innerWidth <= 768
    categoryChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} 个 ({d}%)'
      },
      legend: {
        type: 'scroll',
        orient: 'horizontal',
        bottom: 0,
        left: 'center',
        itemWidth: isMobile ? 12 : 16,
        itemHeight: isMobile ? 12 : 16,
        textStyle: {
          fontSize: isMobile ? 12 : 14
        }
      },
      series: [{
        name: '应用类别',
        type: 'pie',
        radius: ['30%', '70%'],
        center: ['50%', '40%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#fff',
          borderWidth: 1
        },
        label: {
          show: !isMobile,
          formatter: '{b}: {c}个'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: isMobile ? 12 : 14,
            fontWeight: 'bold'
          }
        },
        data: categoryData.value
      }]
    })
  }

  // 开发者提交应用图表
  if (developerActivityChartRef.value) {
    developerActivityChart = echarts.init(developerActivityChartRef.value)
    const isMobile = window.innerWidth <= 768

    // 对数据进行排序
    const sortedData = [...developerData.value].sort((a, b) => a.value - b.value)
    
    developerActivityChart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        },
        formatter: '{b}: {c} 个应用'
      },
      grid: {
        top: '5%',
        left: '25%',
        right: '15%',
        bottom: '5%',
        containLabel: true
      },
      xAxis: {
        type: 'value',
        name: '应用数量',
        nameTextStyle: {
          fontSize: isMobile ? 12 : 14,
          padding: [0, 0, 0, 10]
        },
        axisLabel: {
          fontSize: isMobile ? 11 : 12
        },
        splitLine: {
          show: true,
          lineStyle: {
            type: 'dashed',
            color: '#E5E7EB'
          }
        }
      },
      yAxis: {
        type: 'category',
        data: sortedData.map(item => item.name),
        axisLabel: {
          width: isMobile ? 160 : 200,
          overflow: 'break',
          lineHeight: 16,
          fontSize: isMobile ? 11 : 12,
          margin: 12,
          align: 'left',
          rich: {
            overflow: 'breakAll',
            width: isMobile ? 160 : 200
          }
        },
        axisTick: {
          show: false
        },
        axisLine: {
          show: false
        }
      },
      series: [{
        name: '提交应用数',
        type: 'bar',
        data: sortedData.map(item => ({
          value: item.value,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#83bff6' },
              { offset: 0.5, color: '#188df0' },
              { offset: 1, color: '#188df0' }
            ]),
            borderRadius: [0, 4, 4, 0]
          }
        })),
        barWidth: '60%',
        label: {
          show: true,
          position: 'right',
          fontSize: isMobile ? 11 : 12,
          formatter: '{c} 个',
          distance: 8,
          color: '#666'
        }
      }]
    })
  }

  // 应用更新频率图表
  if (updateFrequencyChartRef.value) {
    updateFrequencyChart = echarts.init(updateFrequencyChartRef.value)
    const isMobile = window.innerWidth <= 768

    // 对数据进行排序
    const sortedData = [...updateData.value].sort((a, b) => a.count - b.count)

    updateFrequencyChart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        },
        formatter: '{b}: {c} 次更新'
      },
      grid: {
        top: '5%',
        left: '25%',
        right: '15%',
        bottom: '5%',
        containLabel: true
      },
      xAxis: {
        type: 'value',
        name: '更新次数',
        nameTextStyle: {
          fontSize: isMobile ? 12 : 14,
          padding: [0, 0, 0, 10]
        },
        axisLabel: {
          fontSize: isMobile ? 11 : 12
        },
        splitLine: {
          show: true,
          lineStyle: {
            type: 'dashed',
            color: '#E5E7EB'
          }
        }
      },
      yAxis: {
        type: 'category',
        data: sortedData.map(item => item.date),
        axisLabel: {
          width: isMobile ? 160 : 200,
          overflow: 'break',
          lineHeight: 16,
          fontSize: isMobile ? 11 : 12,
          margin: 12,
          align: 'left',
          rich: {
            overflow: 'breakAll',
            width: isMobile ? 160 : 200
          }
        },
        axisTick: {
          show: false
        },
        axisLine: {
          show: false
        }
      },
      series: [{
        name: '更新次数',
        type: 'bar',
        data: sortedData.map(item => ({
          value: item.count,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#60a5fa' },
              { offset: 0.5, color: '#3b82f6' },
              { offset: 1, color: '#2563eb' }
            ]),
            borderRadius: [0, 4, 4, 0]
          }
        })),
        barWidth: '60%',
        label: {
          show: true,
          position: 'right',
          fontSize: isMobile ? 11 : 12,
          formatter: '{c} 次',
          distance: 8,
          color: '#666'
        }
      }]
    })
  }
}

const handleResize = () => {
  const isMobile = window.innerWidth <= 768
  monthlyChart?.resize()
  categoryChart?.resize()
  developerActivityChart?.resize()
  updateFrequencyChart?.resize()
  
  // 重新设置图表选项以适应移动端
  initCharts()
}

onMounted(() => {
  fetchAppData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  monthlyChart?.dispose()
  categoryChart?.dispose()
  developerActivityChart?.dispose()
  updateFrequencyChart?.dispose()
})
</script>

<style scoped>
.trend-analysis {
  padding: 16px;
  background-color: #f5f7fa;
}

.chart-container {
  height: 360px;
  background-color: #fff;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.n-card) {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03);
}

:deep(.n-card-header) {
  font-size: 16px;
  font-weight: 500;
}

@media screen and (max-width: 768px) {
  .trend-analysis {
    padding: 8px;
  }
  
  :deep(.n-card) {
    padding: 12px !important;
  }

  :deep(.n-card-header) {
    padding: 0 0 12px 0 !important;
    font-size: 14px !important;
  }

  :deep(.n-card__content) {
    padding: 0 !important;
  }

  :deep(.n-space:not(.n-space-vertical)) {
    flex-direction: column !important;
  }

  :deep(.n-space-item) {
    margin-right: 0 !important;
    margin-bottom: 12px !important;
  }

  .chart-container {
    height: 300px;
  }
}

@media screen and (max-width: 480px) {
  .trend-analysis {
    padding: 4px;
  }

  :deep(.n-space) {
    gap: 8px !important;
  }

  :deep(.n-card) {
    padding: 8px !important;
  }

  :deep(.n-card-header) {
    padding: 0 0 8px 0 !important;
    font-size: 13px !important;
  }

  .chart-container {
    height: 260px;
  }
}
</style> 