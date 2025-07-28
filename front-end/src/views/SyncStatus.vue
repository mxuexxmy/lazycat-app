<template>
  <div class="sync-status">
    <n-card title="同步信息管理" :bordered="false">
      <template #header-extra>
        <n-button type="primary" @click="refreshData" :loading="loading">
          <template #icon>
            <n-icon>
              <Refresh/>
            </n-icon>
          </template>
          刷新
        </n-button>
      </template>

      <!-- 总体同步进度 -->
      <n-card title="总体同步进度" :bordered="false" class="section-card">
        <n-grid :cols="4" :x-gap="16" :y-gap="16" responsive="screen">
          <n-grid-item v-for="item in dataSourceInfo" :key="item.dataName">
            <n-card hoverable class="progress-card">
              <template #header>
                <div class="stat-header">
                  <n-icon :component="getProgressIcon(item)" size="20"/>
                  <span>{{ item.dataName }}</span>
                </div>
              </template>
              <div class="progress-content">
                <div class="progress-value">{{ item.dataCount }} / {{ item.targetCount }}</div>
                <n-progress
                    type="line"
                    :percentage="getProgressPercentage(item)"
                    :color="item.dataCount >= item.targetCount ? '#18a058' : '#2080f0'"
                    :show-indicator="false"
                    :height="8"
                />
                <div class="progress-text">{{ getProgressPercentage(item) }}%</div>
              </div>
            </n-card>
          </n-grid-item>
        </n-grid>
      </n-card>

      <!-- 同步任务状态 -->
      <n-card title="同步任务状态" :bordered="false" class="section-card">
        <n-grid :cols="2" :x-gap="20" :y-gap="20" responsive="screen">
          <n-grid-item v-for="task in syncInfo" :key="task.id">
            <n-card hoverable class="task-card">
              <template #header>
                <div class="task-header">
                  <div class="task-info">
                    <h3>{{ task.syncTypeName }}</h3>
                    <div class="task-meta">
                      <n-tag :type="getStatusType(task.syncStatus)" size="small">
                        {{ task.syncStatusName }}
                      </n-tag>
                    </div>
                  </div>
                  <div class="task-actions">
                    <n-button
                        type="primary"
                        size="small"
                        @click="fullSync(task.syncType as SyncType)"
                        :loading="syncLoading[task.syncType]"
                    >
                      全量同步
                    </n-button>

                    <n-button
                        type="primary"
                        size="small"
                        @click="incrementalSync(task.syncType as SyncType)"
                        :loading="syncLoading[task.syncType]"
                    >
                      增量同步
                    </n-button>
                  </div>

                </div>
              </template>

              <div class="task-details">
                <div class="detail-item">
                  <span class="label">同步时间：</span>
                  <span class="value">{{ task.lastSyncTime || '未设置' }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">同步策略：</span>
                  <span class="value">{{ task.syncStrategyName || task.syncStrategy || '未设置' }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">增量下次同步时间：</span>
                  <span class="value">{{ task.incrementalNextSyncTime || '未设置' }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">增量同步间隔：</span>
                  <span class="value">{{ formatInterval(task.incrementalSyncInterval) }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">全量下次同步时间：</span>
                  <span class="value">{{ task.fullNextSyncTime || '未设置' }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">全量同步间隔：</span>
                  <span class="value">{{ formatInterval(task.fullSyncInterval) }}</span>
                </div>
                <div class="detail-item" v-if="task.totalCount">
                  <span class="label">总数量：</span>
                  <span class="value">{{ task.totalCount }}</span>
                </div>
                <div class="detail-item" v-if="task.retryCount > 0">
                  <span class="label">重试次数：</span>
                  <span class="value">{{ task.retryCount }}</span>
                </div>
                <div class="detail-item" v-if="task.description">
                  <span class="label">同步说明：</span>
                  <span 
                    v-if="task.description.length > 10" 
                    class="value description clickable"
                    @click="showDescriptionDetail(task.description)"
                    @click.stop
                  >
                    {{ task.description.substring(0, 10) }}...
                  </span>
                  <span v-else class="value description">{{ task.description }}</span>
                </div>
                <div class="detail-item" v-if="task.lastError">
                  <span class="label">错误：</span>
                  <span 
                    v-if="task.lastError.length > 10" 
                    class="value error clickable"
                    @click="showErrorDetail(task.lastError)"
                    @click.stop
                  >
                    {{ task.lastError.substring(0, 10) }}...
                  </span>
                  <span v-else class="value error">{{ task.lastError }}</span>
                </div>
              </div>
            </n-card>
          </n-grid-item>
        </n-grid>
      </n-card>
    </n-card>

    <!-- 错误详情弹窗 -->
    <n-modal
      v-model:show="showErrorModal"
      :mask-closable="false"
      :style="{ width: '600px' }"
    >
      <div style="background: white; padding: 20px; border-radius: 8px;">
        <div style="font-size: 16px; font-weight: 600; margin-bottom: 16px; color: #333;">
          错误详情
        </div>
        <div style="margin-bottom: 20px;">
          <pre style="white-space: pre-wrap; word-break: break-all; font-size: 14px; color: #f56c6c; background: #fef0f0; padding: 12px; border-radius: 4px; margin: 0;">{{ currentError }}</pre>
        </div>
        <div style="text-align: right;">
          <n-button type="primary" @click="showErrorModal = false">关闭</n-button>
        </div>
      </div>
    </n-modal>

    <!-- 描述详情弹窗 -->
    <n-modal
      v-model:show="showDescriptionModal"
      :mask-closable="false"
      :style="{ width: '600px' }"
    >
      <div style="background: white; padding: 20px; border-radius: 8px;">
        <div style="font-size: 16px; font-weight: 600; margin-bottom: 16px; color: #333;">
          同步说明
        </div>
        <div style="margin-bottom: 20px;">
          <pre style="white-space: pre-wrap; word-break: break-all; font-size: 14px; color: #303133; background: #f4f4f4; padding: 12px; border-radius: 4px; margin: 0;">{{ currentDescription }}</pre>
        </div>
        <div style="text-align: right;">
          <n-button type="primary" @click="showDescriptionModal = false">关闭</n-button>
        </div>
      </div>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import {ref, onMounted, onUnmounted} from 'vue'
import {useRouter} from 'vue-router'
import {useMessage} from 'naive-ui'
import {
  Refresh,
  AppsOutline,
  FolderOutline,
  PeopleOutline,
  DocumentTextOutline,
  StarOutline,
  CodeSlashOutline,
  GitNetworkOutline
} from '@vicons/ionicons5'
import {
  NCard,
  NGrid,
  NGridItem,
  NButton,
  NIcon,
  NTag,
  NProgress,
  NModal
} from 'naive-ui'
import request from '@/utils/request'

interface SyncInfoVO {
  id: number
  syncType: string
  syncTypeName: string
  lastSyncTime: string
  initialSyncCompleted: boolean
  initialSyncCompletedName: string
  syncStatus: number
  syncStatusName: string
  syncStrategy: string
  syncStrategyName: string
  lastError: string
  fullNextSyncTime: string
  fullSyncInterval: number
  incrementalNextSyncTime: string
  incrementalSyncInterval: number
  retryCount: number
  enabled: boolean
  totalCount: number
  description: string
}

interface SyncDataSourceVO {
  dataName: string
  dataCount: number
  targetCount: number
}

type SyncType = 'APP' | 'CATEGORY' | 'USER'

const router = useRouter()
const message = useMessage()
const loading = ref(false)
const syncInfo = ref<SyncInfoVO[]>([])
const dataSourceInfo = ref<SyncDataSourceVO[]>([])
const syncLoading = ref<Record<string, boolean>>({})
const quickSyncLoading = ref<Record<SyncType, boolean>>({
  APP: false,
  CATEGORY: false,
  USER: false
})

// 错误详情弹窗
const showErrorModal = ref(false)
const currentError = ref('')

// 描述详情弹窗
const showDescriptionModal = ref(false)
const currentDescription = ref('')

const showErrorDetail = (error: string) => {
  console.log('错误详情:', error)
  console.log('点击事件被触发')
  currentError.value = error
  showErrorModal.value = true
  console.log('模态框状态:', showErrorModal.value)
}

const showDescriptionDetail = (description: string) => {
  console.log('描述详情:', description)
  console.log('点击事件被触发')
  currentDescription.value = description
  showDescriptionModal.value = true
  console.log('模态框状态:', showDescriptionModal.value)
}

let timer: number | null = null

// 获取进度图标
const getProgressIcon = (item: SyncDataSourceVO) => {
  const iconMap: Record<string, any> = {
    '应用': AppsOutline,
    '应用评论': DocumentTextOutline,
    '应用评分': StarOutline,
    '应用分类': FolderOutline,
    '社区用户信息': PeopleOutline,
    'GitHub 信息': GitNetworkOutline,
    '用户': PeopleOutline,
    '教程': CodeSlashOutline
  }
  return iconMap[item.dataName] || AppsOutline
}

// 获取同步信息
const fetchSyncInfo = async () => {
  try {
    console.log('开始获取同步信息...')
    const result = await request.get('/sync/info')
    console.log('同步信息接口返回:', result)

    if (result.success === true) {
      syncInfo.value = result.data
      console.log('同步信息数据:', syncInfo.value)
    } else {
      console.error('同步信息接口返回错误:', result)
      message.error(`获取同步信息失败: ${result.message || '未知错误'}`)
    }
  } catch (error) {
    console.error('获取同步信息失败:', error)
    message.error('获取同步信息失败')
  }
}

// 获取数据源信息
const fetchDataSourceInfo = async () => {
  try {
    console.log('开始获取数据源信息...')
    const result = await request.get('/sync/source')
    console.log('数据源信息接口返回:', result)

    if (result.success === true) {
      dataSourceInfo.value = result.data
      console.log('数据源信息数据:', dataSourceInfo.value)
    } else {
      console.error('数据源信息接口返回错误:', result)
      message.error(`获取数据源信息失败: ${result.message || '未知错误'}`)
    }
  } catch (error) {
    console.error('获取数据源信息失败:', error)
    message.error('获取数据源信息失败')
  }
}

// 刷新数据
const refreshData = async () => {
  loading.value = true
  try {
    await Promise.all([fetchDataSourceInfo(), fetchSyncInfo()])
    message.success('数据刷新成功')
  } catch (error) {
    message.error('数据刷新失败')
  } finally {
    loading.value = false
  }
}

// 全量同步
const fullSync = async (type: SyncType) => {
  const syncMap: Record<SyncType, string> = {
    APP: 'syncApps',
    CATEGORY: 'syncCategories',
    USER: 'syncUsers'
  }

  quickSyncLoading.value[type] = true
  try {

    const result = await request.get(`/job/${syncMap[type]}`)
    if (result.success === true) {
      message.success(result.data)
      // 延迟刷新数据
      setTimeout(() => {
        refreshData()
      }, 2000)
    } else {
      message.error(`全量同步失败: ${result.message || '未知错误'}`)
    }
  } catch (error) {
    console.error('全量同步失败:', error)
    message.error('全量同步失败')
  } finally {
    quickSyncLoading.value[type] = false
  }
}
// 增量同步
const incrementalSync = async (type: SyncType) => {
  const syncMap: Record<SyncType, string> = {
    APP: 'incrementalSyncApps',
    CATEGORY: 'incrementalSyncCategories',
    USER: 'incrementalSyncUsers'
  }

  quickSyncLoading.value[type] = true
  try {

    const result = await request.get(`/job/${syncMap[type]}`)
    if (result.success === true) {
      message.success(result.data)
      // 延迟刷新数据
      setTimeout(() => {
        refreshData()
      }, 2000)
    } else {
      message.error(`增量同步失败: ${result.message || '未知错误'}`)
    }
  } catch (error) {
    console.error('增量同步失败:', error)
    message.error('增量同步失败')
  } finally {
    quickSyncLoading.value[type] = false
  }
}


// 计算进度百分比
const getProgressPercentage = (item: SyncDataSourceVO) => {
  if (item.targetCount === 0) return 0
  return Math.round((item.dataCount / item.targetCount) * 100)
}

// 获取状态类型
const getStatusType = (status: number) => {
  switch (status) {
      // 开始
    case 1:
      return 'info'
      // 运行中
    case 2:
      return 'info'
    case 3:
      return 'success'
    case 4:
      return 'error'
    default:
      return 'default'
  }
}

// 格式化时间间隔
const formatInterval = (interval: number) => {
  if (!interval) return '未设置'
  const hours = Math.floor(interval / (1000 * 60 * 60))
  const minutes = Math.floor((interval % (1000 * 60 * 60)) / (1000 * 60))
  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  }
  return `${minutes}分钟`
}

// 定时刷新数据
const startAutoRefresh = () => {
  timer = window.setInterval(() => {
    fetchSyncInfo()
    fetchDataSourceInfo()
  }, 30000) // 每30秒刷新一次
}

onMounted(() => {
  refreshData()
  startAutoRefresh()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.sync-status {
  padding: 16px;
  background-color: #f5f7fa;
  width: 70%;
  margin: 0 auto;
}

.section-card {
  margin-bottom: 20px;
}

.section-card :deep(.n-card__content) {
  padding-top: 16px;
}

.progress-card {
  height: 100%;
}

.stat-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
}

.progress-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.progress-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  text-align: center;
}

.progress-text {
  text-align: center;
  font-size: 12px;
  color: #909399;
}

.task-card {
  height: 100%;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.task-info h3 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.task-meta {
  display: flex;
  gap: 8px;
}

.task-actions {
  display: flex;
  gap: 10px; /* Adjusted gap between buttons */
}

.task-details {
  display: grid;
  grid-template-columns: 1fr;
  gap: 8px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.detail-item .label {
  color: #606266;
  font-weight: 500;
}

.detail-item .value {
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
  word-break: break-all;
}

.detail-item .value.error {
  color: #f56c6c;
  max-width: 180px;
  cursor: pointer;
}

.detail-item .value.error.clickable {
  color: #f56c6c;
  max-width: 180px;
  cursor: pointer;
  text-decoration: underline;
  text-decoration-color: #f56c6c;
  pointer-events: auto;
  user-select: none;
}

.detail-item .value.error.clickable:hover {
  text-decoration: underline;
  text-decoration-color: #ff7875;
  color: #ff7875;
}

.detail-item .value.description {
  color: #303133;
  max-width: 180px;
  cursor: pointer;
  text-decoration: underline;
  text-decoration-color: #303133;
  pointer-events: auto;
  user-select: none;
}

.detail-item .value.description.clickable:hover {
  text-decoration: underline;
  text-decoration-color: #606266;
  color: #606266;
}

@media screen and (max-width: 768px) {
  .sync-status {
    padding: 8px;
  }
}

@media screen and (max-width: 480px) {
  .sync-status {
    padding: 4px;
  }

  .task-header {
    flex-direction: column;
    gap: 12px;
  }
}
</style> 