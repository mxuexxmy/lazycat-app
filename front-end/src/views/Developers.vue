<template>
  <div class="developers-view">
    <n-card>
      <div class="nav-buttons">
        <n-space>
          <n-button
            type="primary"
            @click="router.push({ name: 'DeveloperRanking' })"
            :ghost="route.name !== 'DeveloperRanking'"
          >
            <template #icon>
              <n-icon><trophy /></n-icon>
            </template>
            开发者排行
          </n-button>

          <n-button
            type="primary"
            @click="router.push({ name: 'DeveloperCommunity' })"
            :ghost="route.name !== 'DeveloperCommunity'"
          >
            <template #icon>
              <n-icon><people /></n-icon>
            </template>
            开发者社区
          </n-button>

          <n-button
            type="primary"
            @click="router.push({ name: 'AppRepositories' })"
            :ghost="route.name !== 'AppRepositories'"
          >
            <template #icon>
              <n-icon><code /></n-icon>
            </template>
            应用仓库
          </n-button>
        </n-space>
      </div>
    </n-card>

    <div class="content-area">
      <router-view></router-view>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { NCard, NButton, NSpace, NIcon } from 'naive-ui'
import {
  TrophyOutline as trophy,
  PeopleOutline as people,
  CodeSlashOutline as code
} from '@vicons/ionicons5'

const router = useRouter()
const route = useRoute()
</script>

<style scoped>
.developers-view {
  max-width: 1440px;
  margin: 0 auto;
  padding: 24px;
  min-height: 100vh;
}

.nav-buttons {
  display: flex;
  justify-content: center;
  padding: 8px 0;
}

.content-area {
  margin-top: 24px;
}

@media screen and (max-width: 768px) {
  .developers-view {
    padding: 16px;
  }

  .nav-buttons {
    overflow-x: auto;
    padding: 8px;
  }

  :deep(.n-space) {
    flex-wrap: nowrap !important;
  }

  :deep(.n-button) {
    white-space: nowrap;
  }
}
</style> 