<template>
  <div class="data-overview">
    <n-card class="header-card">
      <div class="header-content">
        <n-button type="primary" @click="goBack" class="back-button">
          <template #icon>
            <n-icon><ArrowBack /></n-icon>
          </template>
          返回
        </n-button>
        <h1>数据概览</h1>
      </div>
    </n-card>
    <n-grid :cols="2" :x-gap="20" :y-gap="20">
      <n-grid-item>
        <n-card title="应用统计">
          <n-statistic label="总应用数" :value="totalApps" />
          <n-statistic label="总下载量" :value="totalDownloads" />
          <n-statistic label="总用户数" :value="totalUsers" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card title="用户增长">
          <n-statistic label="今日新增" :value="userGrowth.daily" />
          <n-statistic label="本周新增" :value="userGrowth.weekly" />
          <n-statistic label="本月新增" :value="userGrowth.monthly" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card title="热门应用">
          <n-list>
            <n-list-item v-for="app in popularApps" :key="app.pkgId">
              <n-thing :title="app.name" :description="app.pkgId">
                <template #footer>
                  <n-tag type="info">{{ formatDownloadCount(app.downloads) }} 下载</n-tag>
                </template>
              </n-thing>
            </n-list-item>
          </n-list>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card title="活跃用户">
          <n-list>
            <n-list-item v-for="user in activeUsers" :key="user.id">
              <n-thing :title="user.nickname" :description="user.username">
                <template #avatar>
                  <n-avatar :src="user.avatar" />
                </template>
              </n-thing>
            </n-list-item>
          </n-list>
        </n-card>
      </n-grid-item>
    </n-grid>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowBack } from '@vicons/ionicons5'
import { NCard, NGrid, NGridItem, NStatistic, NList, NListItem, NThing, NTag, NAvatar, NButton, NIcon } from 'naive-ui'
import axios from 'axios'

interface App {
  pkgId: string
  name: string
  downloads: number
}

interface User {
  id: number
  username: string
  nickname: string
  avatar: string
}

interface UserGrowth {
  daily: number
  weekly: number
  monthly: number
}

const router = useRouter()
const totalApps = ref(0)
const totalDownloads = ref(0)
const totalUsers = ref(0)
const userGrowth = ref<UserGrowth>({ daily: 0, weekly: 0, monthly: 0 })
const popularApps = ref<App[]>([])
const activeUsers = ref<User[]>([])

const goBack = () => {
  router.back()
}

const formatDownloadCount = (count: number) => {
  if (count >= 10000) {
    return `${(count / 10000).toFixed(1)}w`
  }
  if (count >= 1000) {
    return `${(count / 1000).toFixed(1)}k`
  }
  return count.toString()
}

const fetchData = async () => {
  try {
    const [appsRes, usersRes, growthRes, popularRes, activeRes] = await Promise.all([
      axios.get('/api/apps/count'),
      axios.get('/api/users/count'),
      axios.get('/api/users/growth'),
      axios.get('/api/apps/popular?limit=5'),
      axios.get('/api/users/active?limit=5')
    ])

    totalApps.value = appsRes.data
    totalUsers.value = usersRes.data
    userGrowth.value = growthRes.data
    popularApps.value = popularRes.data
    activeUsers.value = activeRes.data
  } catch (error) {
    console.error('Failed to fetch data:', error)
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.data-overview {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-content h1 {
  margin: 0;
  font-size: 24px;
  flex: 1;
}

.back-button {
  margin-right: 10px;
}
</style> 