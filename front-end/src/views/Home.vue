<template>
  <div class="home-view">
    <StatisticsCard />
    <AppWordCloud />
    <div class="search-section">
      <n-input-group>
        <n-input
          v-model:value="searchQuery"
          placeholder="搜索懒猫应用"
          class="search-input"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <n-icon :component="SearchOutline" />
          </template>
        </n-input>
        <n-button type="primary" @click="handleSearch">
          搜索
        </n-button>
      </n-input-group>
    </div>

    <!-- 功能区域 -->
    <div v-for="category in functions" :key="category.id" class="section">
      <h2 class="section-title">
        <n-icon :component="category.icon" :color="category.iconColor" class="section-icon" />
        {{ category.title }}
      </h2>
      <div class="function-grid">
        <div 
          v-for="func in category.children" 
          :key="func.id" 
          class="function-item"
          :class="{ active: selectedFunction?.id === func.id }"
          @click="handleFunctionClick(func)"
        >
          <div class="function-icon" :style="{ color: func.iconColor }">
            <n-icon :component="func.icon" />
          </div>
          <div class="function-info">
            <div class="function-title">{{ func.title }}</div>
            <div class="function-desc">{{ func.description }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 最新评论 -->
    <n-card title="最新评论" class="mt-4">
      <n-spin :show="loading">
        <n-empty v-if="comments.length === 0" description="暂无评论" />
        <div v-else class="comments-container">
          <n-card v-for="comment in comments" :key="comment.commentId" class="comment-card">
            <n-space vertical>
              <!-- 应用信息 -->
              <n-card size="small" class="app-info">
                <n-space align="center">
                  <n-avatar :src="getAppIcon(comment)" round />
                  <n-space vertical size="small">
                    <n-text strong>{{ comment.appName }}</n-text>
                    <n-text depth="3">{{ comment.brief }}</n-text>
                  </n-space>
                </n-space>
              </n-card>

              <!-- 评论内容 -->
              <n-space vertical>
                <n-space align="center">
                  <div class="avatar-wrapper">
                    <img
                      v-if="comment.avatar"
                      :src="comment.avatar"
                      :alt="comment.nickname"
                      class="avatar-img"
                    />
                    <div v-else class="avatar-placeholder" :style="{ background: getAvatarColor(comment.nickname) }">
                      {{ getFirstChar(comment.nickname) }}
                    </div>
                  </div>
                  <n-space vertical size="small">
                    <n-text strong>{{ comment.nickname }}</n-text>
                    <n-space align="center">
                      <n-rate :value="comment.score" readonly />
                      <n-text depth="3">{{ formatDate(comment.createdAt) }}</n-text>
                    </n-space>
                  </n-space>
                </n-space>
                
                <n-text>{{ comment.content }}</n-text>
                
                <n-space justify="space-between" align="center">
                  <n-space>
                    <n-text depth="3">{{ formatDate(comment.createdAt) }}</n-text>
                  </n-space>
                </n-space>
              </n-space>
            </n-space>
          </n-card>
        </div>
      </n-spin>
      <template #footer>
        <n-button text @click="router.push('/comments')">查看全部评论</n-button>
      </template>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import type { Component } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { RouteLocationRaw, RouteRecordName } from 'vue-router'
import {
  TrophyOutline,
  RefreshOutline,
  AnalyticsOutline,
  StatsChartOutline,
  CompassOutline,
  GameControllerOutline,
  CodeSlashOutline,
  StarOutline,
  SearchOutline,
  AppsOutline,
  ShieldCheckmarkOutline,
  PeopleOutline,
  LogoGithub,
  TrendingUpOutline,
} from '@vicons/ionicons5'
import StatisticsCard from '@/components/StatisticsCard.vue'
import AppWordCloud from '@/components/AppWordCloud.vue'
import request from '@/utils/request'
import { 
  NCard, NSpace, NText, NAvatar, NRate, NButton, NIcon, NEmpty, NSpin, NInput 
} from 'naive-ui'

interface Function {
  id: string;
  title: string;
  description?: string;
  icon: Component;
  iconColor: string;
  children?: Function[];
  component?: Component;
  route?: RouteLocationRaw;
}

interface Comment {
  commentId: number
  pkgId: string
  userId: number
  nickname: string
  avatar: string
  score: number
  content: string
  liked: boolean
  likeCounts: number
  createdAt: string
  updatedAt: string
  appName: string
  appIcon: string
  brief: string
}

const router = useRouter()
const route = useRoute()
const searchQuery = ref('')
const selectedFunction = ref<Function | null>(null)
const comments = ref<Comment[]>([])
const loading = ref(true)

const functions: Function[] = [
  {
    id: 'rankings',
    title: '排行榜',
    icon: TrophyOutline,
    iconColor: '#ffb800',
    children: [
      {
        id: 'most-popular',
        title: '最受欢迎',
        description: '最受用户欢迎的应用',
        icon: TrophyOutline,
        iconColor: '#ffb800',
        route: { name: 'MostPopular' }
      },
      // {
      //   id: 'monthly-new',
      //   title: '月度新品推荐',
      //   description: '本月新上架的优质应用',
      //   icon: RocketOutline,
      //   iconColor: '#13c2c2',
      //   route: { name: 'MonthlyNew' }
      // },
      // {
      //   id: 'latest-release',
      //   title: '最新上架',
      //   description: '最新发布的应用',
      //   icon: TimeOutline,
      //   iconColor: '#722ed1',
      //   route: { name: 'LatestRelease' }
      // },
      {
        id: 'recent-updates',
        title: '最近更新',
        description: '近期更新的应用',
        icon: RefreshOutline,
        iconColor: '#2f54eb',
        route: { name: 'RecentUpdates' }
      }
    ]
  },
  {
    id: 'statistics',
    title: '统计分析',
    icon: AnalyticsOutline,
    iconColor: '#722ed1',
    children: [
      {
        id: 'overview',
        title: '数据概览',
        description: '平台整体数据统计',
        icon: StatsChartOutline,
        iconColor: '#722ed1',
        route: { name: 'StatisticsOverview' }
      },
      {
        id: 'apps',
        title: '应用统计',
        description: '应用相关数据统计',
        icon: AppsOutline,
        iconColor: '#13c2c2',
        route: { name: 'StatisticsApps' }
      },
      {
        id: 'users',
        title: '用户统计',
        description: '用户相关数据统计',
        icon: PeopleOutline,
        iconColor: '#1890ff',
        route: { name: 'StatisticsUsers' }
      },
      {
        id: 'trend-analysis',
        title: '趋势分析',
        description: '数据趋势分析',
        icon: TrendingUpOutline,
        iconColor: '#fa8c16',
        route: { name: 'TrendAnalysis' }
      },
      {
        id: 'five-star',
        title: '五星应用排行榜',
        description: '评分最高的应用',
        icon: StarOutline,
        iconColor: '#ffb800',
        route: { name: 'FiveStarApps' }
      }
    ]
  },
  {
    id: 'developers',
    title: '开发者',
    icon: CodeSlashOutline,
    iconColor: '#1890ff',
    children: [
      {
        id: 'developer-ranking',
        title: '开发者排行',
        description: '最活跃的开发者',
        icon: StarOutline,
        iconColor: '#fa8c16',
        route: { name: 'DeveloperRanking' }
      },
      {
        id: 'github-achievements',
        title: 'GitHub 成就',
        description: '开发者 GitHub 成就展示',
        icon: LogoGithub,
        iconColor: '#333',
        route: { name: 'GitHubAchievements' }
      },
      {
        id: 'developer-community',
        title: '开发者社区',
        description: '探索开发者社区',
        icon: PeopleOutline,
        iconColor: '#1890ff',
        route: { name: 'DeveloperCommunity' }
      },
      {
        id: 'app-repositories',
        title: '应用代码仓库',
        description: '开源应用代码仓库',
        icon: CodeSlashOutline,
        iconColor: '#52c41a',
        route: { name: 'AppRepositories' }
      }
    ]
  },
  {
    id: 'explore',
    title: '开始探索',
    icon: CompassOutline,
    iconColor: '#13c2c2',
    children: [
      {
        id: 'all',
        title: '全部',
        description: '所有应用',
        icon: AppsOutline,
        iconColor: '#52c41a',
        route: { name: 'Category', params: { id: 0 } }
      },
      {
        id: 'official',
        title: '开发者原创',
        description: '开发者原创应用',
        icon: ShieldCheckmarkOutline,
        iconColor: '#52c41a',
        route: { name: 'Category', params: { id: 1 } }
      },
      {
        id: 'games',
        title: '官方应用',
        description: '官方出品的应用',
        icon: GameControllerOutline,
        iconColor: '#ff4d4f',
        route: { name: 'Category', params: { id: 2 } }
      },
    ]
  }
]

// Find the selected function based on current route
const findFunctionByRoute = (routeName: RouteRecordName | null | undefined): Function | null => {
  if (!routeName) return null
  for (const category of functions) {
    const found = category.children?.find(func => {
      const route = func.route as { name?: RouteRecordName }
      return route?.name === routeName
    })
    if (found) return found
  }
  return null
}

// Watch route changes
watch(
  () => route.name,
  (newRouteName) => {
    selectedFunction.value = findFunctionByRoute(newRouteName as RouteRecordName)
  },
  { immediate: true }
)

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({ name: 'Search', query: { q: searchQuery.value } })
  }
}

const handleFunctionClick = (func: Function) => {
  if (func.route) {
    router.push(func.route)
  }
}

const fetchComments = async () => {
  try {
    const response = await request.get<Comment[]>('/comment/latest')
    // 按创建时间倒序排序，只取前5条
    comments.value = response
      .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
      .slice(0, 5)
  } catch (error) {
    console.error('获取评论列表失败:', error)
  } finally {
    loading.value = false
  }
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}

// 头像背景色列表
const avatarColors = [
  '#1677ff', '#13c2c2', '#52c41a', '#faad14', '#eb2f96',
  '#722ed1', '#2f54eb', '#fa8c16', '#fadb14', '#a0d911'
]

const getAvatarColor = (name: string | undefined) => {
  if (!name) return avatarColors[0] // 默认返回第一个颜色
  // 使用名字的 charCode 来选择颜色
  const charSum = name.split('').reduce((sum, char) => sum + char.charCodeAt(0), 0)
  return avatarColors[charSum % avatarColors.length]
}

const getFirstChar = (name: string | undefined) => {
  if (!name) return '?' // 返回问号作为默认字符
  return name.charAt(0)
}

// Get app icon URL
const getAppIcon = (comment: Comment) => {
  if (!comment.appIcon) return '/path/to/default-icon.png'
  return `https://dl.lazycatmicroserver.com/appstore/metarepo/apps/${comment.pkgId}/icon.png`
}

onMounted(() => {
  fetchComments()
})
</script>

<style scoped>
.home-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px;
}

.search-section {
  margin: 16px auto 32px;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
}

.search-input {
  width: 500px;
  max-width: calc(100% - 88px);
}

:deep(.n-input-group) {
  width: 600px;
  max-width: 100%;
  margin: 0 auto;
}

.section {
  margin-bottom: 48px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  font-size: 24px;
  font-weight: 500;
  color: #333;
}

.section-icon {
  width: 24px;
  height: 24px;
}

.function-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  padding: 16px;
}

.function-item {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  gap: 12px;
  border: 1px solid #f0f0f0;
}

.function-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.function-item.active {
  border-color: #18a058;
  background-color: #f0faf5;
}

.function-icon {
  width: 32px;
  height: 32px;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-shrink: 0;
}

.function-info {
  flex: 1;
  min-width: 0;
}

.function-title {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.function-desc {
  font-size: 13px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

@media screen and (max-width: 1200px) {
  .function-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media screen and (max-width: 992px) {
  .function-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media screen and (max-width: 768px) {
  .home-view {
    padding: 12px;
  }

  .function-grid {
    grid-template-columns: 1fr;
    gap: 8px;
    padding: 12px;
  }

  .function-item {
    padding: 12px;
  }

  .function-icon {
    width: 24px;
    height: 24px;
  }

  .function-title {
    font-size: 15px;
  }

  .function-desc {
    font-size: 13px;
    -webkit-line-clamp: 2;
  }

  .section-title {
    font-size: 16px;
    padding: 0 12px;
    margin: 16px 0 4px;
    gap: 8px;
  }

  .section-title :deep(svg) {
    width: 20px;
    height: 20px;
  }
}

@media screen and (max-width: 480px) {
  .home-view {
    padding: 8px;
  }

  .function-grid {
    padding: 8px;
  }

  .function-item {
    padding: 10px;
  }

  .function-icon {
    width: 20px;
    height: 20px;
  }

  .function-title {
    font-size: 14px;
  }

  .function-desc {
    font-size: 12px;
  }

  .section-title {
    font-size: 15px;
    margin: 12px 0 4px;
  }
}

.mt-4 {
  margin-top: 16px;
}

.comments-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-card {
  transition: all 0.3s;
}

.comment-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.app-info {
  //background-color: var(--n-color-embedded);
  margin-bottom: 12px;
}

.avatar-wrapper {
  position: relative;
  width: 40px;
  height: 40px;
  cursor: pointer;
  perspective: 1000px;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
  transition: all 0.6s ease-in-out;
  transform-style: preserve-3d;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  transition: all 0.6s ease-in-out;
  transform-style: preserve-3d;
}

.avatar-wrapper:hover .avatar-img,
.avatar-wrapper:hover .avatar-placeholder {
  transform: rotateY(360deg);
}
</style> 