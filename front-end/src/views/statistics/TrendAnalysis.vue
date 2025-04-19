<template>
  <div class="trend-analysis">
    <n-card title="应用类别分布" :loading="loading">
      <div ref="categoryChartRef" class="chart-container">
        <n-empty v-if="!loading && !categoryData.length" description="暂无数据" />
      </div>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { NCard, NEmpty } from 'naive-ui'
import * as echarts from 'echarts'
import type { EChartsType } from 'echarts'

// 类别名称映射
const categoryNameMap: Record<string, string> = {
  'Learning': '学习',
  'Design': '设计',
  'Productivity': '效率',
  'Others': '其他',
  'Game': '游戏',
  'Development Tools': '开发工具',
  'Official Apps': '官方应用',
  'Media': '媒体',
  'Lifestyle': '生活'
}

interface CategoryData {
  name: string
  value: number
}

const loading = ref(true)
const categoryData = ref<CategoryData[]>([])
let categoryChart: EChartsType | null = null
const categoryChartRef = ref<HTMLElement | null>(null)

// 获取类别分布数据
const fetchCategoryData = async () => {
  try {
    const response = await fetch('/api/apps/statistics/category-distribution')
    const result = await response.json()
    
    if (Array.isArray(result)) {
      categoryData.value = result
        .map(item => ({
          name: categoryNameMap[item.category] || item.category,
          value: item.count
        }))
        .sort((a, b) => b.value - a.value)
      
      initializeChart()
    }
  } catch (error) {
    console.error('Failed to fetch category data:', error)
  } finally {
    loading.value = false
  }
}

// 初始化图表
const initializeChart = () => {
  const isMobile = window.innerWidth <= 768

  if (categoryChartRef.value) {
    categoryChart = echarts.init(categoryChartRef.value)
    categoryChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} 个应用 ({d}%)'
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
          position: 'outside',
          formatter: '{b}\n{c}个 ({d}%)',
          fontSize: isMobile ? 12 : 14
        },
        emphasis: {
          label: {
            show: true,
            fontSize: isMobile ? 14 : 16,
            fontWeight: 'bold'
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        data: categoryData.value
      }]
    })
  }
}

// 处理窗口大小变化
const handleResize = () => {
  categoryChart?.resize()
  initializeChart()
}

onMounted(() => {
  fetchCategoryData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  categoryChart?.dispose()
})
</script>

<style scoped>
.trend-analysis {
  padding: 16px;
  background-color: #f5f7fa;
}

.chart-container {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff;
  border-radius: 4px;
}

@media screen and (max-width: 768px) {
  .trend-analysis {
    padding: 8px;
  }

  .chart-container {
    height: 300px;
  }
}

@media screen and (max-width: 480px) {
  .trend-analysis {
    padding: 4px;
  }

  .chart-container {
    height: 260px;
  }
}
</style>
