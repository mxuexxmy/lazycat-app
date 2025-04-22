<template>
  <div class="sync-notification" v-if="progress < 100 || showComplete">
    <n-alert :type="progress < 100 ? 'info' : 'success'" :bordered="false">
      <template #icon>
        <n-spin v-if="progress < 100" size="small" />
        <n-icon v-else><checkmark-circle /></n-icon>
      </template>
      <div class="sync-content">
        <span v-if="progress < 100">数据正在同步中 ({{ progress }}%)</span>
        <span v-else>数据同步完成</span>
        <n-progress
          v-if="progress < 100"
          type="line"
          :percentage="progress"
          :indicator-placement="'inside'"
          processing
          :height="4"
        />
      </div>
    </n-alert>
  </div>
</template>

<script setup lang="ts">
import { NAlert, NSpin, NProgress, NIcon } from 'naive-ui'
import { ref, watch } from 'vue'
import { CheckmarkCircle } from '@vicons/ionicons5'

const props = defineProps<{
  progress: number
}>()

const showComplete = ref(false)

watch(() => props.progress, (newProgress) => {
  if (newProgress >= 100) {
    showComplete.value = true
    // 3秒后隐藏完成提示
    setTimeout(() => {
      showComplete.value = false
    }, 3000)
  }
})
</script>

<style scoped>
.sync-notification {
  position: fixed;
  top: 80px;
  right: 24px;
  z-index: 1000;
  width: 300px;
}

.sync-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

:deep(.n-alert) {
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

:deep(.n-alert--success) {
  background-color: rgba(24, 160, 88, 0.1);
}

@media screen and (max-width: 768px) {
  .sync-notification {
    top: 70px;
    right: 16px;
    width: calc(100% - 32px);
  }
}
</style> 