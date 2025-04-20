<template>
  <div class="comment-list">
    <n-card title="评论列表" class="mb-4">
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
                    <n-text depth="3">{{ comment.pkgId }}</n-text>
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
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NCard, NSpace, NText, NAvatar, NRate, NEmpty, NSpin } from 'naive-ui'
import request from '@/utils/request'

interface Comment {
  commentId: number
  pkgId: string
  userId: number
  nickname: string
  avatar: string
  score: number
  content: string
  createdAt: string
  updatedAt: string
  appName: string
  appIcon: string
}

const comments = ref<Comment[]>([])
const loading = ref(true)

const fetchComments = async () => {
  try {
    const response = await request.get<Comment[]>('/apps/comments/all')
    // 按创建时间倒序排序
    comments.value = response.sort((a, b) => 
      new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    )
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
    minute: '2-digit'
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
.comment-list {
  max-width: 1440px;
  margin: 0 auto;
  padding: 16px;
  min-height: 100vh;
  background: #f5f5f5;
  padding-top: 80px; /* 为固定头部留出空间 */
}

.mb-4 {
  margin-bottom: 12px;
}

.comments-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-card {
  transition: all 0.3s;
}

.comment-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.app-info {
  background-color: var(--n-color-embedded);
  margin-bottom: 8px;
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

@media screen and (max-width: 768px) {
  .comment-list {
    padding: 12px;
    padding-top: 72px;
  }
}

@media screen and (max-width: 480px) {
  .comment-list {
    padding: 8px;
    padding-top: 64px;
  }
}
</style> 