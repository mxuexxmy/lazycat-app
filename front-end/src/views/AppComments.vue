<template>
  <div class="app-comments">
    <n-card title="应用评论" class="mb-4">
      <n-spin :show="loading">
        <n-empty v-if="comments.length === 0" description="暂无评论" />
        <div v-else class="comments-list">
          <n-card v-for="comment in comments" :key="comment.commentId" class="comment-card">
            <n-space vertical>
              <n-space align="center">
                <n-avatar :src="comment.avatar" round />
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
                  <n-button text @click="handleLike(comment)">
                    <template #icon>
                      <n-icon><HeartOutline /></n-icon>
                    </template>
                    {{ comment.likeCounts || 0 }}
                  </n-button>
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
import { useRoute } from 'vue-router'
import { NCard, NSpace, NText, NAvatar, NRate, NButton, NIcon, NEmpty, NSpin } from 'naive-ui'
import { HeartOutline } from '@vicons/ionicons5'
import request from '@/utils/request'

interface AppComment {
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
}

const route = useRoute()
const comments = ref<AppComment[]>([])
const loading = ref(true)

const fetchComments = async () => {
  try {
    const response = await request.get<AppComment[]>(`/apps/${route.params.pkgId}/comments`)
    comments.value = response
  } catch (error) {
    console.error('获取评论失败:', error)
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

const handleLike = async (comment: AppComment) => {
  try {
    await request.post(`/apps/comments/${comment.commentId}/like`)
    comment.liked = !comment.liked
    comment.likeCounts += comment.liked ? 1 : -1
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

onMounted(() => {
  fetchComments()
})
</script>

<style scoped>
.app-comments {
  padding: 20px;
}

.mb-4 {
  margin-bottom: 16px;
}

.comments-list {
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
</style> 