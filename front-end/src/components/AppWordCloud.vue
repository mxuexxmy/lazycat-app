<template>
  <div class="word-cloud">
    <n-card title="应用关键词" :bordered="false">
      <template #header-extra>
        <n-button text size="small" @click="toggleExpand">
          <template #icon>
            <n-icon>
              <expand-outline v-if="!isExpanded" />
              <contract-outline v-else />
            </n-icon>
          </template>
          {{ isExpanded ? '收起' : '展开' }}
        </n-button>
      </template>
      <n-spin :show="loading">
        <div v-if="displayKeywords.length > 0" 
             class="cloud-container"
             :class="{ 'expanded': isExpanded }">
          <div v-for="keyword in displayKeywords" 
               :key="keyword.text" 
               class="keyword-item"
               :style="getKeywordStyle(keyword)"
               @click="handleKeywordClick(keyword.text)">
            {{ keyword.text }}
          </div>
        </div>
        <n-empty v-else description="暂无关键词数据" />
      </n-spin>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NCard, NSpin, NEmpty, NButton, NIcon } from 'naive-ui'
import { ExpandOutline, ContractOutline } from '@vicons/ionicons5'

interface Keyword {
  text: string
  weight: number
  color?: string
}

const loading = ref(false)
const keywords = ref<Keyword[]>([])
const isExpanded = ref(false)
const router = useRouter()

// 颜色列表
const colors = [
  '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', 
  '#FFEEAD', '#D4A5A5', '#9B59B6', '#3498DB'
]

// 计算显示的关键词
const displayKeywords = computed(() => {
  return isExpanded.value ? keywords.value : keywords.value.slice(0, 8)
})

const fetchKeywords = async () => {
  loading.value = true
  try {
    const response = await fetch('/api/apps/keywords')
    const data = await response.json()
    
    // 处理关键词数据
    keywords.value = data.map((item: any, index: number) => ({
      text: item.keyword,
      weight: item.count,
      color: colors[index % colors.length]
    }))
  } catch (error) {
    console.error('获取关键词数据失败:', error)
  } finally {
    loading.value = false
  }
}

const getKeywordStyle = (keyword: Keyword) => {
  const minSize = 14
  const maxSize = 32
  const minWeight = Math.min(...keywords.value.map(k => k.weight))
  const maxWeight = Math.max(...keywords.value.map(k => k.weight))
  
  // 计算字体大小
  const fontSize = minSize + (keyword.weight - minWeight) * (maxSize - minSize) / (maxWeight - minWeight)
  
  return {
    fontSize: `${fontSize}px`,
    color: keyword.color,
    cursor: 'pointer',
    transition: 'all 0.3s ease',
    padding: '8px 12px',
    borderRadius: '16px',
    backgroundColor: 'rgba(0, 0, 0, 0.05)'
  }
}

const handleKeywordClick = (keyword: string) => {
  router.push({
    name: 'Search',
    query: { q: keyword }
  })
}

const toggleExpand = () => {
  isExpanded.value = !isExpanded.value
}

onMounted(() => {
  fetchKeywords()
})
</script>

<style scoped>
.word-cloud {
  margin-bottom: 24px;
}

.cloud-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
  padding: 16px 24px;
  max-height: 72px;
  overflow: hidden;
  transition: max-height 0.3s ease;
}

.cloud-container.expanded {
  max-height: none;
  padding: 24px;
}

.keyword-item {
  font-weight: 500;
  user-select: none;
}

.keyword-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

@media screen and (max-width: 768px) {
  .cloud-container {
    padding: 12px 16px;
    gap: 8px;
    max-height: 60px;
  }
  
  .cloud-container.expanded {
    padding: 16px;
  }
}
</style> 