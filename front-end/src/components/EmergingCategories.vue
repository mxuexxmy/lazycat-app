<template>
  <n-card title="新兴分类趋势" :bordered="false">
    <n-spin :show="loading">
      <div class="trend-list" v-if="categories.length > 0">
        <div v-for="(category, index) in categories" :key="index" class="trend-item">
          <div class="trend-info">
            <div class="trend-rank">{{ index + 1 }}</div>
            <div class="trend-name">{{ category.category }}</div>
          </div>
          <div class="trend-stats">
            <n-progress
              type="line"
              :percentage="getPercentage(category.growth)"
              :height="8"
              :border-radius="4"
              :color="getColor(index)"
            />
            <div class="trend-growth">{{ category.growth }} 个新应用</div>
          </div>
        </div>
      </div>
      <n-empty v-else description="暂无趋势数据" />
    </n-spin>
  </n-card>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { NCard, NSpin, NEmpty, NProgress } from 'naive-ui'

interface Category {
  category: string
  growth: number
}

const loading = ref(false)
const categories = ref<Category[]>([])

// 颜色列表
const colors = [
  '#18a058',
  '#2080f0',
  '#f0a020',
  '#d03050',
  '#909399',
  '#606266',
  '#303133',
  '#909399',
  '#c0c4cc',
  '#dcdfe6'
]

// 获取进度条颜色
const getColor = (index: number) => colors[index] || colors[colors.length - 1]

// 计算百分比
const getPercentage = (growth: number) => {
  const maxGrowth = Math.max(...categories.value.map(c => c.growth))
  return Math.round((growth / maxGrowth) * 100)
}

const fetchEmergingCategories = async () => {
  loading.value = true
  try {
    const response = await fetch('/api/recommendations/categories/emerging')
    const data = await response.json()
    categories.value = data
  } catch (error) {
    console.error('获取新兴分类趋势失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchEmergingCategories()
})
</script>

<style scoped>
.trend-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.trend-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.trend-info {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 160px;
}

.trend-rank {
  width: 24px;
  height: 24px;
  border-radius: 12px;
  background: #f4f4f5;
  color: #909399;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 500;
}

.trend-item:nth-child(1) .trend-rank {
  background: #f0f9eb;
  color: #67c23a;
}

.trend-item:nth-child(2) .trend-rank {
  background: #ecf5ff;
  color: #409eff;
}

.trend-item:nth-child(3) .trend-rank {
  background: #fdf6ec;
  color: #e6a23c;
}

.trend-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.trend-stats {
  flex: 1;
  min-width: 0;
}

.trend-growth {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  text-align: right;
}

@media screen and (max-width: 768px) {
  .trend-info {
    width: 120px;
  }
  
  .trend-name {
    font-size: 13px;
  }
}
</style> 