<template>
  <n-card title="我的应用" :loading="loading">
    <n-list>
      <n-list-item v-for="app in apps" :key="app.appid">
        <template #header>
          <n-space>
            <n-avatar :src="app.icon" />
            <span>{{ app.title || app.appid }}</span>
          </n-space>
        </template>
        <n-space vertical>
          <div>状态: {{ getStatusText(app.status) }}</div>
          <div>运行状态: {{ getInstanceStatusText(app.instanceStatus) }}</div>
          <n-progress
            v-if="app.downloadProgress"
            :percentage="Math.round((app.downloadProgress.current / app.downloadProgress.total) * 100)"
            :height="12"
          />
        </n-space>
        <template #footer>
          <n-space>
            <n-button
              text
              :disabled="app.status !== 4 || app.instanceStatus === 4"
              @click="handleStart(app.appid)"
            >
              <template #icon><play-circle-outlined /></template>
              启动
            </n-button>
            <n-button
              text
              :disabled="app.instanceStatus !== 4"
              @click="handleStop(app.appid)"
            >
              <template #icon><stop-outlined /></template>
              停止
            </n-button>
            <n-button
              text
              :disabled="app.status !== 4"
              @click="handleReinstall(app.appid)"
            >
              <template #icon><reload-outlined /></template>
              重装
            </n-button>
            <n-button
              text
              type="error"
              :disabled="app.status !== 4 || app.builtin"
              :loading="uninstallingApp === app.appid"
              @click="handleUninstall(app.appid)"
            >
              <template #icon><delete-outlined /></template>
              卸载
            </n-button>
          </n-space>
        </template>
      </n-list-item>
    </n-list>
  </n-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { lzcAPIGateway } from '@lazycatcloud/sdk'
import {
  DeleteOutlined,
  DownloadOutlined,
  PlayCircleOutlined,
  StopOutlined,
  ReloadOutlined
} from '@vicons/antd'

interface AppInfo {
  appid: string
  title?: string
  icon?: string
  status: number
  instanceStatus: number
  builtin?: boolean
  downloadProgress?: {
    current: number
    total: number
  }
}

const lzcapi = new lzcAPIGateway(window.location.origin, false)
const message = useMessage()
const apps = ref<AppInfo[]>([])
const loading = ref(false)
const uninstallingApp = ref<string | null>(null)

const fetchApps = async () => {
  try {
    loading.value = true
    const response = await lzcapi.pkgm.QueryApplication({ appidList: [] })
    apps.value = response.infoList || []
  } catch (error) {
    console.error('Failed to fetch apps:', error)
    message.error('获取应用列表失败')
  } finally {
    loading.value = false
  }
}

const handleUninstall = async (appid: string) => {
  try {
    uninstallingApp.value = appid
    await lzcapi.pkgm.Uninstall({ appid, clearData: true })
    message.success('卸载成功')
    fetchApps()
  } catch (error) {
    console.error('Failed to uninstall app:', error)
    message.error('卸载失败')
  } finally {
    uninstallingApp.value = null
  }
}

const handleStart = async (appid: string) => {
  try {
    await lzcapi.pkgm.Resume({ appid, uid: '0' })
    message.success('启动成功')
    fetchApps()
  } catch (error) {
    console.error('Failed to start app:', error)
    message.error('启动失败')
  }
}

const handleStop = async (appid: string) => {
  try {
    await lzcapi.pkgm.Pause({ appid, uid: '0' })
    message.success('停止成功')
    fetchApps()
  } catch (error) {
    console.error('Failed to stop app:', error)
    message.error('停止失败')
  }
}

const handleReinstall = async (appid: string) => {
  try {
    await lzcapi.pkgm.Uninstall({ appid, clearData: true })
    await lzcapi.pkgm.Install({ appid })
    message.success('重装成功')
    fetchApps()
  } catch (error) {
    console.error('Failed to reinstall app:', error)
    message.error('重装失败')
  }
}

const getStatusText = (status: number) => {
  switch (status) {
    case 0: return '未安装'
    case 1: return '下载中'
    case 2: return '已暂停'
    case 3: return '安装中'
    case 4: return '已安装'
    case 5: return '安装失败'
    default: return '未知状态'
  }
}

const getInstanceStatusText = (status: number) => {
  switch (status) {
    case 0: return '错误'
    case 1: return '已暂停'
    case 2: return '停止中'
    case 3: return '启动中'
    case 4: return '运行中'
    default: return '未知状态'
  }
}

onMounted(() => {
  fetchApps()
})
</script> 