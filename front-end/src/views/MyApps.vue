<template>
  <div class="my-apps-view">
    <n-card title="我的应用">
      <n-spin :show="loading">
        <n-empty v-if="apps.length === 0" description="暂无已安装的应用" />
        <n-list v-else>
          <n-list-item v-for="app in apps" :key="app.appid">
            <template #prefix>
              <n-avatar :src="app.icon" round />
            </template>
            <template #suffix>
              <n-space>
                <n-button 
                  v-if="app.status === 1"
                  type="primary" 
                  @click="handleStop(app.appid)"
                >
                  停止
                </n-button>
                <n-button 
                  v-else
                  type="primary" 
                  @click="handleStart(app.appid)"
                >
                  启动
                </n-button>
                <n-button 
                  type="info" 
                  @click="handleReinstall(app.appid)"
                >
                  重新安装
                </n-button>
                <n-button 
                  type="error" 
                  @click="handleUninstall(app.appid)"
                >
                  卸载
                </n-button>
              </n-space>
            </template>
            <n-space vertical>
              <n-text strong>{{ app.title || app.appid }}</n-text>
              <n-text depth="3">{{ app.description }}</n-text>
              <n-progress
                v-if="app.downloadProgress"
                :percentage="Math.round((app.downloadProgress.current / app.downloadProgress.total) * 100)"
                size="small"
              />
              <n-text v-if="app.errorReason" type="error">
                错误: {{ app.errorReason }}
              </n-text>
            </n-space>
          </n-list-item>
        </n-list>
      </n-spin>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NCard, NList, NListItem, NAvatar, NSpace, NText, NButton, NSpin, NEmpty, NProgress, useMessage } from 'naive-ui'
import { lzcAPIGateway } from '@lazycatcloud/sdk'

type App = {
  appid: string
  status: number
  instanceStatus: number
  title?: string
  description?: string
  icon?: string
  downloadProgress?: {
    current: number
    total: number
  }
  errorReason?: string
}

const message = useMessage()
const lzcapi = new lzcAPIGateway(window.location.origin, false)
const apps = ref<App[]>([])
const loading = ref(false)

const fetchApps = async () => {
  try {
    loading.value = true
     console.log("lzcapi",lzcapi)
    const response = await lzcapi.pkgm.QueryApplication({ appidList: [] })
    console.debug("applications: ", response)
    apps.value = response.infoList || []
  } catch (error) {
    console.error('获取应用列表失败:', error)
    message.error('获取应用列表失败')
  } finally {
    loading.value = false
  }
}

const testSDK = async () => {
  try {
    const response = await lzcapi.pkgm.QueryApplication({ appidList: [] })
    console.debug("SDK test - applications: ", response)
    return response
  } catch (error) {
    console.error('SDK test failed:', error)
    throw error
  }
}

const handleUninstall = async (appid: string) => {
  try {
    await lzcapi.pkgm.Uninstall({ appid, clearData: true })
    message.success('卸载成功')
    fetchApps()
  } catch (error) {
    console.error('卸载失败:', error)
    message.error('卸载失败')
  }
}

const handleStart = async (appid: string) => {
  try {
    await lzcapi.pkgm.Resume({ appid, uid: '0' })
    message.success('启动成功')
    fetchApps()
  } catch (error) {
    console.error('启动失败:', error)
    message.error('启动失败')
  }
}

const handleStop = async (appid: string) => {
  try {
    await lzcapi.pkgm.Pause({ appid, uid: '0' })
    message.success('停止成功')
    fetchApps()
  } catch (error) {
    console.error('停止失败:', error)
    message.error('停止失败')
  }
}

const handleReinstall = async (appid: string) => {
  try {
    await lzcapi.pkgm.Uninstall({ appid, clearData: true })
    await lzcapi.pkgm.Install({ appid })
    message.success('重新安装成功')
    fetchApps()
  } catch (error) {
    console.error('重新安装失败:', error)
    message.error('重新安装失败')
  }
}

onMounted(async () => {
  await testSDK()
  fetchApps()
})
</script>

<style scoped>
.my-apps-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px;
}
</style> 