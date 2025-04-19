<template>
  <div class="developer-community">
    <n-card>
      <template #header>
        <div class="section-header">
          <h2>开发者社区</h2>
          <n-button text type="primary" @click="refreshData">
            <template #icon>
              <n-icon><refresh /></n-icon>
            </template>
            刷新
          </n-button>
        </div>
      </template>

      <div class="developer-list">
        <n-spin :show="loading">
          <n-empty v-if="developers.length === 0" description="暂无开发者信息" />
          <n-grid :cols="3" :x-gap="16" :y-gap="16" responsive="screen" v-else>
            <n-grid-item v-for="developer in developers" :key="developer.id">
              <n-card hoverable @click="handleDeveloperClick(developer)">
                <div class="developer-card">
                  <div class="developer-header">
                    <n-avatar
                      round
                      :size="64"
                      :src="developer.avatar"
                      :fallback-src="defaultAvatar"
                    />
                    <div class="developer-info">
                      <h3 class="developer-name">{{ developer.nickname || developer.username }}</h3>
                      <p class="developer-desc" v-if="developer.description">{{ developer.description }}</p>
                    </div>
                  </div>
                  
                  <div class="developer-stats">
                    <div class="stat-item">
                      <n-icon><apps /></n-icon>
                      <span>应用: {{ developer.appsCount || 0 }}</span>
                    </div>
                    <div class="stat-item">
                      <n-icon><thumbs-up /></n-icon>
                      <span>获赞: {{ developer.receiveThumbs || 0 }}</span>
                    </div>
                    <div class="stat-item">
                      <n-icon><people /></n-icon>
                      <span>粉丝: {{ developer.fans || 0 }}</span>
                    </div>
                  </div>

                  <div class="developer-social">
                    <n-tag v-if="developer.githubUsername" size="small" round>
                      <template #icon>
                        <n-icon><logo-github /></n-icon>
                      </template>
                      {{ developer.githubUsername }}
                    </n-tag>
                    <n-tag v-if="developer.guidelineCounts" size="small" type="info" round>
                      攻略: {{ developer.guidelineCounts }}
                    </n-tag>
                  </div>
                </div>
              </n-card>
            </n-grid-item>
          </n-grid>
        </n-spin>
      </div>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  NCard,
  NGrid,
  NGridItem,
  NAvatar,
  NIcon,
  NTag,
  NSpin,
  NEmpty,
  NButton
} from 'naive-ui'
import {
  RefreshOutline as refresh,
  AppsOutline as apps,
  ThumbsUpOutline as thumbsUp,
  PeopleOutline as people,
  LogoGithub as logoGithub
} from '@vicons/ionicons5'

interface Developer {
  id: number
  username: string
  nickname: string
  avatar: string
  description: string
  status: number
  githubUsername: string
  isCurrentLoginUser: boolean
  receiveThumbs: number
  follows: number
  fans: number
  followed: boolean
  guidelineCounts: number
  hideFollows: boolean
  hideFans: boolean
  hideThumbs: boolean
  appsCount: number
}

const router = useRouter()
const developers = ref<Developer[]>([])
const loading = ref(false)
const defaultAvatar = 'https://dl.lazycatmicroserver.com/appstore/metarepo/default-avatar.png'

const fetchDevelopers = async () => {
  loading.value = true
  try {
    const response = await fetch('/api/users/developers')
    const result = await response.json()
    if (result.success && Array.isArray(result.data)) {
      developers.value = result.data
    }
  } catch (error) {
    console.error('Failed to fetch developers:', error)
  } finally {
    loading.value = false
  }
}

const refreshData = () => {
  fetchDevelopers()
}

const handleDeveloperClick = (developer: Developer) => {
  window.open(`https://playground.lazycat.cloud/#/user-profile/${developer.id}/product`, '_blank')
}

onMounted(() => {
  fetchDevelopers()
})
</script>

<style scoped>
.developer-community {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
}

.developer-list {
  margin-top: 16px;
}

.developer-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.developer-header {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.developer-info {
  flex: 1;
  min-width: 0;
}

.developer-name {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.developer-desc {
  margin: 8px 0 0;
  font-size: 14px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.developer-stats {
  display: flex;
  gap: 16px;
  padding: 12px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #666;
}

.developer-social {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

@media screen and (max-width: 1200px) {
  :deep(.n-grid) {
    --n-cols: 2 !important;
  }
}

@media screen and (max-width: 768px) {
  .developer-community {
    padding: 16px;
  }

  :deep(.n-grid) {
    --n-cols: 1 !important;
  }

  .developer-card {
    gap: 12px;
  }

  .developer-header {
    gap: 12px;
  }

  .developer-stats {
    padding: 8px 0;
  }
}
</style> 