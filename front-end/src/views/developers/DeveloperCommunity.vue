<template>
  <div class="rankings-container">
    <n-card title="开发者社区" class="developer-community">
      <n-spin :show="loading">
        <n-empty v-if="!loading && developers.length === 0" description="暂无开发者信息" />
        <div v-else class="developer-list">
          <n-list>
            <n-list-item v-for="developer in developers" :key="developer.id">
              <div class="developer-item" @click="handleDeveloperClick(developer)">
                <div class="avatar-wrapper">
                  <img
                    v-if="developer.avatar"
                    :src="developer.avatar"
                    :alt="developer.nickname || developer.username"
                    class="avatar-img"
                  />
                  <div 
                    v-else 
                    class="avatar-placeholder" 
                    :style="{ background: getAvatarColor(developer.nickname || developer.username) }"
                  >
                    {{ getFirstChar(developer.nickname || developer.username) }}
                  </div>
                </div>
                <div class="developer-info">
                  <div class="developer-name">{{ developer.nickname || developer.username }}</div>
                  <div class="developer-stats">
                    <span>应用: {{ developer.appsCount || 0 }}</span>
                    <n-divider vertical />
                    <span>获赞: {{ formatNumber(developer.receiveThumbs || 0) }}</span>
                    <n-divider vertical />
                    <span>粉丝: {{ formatNumber(developer.fans || 0) }}</span>
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
                    <n-tooltip v-if="developer.description" placement="bottom">
                      <template #trigger>
                        <n-ellipsis style="max-width: 200px">
                          <n-tag size="small" type="success" round>
                            {{ developer.description }}
                          </n-tag>
                        </n-ellipsis>
                      </template>
                      {{ developer.description }}
                    </n-tooltip>
                  </div>
                </div>
              </div>
            </n-list-item>
          </n-list>
        </div>
      </n-spin>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  NCard,
  NList,
  NListItem,
  NAvatar,
  NIcon,
  NTag,
  NSpin,
  NEmpty,
  NDivider,
  NTooltip,
  NEllipsis
} from 'naive-ui'
import {
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

// 头像背景色列表
const avatarColors = [
  '#1677ff', '#13c2c2', '#52c41a', '#faad14', '#eb2f96',
  '#722ed1', '#2f54eb', '#fa8c16', '#fadb14', '#a0d911'
]

const getAvatarColor = (name: string | undefined) => {
  if (!name) return avatarColors[0]
  const charSum = name.split('').reduce((sum, char) => sum + char.charCodeAt(0), 0)
  return avatarColors[charSum % avatarColors.length]
}

const getFirstChar = (name: string | undefined) => {
  if (!name) return '?'
  return name.charAt(0).toUpperCase()
}

const formatNumber = (num: number) => {
  if (num >= 10000) {
    return `${(num / 10000).toFixed(1)}万`
  }
  return num.toString()
}

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

const handleDeveloperClick = (developer: Developer) => {
  window.open(`https://playground.lazycat.cloud/#/user-profile/${developer.id}/product`, '_blank')
}

onMounted(() => {
  fetchDevelopers()
})
</script>

<style scoped>
.rankings-container {
  width: 100%;
}

.developer-community {
  width: 100%;
}

.developer-list {
  margin-top: 16px;
}

.developer-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.developer-item:hover {
  background: #f9f9f9;
}

.avatar-wrapper {
  position: relative;
  width: 48px;
  height: 48px;
  cursor: pointer;
  perspective: 1000px;
  flex-shrink: 0;
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
  font-size: 20px;
  font-weight: 500;
  transition: all 0.6s ease-in-out;
  transform-style: preserve-3d;
}

.avatar-wrapper:hover .avatar-img,
.avatar-wrapper:hover .avatar-placeholder {
  transform: rotateY(360deg);
}

.developer-info {
  flex: 1;
  min-width: 0;
}

.developer-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.developer-stats {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #666;
  font-size: 14px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.developer-social {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

:deep(.n-tag) {
  transition: all 0.3s ease;
  max-width: 100%;
}

:deep(.n-tag:hover) {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

:deep(.n-ellipsis) {
  display: inline-flex;
}

@media screen and (max-width: 768px) {
  .developer-community {
    padding: 0;
  }

  .developer-item {
    padding: 12px;
    border-bottom: 1px solid #f0f0f0;
  }

  .avatar-wrapper {
    width: 40px;
    height: 40px;
  }

  .developer-name {
    font-size: 15px;
    margin-bottom: 6px;
  }

  .developer-stats {
    font-size: 13px;
    gap: 12px;
    margin-bottom: 8px;
  }

  .developer-stats > span {
    display: flex;
    align-items: center;
  }

  :deep(.n-divider) {
    display: none;
  }

  .developer-social {
    margin-top: 8px;
  }

  :deep(.n-tag) {
    font-size: 12px;
  }
}

@media screen and (max-width: 480px) {
  .developer-item {
    padding: 10px;
  }

  .avatar-wrapper {
    width: 36px;
    height: 36px;
  }

  .avatar-placeholder {
    font-size: 16px;
  }

  .developer-name {
    font-size: 14px;
  }

  .developer-stats {
    font-size: 12px;
    gap: 8px;
  }
}
</style> 